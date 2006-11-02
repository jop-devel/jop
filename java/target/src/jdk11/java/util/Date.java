/* java.util.Date
 Copyright (C) 1998, 1999, 2000, 2001, 2005  Free Software Foundation, Inc.

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
 * <p>
 * This class represents a specific time in milliseconds since the epoch. The
 * epoch is 1970, January 1 00:00:00.0000 UTC.
 * </p>
 * <p>
 * <code>Date</code> is intended to reflect universal time coordinate (UTC),
 * but this depends on the underlying host environment. Most operating systems
 * don't handle the leap second, which occurs about once every year or so. The
 * leap second is added to the last minute of the day on either the 30th of June
 * or the 31st of December, creating a minute 61 seconds in length.
 * </p>
 * <p>
 * The representations of the date fields are as follows:
 * <ul>
 * <li> Years are specified as the difference between the year and 1900. Thus,
 * the final year used is equal to 1900 + y, where y is the input value. </li>
 * <li> Months are represented using zero-based indexing, making 0 January and
 * 11 December. </li>
 * <li> Dates are represented with the usual values of 1 through to 31. </li>
 * <li> Hours are represented in the twenty-four hour clock, with integer values
 * from 0 to 23. 12am is 0, and 12pm is 12. </li>
 * <li> Minutes are again as usual, with values from 0 to 59. </li>
 * <li> Seconds are represented with the values 0 through to 61, with 60 and 61
 * being leap seconds (as per the ISO C standard). </li>
 * </ul>
 * </p>
 * <p>
 * Prior to JDK 1.1, this class was the sole class handling date and time
 * related functionality. However, this particular solution was not amenable to
 * internationalization. The new <code>Calendar</code> class should now be
 * used to handle dates and times, with <code>Date</code> being used only for
 * values in milliseconds since the epoch. The <code>Calendar</code> class,
 * and its concrete implementations, handle the interpretation of these values
 * into minutes, hours, days, months and years. The formatting and parsing of
 * dates is left to the <code>DateFormat</code> class, which is able to handle
 * the different types of date format which occur in different locales.
 * </p>
 * 
 * @see Calendar
 * @see GregorianCalendar
 * @see java.text.DateFormat
 * @author Jochen Hoenicke
 * @author Per Bothner (bothner@cygnus.com)
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 */
public class Date

{

	/**
	 * The time in milliseconds since the epoch.
	 */
	private transient long time;

	/**
	 * An array of week names used to map names to integer values.
	 */
	private static final String[] weekNames = { "Sun", "Mon", "Tue", "Wed",
			"Thu", "Fri", "Sat" };

	/**
	 * An array of month names used to map names to integer values.
	 */
	private static final String[] monthNames = { "Jan", "Feb", "Mar", "Apr",
			"May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	/**
	 * Creates a new Date Object representing the current time.
	 */
	public Date() {
		time = System.currentTimeMillis();
	}

	/**
	 * Creates a new Date Object representing the given time.
	 * 
	 * @param time
	 *            the time in milliseconds since the epoch.
	 */
	public Date(long time) {
		this.time = time;
	}

	/**
	 * Compares two dates for equality.
	 * 
	 * @param obj
	 *            the object to compare.
	 * @return true, if obj is a Date object and the time represented by obj is
	 *         exactly the same as the time represented by this object.
	 */
	public boolean equals(Object obj) {
		// TODO: instaceof not implemented
		//return (obj instanceof Date && time == ((Date) obj).time);
		return time == ((Date) obj).time;
	}

	/**
	 * Gets the time represented by this object.
	 * 
	 * @return the time in milliseconds since the epoch.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Computes the hash code of this <code>Date</code> as the XOR of the most
	 * significant and the least significant 32 bits of the 64 bit milliseconds
	 * value.
	 * 
	 * @return the hash code.
	 */
	public int hashCode() {
		return (int) time ^ (int) (time >>> 32);
	}

	/**
	 * Sets the time which this object should represent.
	 * 
	 * @param time
	 *            the time in milliseconds since the epoch.
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * <p>
	 * Returns a string representation of this date using the following date
	 * format:
	 * </p>
	 * <p>
	 * <code>day mon dd hh:mm:ss zz yyyy</code>
	 * </p>
	 * <p>
	 * where the fields used here are:
	 * <ul>
	 * <li> <code>day</code> -- the day of the week (Sunday through to
	 * Saturday). </li>
	 * <li> <code>mon</code> -- the month (Jan to Dec). </li>
	 * <li> <code>dd</code> -- the day of the month as two decimal digits (01
	 * to 31). </li>
	 * <li> <code>hh</code> -- the hour of the day as two decimal digits in
	 * 24-hour clock notation (01 to 23). </li>
	 * <li> <code>mm</code> -- the minute of the day as two decimal digits (01
	 * to 59). </li>
	 * <li> <code>ss</code> -- the second of the day as two decimal digits (01
	 * to 61). </li>
	 * <li> <code>zz</code> -- the time zone information if available. The
	 * possible time zones used include the abbreviations recognised by
	 * <code>parse()</code> (e.g. GMT, CET, etc.) and may reflect the fact
	 * that daylight savings time is in effect. The empty string is used if
	 * there is no time zone information. </li>
	 * <li> <code>yyyy</code> -- the year as four decimal digits. </li>
	 * </ul>
	 * <p>
	 * The <code>DateFormat</code> class should now be preferred over using
	 * this method.
	 * </p>
	 * 
	 * @return A string of the form 'day mon dd hh:mm:ss zz yyyy'
	 * @see #parse(String)
	 * @see DateFormat
	 */
	public String toString() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		String day = "0" + cal.get(Calendar.DATE);
		String hour = "0" + cal.get(Calendar.HOUR_OF_DAY);
		String min = "0" + cal.get(Calendar.MINUTE);
		String sec = "0" + cal.get(Calendar.SECOND);
		String year = "000" + cal.get(Calendar.YEAR);
		return weekNames[cal.get(Calendar.DAY_OF_WEEK) - 1] + " "
				+ monthNames[cal.get(Calendar.MONTH)] + " "
				+ day.substring(day.length() - 2) + " "
				+ hour.substring(hour.length() - 2) + ":"
				+ min.substring(min.length() - 2) + ":"
				+ sec.substring(sec.length() - 2) + " "
				+ year.substring(year.length() - 4);
	}

}
