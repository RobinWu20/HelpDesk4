package com.amica.help;

import java.util.Map;
import java.util.stream.Stream;

import com.amica.help.Ticket.Priority;
import com.amica.help.Ticket.Status;

/**
 * This interface defines the top-level contract to be implemented
 * by your system. The {@link TestProgram} relies on this interface
 * to simulate a series of events such as creating and resolving help tickets,
 * adding notes and tags, and running various queries.
 * 
 * @author Will Provost
 */
public interface HelpDeskAPI {
	
	/**
	 * Create a technisian with the given information.
	 */
	public void addTechnician(String ID, String name, int extension);
	
	/**
	 * Create a ticket with the given information; assign it to the
	 * least-busy technician, and return your generated ticket ID.  
	 */
	public int createTicket(String originator, String description, Priority priority);
	
	/**
	 * Create a new ticket based on a previously-resolved ticket.
	 * The new ricket should be assined to the original technician. 
	 */
	public int reopenTicket(int priorTicketID, String reason, Priority priority);
	
	/////////////////////////////////////////////////////////////////
	// Query methods:
	
	/**
	 * Return the ticket with the given ID, or null if no such ticket.
	 */
	public Ticket getTicketByID(int ID);

	/**
	 * Return a list of all tickets with the given status.
	 */
	public Stream<Ticket> getTicketsByStatus(Status status);

	/**
	 * Return a stream of all tickets with a status 
	 * <strong>other than</strong> the given status.
	 */
	public Stream<Ticket> getTicketsByNotStatus(Status status);

	/**
	 * Return a stream of tickets assigned to the given technician
	 * as found by technician ID. Note that this is not the same as the
	 * "active" or un-resolved tickets for that technician: it's all
	 * tickets that have been assigned, including resolved ones.
	 */
	public Stream<Ticket> getTicketsByTechnician(String techID);

	/**
	 * Return a stream of all tickets that have at least one of the given tags.
	 */
	public Stream<Ticket> getTicketsWithAnyTag(Tag... tags);

	/**
	 * Return the average time, in minutes, from creation to resolution
	 * for all resolved tickets. Un-resolved tickets are not considered.
	 */
	public int getAverageMinutesToResolve();
	/**
	 * Return a map with keys that are technician IDs and values that are
	 * the average time to resolve tickets for the corresponding technician.
	 * If a technician has no resolved tickets, there should not
	 * be any entry in the map.
	 */
	public Map<String,Double> getAverageMinutesToResolvePerTechnician();

	/**
	 * Return a stream of all tickets whose descriptions and/or event notes
	 * include the given text.
	 */
	public Stream<Ticket> getTicketsByText(String text);
	
	/**
	 * Returns a stream of the most recent N events, system-side.
	 */
	public Stream<Event> getLatestActivity(int count);
}
