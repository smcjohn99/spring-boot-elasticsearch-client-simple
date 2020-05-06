package com.smc.elastic.search.search;

import org.json.JSONObject;

public class Range extends Condition {
	protected Range(String field) {
		super(field);
		value = new JSONObject();
	}
	public Range gte(Object value) {
		((JSONObject)this.value).put("gte", value);
		return this;
	}
	public Range lte(Object value) {
		((JSONObject)this.value).put("lte", value);
		return this;
	}
	public Range gt(Object value) {
		((JSONObject)this.value).put("gt", value);
		return this;
	}
	public Range lt(Object value) {
		((JSONObject)this.value).put("lt", value);
		return this;
	}
	public Range customer(String key, Object value) {
		((JSONObject)this.value).put(key, value);
		return this;
	}
	public Range timezone(String timezone) { // Asia/Hong_Kong
		((JSONObject)this.value).put("time_zone", timezone);
		return this;
	}
	@Override
	public String key() {
		return "range";
	}

}
