package com.smc.elastic.search.search;
public class CommonAggregation extends Aggregation{
	String key;
	protected CommonAggregation(String key, String field) {
		this.key = key;
		this.fields.put("field", field);
	}
	public CommonAggregation size(Integer size) {
		this.fields.put("size", size);
		return this;
	}
	@Override
	public String key() {
		return this.key;
	}
	
}
