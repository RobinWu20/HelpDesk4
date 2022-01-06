package com.amica.help;

import static com.amica.help.Ticket.Status;

import lombok.Getter;

/**
 * Represents an event in a ticket's history.
 *
 * @author Will Provost
 */
@Getter
public class Event implements Comparable<Event> {

	private int ticketID;
	private long timestamp;
	private Status newStatus;
	private String note;
	
	public Event(int ticketID, String note) {
		this(ticketID, null, note);
	}
	
	public Event(int ticketID, Status newStatus, String note) {
		this.ticketID = ticketID;
		this.timestamp= Clock.getTime();
		this.newStatus = newStatus;
		this.note = note;
	}
	
	@Override
	public String toString() {
		String result = "Event: ticket" + ticketID + ", " + note;
		if (newStatus != null) {
			result += " [" + newStatus + "]";
		}
		result += " (" + Clock.format(timestamp) + ")";
		return result;
	}
	
	public int compareTo(Event other) {
		return Long.compare(timestamp, other.getTimestamp());
	}
}
