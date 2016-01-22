/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.media.server.spi;





/**
 * This enum represent connection mode:
 * <ul>
 * <li>INACTIVE - This is default mode where there is not tx or rx of media</li>
 * <li>SEND_ONLY - only send</li>
 * <li>RECV_ONLY - only receive</li>
 * <li>SEND_RECV - send and receive</li>
 * <ul>
 * 
 * @author baranowb
 * @author amit bhayani
 * @kulikov
 */
public enum ConnectionMode {	
    
		INACTIVE(new String("inactive")), 
        SEND_ONLY(new String("sendonly")),
        RECV_ONLY(new String("recvonly")),
        SEND_RECV(new String("sendrecv")),
        CONFERENCE(new String("confrnce")),
        NETWORK_LOOPBACK(new String("netwloop")),
        LOOPBACK(new String("loopback")),
        CONTINUITY_TEST(new String("conttest")),
        NETWORK_CONTINUITY_TEST(new String("netwtest"));
		
		private CharSequence description;
		
		private ConnectionMode(CharSequence value) {
			this.description = value;
		}
		
		public CharSequence getDescription() {
			return description;
		}

        public static ConnectionMode valueOf(CharSequence v) {
            if (v.equals(inactive)) {
                return INACTIVE;
            } else if (v.equals(send_only)) {
                return SEND_ONLY;
            } else if (v.equals(recv_only)) {
                return RECV_ONLY;
            } else if (v.equals(send_recv)) {
                return SEND_RECV;
            } else if (v.equals(network_loopback)) {
                return NETWORK_LOOPBACK;
            } else if (v.equals(loopback)) {
                return LOOPBACK;
            } else if (v.equals(conference)) {
                return CONFERENCE;
            } else if (v.equals(continuity_test)) {
                return CONTINUITY_TEST;
            } else if (v.equals(network_continuity_test)) {
                return NETWORK_CONTINUITY_TEST;
            }
            
            return null;
        }
        
    private final static CharSequence inactive = new String("inactive");
    private final static CharSequence send_only = new String("sendonly");
    private final static CharSequence recv_only = new String("recvonly");
    private final static CharSequence send_recv= new String("sendrecv");
    private final static CharSequence network_loopback = new String("netwloop");
    private final static CharSequence loopback = new String("loopback");
    private final static CharSequence conference  = new String("confrnce");
    private final static CharSequence continuity_test  = new String("conttest");
    private final static CharSequence network_continuity_test  = new String("netwtest");
        
}
