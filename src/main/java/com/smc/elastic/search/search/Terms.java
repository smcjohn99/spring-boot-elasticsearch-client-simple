package com.smc.elastic.search.search;

import java.util.Collection;

import org.json.JSONArray;

public class Terms extends Condition {
	protected Terms(String field) {
		super(field);
		value = new JSONArray();
	}
	public Terms in(Object... value) {
		for(Object v:value)
			((JSONArray)this.value).put(v);
		return this;
	}
	public Terms in(Collection<?> value) {
		for(Object v:value)
			((JSONArray)this.value).put(v);
		return this;
	}
	@Override
	public String key() {
		return "terms";
	}

}
