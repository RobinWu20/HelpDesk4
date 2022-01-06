package com.amica.help;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static com.amica.help.TicketTest.*;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.amica.help.Ticket.Priority;
import com.amica.help.Ticket.Status;

/**
 * JUnit test that is initially created as a port of {@link TestProgram},
 * and adds some finter-grained test cases of its own.
 * Uses @Nested tests to organize cases by the state of the target help desk
 * as it is initialized in stages: we can test before technicians are added,
 * after there are technicians are added, and after the big master scenario
 * from the original test program is run.
 * 
 * @author Will Provost
 */
public class HelpDeskTest {

	public static final String TECH1 = "Andree";
	public static final String TECH2 = "Boris";
	public static final String TECH3 = "Caelem";
	public static final String TECH4 = "Dineh";
	
	private HelpDesk helpDesk = new HelpDesk();;
	
	/**
	 * To ease migration from our main-method testing to JUnit,
	 * we keep this helper functions, and it turns around and makes a
	 * Hamcrest matcher-based assertion. 
	 */
	public static void assertThat(boolean condition, String error) {
		MatcherAssert.assertThat(error, condition, equalTo(true));
	}

	/**
	 * To ease migration from our main-method testing to JUnit,
	 * we keep this helper functions, and it turns around and makes a
	 * Hamcrest matcher-based assertion. 
	 */
	public static void assertEqual(Object actual, Object expected, String error) {
		MatcherAssert.assertThat(error, actual, equalTo(expected));
	}

	/**
	 * We set up synonyms and capitalizations just once, because they are
	 * managed as statics.
	 */
	@BeforeAll
	public static void setUpBeforeAll() {
		Tag.addSynonym("RDP", "remoting");
		Tag.addSynonym("remote desktop", "remoting");
		Tag.addCapitalization("CMA");
		Tag.addCapitalization("GitHub");
		Tag.addCapitalization("VM");
		Tag.addCapitalization("VPN");
	}
	
	/**
	 * Asserthtat the help desk throws the correct exception if asked to
	 * create and assign a ticket when there is no staff.
	 */
	@Test
	public void testNoTechnicians() {
		assertThrows(IllegalStateException.class, () -> 
			helpDesk.createTicket(ORIGINATOR, DESCRIPTION, PRIORITY));
	}

	/**
	 * The rest of the tests are in this nested class, so they will all
	 * run against a help desk with a staff of technicians. This lets us
	 * prove out assignment functionality in better isolation, avoiding
	 * the complexity of the master scenario.
	 */
	@Nested
	public class TechnicianTests {
		
		@BeforeEach
		public void setUp() {
	
			helpDesk.addTechnician("A05589", TECH1, 55491);
			helpDesk.addTechnician("A12312", TECH2, 12399);
			helpDesk.addTechnician("A17440", TECH3, 34002);
			helpDesk.addTechnician("A20265", TECH4, 60709);

			Clock.setTime("11/1/21 8:00");
		}
		
		private int createTicket() {
			return helpDesk.createTicket(ORIGINATOR, DESCRIPTION, PRIORITY);
		}
		
		@Test
		public void testCreateTicket() {
			int ID = createTicket();
			MatcherAssert.assertThat(ID, equalTo(1));
			Ticket ticket = helpDesk.getTicketByID(1);
			MatcherAssert.assertThat(ticket.getStatus(), equalTo(Status.ASSIGNED));
		}
		
		@Test
		public void testAssignment1() {
			int ID = createTicket();
			Ticket ticket = helpDesk.getTicketByID(ID);
			MatcherAssert.assertThat(ticket.getTechnician().getName(), equalTo(TECH1));
		}
		
		@Test
		public void testAssignment2() {
			createTicket();
			int ID = createTicket();
			Ticket ticket = helpDesk.getTicketByID(ID);
			MatcherAssert.assertThat(ticket.getTechnician().getName(), equalTo(TECH2));
		}
		
		@Test
		public void testAssignment3() {
			int ID = createTicket();
			helpDesk.getTicketByID(ID).resolve(RESOLVE_REASON);
			
			ID = createTicket();
			Ticket ticket = helpDesk.getTicketByID(ID);
			MatcherAssert.assertThat(ticket.getTechnician().getName(), equalTo(TECH1));
		}
		
		/**
		 * This final nested class sets up the master scenario, which supports
		 * the original 9 test cases from the test program.
		 */
		@Nested
		public class ScenarioTests {
			
			@BeforeEach
			public void setUp() {
				Clock.setTime("11/1/21 8:22");
				helpDesk.createTicket("A21013", "Unable to log in.", Priority.HIGH);
				Clock.setTime("11/1/21 8:23");
				helpDesk.getTicketByID(1).addTags("remoting");
				Clock.setTime("11/1/21 8:33");
				helpDesk.createTicket("A19556", "Can't connect to remote desktop from my laptop.", Priority.HIGH);
				Clock.setTime("11/1/21 8:34");
				helpDesk.getTicketByID(2).addTags("remoting", "laptop");
				Clock.setTime("11/1/21 8:36");
				helpDesk.getTicketByID(2).wait("Checking if the user can connect from other machines.");
				Clock.setTime("11/1/21 8:37");
				helpDesk.createTicket("A05989", "Need GitHub access.", Priority.MEDIUM);
				Clock.setTime("11/1/21 8:38");
				helpDesk.getTicketByID(3).addTags("permissions", "GitHub");
				Clock.setTime("11/1/21 8:39");
				helpDesk.getTicketByID(3).wait("Requested approval from manager.");
				Clock.setTime("11/1/21 9:05");
				helpDesk.createTicket("T17549", "Can't use just one screen for remote desktop.", Priority.MEDIUM);
				Clock.setTime("11/1/21 9:06");
				helpDesk.getTicketByID(4).addTags("remote desktop");
				Clock.setTime("11/1/21 9:07");
				helpDesk.getTicketByID(4).resolve("Explained that this is not a feature we support right now.");
				Clock.setTime("11/1/21 9:48");
				helpDesk.getTicketByID(1).addNote("Determined that it's a VPN problem rather than RDP.");
				Clock.setTime("11/1/21 9:51");
				helpDesk.getTicketByID(1).addNote("Recommended that the user update their browser.");
				Clock.setTime("11/1/21 9:52");
				helpDesk.getTicketByID(1).addTags("VPN");
				Clock.setTime("11/1/21 14:11");
				helpDesk.createTicket("A24490", "Files on my user drive are currupt.", Priority.HIGH);
				Clock.setTime("11/1/21 14:12");
				helpDesk.getTicketByID(5).addTags("VM");
				Clock.setTime("11/1/21 14:14");
				helpDesk.getTicketByID(2).resume("User: Yes, I can connect from other desktop machines at Amica.");
				Clock.setTime("11/1/21 14:17");
				helpDesk.getTicketByID(5).wait("Requested examples of corrupt files.");
				Clock.setTime("11/1/21 16:39");
				helpDesk.createTicket("T24090", "Need CMA access.", Priority.MEDIUM);
				Clock.setTime("11/1/21 16:41");
				helpDesk.getTicketByID(6).addTags("Permissions", "CMA");
				Clock.setTime("11/1/21 16:42");
				helpDesk.getTicketByID(6).wait("Requested approval from manager.");
				
				Clock.setTime("11/2/21 8:11");
				helpDesk.createTicket("A15711", "Laptop won't start up.", Priority.URGENT);
				Clock.setTime("11/2/21 8:12");
				helpDesk.getTicketByID(7).addTags("laptop");
				Clock.setTime("11/2/21 8:45");
				helpDesk.getTicketByID(6).resume("Received approval.");
				helpDesk.getTicketByID(6).resolve("Added permission.");
				Clock.setTime("11/2/21 8:52");
				helpDesk.createTicket("A20271", "Can't login.", Priority.HIGH);
				Clock.setTime("11/2/21 8:53");
				helpDesk.getTicketByID(8).addTags("remoting");
				Clock.setTime("11/2/21 10:19");
				helpDesk.createTicket("T13370", "Need to reset MobilePass.", Priority.HIGH);
				Clock.setTime("11/2/21 10:20");
				helpDesk.getTicketByID(3).resume("Received approval.");
				helpDesk.getTicketByID(3).resolve("Added permission.");
				Clock.setTime("11/2/21 10:21");
				helpDesk.getTicketByID(9).addTags("vpn");
				Clock.setTime("11/2/21 10:22");
				helpDesk.getTicketByID(9).wait("Tried to contact user; left voice mail.");
				Clock.setTime("11/2/21 11:00");
				helpDesk.createTicket("A14401", "Unable to log in.", Priority.HIGH);
				Clock.setTime("11/2/21 11:01");
				helpDesk.getTicketByID(10).addTags("RDP");
				Clock.setTime("11/2/21 11:32");
				helpDesk.createTicket("T11918", "No disk space left! I don't have that much stuff on here; not sure what's taking up all the space.", Priority.URGENT);
				Clock.setTime("11/2/21 11:33");
				helpDesk.getTicketByID(11).addTags("vm");
				Clock.setTime("11/2/21 14:49");
				helpDesk.getTicketByID(1).resolve("User reports that the browser update fixed it.");
				
				Clock.setTime("11/3/21 9:22");
				helpDesk.createTicket("A13288", "Need GitHub access.", Priority.MEDIUM);
				Clock.setTime("11/3/21 9:23");
				helpDesk.getTicketByID(12).addTags("permissions", "github");
				Clock.setTime("11/3/21 9:24");
				helpDesk.getTicketByID(12).wait("Requested approval from manager.");
				Clock.setTime("11/3/21 11:11");
				helpDesk.createTicket("A22465", "Laptop audio seems to be broken.", Priority.MEDIUM);
				Clock.setTime("11/3/21 11:12");
				helpDesk.getTicketByID(13).addTags("laptop", "audio");
				Clock.setTime("11/3/21 11:39");
				helpDesk.createTicket("A18087", "Can't log in.", Priority.HIGH);
				Clock.setTime("11/3/21 11:40");
				helpDesk.getTicketByID(14).addTags("remote desktop");
				Clock.setTime("11/3/21 13:11");
				helpDesk.getTicketByID(10).resolve("Opened remote access to RI150WS3344; confirmed user can connect.");
				Clock.setTime("11/3/21 13:16");
				helpDesk.getTicketByID(5).resume("User: See /Users/A10551/Projects/Spec_20211015.pdf.");
				Clock.setTime("11/3/21 13:17");
				helpDesk.getTicketByID(5).addNote("Building a new VM.");
				Clock.setTime("11/3/21 13:18");
				helpDesk.getTicketByID(5).resolve("Migrated most files to new VM, restored remaining files from backups, switched IP address over.");
				Clock.setTime("11/3/21 13:19");
				helpDesk.getTicketByID(11).resolve("Found user's ME2020 Maven cache way overloaded, recommended cleaning it out.");
			}
			
			/**
			 * This method tests that the ijmplementation meets the following requirements.
			 * <ul>
			 *   <li>We can find a ticket by ID.</li>
			 *   <li>IDs are a generated sequence starting at 1.</li>
			 *   <li>We can find all tickets in a given status.</li>
			 *   <li>We can find all tickets not in a given status.</li>
			 *   <li>Tickets are automatically assigned after being created.</li>
			 *   <li>Tickets show the RESOLVED status after being resolved.</li>
			 * </ul>
			 */
			@Test
			public void test1_Tickets() {
				assertThat(helpDesk.getTicketByID(0) == null, "There shouldn't be a ticket 0.");
				assertThat(helpDesk.getTicketByID(1) != null, "There should be a ticket 1.");
				assertThat(helpDesk.getTicketByID(14) != null, "There should be a ticket 14.");
				 
				assertThat(helpDesk.getTicketsByStatus(Status.CREATED).count() == 0,
						"There shuldn't be any tickets in the CREATED state.");
				assertThat(helpDesk.getTicketsByStatus(Status.RESOLVED).count() == 7,
						"There shuld be 7 tickets in the RESOLVED state.");
			}
			
			/**
			 * This method tests that the ijmplementation meets the following requirements.
			 * <ul>
			 *   <li>We can't re-assign a resolved ticket.</li>
			 *   <li>Each ticket has a history that includes events for assignment and 
			 *       resolution, with timestamps taken from the {@link Clock}.</li>
			 *   <li>Notes added to the ticket appear in the event history as well.</li>
			 * </ul>
			 */
			@Test
			public void test2_History() {
				Iterator<Event> history = helpDesk.getTicketByID(4).getHistory().iterator();
				Event created4 = history.next();
				assertEqual(Clock.format(created4.getTimestamp()), "11/1/21 9:05", 
						"Ticket 4 should have been created at 9:05, was %s.");
				assertEqual(created4.getNewStatus(), Status.CREATED, 
						"Ticket 4's first event should be CREATED, was %s.");
				assertEqual(created4.getNote(), "Created ticket.", 
						"Ticket 4 creation note is wrong: %s.");
				Event assigned4 = history.next();
				assertEqual(Clock.format(assigned4.getTimestamp()), "11/1/21 9:05", 
						"Ticket 4 should have been assigned at 9:05, was %s.");
				assertEqual(assigned4.getNewStatus(), Status.ASSIGNED, 
						"Ticket 4's second event should be ASSIGNED, was %s.");
				assertEqual(assigned4.getNote(), "Assigned to Technician A20265, Dineh.", 
						"Ticket 4 assignment note is wrong: %s.");
				Event resolved4 = history.next();
				assertEqual(Clock.format(resolved4.getTimestamp()), "11/1/21 9:07", 
						"Ticket 4 should have been resolved at 9:07, was %s.");
				assertEqual(resolved4.getNewStatus(), Status.RESOLVED, 
						"Ticket 4's second event should be RESOLVED, was %s.");
				assertEqual(resolved4.getNote(), "Explained that this is not a feature we support right now.", 
						"Ticket 4 resolution note is wrong: %s.");
				
				history = helpDesk.getTicketByID(2).getHistory().iterator();
				history.next();
				history.next();
				history.next();
				Event note7 = history.next();
				assertEqual(Clock.format(note7.getTimestamp()), "11/1/21 14:14", 
						"Ticket 2's 2nd note should be stamped 14:14, was %s.");
				assertThat(note7.getNewStatus() == Ticket.Status.ASSIGNED, 
						"Ticket 2's second note status should be ASSIGNED, was " + 
				note7.getNewStatus() + ".");
				assertEqual(note7.getNote(), "User: Yes, I can connect from other desktop machines at Amica.", 
						"Ticket 2's 2nd note is wrong: %s.");
			}
			
			/**
			 * This method tests that the ijmplementation meets the following requirements.
			 * <ul>
			 *   <li>We can find all tickets assigned to a given technician.</li>
			 *   <li>Tickets are always assigned to the technician with the fewest
			 *       active (i.e. unresolved) tickets.</li>
			 *   <li>Tickets are sorted from highest priority to lowest, 
			 *       in the master data set and for each technician.</li>
			 * </ul>
			 */
			@Test
			public void test3_Assignment() {
				assertEqual(helpDesk.getTicketsByTechnician("A05589").count(), 5L, 
						"Andree should have been assigned 5 tickets, but has %s.");
				assertEqual(helpDesk.getTicketsByTechnician("A12312").count(), 3L, 
						"Boris should have been assigned 3 tickets, but has %s.");
				assertEqual(helpDesk.getTicketsByTechnician("A17440").count(), 3L, 
						"Caelem should have been assigned 3 tickets, but has %s.");
				assertEqual(helpDesk.getTicketsByTechnician("A20265").count(), 3L, 
						"Dineh should have been assigned 3 tickets, but has %s.");
				
				Iterator<Ticket> tickets = ((HelpDesk) helpDesk).getTickets().iterator();
				assertEqual(tickets.next().getID(), 7, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 11, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 1, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 2, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 5, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 8, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 9, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 10, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 14, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 3, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 4, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 6, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 12, "Out of sequence in master set: %s");
				assertEqual(tickets.next().getID(), 13, "Out of sequence in master set: %s");
				
				Iterator<Technician> techs = ((HelpDesk) helpDesk).getTechnicians().iterator();
				
				Stream<Ticket> forTech = techs.next().getActiveTickets(); 
				tickets = forTech.iterator();
				assertEqual(tickets.next().getID(), 8, "Out of sequence for Andree: %s");
				assertEqual(tickets.next().getID(), 12, "Out of sequence for Andree: %s");
				assertEqual(tickets.next().getID(), 13, "Out of sequence for Andree: %s");
				assertThat(!tickets.hasNext(), "Andree should have 3 active tickets, was %s");
				
				forTech = techs.next().getActiveTickets(); 
				tickets = forTech.iterator();
				assertEqual(tickets.next().getID(), 7, "Out of sequence for Boris: %s");
				assertEqual(tickets.next().getID(), 2, "Out of sequence for Boris: %s");
				assertEqual(tickets.next().getID(), 14, "Out of sequence for Boris: %s");
				assertThat(!tickets.hasNext(), "Boris should have 3 active tickets, was %s");
				
				forTech = techs.next().getActiveTickets(); 
				tickets = forTech.iterator();
				assertEqual(tickets.next().getID(), 9, "Out of sequence for Caelem: %s");
				assertThat(!tickets.hasNext(), "Cealem should have 1 active ticket, was %s");
				
				forTech = techs.next().getActiveTickets(); 
				assertEqual(forTech.count(), 0L, "Dineh should have 0 active tickets, was %s");
		
			}
		
			/**
			 * This method tests that the ijmplementation meets the following requirements.
			 * <ul>
			 *   <li>We can tag tickets and find them by tags.</li>
			 *   <li>A ticket will only appear once in these results, even if 
			 *       it has multiple requested tags.</li>
			 *   <li>Tags are considered equal on a case-insensitive basis.</li>
			 * </ul>
			 */
			@Test
			public void test4_Tags() {
				Tag laptop = Tag.getTag("laptop");
				assertEqual(helpDesk.getTicketsWithAnyTag(laptop).count(), 3L, 
						"There should be 3 tickets with the 'laptop' tag, was %s.");
		
				Tag VM = Tag.getTag("VM");
				assertEqual(helpDesk.getTicketsWithAnyTag(VM).count(), 2L, 
						"There should be 2 tickets with the 'vm' tag, was %s.");
				
				Tag permissions = Tag.getTag("permissions");
				Tag CMA = Tag.getTag("CMA");
				assertEqual(helpDesk.getTicketsWithAnyTag(permissions, CMA).count(), 3L, 
						"There should be 2 tickets with the 'permissions' and/or 'CMA' tags, was %s.");
			}
			
			/**
			 * This method tests that the ijmplementation meets the following requirements.
			 * <ul>
			 *   <li>We can correctly calculate our average time to resolve a ticket.</li>
			 *   <li>We can also see the average time per technician.</li>
			 * </ul>
			 */
			@Test
			public void test5_TimeToResolve() {
				int minutes = helpDesk.getAverageMinutesToResolve();
				int hours = minutes / 60;
				minutes %= 60;
				assertEqual(hours, 24, "Average hours to resolve should be 24, was %s.");
				assertEqual(minutes, 29, "Average minutes to resolve should be 29, was %s.");
				
				Map<String,Double> byTech = helpDesk.getAverageMinutesToResolvePerTechnician();
				assertEqual(byTech.get("A05589").intValue(), 1396, 
						"Andree's average should be 1396, was %s.");
				assertThat(!byTech.containsKey("A12312"), 
						"Boris shouldn't have an average time to resolve.");
				assertEqual(byTech.get("A17440").intValue(), 1557, 
						"Caelem average should be 1557, was %s.");
				assertEqual(byTech.get("A20265").intValue(), 1458, 
						"Dineh's average should be 1458, was %s.");
			}
			
			/**
			 * This method tests that the ijmplementation meets the following requirements.
			 * <ul>
			 *   <li>We can find tickets whose descriptions or notes include a given substring.</li>
			 * </ul>
			 */
			@Test
			public void test6_TextSearch() {
				Stream<Ticket> tickets = helpDesk.getTicketsByText("corrupt");
				assertEqual(tickets.count(), 1L, 
						"There should be one ticket with the text 'corrupt', was %s.");
				
				tickets = helpDesk.getTicketsByText("browser");
				assertEqual(tickets.count(), 1L, 
						"There should be one ticket with the text 'browser', was %s.");
			}
			
			/**
			 * This method tests that the ijmplementation meets the following requirements.
			 * <ul>
			 *   <li>We can reopen a resolved ticket, with a new reason and priority.</li>
			 *   <li>We can't reopen an un-resolved ticket.</li>
			 *   <li>Reopened tickets take on the originator of the prior ticket,
			 *       and are assigned to the original technician.</li>
			 *   <li>Reopened tickets compile their own history and that of the prior ticket.</li>
			 *   <li>Reopened tickets compile their own tags and those of the prior ticket.</li>
			 *   <li>Reopened tags search their own and the prior ticket's text.</li>
			 */
			@Test
			public void test7_ReopenedTickets() {
				Clock.setTime("11/3/21 14:01");
				helpDesk.reopenTicket(6, "Still can't connect.", Priority.MEDIUM);
				Clock.setTime("11/3/21 14:12");
				helpDesk.reopenTicket(3, "Still can't log in.", Priority.HIGH);
				Clock.setTime("11/3/21 14:59");
				helpDesk.getTicketByID(3).addTags("VPN");
				
				Stream<Event> originalHistory = helpDesk.getTicketByID(6).getHistory();
				Stream<Event> reopenedHistory = helpDesk.getTicketByID(15).getHistory();
				long expectedSize = originalHistory.count() + 2;
				long actualSize = reopenedHistory.count(); 
				assertEqual(actualSize, expectedSize, "Reopened ticket should have " + 
						expectedSize + " events, was %s");
				
				assertThat(helpDesk.getTicketsWithAnyTag(Tag.getTag("GitHub"))
						.mapToInt(Ticket::getID).anyMatch(ID -> ID == 16),
							"Reopened ticket not found by prior ticket's tag;");
				assertThat(helpDesk.getTicketsWithAnyTag(Tag.getTag("VPN"))
						.mapToInt(Ticket::getID).anyMatch(ID -> ID == 16),
							"Reopened ticket not found by its own tag;");
					
				Stream<Ticket> tickets = helpDesk.getTicketsByText("access");
				assertThat(tickets.mapToInt(Ticket::getID).anyMatch(ID -> ID == 15),
						"Reopened ticked should be found by original description.");
			}
			
			/**
			 * This method tests that the ijmplementation meets the following requirements.
			 * <ul>
			 *   <li>We can pre-define synonyms for tags, and see them consolidated
			 *       to a chosen master tag.</li>
			 *   <li>We can pre-define preferred capitalization for tags,
			 *       instead of having everything pushed to lower case.</li>
			 * </ul>
			 */
			@Test
			public void test8_Synonyms() {
				Tag remoting = Tag.getTag("remoting");
				assertEqual(helpDesk.getTicketsWithAnyTag(remoting).count(), 6L, 
						"There should be 6 tickets with the 'remoting' tag, was %s.");
				
				assertEqual(Tag.getTag("github").getValue(), "GitHub",
						"The tag capitalization GitHub should be used, was %s");
				
				// These assertions would best be moved to a separate test class
				// dedicated to {@link Tag}, and they're hard to manage in
				// our test because they will reflect side effects of casual
				// tagusage in various test cases, and even static imports.
				// For the scope of this exercise, we just comment them out:
//				Iterator<Tag> tags = Tag.getTags().iterator();
//				assertEqual(tags.next().getValue(), "audio", "Unexpected tag: %s.");
//				assertEqual(tags.next().getValue(), "CMA", "Unexpected tag: %s.");
//				assertEqual(tags.next().getValue(), "GitHub", "Unexpected tag: %s.");
//				assertEqual(tags.next().getValue(), "laptop", "Unexpected tag: %s.");
//				assertEqual(tags.next().getValue(), "permissions", "Unexpected tag: %s.");
//				assertEqual(tags.next().getValue(), "remoting", "Unexpected tag: %s.");
//				assertEqual(tags.next().getValue(), "VM", "Unexpected tag: %s.");
//				assertEqual(tags.next().getValue(), "VPN", "Unexpected tag: %s.");
			}
			
			/**
			 * Tests the latest-activity query.
			 */
			@Test
			public void test9_LatestActivity() {
				assertEqual(helpDesk.getLatestActivity(10).filter(e -> e.getTicketID() == 5).count(),
						3L, "3 of the events in the latest 10 should relate to ticket 5; was %d.");
			}
		}
	}
}
