package com.amica.help;

import java.util.stream.Stream;

import lombok.Getter;

/**
 * A specialized ticket that represents a reopened ticket by holding a
 * reference to the prior ticket (which could itself be a reopened ticket)
 * and takes that ticket into account in some of its behaviors.
 */
public class ReopenedTicket extends Ticket {

	@Getter
	private Ticket priorTicket;
	
	public ReopenedTicket(int ID, Ticket priorTicket, String reason, Priority priority) {
		super(ID, priorTicket.getOriginator(), reason, priority);
		this.priorTicket = priorTicket;
		assign(priorTicket.getTechnician());
	}
	
	@Override
	public Stream<Event> getHistory() {
		return Stream.concat(priorTicket.getHistory(), super.getHistory());
	}
	
	@Override
	public Stream<Tag> getTags() {
		return Stream.concat(priorTicket.getTags(), super.getTags());
	}
	
	@Override
	public boolean includesText(String text) {
		return super.includesText(text) || priorTicket.includesText(text);
	}
}
