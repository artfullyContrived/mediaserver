package org.mobicents.media.core.ice.network.stun;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.List;

import org.mobicents.media.core.ice.TransportAddress;
import org.mobicents.media.core.ice.TransportAddress.TransportProtocol;
import org.mobicents.media.core.ice.network.ExpirableProtocolHandler;
import org.mobicents.media.core.ice.network.stun.messages.StunMessage;
import org.mobicents.media.core.ice.network.stun.messages.StunMessageFactory;
import org.mobicents.media.core.ice.network.stun.messages.StunRequest;
import org.mobicents.media.core.ice.network.stun.messages.StunResponse;
import org.mobicents.media.core.ice.network.stun.messages.attributes.StunAttribute;
import org.mobicents.media.core.ice.network.stun.messages.attributes.StunAttributeFactory;
import org.mobicents.media.core.ice.network.stun.messages.attributes.general.MessageIntegrityAttribute;
import org.mobicents.media.core.ice.network.stun.messages.attributes.general.PriorityAttribute;
import org.mobicents.media.core.ice.network.stun.messages.attributes.general.UsernameAttribute;
import org.mobicents.media.core.ice.security.IceAuthenticator;

/**
 * Handles STUN traffic.
 * 
 * @author Henrique Rosa
 * 
 */
public class StunHandler implements ExpirableProtocolHandler {

	private static final String PROTOCOL = "stun";

	private final IceAuthenticator authenticator;
	private final List<StunListener> listeners;

	private boolean expired;

	public StunHandler(IceAuthenticator authenticator) {
		this.authenticator = authenticator;
		this.expired = false;
		this.listeners = new ArrayList<StunListener>();
	}

	public void addListener(StunListener listener) {
		synchronized (this.listeners) {
			if (!this.listeners.contains(listener)) {
				this.listeners.add(listener);
			}
		}
	}

	public byte[] handleRead(SelectionKey key, byte[] data, int length)
			throws IOException {
		try {
			StunMessage message = StunMessage.decode(data, (char) 0,
					(char) length);
			if (message instanceof StunRequest) {
				return handleRequest((StunRequest) message, key);
			} else if (message instanceof StunResponse) {
				return handleResponse((StunResponse) message, key);
			}
			// TODO STUN Indication is not supported
			return null;
		} catch (StunException e) {
			throw new IOException("Could not decode STUN packet.", e);
		}
	}

	public byte[] handleWrite(SelectionKey key, byte[] data, int length)
			throws IOException {
		// XXX Cannot decode stun messages - hrosa
		// try {
		// StunMessage message = StunMessage.decode(data, (char) 0,
		// (char) length);
		// if (message instanceof StunResponse) {
		// StunResponse response = (StunResponse) message;
		// if (response.getMessageType() ==
		// StunMessage.BINDING_SUCCESS_RESPONSE) {
		// if (response.containsAttribute(StunAttribute.USE_CANDIDATE)) {
		// fireOnSuccessResponse(key);
		// }
		// }
		// }
		// } catch (StunException e) {
		// throw new IOException("Could not decode STUN packet.", e);
		// }
		fireOnSuccessResponse(key);
		return null;
	}

	public String getProtocol() {
		return PROTOCOL;
	}

	public boolean isExpired() {
		return this.expired;
	}

	public void expire() {
		this.expired = true;
	}

	private byte[] handleRequest(StunRequest request, SelectionKey key)
			throws IOException {
		byte[] transactionID = request.getTransactionId();

		/*
		 * The agent MUST use a short-term credential to authenticate the
		 * request and perform a message integrity check.
		 */
		UsernameAttribute remoteUsernameAttribute = (UsernameAttribute) request
				.getAttribute(StunAttribute.USERNAME);

		String remoteUsername = new String(
				remoteUsernameAttribute.getUsername());
		long priority = extractPriority(request);
		boolean useCandidate = request
				.containsAttribute(StunAttribute.USE_CANDIDATE);
		/*
		 * The agent MUST consider the username to be valid if it consists of
		 * two values separated by a colon, where the first value is equal to
		 * the username fragment generated by the agent in an offer or answer
		 * for a session in-progress.
		 */
		int colon = remoteUsername.indexOf(":");
		String remoteUfrag = remoteUsername.substring(0, colon);
		String localUFrag = null;

		if (useCandidate) {
			// Produce Binding Response
			DatagramChannel channel = (DatagramChannel) key.channel();
			InetSocketAddress remoteAddress = (InetSocketAddress) channel
					.getRemoteAddress();
			TransportAddress transportAddress = new TransportAddress(
					remoteAddress.getAddress(), remoteAddress.getPort(),
					TransportProtocol.UDP);
			StunResponse response = StunMessageFactory.createBindingResponse(
					request, transportAddress);
			try {
				response.setTransactionID(transactionID);
			} catch (StunException e) {
				throw new IOException("Illegal STUN Transaction ID: "
						+ new String(transactionID), e);
			}
			/*
			 * Add USERNAME and MESSAGE-INTEGRITY attribute in the response. The
			 * responses utilize the same usernames and passwords as the
			 * requests
			 */
			StunAttribute usernameAttribute = StunAttributeFactory
					.createUsernameAttribute(remoteUsernameAttribute
							.getUsername());
			response.addAttribute(usernameAttribute);

			byte[] localKey = this.authenticator.getLocalKey(remoteUsername);
			// String username = new String(uname.getUsername());
			MessageIntegrityAttribute messageIntegrityAttribute = StunAttributeFactory
					.createMessageIntegrityAttribute(new String(
							remoteUsernameAttribute.getUsername()), localKey);
			response.addAttribute(messageIntegrityAttribute);

			// Pass response to the server
			return response.encode();
		} else {
			return null;
		}
	}

	private byte[] handleResponse(StunResponse response, SelectionKey key) {
		throw new UnsupportedOperationException(
				"Support to handle STUN responses is not implemented.");
	}

	private long extractPriority(StunRequest request)
			throws IllegalArgumentException {
		// make sure we have a priority attribute and ignore otherwise.
		PriorityAttribute priorityAttr = (PriorityAttribute) request
				.getAttribute(StunAttribute.PRIORITY);
		// extract priority
		if (priorityAttr == null) {
			throw new IllegalArgumentException("Missing PRIORITY attribtue!");
		}
		return priorityAttr.getPriority();
	}

	private void fireOnSuccessResponse(SelectionKey key) {
		StunListener[] copy;
		synchronized (this.listeners) {
			copy = this.listeners.toArray(new StunListener[this.listeners
					.size()]);
		}

		// Fire the successful binding event
		BindingSuccessEvent event = new BindingSuccessEvent(this, key);
		for (StunListener listener : copy) {
			listener.onBinding(event);
		}
	}

}
