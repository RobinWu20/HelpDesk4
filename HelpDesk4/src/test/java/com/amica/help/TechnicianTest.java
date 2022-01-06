package com.amica.help;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.amica.help.Ticket.Priority;
import com.amica.help.Ticket.Status;

/**
 * Unit test for the {@link Technician} class.
 * 
 * @author Will Provost
 */
public class TechnicianTest {
	
	public static final String ID =  "ID";
	public static final String NAME = "NAME";
	public static final int EXTENSION = 12345;
	
	public static final int ID1 = 1;
	public static final int ID2 = 2;
	public static final int ID3 = 3;
	public static final String ORIGINATOR = "ROGINATOR";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final Priority PRIORITY1 = Priority.HIGH;
	public static final Priority PRIORITY2 = Priority.HIGH;
	public static final Priority PRIORITY3 = Priority.URGENT;
	
	private Technician technician;
	private Ticket ticket1;
	private Ticket ticket2;
	private Ticket ticket3;

	private List<Integer> activeTickets() {
		return technician.getActiveTickets().map(Ticket::getID).toList();
	}
	
	@BeforeEach
	public void setUp() {
		technician = new Technician(ID, NAME, EXTENSION);
		
		Clock.setTime("1/6/22 8:00");
		ticket1 = new Ticket(ID1, ORIGINATOR, DESCRIPTION, PRIORITY1);
		Clock.setTime("1/6/22 7:00");
		ticket2 = new Ticket(ID2, ORIGINATOR, DESCRIPTION, PRIORITY2);
		Clock.setTime("1/6/22 9:00");
		ticket3 = new Ticket(ID3, ORIGINATOR, DESCRIPTION, PRIORITY3);
	}
	
	@Test
	public void testInitialization() {
		assertThat(technician.getID(), equalTo(ID));
		assertThat(technician.getName(), equalTo(NAME));
		assertThat(technician.getExtension(), equalTo(EXTENSION));
		assertThat(activeTickets(), empty());
	}
	
	@Test
	public void testAddActiveTicket() {
		ticket1.assign(technician);
		assertThat(activeTickets(), contains(1));
	}
	
	public void testAddActiveTicket_Created() {
		assertThrows(IllegalArgumentException.class, 
				() ->technician.addActiveTicket(ticket1));
	}
	
	@Test
	public void testAddActiveTickets() {
		ticket1.assign(technician);
		ticket2.assign(technician);
		assertThat(activeTickets(), contains(1, 2));
	}
	
	public void testAddActiveTicket_Duplicate() {
		ticket1.assign(technician);
		technician.addActiveTicket(ticket1);
		assertThat(activeTickets(), contains(1));
	}
	
	@Test
	public void testTicketOrdering() {
		ticket1.assign(technician);
		ticket2.assign(technician);
		ticket3.assign(technician);
		assertThat(activeTickets(), contains(3, 1, 2));
	}
	
	@Test
	public void testRemoveActiveTicket() {
		ticket1.assign(technician);
		ticket2.assign(technician);
		ticket1.resolve("");
		assertThat(activeTickets(), contains(2));
	}
	
	@Test
	public void testRemoveActiveTicket_Unresolved() {
		ticket1.assign(technician);
		assertThrows(IllegalArgumentException.class, 
				() ->technician.removeActiveTicket(ticket1));
	}
}
