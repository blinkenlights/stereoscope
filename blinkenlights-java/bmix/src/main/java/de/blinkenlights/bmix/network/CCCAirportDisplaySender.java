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
import java.util.logging.Level;
import java.util.logging.Logger;

import de.blinkenlights.bmix.mixer.OutputSender;

/**
 * This class is a packet sender that sends packets over the network to a CCC
 * Airport Display unit. The raw bytes are converted into the format expected
 * by the display driver. Documented as follows: 
 * 
 * Hi
 * 
 * The protocoll is like following example:
 * 
 * each packet starts with a ten byte header.
 * five 16 bit words in network byte order.
 * 
 * word 0 is the command:
 * 
 * enum display_command {
 * 	CMD_CLEAR             = 0x0002,
 * 	CMD_CP437DATA         = 0x0003,
 * 	CMD_CHARBRIGHTNESS    = 0x0005,
 * 	CMD_BRIGHTNESS        = 0x0007,
 * 	CMD_HARDRESET         = 0x000b,
 * 	CMD_FADE_OUT          = 0x000d,
 * 	CMD_BITMAPLEGACY      = 0x0010,
 * 	CMD_BITMAPLINEAR      = 0x0012,
 * 	CMD_BITMAPLINEARWIN   = 0x0013,
 * };
 * 
 * clear needs only word zero
 * 
 * cp437 is the textmode and takes x,y,w,h parameters which are
 * measured in characters and zero based. 0,0 is top left and 55,19
 * is bottom right. the rest of the packet is character data. lines
 * are wrapped.
 * 
 * charbrightness works like cp437 but instead of characterdata contains
 * character brightness from 0 to 15 where 0 is the lowest brightness but
 * not completely off, for that you need to set a space
 * 
 * brightess ignores words 1-4 and takes a single 11th byte to set the
 * brightness for the whole screen
 * 
 * bitmap linear uses word 1,2 as offset,length counted in bytes (8 pixels).
 * offset originates at top left. offset 56 is the leftmost pixel one row
 * of pixels down from the top.
 * 
 * this is the receiving code running on the display controller:
 * 
 * http://git.stuge.se/?p=cape-cccb-apd.git;a=blob;f=udploop.c
 * 
 * looking forward to see what you can make out of it.
 * 
 * put your questions on the display! 
 * 
 * UDP 151.217.63.24:2342
 * 
 * bash example to set max global brightness:
 * printf '\x00\x07\x00\x00\x00\x00\x00\x00\x00\x00\x0f' | nc -u --send-only 151.217.63.24 2342
 * (on a mac remove the send-only option)
 * 
 * denis and peter
 * 
 */
public class CCCAirportDisplaySender implements OutputSender {
    DatagramSocket socket = null;
	InetAddress address = null;
	int port;
	
	private final static Logger logger = Logger.getLogger(CCCAirportDisplaySender.class.getName());
	private final static int CMD_CP437DATA = 0x0003;
	private final static int DISPLAY_W = 55;
	private final static int DISPLAY_H = 19;
	private final static int CHAR_BLANK = 32;
	private final static int CHAR_QUARTER = 176;
	private final static int CHAR_HALF = 177;
	private final static int CHAR_THREE_QUARTER = 178;
	private final static int CHAR_FULL = 219;
	
	/**
	 * Creates a new BLPacketSender.
	 * 
	 * @param host the hostname or IP address of the destination
	 * @param port the port on the destination
	 */
	public CCCAirportDisplaySender(String host, int port) throws BLNetworkException {
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
		byte outBufChar[] = new byte[10 + (DISPLAY_W * DISPLAY_H)];
		int outCount = 0;
		int inCount = 0;
		if(buf.length < 12) {
			throw new IllegalArgumentException("buffer length must be >= 12");
		}
		if(offset < 0 || length < 1 || (offset + length) > buf.length) {
			throw new IllegalArgumentException("send parameters are invalid - offset: " + offset + 
					" - length: " + length + " - buf len: " + buf.length);
		}
		
		int inHeight = ((buf[4] & 0xff) << 8) | (buf[5] & 0xff);
		int inWidth = ((buf[6] & 0xff) << 8) | (buf[7] & 0xff);
		int outHeight = inHeight;
		int outWidth = inWidth;
		if(inHeight > DISPLAY_H) {
			outHeight = DISPLAY_H;
		}
		if(inWidth > DISPLAY_W) {
			outWidth = DISPLAY_W;
		}
				
		// do header bytes
		outBufChar[0] = (byte)(CMD_CP437DATA >> 8);
		outBufChar[1] = (byte)(CMD_CP437DATA & 0xff);
		outBufChar[2] = 0;  // X hi
		outBufChar[3] = 0;  // X lo
		outBufChar[4] = 0;  // Y hi
		outBufChar[5] = 0;  // Y lo
		outBufChar[6] = 0;  // W hi
		outBufChar[7] = (byte)DISPLAY_W;  // W lo
		outBufChar[8] = 0;  // H hi
		outBufChar[9] = (byte)DISPLAY_H;  // H lo

		inCount = 12;  // skip BL header
		outCount = 10;  // skip the header we already wrote
		// do each line
		for(int row = 0; row < outHeight; row ++) {
			// do each column of a row
			for(int col = 0; col < outWidth; col ++) {
				switch(buf[inCount++]) {
				case 0:
				case 1:
				case 2:
					outBufChar[outCount++] = (byte)CHAR_BLANK;
					break;
				case 3:
				case 4:
				case 5:
					outBufChar[outCount++] = (byte)CHAR_QUARTER;
					break;
				case 6:
				case 7:
				case 8:
					outBufChar[outCount++] = (byte)CHAR_HALF;
					break;
				case 9:
				case 10:
				case 11:
					outBufChar[outCount++] = (byte)CHAR_THREE_QUARTER;
					break;
				case 12:
				case 13:
				case 14:
				case 15:					
					outBufChar[outCount++] = (byte)CHAR_FULL;
					break;					
				}
			}
			if(inWidth > outWidth) {
				inCount += (inWidth - outWidth);
			}
		}
		
        DatagramPacket packet = new DatagramPacket(outBufChar, 0, outBufChar.length, address, port);
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
