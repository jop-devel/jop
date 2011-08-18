/* Calendar.java --
 Copyright (C) 1998, 1999, 2000, 2001, 2002, 2004, 2005  Free Software Foundation, Inc.

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
 * This class is an abstract base class for Calendars, which can be used to
 * convert between <code>Date</code> objects and a set of integer fields which
 * represent <code>YEAR</code>, <code>MONTH</code>, <code>DAY</code>,
 * etc. The <code>Date</code> object represents a time in milliseconds since
 * the Epoch. <br>
 * 
 * This class is locale sensitive. To get the Object matching the current locale
 * you can use <code>getInstance</code>. You can even provide a locale or a
 * timezone. <code>getInstance</code> returns currently a
 * <code>GregorianCalendar</code> for the current date. <br>
 * 
 * If you want to convert a date from the Year, Month, Day, DayOfWeek, etc.
 * Representation to a <code>Date</code>-Object, you can create a new
 * Calendar with <code>getInstance()</code>, <code>clear()</code> all
 * fields, <code>set(int,int)</code> the fields you need and convert it with
 * <code>getTime()</code>. <br>
 * 
 * If you want to convert a <code>Date</code>-object to the Calendar
 * representation, create a new Calendar, assign the <code>Date</code>-Object
 * with <code>setTime()</code>, and read the fields with
 * <code>get(int)</code>. <br>
 * 
 * When computing the date from time fields, it may happen, that there are
 * either two few fields set, or some fields are inconsistent. This cases will
 * handled in a calendar specific way. Missing fields are replaced by the fields
 * of the epoch: 1970 January 1 00:00. <br>
 * 
 * To understand, how the day of year is computed out of the fields look at the
 * following table. It is traversed from top to bottom, and for the first line
 * all fields are set, that line is used to compute the day. <br>
 * 
 * 
 * <pre>
 *  month + day_of_month
 *   month + week_of_month + day_of_week
 *   month + day_of_week_of_month + day_of_week
 *   day_of_year
 *   day_of_week + week_of_year
 * </pre>
 * 
 * The hour_of_day-field takes precedence over the ampm and hour_of_ampm fields.
 * <br>
 * 
 * <STRONG>Note:</STRONG> This can differ for non-Gregorian calendar. <br>
 * 
 * To convert a calendar to a human readable form and vice versa, use the
 * <code>java.text.DateFormat</code> class. <br>
 * 
 * Other useful things you can do with an calendar, is <code>roll</code>ing
 * fields (that means increase/decrease a specific field by one, propagating
 * overflows), or <code>add</code>ing/substracting a fixed amount to a field.
 * 
 * @see Date
 * @see GregorianCalendar
 * @see TimeZone
 * @see java.text.DateFormat
 */
public abstract class Calendar {
	/**
	 * Constant representing the era time field.
	 */
	public static final int ERA = 0;

	/**
	 * Constant representing the year time field.
	 */
	public static final int YEAR = 1;

	/**
	 * Constant representing the month time field. This field should contain one
	 * of the JANUARY,...,DECEMBER constants below.
	 */
	public static final int MONTH = 2;

	/**
	 * Constant representing the week of the year field.
	 * 
	 * @see #setFirstDayOfWeek(int)
	 */
	public static final int WEEK_OF_YEAR = 3;

	/**
	 * Constant representing the week of the month time field.
	 * 
	 * @see #setFirstDayOfWeek(int)
	 */
	public static final int WEEK_OF_MONTH = 4;

	/**
	 * Constant representing the day time field, synonym for DAY_OF_MONTH.
	 */
	public static final int DATE = 5;

	/**
	 * Constant representing the day time field.
	 */
	public static final int DAY_OF_MONTH = 5;

	/**
	 * Constant representing the day of year time field. This is 1 for the first
	 * day in month.
	 */
	public static final int DAY_OF_YEAR = 6;

	/**
	 * Constant representing the day of week time field. This field should
	 * contain one of the SUNDAY,...,SATURDAY constants below.
	 */
	public static final int DAY_OF_WEEK = 7;

	/**
	 * Constant representing the day-of-week-in-month field. For instance this
	 * field contains 2 for the second thursday in a month. If you give a
	 * negative number here, the day will count from the end of the month.
	 */
	public static final int DAY_OF_WEEK_IN_MONTH = 8;

	/**
	 * Constant representing the part of the day for 12-hour clock. This should
	 * be one of AM or PM.
	 */
	public static final int AM_PM = 9;

	/**
	 * Constant representing the hour time field for 12-hour clock.
	 */
	public static final int HOUR = 10;

	/**
	 * Constant representing the hour of day time field for 24-hour clock.
	 */
	public static final int HOUR_OF_DAY = 11;

	/**
	 * Constant representing the minute of hour time field.
	 */
	public static final int MINUTE = 12;

	/**
	 * Constant representing the second time field.
	 */
	public static final int SECOND = 13;

	/**
	 * Constant representing the millisecond time field.
	 */
	public static final int MILLISECOND = 14;

	/**
	 * Constant representing the time zone offset time field for the time given
	 * in the other fields. It is measured in milliseconds. The default is the
	 * offset of the time zone.
	 */
	public static final int ZONE_OFFSET = 15;

	/**
	 * Constant representing the daylight saving time offset in milliseconds.
	 * The default is the value given by the time zone.
	 */
	public static final int DST_OFFSET = 16;

	/**
	 * Number of time fields.
	 */
	public static final int FIELD_COUNT = 17;

	/**
	 * Constant representing Sunday.
	 */
	public static final int SUNDAY = 1;

	/**
	 * Constant representing Monday.
	 */
	public static final int MONDAY = 2;

	/**
	 * Constant representing Tuesday.
	 */
	public static final int TUESDAY = 3;

	/**
	 * Constant representing Wednesday.
	 */
	public static final int WEDNESDAY = 4;

	/**
	 * Constant representing Thursday.
	 */
	public static final int THURSDAY = 5;

	/**
	 * Constant representing Friday.
	 */
	public static final int FRIDAY = 6;

	/**
	 * Constant representing Saturday.
	 */
	public static final int SATURDAY = 7;

	/**
	 * Constant representing January.
	 */
	public static final int JANUARY = 0;

	/**
	 * Constant representing February.
	 */
	public static final int FEBRUARY = 1;

	/**
	 * Constant representing March.
	 */
	public static final int MARCH = 2;

	/**
	 * Constant representing April.
	 */
	public static final int APRIL = 3;

	/**
	 * Constant representing May.
	 */
	public static final int MAY = 4;

	/**
	 * Constant representing June.
	 */
	public static final int JUNE = 5;

	/**
	 * Constant representing July.
	 */
	public static final int JULY = 6;

	/**
	 * Constant representing August.
	 */
	public static final int AUGUST = 7;

	/**
	 * Constant representing September.
	 */
	public static final int SEPTEMBER = 8;

	/**
	 * Constant representing October.
	 */
	public static final int OCTOBER = 9;

	/**
	 * Constant representing November.
	 */
	public static final int NOVEMBER = 10;

	/**
	 * Constant representing December.
	 */
	public static final int DECEMBER = 11;

	/**
	 * Constant representing Undecimber. This is an artificial name useful for
	 * lunar calendars.
	 */
	public static final int UNDECIMBER = 12;

	/**
	 * Useful constant for 12-hour clock.
	 */
	public static final int AM = 0;

	/**
	 * Useful constant for 12-hour clock.
	 */
	public static final int PM = 1;

	/**
	 * The time fields. The array is indexed by the constants YEAR to
	 * DST_OFFSET.
	 * 
	 * @serial
	 */
	protected int[] fields = new int[FIELD_COUNT];

	/**
	 * The flags which tell if the fields above have a value.
	 * 
	 * @serial
	 */
	protected boolean[] isSet = new boolean[FIELD_COUNT];

	/**
	 * The time in milliseconds since the epoch.
	 * 
	 * @serial
	 */
	protected long time;

	/**
	 * Tells if the above field has a valid value.
	 * 
	 * @serial
	 */
	protected boolean isTimeSet;

	/**
	 * Tells if the fields have a valid value. This superseeds the isSet array.
	 * 
	 * @serial
	 */
	protected boolean areFieldsSet;

	/**
	 * The time zone of this calendar. Used by sub classes to do UTC / local
	 * time conversion. Sub classes can access this field with getTimeZone().
	 * 
	 * @serial
	 */
	private TimeZone zone;

	
	/**
	 * Specifies if the date/time interpretation should be lenient. If the flag
	 * is set, a date such as "February 30, 1996" will be treated as the 29th
	 * day after the February 1. If this flag is false, such dates will cause an
	 * exception.
	 * 
	 * @serial
	 */
	private boolean lenient;

	/**
	 * Sets what the first day of week is. This is used for WEEK_OF_MONTH and
	 * WEEK_OF_YEAR fields.
	 * 
	 * @serial
	 */
	private int firstDayOfWeek;

	/**
	 * Sets how many days are required in the first week of the year. If the
	 * first day of the year should be the first week you should set this value
	 * to 1. If the first week must be a full week, set it to 7.
	 * 
	 * @serial
	 */
	private int minimalDaysInFirstWeek;

	/**
	 * Is set to true if DST_OFFSET is explicitly set. In that case it's value
	 * overrides the value computed from the current time and the timezone.
	 */
	private boolean explicitDSTOffset = false;

	

	
	/**
	 * Constructs a new Calendar with the default time zone and the default
	 * locale.
	 */
	protected Calendar() {
		this(TimeZone.getDefault());
	}

	/**
	 * Constructs a new Calendar with the given time zone and the given locale.
	 * 
	 * @param zone
	 *            a time zone.
	 * @param locale
	 *            a locale.
	 */
	protected Calendar(TimeZone zone) {
		this.zone = zone;
		lenient = true;
		
		minimalDaysInFirstWeek = 1;

		firstDayOfWeek = 1;

		clear();
	}

	/**
	 * Compares the given calendar with this.
	 * 
	 * @param o
	 *            the object to that we should compare.
	 * @return true, if the given object is a calendar, and this calendar
	 *         represents a bigger time than the calendar o.
	 * @exception ClassCastException
	 *                if o is not an calendar.
	 * @since JDK1.2 you don't need to override this method
	 */
	public boolean after(Object o) {
		return getTimeInMillis() > ((Calendar) o).getTimeInMillis();
	}

	/**
	 * Compares the given calendar with this.
	 * 
	 * @param o
	 *            the object to that we should compare.
	 * @return true, if the given object is a calendar, and this calendar
	 *         represents a smaller time than the calendar o.
	 * @exception ClassCastException
	 *                if o is not an calendar.
	 * @since JDK1.2 you don't need to override this method
	 */
	public boolean before(Object o) {
		return getTimeInMillis() < ((Calendar) o).getTimeInMillis();
	}

	/**
	 * Converts the milliseconds since the epoch UTC (<code>time</code>) to
	 * time fields (<code>fields</code>). Override this method if you write
	 * your own Calendar.
	 */
	protected abstract void computeFields();

	/**
	 * Converts the time field values (<code>fields</code>) to milliseconds
	 * since the epoch UTC (<code>time</code>). Override this method if you
	 * write your own Calendar.
	 */

	public boolean equals(Object o) {
		//TODO: instaceof not implemented
		//if (!(o instanceof Calendar))
		//	return false;
		Calendar cal = (Calendar) o;
		if (getTimeInMillis() == ((Calendar) o).getTimeInMillis()
				&& cal.getFirstDayOfWeek() == getFirstDayOfWeek()
				&& cal.isLenient() == isLenient()
				&& cal.getMinimalDaysInFirstWeek() == getMinimalDaysInFirstWeek()) {
			TimeZone self = getTimeZone();
			TimeZone oth = cal.getTimeZone();
			return self == null ? oth == null : self.equals(oth);
		}
		return false;
	}

	/**
	 * Gets the value of the specified field. They are recomputed if they are
	 * invalid.
	 * 
	 * @param field
	 *            the time field. One of the time field constants.
	 * @return the value of the specified field
	 * @throws ArrayIndexOutOfBoundsException
	 *             if the field is outside the valid range. The value of field
	 *             must be >= 0 and <= <code>FIELD_COUNT</code>.
	 * @specnote Not final since JDK 1.4
	 */
	public int get(int field) {
		// If the requested field is invalid, force all fields to be recomputed.
		if (!isSet[field])
			areFieldsSet = false;

		return fields[field];
	}

	/**
	 * Creates a calendar representing the actual time, using the default time
	 * zone and locale.
	 */
	public static // synchronized
		Calendar getInstance() {
		// TODO: not implemented
		//return null;
		throw new Error("Calendar.getInstance: NYI");
	}

	/**
	 * Creates a calendar representing the actual time, using the given time
	 * zone and the default locale.
	 * 
	 * @param zone
	 *            a time zone.
	 */
	public static // synchronized
		Calendar getInstance(TimeZone zone) {
		// TODO: not implemented
		//return null;
		throw new Error("Calendar.getInstance: NYI");
	}

	/**
	 * Converts the time represented by this object to a <code>Date</code>-Object.
	 * 
	 * @return the Date.
	 */
	public final Date getTime() {
		return new Date(time);
	}

	/**
	 * Returns the time represented by this Calendar.
	 * 
	 * @return the time in milliseconds since the epoch.
	 * @specnote This was made public in 1.4.
	 */
	public long getTimeInMillis() {

		return time;
	}

	/**
	 * Gets the time zone of this calendar
	 * 
	 * @return the current time zone.
	 */
	public TimeZone getTimeZone() {
		return zone;
	}

	/**
	 * Sets the time field with the given value. This does invalidate the time
	 * in milliseconds.
	 * 
	 * @param field
	 *            the time field. One of the time field constants
	 * @param value
	 *            the value to be set.
	 * @throws ArrayIndexOutOfBoundsException
	 *             if field is outside the valid range. The value of field must
	 *             be >= 0 and <= <code>FIELD_COUNT</code>.
	 * @specnote Not final since JDK 1.4
	 */
	public void set(int field, int value) {
		if (isTimeSet)
			for (int i = 0; i < FIELD_COUNT; i++)
				isSet[i] = false;
		isTimeSet = false;
		fields[field] = value;
		isSet[field] = true;

		// The five valid date patterns, in order of priority
		// 1 YEAR + MONTH + DAY_OF_MONTH
		// 2 YEAR + MONTH + WEEK_OF_MONTH + DAY_OF_WEEK
		// 3 YEAR + MONTH + DAY_OF_WEEK_IN_MONTH + DAY_OF_WEEK
		// 4 YEAR + DAY_OF_YEAR
		// 5 YEAR + DAY_OF_WEEK + WEEK_OF_YEAR
		switch (field) {
		case MONTH: // pattern 1,2 or 3
			isSet[DAY_OF_YEAR] = false;
			isSet[WEEK_OF_YEAR] = false;
			break;
		case DAY_OF_MONTH: // pattern 1
			isSet[YEAR] = true;
			isSet[MONTH] = true;
			isSet[WEEK_OF_MONTH] = true;
			isSet[DAY_OF_WEEK] = false;
			isSet[DAY_OF_WEEK_IN_MONTH] = false;
			isSet[DAY_OF_YEAR] = false;
			isSet[WEEK_OF_YEAR] = false;
			break;
		case WEEK_OF_MONTH: // pattern 2
			if (!isSet[DAY_OF_WEEK])
				fields[DAY_OF_WEEK] = getFirstDayOfWeek();
			isSet[YEAR] = true;
			isSet[MONTH] = true;
			isSet[DAY_OF_WEEK] = true;
			isSet[DAY_OF_MONTH] = false;
			isSet[DAY_OF_WEEK_IN_MONTH] = false;
			isSet[DAY_OF_YEAR] = false;
			isSet[WEEK_OF_YEAR] = false;
			break;
		case DAY_OF_WEEK_IN_MONTH: // pattern 3
			if (!isSet[DAY_OF_WEEK])
				fields[DAY_OF_WEEK] = getFirstDayOfWeek();
			isSet[YEAR] = true;
			isSet[MONTH] = true;
			isSet[DAY_OF_WEEK] = true;
			isSet[DAY_OF_YEAR] = false;
			isSet[DAY_OF_MONTH] = false;
			isSet[WEEK_OF_MONTH] = false;
			isSet[WEEK_OF_YEAR] = false;
			break;
		case DAY_OF_YEAR: // pattern 4
			isSet[YEAR] = true;
			isSet[MONTH] = false;
			isSet[WEEK_OF_MONTH] = false;
			isSet[DAY_OF_MONTH] = false;
			isSet[DAY_OF_WEEK] = false;
			isSet[WEEK_OF_YEAR] = false;
			isSet[DAY_OF_WEEK_IN_MONTH] = false;
			break;
		case WEEK_OF_YEAR: // pattern 5
			if (!isSet[DAY_OF_WEEK])
				fields[DAY_OF_WEEK] = getFirstDayOfWeek();
			isSet[YEAR] = true;
			isSet[DAY_OF_WEEK] = true;
			isSet[MONTH] = false;
			isSet[DAY_OF_MONTH] = false;
			isSet[WEEK_OF_MONTH] = false;
			isSet[DAY_OF_YEAR] = false;
			isSet[DAY_OF_WEEK_IN_MONTH] = false;
			break;
		case AM_PM:
			isSet[HOUR] = true;
			isSet[HOUR_OF_DAY] = false;
			break;
		case HOUR_OF_DAY:
			isSet[AM_PM] = false;
			isSet[HOUR] = false;
			break;
		case HOUR:
			isSet[AM_PM] = true;
			isSet[HOUR_OF_DAY] = false;
			break;
		case DST_OFFSET:
			explicitDSTOffset = true;
		}

		// May have crossed over a DST boundary.
		if (!explicitDSTOffset && (field != DST_OFFSET && field != ZONE_OFFSET))
			isSet[DST_OFFSET] = false;
	}
	  /**
	   * Sets this Calendar's time to the given Date.  All time fields
	   * are invalidated by this method.
	   */
	  public final void setTime(Date date)
	  {
	    setTimeInMillis(date.getTime());
	  }
	  
	  
	/**
	 * Sets this Calendar's time to the given Time. All time fields are
	 * invalidated by this method.
	 * 
	 * @param time
	 *            the time in milliseconds since the epoch
	 * @specnote This was made public in 1.4.
	 */
	public void setTimeInMillis(long time) {
		clear();
		this.time = time;
		isTimeSet = true;
		computeFields();
	}

	/**
	 * Sets the time zone to the specified value.
	 * 
	 * @param zone
	 *            the new time zone
	 */
	public void setTimeZone(TimeZone zone) {
		this.zone = zone;

	}

	private void clear() {
		isTimeSet = false;
		areFieldsSet = false;
		int zoneOffs = zone.getRawOffset();
		int[] tempFields = { 1, 1970, JANUARY, 1, 1, 1, 1, THURSDAY, 1, AM, 0,
				0, 0, 0, 0, zoneOffs, 0 };
		fields = tempFields;
		for (int i = 0; i < FIELD_COUNT; i++)
			isSet[i] = false;
	}

	/**
	 * Gets what the first day of week is. This is used for WEEK_OF_MONTH and
	 * WEEK_OF_YEAR fields.
	 * 
	 * @return the first day of week. One of SUNDAY to SATURDAY.
	 */
	private int getFirstDayOfWeek() {
		return firstDayOfWeek;
	}

	/**
	 * Tells if the date/time interpretation is lenient.
	 * 
	 * @return true, if the date should be interpreted linient, false if it
	 *         should be interpreted strict.
	 */
	private boolean isLenient() {
		return lenient;
	}

	/**
	 * Gets how many days are required in the first week of the year.
	 * 
	 * @return the minimal days required in the first week.
	 * @see #setMinimalDaysInFirstWeek
	 */
	public int getMinimalDaysInFirstWeek() {
		return minimalDaysInFirstWeek;
	}
}
