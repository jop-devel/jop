/* java.util.TimeZone
 Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005
 Free Software Foundation, Inc.

 This file is part of GNU Classpath.

 GNU Classpath is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2, or (at your option)
 any later version.

 GNU Classpath is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GNU Classpath; see the file COPYING.  If not, write to the
 Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version. */

package java.util;

/**
 * This class represents a time zone offset and handles daylight savings.
 * 
 * You can get the default time zone with <code>getDefault</code>. This
 * represents the time zone where program is running.
 * 
 * Another way to create a time zone is <code>getTimeZone</code>, where you
 * can give an identifier as parameter. For instance, the identifier of the
 * Central European Time zone is "CET".
 * 
 * With the <code>getAvailableIDs</code> method, you can get all the supported
 * time zone identifiers.
 * 
 * @see Calendar
 * @see SimpleTimeZone
 * @author Jochen Hoenicke
 */
public abstract class TimeZone {

	/**
	 * Constant used to indicate that a short timezone abbreviation should be
	 * returned, such as "EST"
	 */
	public static final int SHORT = 0;

	/**
	 * Constant used to indicate that a long timezone name should be returned,
	 * such as "Eastern Standard Time".
	 */
	public static final int LONG = 1;

	/**
	 * The time zone identifier, e.g. PST.
	 */
	private String ID;

	/**
	 * The default time zone, as returned by getDefault.
	 */

	private static TimeZone defaultZone0;

	public TimeZone() {
	}

	/**
	 * Gets all available IDs.
	 * 
	 * @return An array of all supported IDs.
	 */
	public static String[] getAvailableIDs() {
		String[] jopId = new String[1];
		jopId[0] = "joptimezone";
		return jopId;
	}

	/**
	 * Returns the time zone under which the host is running. This can be
	 * changed with setDefault.
	 * 
	 * @return A clone of the current default time zone for this host.
	 * @see #setDefault
	 */
	public static TimeZone getDefault() {

		return defaultZone0;
	}

	/**
	 * Gets the identifier of this time zone. For instance, PST for Pacific
	 * Standard Time.
	 * 
	 * @returns the ID of this time zone.
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Gets the time zone offset, for current date, modified in case of daylight
	 * savings. This is the offset to add to UTC to get the local time.
	 * 
	 * @param era
	 *            the era of the given date
	 * @param year
	 *            the year of the given date
	 * @param month
	 *            the month of the given date, 0 for January.
	 * @param day
	 *            the day of month
	 * @param dayOfWeek
	 *            the day of week
	 * @param milliseconds
	 *            the millis in the day (in local standard time)
	 * @return the time zone offset in milliseconds.
	 */
	public abstract int getOffset(int era, int year, int month, int day,
			int dayOfWeek, int milliseconds);

	/**
	 * Gets the time zone offset, ignoring daylight savings. This is the offset
	 * to add to UTC to get the local time.
	 * 
	 * @return the time zone offset in milliseconds.
	 */
	public abstract int getRawOffset();

	/**
	 * Gets the TimeZone for the given ID.
	 * 
	 * @param ID
	 *            the time zone identifier.
	 * @return The time zone for the identifier or GMT, if no such time zone
	 *         exists.
	 */
	// FIXME: XXX: JCL indicates this and other methods are synchronized.
	public static TimeZone getTimeZone(String ID) {

		return defaultZone0;

	}

	/**
	 * Returns true, if this time zone uses Daylight Savings Time.
	 */
	public abstract boolean useDaylightTime();

}
