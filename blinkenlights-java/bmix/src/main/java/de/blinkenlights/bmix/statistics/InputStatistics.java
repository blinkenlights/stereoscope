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

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;

import de.blinkenlights.bmix.network.BLPacketReceiver;
import de.blinkenlights.bmix.network.BLPacketSender;
import de.blinkenlights.bmix.network.BLPacketReceiver.AlphaMode;

public class InputStatistics implements StatisticsItem {

	private static final long serialVersionUID = -1641167816689740295L;

	private static final String timeoutMinput = null;

	/**
	 * The unique ID of the object these stats are about.
	 */
	private final long id;

	private final int inputPort;
	private final String heartBeatDestAddr;
	private final int heartBeatDestPort;
	private final AlphaMode alphaMode;
	private final Color chromaKeyColor;
	private final Map<String, Integer> relaySenderMap;
	private final long lastPacketReceiveTime;
	private final String lastSourceAddr;
	private final String name;
	private final long frameCount;
	private int timeoutMillis;

	public InputStatistics(BLPacketReceiver input) {	
		this.id = System.identityHashCode(input);
		this.name = input.getName();
		this.inputPort = input.getPort();
		this.heartBeatDestAddr = input.getHeartBeatDestAddr();
		this.heartBeatDestPort = input.getHeartBeatDestPort();
		this.alphaMode = input.getAlphaMode();
		this.chromaKeyColor = input.getTransparentColour();
		this.lastPacketReceiveTime = input.getLastPacketReceiveTime();
		this.lastSourceAddr = input.getLastSourceAddr();
		this.frameCount = input.getFrameCount();
		this.timeoutMillis = input.getTimeoutMillis();

		relaySenderMap = new LinkedHashMap<String, Integer>();
		for (BLPacketSender relaySender : input.getRelaySenders()) {
			relaySenderMap.put(relaySender.getAddress(), relaySender.getPort());
		}
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getInputPort() {
		return inputPort;
	}

	public String getHeartBeatDestAddr() {
		return heartBeatDestAddr;
	}

	public int getHeartBeatDestPort() {
		return heartBeatDestPort;
	}

	public AlphaMode getAlphaMode() {
		return alphaMode;
	}

	public Color getChromaKeyColor() {
		return chromaKeyColor;
	}

	public Map<String, Integer> getRelaySenderMap() {
		return relaySenderMap;
	}

	public long getLastPacketReceiveTime() {
		return lastPacketReceiveTime;
	}

	public long getFrameCount() {
		return frameCount;
	}

	public int getTimeoutMillis() {
		return timeoutMillis;
	}

	public boolean isTimedOut() {
		if (System.currentTimeMillis() - lastPacketReceiveTime > timeoutMillis)
			return true;
		return false;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Input - Name: " + name + "\n");
		str.append("  Listen port: " + inputPort + "\n");
		str.append("  Hearbest Dest - Addr: " + heartBeatDestAddr + " - Port: "
				+ heartBeatDestPort + "\n");
		str.append("  Alpha Mode: " + alphaMode.name() + "\n");
		str.append("  Chroma-key Colour: " + chromaKeyColor.toString() + "\n");
		str.append("  Last packet receive time: " + lastPacketReceiveTime
				+ "\n");
		str.append("  Frame count: " + frameCount + "\n");
		str.append("  Relay Senders: \n");
		for (Map.Entry<String, Integer> ent : relaySenderMap.entrySet()) {
			str.append("    Sender - Addr: " + ent.getKey() + " - Port: "
					+ ent.getValue());
		}
		str.append("\n");
		return str.toString();
	}

	public String toHtml() {
		StringBuilder relaySenders = new StringBuilder();
		for (Map.Entry<String, Integer> ent : relaySenderMap.entrySet()) {
			relaySenders.append(ent.getKey()).append(":")
					.append(ent.getValue()).append("<br>");
		}
		String blinkenproxy;
		if (heartBeatDestAddr == null) {
			blinkenproxy = "none";
		} else {
			blinkenproxy = heartBeatDestAddr + ":" + heartBeatDestPort;
		}
		return String.format("<html><table cellpadding=1 cellspacing=0>"
				+ "<tr><th colspan=2>Input<br>%s<br>Port %d"
				+ "<tr><td>Proxy<td>%s" + "<tr><td>Alpha<td>%s"
				+ "<tr><td>Key<td>#%8x" + "<tr><td>Frames<td>%d"
				+ "<tr><td>Timeout<td>%dms" + "<tr><td>Source<td>%s",
				name, inputPort, blinkenproxy, alphaMode.name(), chromaKeyColor
						.getRGB(), frameCount, timeoutMillis, lastSourceAddr);
	}
}
