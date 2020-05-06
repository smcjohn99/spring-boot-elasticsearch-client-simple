package com.smc.elastic.search.search;

public class ESCondition {
	public static Match match(String key) {
		return new Match(key);
	}
	public static WildCard wildcard(String key) {
		return new WildCard(key);
	}
	public static Range range(String key) {
		return new Range(key);
	}
	public static Terms terms(String key) {
		return new Terms(key);
	}
	public static Term term(String key) {
		return new Term(key); 
	}
}