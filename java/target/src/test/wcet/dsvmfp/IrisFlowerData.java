/*
  This file is part of JOP, the Java Optimized Processor
    see <http://www.jopdesign.com/>

  Copyright (C) 2009, Martin Schoeberl (martin@jopdesign.com)

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


/**
 * 
 */
package wcet.dsvmfp;

/**
 * @author Martin Schoeberl (martin@jopdesign.com)
 *
 */
public class IrisFlowerData extends Data {
	private static float data[][] = {
		{ 5.1F, 3.5F, 1.4F, 0.2F, 1F },
		{ 4.9F, 3.0F, 1.4F, 0.2F, 1F },
		{ 4.7F, 3.2F, 1.3F, 0.2F, 1F },
		{ 4.6F, 3.1F, 1.5F, 0.2F, 1F },
		{ 5.0F, 3.6F, 1.4F, 0.2F, 1F },
		{ 5.4F, 3.9F, 1.7F, 0.4F, 1F },
		{ 4.6F, 3.4F, 1.4F, 0.3F, 1F },
		{ 5.0F, 3.4F, 1.5F, 0.2F, 1F },
		{ 4.4F, 2.9F, 1.4F, 0.2F, 1F },
		{ 4.9F, 3.1F, 1.5F, 0.1F, 1F },
		{ 5.4F, 3.7F, 1.5F, 0.2F, 1F },
		{ 4.8F, 3.4F, 1.6F, 0.2F, 1F },
		{ 4.8F, 3.0F, 1.4F, 0.1F, 1F },
		{ 4.3F, 3.0F, 1.1F, 0.1F, 1F },
		{ 5.8F, 4.0F, 1.2F, 0.2F, 1F },
		{ 5.7F, 4.4F, 1.5F, 0.4F, 1F },
		{ 5.4F, 3.9F, 1.3F, 0.4F, 1F },
		{ 5.1F, 3.5F, 1.4F, 0.3F, 1F },
		{ 5.7F, 3.8F, 1.7F, 0.3F, 1F },
		{ 5.1F, 3.8F, 1.5F, 0.3F, 1F },
		{ 5.4F, 3.4F, 1.7F, 0.2F, 1F },
		{ 5.1F, 3.7F, 1.5F, 0.4F, 1F },
		{ 4.6F, 3.6F, 1.0F, 0.2F, 1F },
		{ 5.1F, 3.3F, 1.7F, 0.5F, 1F },
		{ 4.8F, 3.4F, 1.9F, 0.2F, 1F },
		{ 5.0F, 3.0F, 1.6F, 0.2F, 1F },
		{ 5.0F, 3.4F, 1.6F, 0.4F, 1F },
		{ 5.2F, 3.5F, 1.5F, 0.2F, 1F },
		{ 5.2F, 3.4F, 1.4F, 0.2F, 1F },
		{ 4.7F, 3.2F, 1.6F, 0.2F, 1F },
		{ 4.8F, 3.1F, 1.6F, 0.2F, 1F },
		{ 5.4F, 3.4F, 1.5F, 0.4F, 1F },
		{ 5.2F, 4.1F, 1.5F, 0.1F, 1F },
		{ 5.5F, 4.2F, 1.4F, 0.2F, 1F },
		{ 4.9F, 3.1F, 1.5F, 0.2F, 1F },
		{ 5.0F, 3.2F, 1.2F, 0.2F, 1F },
		{ 5.5F, 3.5F, 1.3F, 0.2F, 1F },
		{ 4.9F, 3.6F, 1.4F, 0.1F, 1F },
		{ 4.4F, 3.0F, 1.3F, 0.2F, 1F },
		{ 5.1F, 3.4F, 1.5F, 0.2F, 1F },
		{ 5.0F, 3.5F, 1.3F, 0.3F, 1F },
		{ 4.5F, 2.3F, 1.3F, 0.3F, 1F },
		{ 4.4F, 3.2F, 1.3F, 0.2F, 1F },
		{ 5.0F, 3.5F, 1.6F, 0.6F, 1F },
		{ 5.1F, 3.8F, 1.9F, 0.4F, 1F },
		{ 4.8F, 3.0F, 1.4F, 0.3F, 1F },
		{ 5.1F, 3.8F, 1.6F, 0.2F, 1F },
		{ 4.6F, 3.2F, 1.4F, 0.2F, 1F },
		{ 5.3F, 3.7F, 1.5F, 0.2F, 1F },
		{ 5.0F, 3.3F, 1.4F, 0.2F, 1F },
		{ 7.0F, 3.2F, 4.7F, 1.4F, 2F },
		{ 6.4F, 3.2F, 4.5F, 1.5F, 2F },
		{ 6.9F, 3.1F, 4.9F, 1.5F, 2F },
		{ 5.5F, 2.3F, 4.0F, 1.3F, 2F },
		{ 6.5F, 2.8F, 4.6F, 1.5F, 2F },
		{ 5.7F, 2.8F, 4.5F, 1.3F, 2F },
		{ 6.3F, 3.3F, 4.7F, 1.6F, 2F },
		{ 4.9F, 2.4F, 3.3F, 1.0F, 2F },
		{ 6.6F, 2.9F, 4.6F, 1.3F, 2F },
		{ 5.2F, 2.7F, 3.9F, 1.4F, 2F },
		{ 5.0F, 2.0F, 3.5F, 1.0F, 2F },
		{ 5.9F, 3.0F, 4.2F, 1.5F, 2F },
		{ 6.0F, 2.2F, 4.0F, 1.0F, 2F },
		{ 6.1F, 2.9F, 4.7F, 1.4F, 2F },
		{ 5.6F, 2.9F, 3.6F, 1.3F, 2F },
		{ 6.7F, 3.1F, 4.4F, 1.4F, 2F },
		{ 5.6F, 3.0F, 4.5F, 1.5F, 2F },
		{ 5.8F, 2.7F, 4.1F, 1.0F, 2F },
		{ 6.2F, 2.2F, 4.5F, 1.5F, 2F },
		{ 5.6F, 2.5F, 3.9F, 1.1F, 2F },
		{ 5.9F, 3.2F, 4.8F, 1.8F, 2F },
		{ 6.1F, 2.8F, 4.0F, 1.3F, 2F },
		{ 6.3F, 2.5F, 4.9F, 1.5F, 2F },
		{ 6.1F, 2.8F, 4.7F, 1.2F, 2F },
		{ 6.4F, 2.9F, 4.3F, 1.3F, 2F },
		{ 6.6F, 3.0F, 4.4F, 1.4F, 2F },
		{ 6.8F, 2.8F, 4.8F, 1.4F, 2F },
		{ 6.7F, 3.0F, 5.0F, 1.7F, 2F },
		{ 6.0F, 2.9F, 4.5F, 1.5F, 2F },
		{ 5.7F, 2.6F, 3.5F, 1.0F, 2F },
		{ 5.5F, 2.4F, 3.8F, 1.1F, 2F },
		{ 5.5F, 2.4F, 3.7F, 1.0F, 2F },
		{ 5.8F, 2.7F, 3.9F, 1.2F, 2F },
		{ 6.0F, 2.7F, 5.1F, 1.6F, 2F },
		{ 5.4F, 3.0F, 4.5F, 1.5F, 2F },
		{ 6.0F, 3.4F, 4.5F, 1.6F, 2F },
		{ 6.7F, 3.1F, 4.7F, 1.5F, 2F },
		{ 6.3F, 2.3F, 4.4F, 1.3F, 2F },
		{ 5.6F, 3.0F, 4.1F, 1.3F, 2F },
		{ 5.5F, 2.5F, 4.0F, 1.3F, 2F },
		{ 5.5F, 2.6F, 4.4F, 1.2F, 2F },
		{ 6.1F, 3.0F, 4.6F, 1.4F, 2F },
		{ 5.8F, 2.6F, 4.0F, 1.2F, 2F },
		{ 5.0F, 2.3F, 3.3F, 1.0F, 2F },
		{ 5.6F, 2.7F, 4.2F, 1.3F, 2F },
		{ 5.7F, 3.0F, 4.2F, 1.2F, 2F },
		{ 5.7F, 2.9F, 4.2F, 1.3F, 2F },
		{ 6.2F, 2.9F, 4.3F, 1.3F, 2F },
		{ 5.1F, 2.5F, 3.0F, 1.1F, 2F },
		{ 5.7F, 2.8F, 4.1F, 1.3F, 2F },
		{ 6.3F, 3.3F, 6.0F, 2.5F, 3F },
		{ 5.8F, 2.7F, 5.1F, 1.9F, 3F },
		{ 7.1F, 3.0F, 5.9F, 2.1F, 3F },
		{ 6.3F, 2.9F, 5.6F, 1.8F, 3F },
		{ 6.5F, 3.0F, 5.8F, 2.2F, 3F },
		{ 7.6F, 3.0F, 6.6F, 2.1F, 3F },
		{ 4.9F, 2.5F, 4.5F, 1.7F, 3F },
		{ 7.3F, 2.9F, 6.3F, 1.8F, 3F },
		{ 6.7F, 2.5F, 5.8F, 1.8F, 3F },
		{ 7.2F, 3.6F, 6.1F, 2.5F, 3F },
		{ 6.5F, 3.2F, 5.1F, 2.0F, 3F },
		{ 6.4F, 2.7F, 5.3F, 1.9F, 3F },
		{ 6.8F, 3.0F, 5.5F, 2.1F, 3F },
		{ 5.7F, 2.5F, 5.0F, 2.0F, 3F },
		{ 5.8F, 2.8F, 5.1F, 2.4F, 3F },
		{ 6.4F, 3.2F, 5.3F, 2.3F, 3F },
		{ 6.5F, 3.0F, 5.5F, 1.8F, 3F },
		{ 7.7F, 3.8F, 6.7F, 2.2F, 3F },
		{ 7.7F, 2.6F, 6.9F, 2.3F, 3F },
		{ 6.0F, 2.2F, 5.0F, 1.5F, 3F },
		{ 6.9F, 3.2F, 5.7F, 2.3F, 3F },
		{ 5.6F, 2.8F, 4.9F, 2.0F, 3F },
		{ 7.7F, 2.8F, 6.7F, 2.0F, 3F },
		{ 6.3F, 2.7F, 4.9F, 1.8F, 3F },
		{ 6.7F, 3.3F, 5.7F, 2.1F, 3F },
		{ 7.2F, 3.2F, 6.0F, 1.8F, 3F },
		{ 6.2F, 2.8F, 4.8F, 1.8F, 3F },
		{ 6.1F, 3.0F, 4.9F, 1.8F, 3F },
		{ 6.4F, 2.8F, 5.6F, 2.1F, 3F },
		{ 7.2F, 3.0F, 5.8F, 1.6F, 3F },
		{ 7.4F, 2.8F, 6.1F, 1.9F, 3F },
		{ 7.9F, 3.8F, 6.4F, 2.0F, 3F },
		{ 6.4F, 2.8F, 5.6F, 2.2F, 3F },
		{ 6.3F, 2.8F, 5.1F, 1.5F, 3F },
		{ 6.1F, 2.6F, 5.6F, 1.4F, 3F },
		{ 7.7F, 3.0F, 6.1F, 2.3F, 3F },
		{ 6.3F, 3.4F, 5.6F, 2.4F, 3F },
		{ 6.4F, 3.1F, 5.5F, 1.8F, 3F },
		{ 6.0F, 3.0F, 4.8F, 1.8F, 3F },
		{ 6.9F, 3.1F, 5.4F, 2.1F, 3F },
		{ 6.7F, 3.1F, 5.6F, 2.4F, 3F },
		{ 6.9F, 3.1F, 5.1F, 2.3F, 3F },
		{ 5.8F, 2.7F, 5.1F, 1.9F, 3F },
		{ 6.8F, 3.2F, 5.9F, 2.3F, 3F },
		{ 6.7F, 3.3F, 5.7F, 2.5F, 3F },
		{ 6.7F, 3.0F, 5.2F, 2.3F, 3F },
		{ 6.3F, 2.5F, 5.0F, 1.9F, 3F },
		{ 6.5F, 3.0F, 5.2F, 2.0F, 3F },
		{ 6.2F, 3.4F, 5.4F, 2.3F, 3F },
		{ 5.9F, 3.0F, 5.1F, 1.8F, 3F },
	};
	
	/* (non-Javadoc)
	 * @see wcet.dsvmfp.Data#getData()
	 */
	public float[][] getData() {
		return data;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Data data = new IrisFlowerData();

		System.out.println(data);
	}

}
