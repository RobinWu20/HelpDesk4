package com.amica.help;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.amica.help.Ticket.Priority;
import com.amica.help.Ticket.Status;

/**
 * Unit test for the {@link Ticket} class.
 * 
 * @author Will Provost
 */
public class TicketTest {

  public static final String TECHNICIAN_ID = "TECHNICIAN_ID";
  public static final String TECHNICIAN_NAME = "TECHNICIAN_NAME";
  public static final int TECHNICIAN_EXT = 12345;

  public static final int ID = 1;
  public static final String ORIGINATOR = "ORIGINATOR";
  public static final String DESCRIPTION = "DESCRIPTION";
  public static final Priority PRIORITY = Priority.HIGH;
  public static final String RESOLVE_REASON = "RESOLVE_REASON";
  public static final String WAIT_REASON = "WAIT_REASON";
  public static final String RESUME_REASON = "RESUME_REASON";
  public static final String NOTE = "NOTE";
  public static final String TAG1_VALUE = "TAG1";
  public static final String TAG2_VALUE = "TAG2";
  public static final Tag TAG1 = Tag.getTag(TAG1_VALUE);
  public static final Tag TAG2 = Tag.getTag(TAG2_VALUE);
      
  public static final String START_TIME = "1/3/22 13:37";
  
  /**
   * Custom matcher that assures that an {@link Event} added to a ticket
   * has the expected ticket ID, timestamp, status, and note. 
   */
  private Matcher<Event> eventWith(Status status, String note) {
    return allOf(isA(Event.class),
        hasProperty("ticketID", equalTo(ID)),
        hasProperty("timestamp", equalTo(Clock.getTime())),
        hasProperty("newStatus", equalTo(status)),
        hasProperty("note", equalTo(note)));
  }
  
  /**
   * Helper method to assert that the Nth (0-based) event on the target ticket
   * has the expected ID, timestamp, status, and note.
   */
  private void assertHasEvent(int index, Status status, String note) {
    assertThat(ticket.getHistory().count(), equalTo(index + 1L));
    assertThat(ticket.getHistory().skip(index).findFirst().get(),
        eventWith(status, note));
  }
  
  private Ticket ticket;
  private Technician technician;
  
  /**
   * Helper method to advance the simulated clock by one minute,
   * so as to avoid false positives when checking timestamps.
   */
  private void passOneMinute() {
    Clock.setTime(Clock.getTime() + 60000);
  }
  
  /**
   * Helper method to {@link #passOneMinute pass one minut} and then
   *  assign the ticket to a technician with pre-defined
   * properties.We start out with an actual {@link Technician} insteance,
   * but eventually we want to verify calls to this object so we switch
   * to a mock object.
   */
  private void assign() {
    passOneMinute();
    technician = mock(Technician.class);
    when(technician.getID()).thenReturn(TECHNICIAN_ID);
    when(technician.getName()).thenReturn(TECHNICIAN_NAME);
    when(technician.toString()).thenReturn("Technician " +
        TECHNICIAN_ID + ", " + TECHNICIAN_NAME);
    ticket.assign(technician);
  }
  
  /**
   * Helper method to {@link #passOneMinute pass one minut} and then
   *  put the ticket in a waiting state, with a standard reason.
   */
  private void wait_() {
    passOneMinute();
    ticket.wait(WAIT_REASON);
  }
  
  /**
   * Helper method to {@link #passOneMinute pass one minut} and then
   *  resume work on the ticket, with a standard reason.
   */
  private void resume() {
    passOneMinute();
    ticket.resume(RESUME_REASON);
  }
  
  /**
   * Helper method to {@link #passOneMinute pass one minut} and then
   *  resolve, with a standard reason.
   */
  private void resolve() {
    passOneMinute();
    ticket.resolve(RESOLVE_REASON);
  }
  
  /**
   * Helper method to {@link #passOneMinute pass one minut} and then
   * add a note to the ticket, with a standard note.
   */
  private void addNote() {
    passOneMinute();
    ticket.addNote(NOTE);
  }
      
  @BeforeEach
  public void setUp() {
    Clock.setTime(START_TIME);
    ticket = new Ticket(ID, ORIGINATOR, DESCRIPTION, PRIORITY);
  }
  
  ///////////////////////////////////////////////////////////////////
  // Normal state transitions
  //
  
  @Test
  public void testInitialization() {
    assertThat(ticket.getID(), equalTo(ID));
    assertThat(ticket.getOriginator(), equalTo(ORIGINATOR));
    assertThat(ticket.getDescription(), equalTo(DESCRIPTION));
    assertThat(ticket.getPriority(), equalTo(PRIORITY));
    
    assertThat(ticket.getStatus(), equalTo(Status.CREATED));
    assertThat(ticket.getTechnician(), nullValue());
    assertThat(ticket.getTags().count(), equalTo(0L));
    
    assertHasEvent(0, Status.CREATED, "Created ticket.");
  }
  
  @Test
  public void testComparison() {
    assertThat(ticket, lessThan(new Ticket(2, "", "", Priority.LOW)));
    assertThat(ticket, greaterThan(new Ticket(2, "", "", Priority.URGENT)));
    assertThat(ticket, lessThan(new Ticket(2, "", "", PRIORITY)));
 }
  
  @Test
  public void testAssign() {
    assign();
    
    assertThat(ticket.getStatus(), equalTo(Status.ASSIGNED));
    assertThat(ticket.getTechnician(), equalTo(technician));
    
    assertHasEvent(1, Status.ASSIGNED, String.format 
        ("Assigned to Technician %s, %s.", TECHNICIAN_ID, TECHNICIAN_NAME));
    verify(technician).addActiveTicket(ticket);
  }
  
  @Test
  public void testWait() {
    assign();
    wait_();
    
    assertThat(ticket.getStatus(), equalTo(Status.WAITING));
    
    assertHasEvent(2, Status.WAITING, WAIT_REASON);
  }
  
  @Test
  public void testResume() {
    assign();
    wait_();
    resume();
    
    assertThat(ticket.getStatus(), equalTo(Status.ASSIGNED));
    
    assertHasEvent(3, Status.ASSIGNED, RESUME_REASON);
  }
  
  @Test
  public void testResolve() {
    assign();
    resolve();
    
    assertThat(ticket.getStatus(), equalTo(Status.RESOLVED));
    
    assertHasEvent(2, Status.RESOLVED, RESOLVE_REASON);
    verify(technician).removeActiveTicket(ticket);
  }
  
  @Test
  public void testAddNote() {
    assign();
    addNote();
    
    assertThat(ticket.getStatus(), equalTo(Status.ASSIGNED));
    
    assertHasEvent(2, null, NOTE);
  }

  ///////////////////////////////////////////////////////////////////
  // Illegal arguments
  //
  
  @Test
  public void testInitialization_NullOriginator() {
    assertThrows(IllegalArgumentException.class, () ->
        new Ticket(ID, null, DESCRIPTION, PRIORITY));
  }

  @Test
  public void testInitialization_NullDescription() {
    assertThrows(IllegalArgumentException.class, () ->
        new Ticket(ID, ORIGINATOR, null, PRIORITY));
  }

  @Test
  public void testInitialization_NullPriority() {
    assertThrows(IllegalArgumentException.class, () ->
        new Ticket(ID, ORIGINATOR, DESCRIPTION, null));
  }

  @Test
  public void testAssign_NullTechnician() {
    assertThrows(IllegalArgumentException.class, 
        () -> ticket.assign(null));
  }

  @Test
  public void testWait_NullReason() {
    assign();
    assertThrows(IllegalArgumentException.class, 
        () -> ticket.wait(null));
  }

  @Test
  public void testResume_NullReason() {
    assign();
    assertThrows(IllegalArgumentException.class, 
        () -> ticket.resume(null));
  }

  @Test
  public void testResolve_NullReason() {
    assign();
    assertThrows(IllegalArgumentException.class, 
        () -> ticket.resolve(null));
  }

  @Test
  public void testAddNote_NullReason() {
    assign();
    assertThrows(IllegalArgumentException.class, 
        () -> ticket.addNote(null));
  }

  @Test
  public void testAddTags_NullArray() {
    assign();
    assertThrows(IllegalArgumentException.class, 
        () -> ticket.addTags((String[]) null));
  }

  @Test
  public void testAddTags_NullValue() {
    assign();
    assertThrows(IllegalArgumentException.class, 
        () -> ticket.addTags(TAG1_VALUE, null));
  }

  
  ///////////////////////////////////////////////////////////////////
  // Illegal state transitions
  //
  
  @Test
  private void assertThrowsOnStateChange(Executable call) {
    assertThrows(IllegalStateException.class, call);
  }
  
  @Test
  public void testAssign_Resolved() {
    assign();
    resolve();
    assertThrowsOnStateChange(this::assign);
  }
  
  @Test
  public void testWait_Created() {
    assertThrowsOnStateChange(this::wait_);
  }
  
  @Test
  public void testWait_Resolved() {
    assign();
    resolve();
    assertThrowsOnStateChange(this::wait_);
  }
  
  @Test
  public void testResume_Created() {
    assertThrowsOnStateChange(this::resume);
  }
  
  @Test
  public void testResume_Assigned() {
    assign();
    assertThrowsOnStateChange(this::resume);
  }
  
  @Test
  public void testResume_Resolved() {
    assign();
    resolve();
    assertThrowsOnStateChange(this::resume);
  }
  
  @Test
  public void testResolve_Created() {
    assertThrowsOnStateChange(this::resolve);
  }
  
  @Test
  public void testResolve_Waiting() {
    assign();
    wait_();
    assertThrowsOnStateChange(this::resolve);         
  }
  
  @Test
  public void testResolve_Resolved() {
    assign();
    resolve();
    assertThrowsOnStateChange(this::resolve);         
  }
  
  ///////////////////////////////////////////////////////////////////
  // Tags
  //
  
  @Test
  public void testGetTags0() {
    assertThat(ticket.getTags().count(), equalTo(0L));
  }
  
  @Test
  public void testGetTags1() {
    ticket.addTags(TAG1_VALUE);
    assertThat(ticket.getTags().count(), equalTo(1L));
    assertThat(ticket.getTags().toList().contains(TAG1), equalTo(true));
  }
  
  @Test
  public void testGetTags2() {
    ticket.addTags(TAG1_VALUE, TAG2_VALUE);
    assertThat(ticket.getTags().count(), equalTo(2L));
    assertThat(ticket.getTags().toList().contains(TAG2), equalTo(true));
  }
  
  @Test
  public void testGetTags_Duplicates() {
    ticket.addTags(TAG1_VALUE, TAG1_VALUE);
    assertThat(ticket.getTags().count(), equalTo(1L));
  }

  ///////////////////////////////////////////////////////////////////
  // Time to resolve
  //
  
  @Test
  public void testGetMinutesToResolve() {
    assign();
    resolve();
    assertThat(ticket.getMinutesToResolve(), equalTo(2));
  }
  
  @Test
  public void testGetMinutesToResolve_Unresolved() {
    assertThrows(IllegalStateException.class, ticket::getMinutesToResolve);
  }
  
  
  ///////////////////////////////////////////////////////////////////
  // Text search
  //
  
  @Test
  public void testIncluesText_Description() {
    assertThat(ticket.includesText(DESCRIPTION), equalTo(true));
  }

  @Test
  public void testIncluesText_DescriptionSubstring() {
    assertThat(ticket.includesText(DESCRIPTION.substring(0, 3)), equalTo(true));
  }

  @Test
  public void testIncluesText_Note() {
    addNote();
    assertThat(ticket.includesText(NOTE), equalTo(true));
  }

  @Test
  public void testIncluesText_NoteSubstring() {
    addNote();
    assertThat(ticket.includesText(NOTE.substring(1, 3)), equalTo(true));
  }

  @Test
  public void testIncluesText_NotIncluded() {
    assertThat(ticket.includesText("Not in the ticket"), equalTo(false));
  }

  @Test
  public void testIncluesText_DescriptionPlusNote() {
    assertThat(ticket.includesText(DESCRIPTION + NOTE), equalTo(false));
  }
}
