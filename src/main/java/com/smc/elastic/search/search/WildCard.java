package com.smc.elastic.search.search;


public class WildCard extends Condition {
	protected WildCard(String field) {
		super(field);
	}
	public WildCard like(Object value) {
		this.value = value;
		return this;
	}
	@Override
	public String key() {
		return "wildcard";
	}

}
