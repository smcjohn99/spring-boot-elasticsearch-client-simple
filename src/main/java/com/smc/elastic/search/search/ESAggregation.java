package com.smc.elastic.search.search;

public class ESAggregation {
	public static CommonAggregation max(String field) {
		return new CommonAggregation("max", field);
	}
	public static CommonAggregation min(String field) {
		return new CommonAggregation("min", field);
	}
	public static CommonAggregation avg(String field) {
		return new CommonAggregation("avg", field);
	}
	public static CommonAggregation sum(String field) {
		return new CommonAggregation("sum", field);
	}
	public static CommonAggregation stats(String field) {
		return new CommonAggregation("stats", field);
	}
	public static CommonAggregation terms(String field) {
		return new CommonAggregation("terms", field);
	}
	public static CommonAggregation count(String field) {
		return new CommonAggregation("value_count", field);
	}
	public static DateHistogram dateHistogram(String field) {
		return new DateHistogram(field);
	}
}
