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


package de.blinkenlights.bmix.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.blinkenlights.bmix.mixer.BLImage;
import de.blinkenlights.bmix.movie.BLMovie;
import de.blinkenlights.bmix.movie.Frame;
import de.blinkenlights.bmix.network.BLPacketSender;
import de.blinkenlights.bmix.protocol.BLFramePacket;
import de.blinkenlights.bmix.util.FileFormatException;

/**
 * This class contains the main for the BMovieSender program which can load and
 * stream multiple movies to multiple destinations/ports. This is mainly used
 * for testing so that many BL frame streams can be generated at once. All
 * streams and movie config are contained within a single XML file.
 */
public class BMovieSender extends Thread {
    
    /**
     * Number of milliseconds between retransmissions of the same frame
     * when this sender is paused. This should be set somewhat lower than
     * the receiver's input timeout.
     */
    private static final int FRAME_INTERVAL_WHEN_PAUSED = 700;

    private static final Logger logger = Logger.getLogger(BMovieSender.class.getName());
    
	BLPacketSender netSend;
	BLMovie movie;
	boolean loop;
    private boolean stopped;
    private boolean paused;
    
	/**
	 * Creates a new BMovieSender 
	 * 
	 * @param movieFilename the movie filename to load
	 * @param hostname the hostname to send to
	 * @param port the port to send to
	 * @param loop true if the movie should loop during playback
	 */
	public BMovieSender(String movieFilename, String hostname, int port, boolean loop) throws BMovieException {
		this.loop = loop;
		try {
			movie = new BLMovie(movieFilename);
			netSend = new BLPacketSender(hostname, port);
		} catch (Exception e) {
			throw new BMovieException(e);
		}
	}
	
	/**
	 * Runs the sender thread for a BMovieSender stream.
	 */
	public void run() {
		while(true) {
			long nextFrameTime = System.currentTimeMillis();
//			long prevFrameTime = nextFrameTime;
			if(movie.getNumFrames() < 1) {
				System.err.println("movie has no frames... sleeping");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// this is ignorable
				}
			}
			for(int i = 0; i < movie.getNumFrames(); i ++) {
			    if (isStopped()) {
			        return;
			    }
				Frame frame = movie.getFrame(i);
				BLImage image = (BLImage)frame;
				BLFramePacket packet = new BLFramePacket(image);
				try {
					netSend.send(packet.getNetworkBytes());
				} catch (Exception e) {
				    logger.log(Level.WARNING, "Send frame failed", e);
				}
                if (isPaused()) {
                    i--;
                    nextFrameTime += FRAME_INTERVAL_WHEN_PAUSED;
                } else {
                    nextFrameTime = System.currentTimeMillis() + frame.getDuration();
                }
				while(System.currentTimeMillis() < nextFrameTime) {
					try {
						Thread.sleep(nextFrameTime - System.currentTimeMillis());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}			
			if (!isLooping()) break;
		}
	}
	
	public synchronized void stopSending() {
	    stopped = true;
	}
	
	public synchronized boolean isStopped() {
	    return stopped;
	}
	
	/**
	 * Main!
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		if(args.length < 1) {
			System.err.println("sender config filename must be specified");
			System.exit(1);
		}
		
		List<BMovieSender> senderList = new LinkedList<BMovieSender>();
		ElementParser ep = new ElementParser(senderList);
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = null;
		try {
			sp = spf.newSAXParser();
			sp.parse(new File(args[0]), ep);	
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (SAXException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		Iterator<BMovieSender> iter = senderList.iterator();
		while(iter.hasNext()) {
			BMovieSender sender = iter.next();
			sender.start();
		}
	}
	
	/**
	 * Class for parsing the BMovieSender config.
	 */
	static class ElementParser extends DefaultHandler {
		List<BMovieSender> senders;
		Locator locator;
		
		/**
		 * Creates a new ElementParser for parsing the document.
		 * 
		 * @param library the image library to use
		 */
		public ElementParser(List<BMovieSender> senders) {
			this.senders = senders;
		}

		/**
		 * Override the doctype so that the parser will work offline.
		 */
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
		}
		
		/**
		 * Handles the start of a document.
		 */
		public void startDocument() {
			// ignore
		}
		
		/**
		 * Handles the end of the a document.
		 */
		public void endDocument() {
			// ignore
		}
		
		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}
		
		/**
		 * Handles a new element.
		 * 
		 * @param uri namespace URI
		 * @param localName the local name (without prefix)
		 * @param qName the qualified name (with prefix)
		 * @param attributes the attributes for this element 
		 * @throws FileFormatException if there is a parsing error 
		 */
		public void startElement(String uri, String localName, String qName, Attributes attributes) 
				throws FileFormatException {
			if(qName.equals("bmoviesender")) {
				// ignore
			}
			else if(qName.equals("sender")) {
				String movie = attributes.getValue("movie");
				if(movie == null) {
					throw new FileFormatException("movie is not specified", locator.getLineNumber(), locator.getColumnNumber());					
				}
				String host = attributes.getValue("host");
				if(host == null) {
					throw new FileFormatException("host must be specified", locator.getLineNumber(), locator.getColumnNumber());
				}
				String val = attributes.getValue("port");
				if(val == null) {
					throw new FileFormatException("port must be specified", locator.getLineNumber(), locator.getColumnNumber());
				}
				int port = 0;
				try {
					port = Integer.parseInt(val);
				} catch(NumberFormatException e) {
					throw new FileFormatException("number format exception", locator.getLineNumber(), locator.getColumnNumber());
				}
				val = attributes.getValue("loop");
				if(val == null) {
					throw new FileFormatException("loop must be specified", locator.getLineNumber(), locator.getColumnNumber());					
				}
				boolean loop = Boolean.parseBoolean(val);
				try {
					senders.add(new BMovieSender(movie, host, port, loop));
				} catch (BMovieException e) {
					throw new FileFormatException(locator.getLineNumber(), locator.getColumnNumber(), e);
				}
			}
			else {
				System.err.println("unrecognised entity: " + qName);
			}
		}
		
		/**
		 * Handles the end of an element.
		 * 
		 * @param uri namespace URI
		 * @param localName the local name (without prefix)
		 * @param qName the qualified name (with prefix)
		 */
		public void endElement(String uri, String localName, String qName) {
			// ignore
		}
		
		/**
		 * Handles character data.
		 */
		public void characters(char ch[], int start, int len) {
			// ignore
		}
	}

    public synchronized void setLooping(boolean looping) {
        loop = looping;
    }
    
    public synchronized boolean isLooping() {
        return loop;
    }
    
    public synchronized boolean isPaused() {
        return paused;
    }
    
    /**
     * When paused, the progression of frames does not continue, but this sender
     * will still continue to send the same frame over and over to avoid blanking
     * the display.
     * 
     * @param paused true to pause; false to resume
     */
    public synchronized void setPaused(boolean paused) {
        this.paused = paused;
    }
}
