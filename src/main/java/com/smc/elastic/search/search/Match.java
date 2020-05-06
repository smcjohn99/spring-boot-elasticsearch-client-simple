package com.smc.elastic.search.search;


public class Match extends Condition {
	protected Match(String field) {
		super(field);
	}
	public Match is(Object value) {
		this.value = value;
		return this;
	}
	@Override
	public String key() {
		return "match";
	}

}
