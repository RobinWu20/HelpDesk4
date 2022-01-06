package com.amica.help;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Class representing a problem ticket for a help desk.
 *
 * @author Will Provost
 */
@Getter
@EqualsAndHashCode(of="ID")
public class Ticket implements Comparable<Ticket> {

    public enum Status { CREATED, ASSIGNED, WAITING, RESOLVED }
    public enum Priority { LOW, MEDIUM, HIGH, URGENT }

    private int ID;
    private Status status;
    private Priority priority;
    private String originator;
    private String description;
    private Technician technician;
    private List<Event> history = new ArrayList<>();
    private SortedSet<Tag> tags = new TreeSet<>();

	public Ticket(int ID, String originator, String description, Priority priority) {
		if (originator != null && description != null && priority != null) {
			this.ID = ID;
			this.status= Status.CREATED;
			this.priority = priority;
			this.originator = originator;
			this.description = description;
			this.history.add(new Event(ID, status, "Created ticket."));
		} else {
			throw new IllegalArgumentException(String.format
			("All arguments must be non-null: originator=%s, description=%s, priority=%s.",
				originator, description, 
				Optional.ofNullable(priority).map(Object::toString).orElse("null")));
		}
	}

    public Stream<Event> getHistory() {
    	return history.stream();
    }

    public Stream<Tag> getTags() {
    	return tags.stream();
    }
    
    public void assign(Technician technician) {
    	if (technician != null) {
	        if (status != Status.RESOLVED) {
	            this.technician = technician;
	            status = Status.ASSIGNED;
	            history.add(new Event(ID, status, "Assigned to " + technician + "."));
	            technician.addActiveTicket(this);
	        } else {
	            throw new IllegalStateException("Can't re-assign a resolved new ticket.");
	        }
    	} else {
    		throw new IllegalArgumentException("Technician must be non-null.");
    	}
    }
    
    public void wait(String reason) {
    	if (reason != null) {
	    	if (status == Status.ASSIGNED) {
	    		status = Status.WAITING;
	    		history.add(new Event(ID, status, reason));
	    	} else {
	    		throw new IllegalStateException("Can't wait until the ticket is assigned.");
	    	}
    	} else {
    		throw new IllegalArgumentException("Reason must be non-null.");
    	}
    }
    
    public void resume(String reason) {
    	if (reason != null) {
	    	if (status == Status.WAITING) { 
	    		status = Status.ASSIGNED;
	    		history.add(new Event(ID, status, reason));
	    	} else {
	    		throw new IllegalStateException("Can't seume a ticket that isn't in the WAITING state.");
	    	}
    	} else {
    		throw new IllegalArgumentException("Reason must be non-null.");
    	}
    }
    
    public void addNote(String note) {
    	if (note != null) {
    		history.add(new Event(ID, note));
    	} else {
    		throw new IllegalArgumentException("Note must be non-null.");
    	}
    }

    public void resolve(String reason) {
    	if (reason != null) {
	        if (status == Status.ASSIGNED) {
	            status = Status.RESOLVED;
	            history.add(new Event(ID, status, reason));
	            technician.removeActiveTicket(this);
	        } else {
	        	throw new IllegalStateException("Can't resolve an unassigned ticket.");
	        }
		} else {
			throw new IllegalArgumentException("Reason must be non-null.");
		}
    }

    public void addTags(String... tagValues) {
    	if (tagValues != null && 
    			!Arrays.stream(tagValues).anyMatch(t -> t == null)) {
			for (String tagValue : tagValues) {
				tags.add(Tag.getTag(tagValue));
			}
    	} else {
    		throw new IllegalArgumentException("Values must be non-null.");
    	}
    }

    public int getMinutesToResolve() {
    	final int MILLISECONDS_PER_MINUTE = 60000;
        if (status == Status.RESOLVED) {
        	long time = history.get(history.size() - 1).getTimestamp() -
        			history.get(0).getTimestamp();
        	return (int) time / MILLISECONDS_PER_MINUTE;
        } else {
        	throw new IllegalStateException("The ticket is not yet resolved.");
        }
    }
    
    public boolean includesText(String text) {
		return description.contains(text) ||
				getHistory().anyMatch(e -> e.getNote().contains(text));
    }
    
    @Override
    public String toString() {
    	return String.format("Ticket %d: %s priority, %s", 
    			ID, priority.toString(), status.toString());
    }
    
    public int compareTo(Ticket other) {
    	if (this.equals(other)) {
    		return 0;
    	}
    	
    	int result = -priority.compareTo(other.getPriority());
    	if (result == 0) {
    		result = Integer.compare(ID, other.getID());
    	}
    	return result;
    }
}
