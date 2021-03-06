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
package de.blinkenlights.bmix.statistics;

import java.awt.Rectangle;
import java.util.List;

import de.blinkenlights.bmix.mixer.AbstractOutput.PacketType;
import de.blinkenlights.bmix.network.HostAndPort;

public class OutputStatistics implements StatisticsItem {

	private static final long serialVersionUID = -982303235686899928L;
	
	/**
	 * The unique ID of the object these stats are about.
	 */
	private final long id;

	private final Rectangle viewport;
	private final long minSendInterval;
	private final PacketType packetType;
	private final int multiframeBpp;

    private final List<HostAndPort> destinations;


	public OutputStatistics(long id, Rectangle viewport, List<HostAndPort> destinations,
			long minSendInterval, PacketType packetType, int multiframeBpp) {
				this.id = id;
				this.viewport = viewport;
                this.destinations = destinations;
				this.minSendInterval = minSendInterval;
				this.packetType = packetType;
				this.multiframeBpp = multiframeBpp;
	}
	
	public long getId() {
		return id;
	}
	
	public Rectangle getViewport() {
		return viewport;
	}

	public long getMinSendInterval() {
		return minSendInterval;
	}

	public PacketType getPacketType() {
		return packetType;
	}
	
	public int getMultiframeBpp() {
		return multiframeBpp;
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Output - Dest Addrs: " + destinations + "\n");
		str.append("  Viewport - x: " + viewport.x + " - y: " + viewport.y + 
				" - w: " + viewport.width + " - h: " + viewport.height + "\n"); 
		str.append("  Minimum Send Interval: " + minSendInterval + "\n");
		str.append("  Packet Type: " + packetType + "\n");
		str.append("  Multiframe bpp: " + multiframeBpp + "\n");
		str.append("\n");
		return str.toString();
	}

	public String getName() {
		return "Output to " + destinations;
	}

	public String toHtml() {
		String packetTypeDesc;
		if (packetType != null) {
			packetTypeDesc = packetType.name();
		} else {
			packetTypeDesc = "none";
		}
	    if (packetType == PacketType.MCU_FRAME) {
	        packetTypeDesc += " (maxval 255)";
	    } else if (packetType == PacketType.MCU_MULTIFRAME) {
	        packetTypeDesc += " (" + multiframeBpp + " bpp)";
	    }
        return String.format(
                "<html><table cellpadding=1 cellspacing=0>" +
                "<tr><th colspan=2>Output<br>%s" +
                "<tr><td>Viewport<td>%dx%d+%d+%d" +
                "<tr><td>Min interval<td>%dms" +
                "<tr><td>Packet<td>%s",
                destinations,
                viewport.width, viewport.height, viewport.x, viewport.y,
                minSendInterval,
                packetTypeDesc);
	}
}
