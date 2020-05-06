package com.smc.elastic.search.search;


public class Term extends Condition {
	protected Term(String field) {
		super(field);
	}
	public Term is(Object value) {
		this.value = value;
		return this;
	}
	@Override
	public String key() {
		return "term";
	}

}
