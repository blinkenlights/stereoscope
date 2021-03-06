/* 
 * This file is part of BMix.
 *
 *    BMix is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 * 
 *    BMix is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BMix.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package de.blinkenlights.bmix.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import de.blinkenlights.bmix.mixer.OutputSender;

/**
 * This class is a packet sender that sends packets over the network. The
 * raw bytes to send must be passed in from a class that knows how to make them.
 */
public class BLPacketSender implements OutputSender {
    DatagramSocket socket = null;
	InetAddress address = null;
	int port;
	
	private final static Logger logger = Logger.getLogger(BLPacketSender.class.getName());
	
	
	/**
	 * Creates a new BLPacketSender.
	 * 
	 * @param host the hostname or IP address of the destination
	 * @param port the port on the destination
	 */
	public BLPacketSender(String host, int port) throws BLNetworkException {
		try {
			address = InetAddress.getByName(host);
			this.port = port;
			socket = new DatagramSocket();
		} catch (UnknownHostException e) {
			throw new BLNetworkException(e.getMessage());
		} catch (SocketException e) {
			throw new BLNetworkException(e.getMessage());
		}
		logger.info("BLPacketSender() - host: " + host + " - port: " + port);		
	}

    /**
     * Releases network resources. Once this method has been called, this
     * sender can no longer be used.
     */
    public synchronized void close() {
        socket.close();
    }
    	
	/**
	 * Sends a message to the client.
	 * 
	 * @param buf the buffer to send
	 * @throws BLNetworkException if there was an error sending the packet
	 */
	public void send(byte buf[]) throws IOException {
		send(buf, 0, buf.length);
	}

	
	/**
	 * Sends a message to the client.
	 * 
	 *  @param buf the buffer to send
	 *  @param offset the offset in the buffer
	 *  @param length the length in the buffer
	 * @throws IOException 
	 */
	public void send(byte buf[], int offset, int length) throws IOException {
		if(buf.length < 1) {
			throw new IllegalArgumentException("buffer length must be > 0");
		}
		if(offset < 0 || length < 1 || (offset + length) > buf.length) {
			throw new IllegalArgumentException("send parameters are invalid - offset: " + offset + 
					" - length: " + length + " - buf len: " + buf.length);
		}
        DatagramPacket packet = new DatagramPacket(buf, offset, buf.length, address, port);
        synchronized (this) {			
	        if (!socket.isClosed()) {
	        	socket.send(packet);
	        }
        }
	}
	
	@Override
	public String toString() {
	    return address.getHostAddress() + ":" + port;
	}


	public String getAddress() {
		return address.getHostAddress();
	}


	public int getPort() {
		return port;
	}

}
