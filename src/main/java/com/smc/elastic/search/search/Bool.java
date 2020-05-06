package com.smc.elastic.search.search;

import org.json.JSONObject;


public class Bool extends Condition{
	JSONObject jsonObject;
	
	public Bool(String field) {
		super(field);
		jsonObject = new JSONObject();
	}
	public Bool() {
		jsonObject = new JSONObject();
	}
	public static Must must(Condition... condition) {
		return new Must(condition);
	}
	public static Should should(Condition... condition) {
		return new Should(condition);
	}
	public static Filter filter(Condition... condition) {
		return new Filter(condition);
	}
	@Override
	public JSONObject toDoc() {
		return new JSONObject().put(this.key(), this.jsonObject);
	}
	@Override
	public String key() {
		return "bool";
	}
	
}
