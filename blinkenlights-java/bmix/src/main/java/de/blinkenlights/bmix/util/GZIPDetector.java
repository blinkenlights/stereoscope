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
package de.blinkenlights.bmix.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class GZIPDetector {
	
	
	public static boolean isGZIPFile(File file) throws IOException {
		byte header[] = new byte[2];
		FileInputStream in = new FileInputStream(file);
		in.read(header);
		in.close();
		System.out.println(Arrays.toString(header));
		// gzip magic header
		if ((header[0] & 0xFF) == 0x1f && (header[1] & 0xFF) == 0x8b) {
			return true;
		} 
		return false;
	}
}
