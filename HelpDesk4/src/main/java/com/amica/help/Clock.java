package com.amica.help;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * This class stands in for the system clock, in a way that lets us
 * simulate longer passage of time than the milliseconds it will take
 * your program to run. The {@link TestProgram} calls this class'
 * {@link #setTime(long) setTime} method before each of its events;
 * your code should call the {@link #getTime getTime} method in 
 * order to initialize time stamps in your data model.
 * 
 * Should you want it for any of your toString() methods or diagnostic
 * code, there is also a {@link #format format} method that produces
 * a nice, readable string with the date and time.
 *  
 * @author Will Provost
 */
public class Clock {
	
	public static final ZoneOffset OFFSET = ZoneOffset.of("-5");
			
	private static Instant time;
	private static DateTimeFormatter formatter =
			DateTimeFormatter.ofPattern("M/d/yy H:mm");
	
	public static long getTime() {
		return time.toEpochMilli();
	}

	public static void setTime(long milliseconds) {
		time = Instant.ofEpochMilli(milliseconds);
	}
	
	public static void setTime(String dateAndTime) {
		time = LocalDateTime.parse(dateAndTime, formatter).toInstant(OFFSET);
	}
	
	/**
	 * Note that this method doesn't foramt the current time on the clock!
	 * It formats whatever you give it. The common usage will be first
	 * to store off a timestampe somewhere (by calling {@link #getTime getTime})
	 * and then passing that value to this method.
	 */
	public static String format(long milliseconds) {
		
		return formatter.format(LocalDateTime.ofInstant
				(Instant.ofEpochMilli(milliseconds), OFFSET));
	}
}
