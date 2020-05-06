package com.smc.elastic.search.search;

import org.json.JSONObject;

public abstract class Condition implements ESDocument{
	String field;
	Object value;
	protected Condition(String field) {
		this.field = field;
	}
	protected Condition() {
	}
	public abstract String key();
	public JSONObject toDoc() {
		return new JSONObject().put( key(), new JSONObject().put(field, value));
	};
}
