package com.amica.help;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Class representing a keyword tag that can be applied to a {@link Ticket}.
 *
 * @author Will Provost
 */
@EqualsAndHashCode(of="value")
public class Tag implements Comparable<Tag> {

	private static SortedSet<Tag> tags = new TreeSet<>();
	private static Map<String,String> synonyms = new HashMap<>();
	private static Set<String> capitalizations = new HashSet<>();
	
	@Getter
	private String value;
	
	private Tag(String value) {
		this.value = value;
	}
	
	public static SortedSet<Tag> getTags() {
		return Collections.unmodifiableSortedSet(tags);
	}
	
	public static Map<String,String> getSynonyms() {
		return Collections.unmodifiableMap(synonyms);
	}
	
	public static void addSynonym(String term, String isSynonymFor) {
		synonyms.put(term.toLowerCase(), isSynonymFor);
		capitalizations.add(isSynonymFor);
	}
	
	public static String translate(String term) {
		if (synonyms.containsKey(term.toLowerCase())) {
			return synonyms.get(term.toLowerCase());
		}
		return term;
	}
	
	public static Set<String> getCapitalizations() {
		return Collections.unmodifiableSet(capitalizations);
	}
	
	private static Optional<String> getExistingCapitalization(String value) {
		return capitalizations.stream()
				.filter(c -> c.equalsIgnoreCase(value))
				.findFirst();
	}
	
	public static String capitalize(String value) {
		return getExistingCapitalization(value).orElse(value.toLowerCase());
	}
	
	public static void addCapitalization(String value) {
		Optional<String> capitalization = getExistingCapitalization(value);
		if (capitalization.isPresent()) {
			throw new IllegalArgumentException
				(String.format("'%s' is already set to '%s'.",
						value, capitalization.get()));
		}

		capitalizations.add(value);
	}
	
	public static Tag getTag(String value) {
		value = translate(value);
		for (Tag tag : tags) {
			if (tag.getValue().equalsIgnoreCase(value)) {
				return tag;
			}
		}
		Tag newTag = new Tag(capitalize(value));
		tags.add(newTag);
		return newTag;
	}
	
	public int compareTo(Tag other) {
		return value.compareToIgnoreCase(other.getValue());
	}
}
