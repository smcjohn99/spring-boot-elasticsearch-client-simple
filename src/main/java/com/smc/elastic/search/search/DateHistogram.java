package com.smc.elastic.search.search;

public class DateHistogram extends Aggregation{
	String interval;
	protected DateHistogram(String field) {
		this.fields.put("field", field);
	}
	public DateHistogram interval(String interval) { // 1y, 2M, 3w, 4d, 5h, 6m ...
		this.fields.put("interval", interval);
		return this;
	}
	public DateHistogram format(String format) {  // yyyy-MM-dd
		this.fields.put("format", format);
		return this;
	}
	public DateHistogram offset(String offset) { // +8h, -1h
		this.fields.put("offset", offset);
		return this;
	}
	public DateHistogram timezone(String timezone) { // Asia/Hong_Kong
		this.fields.put("time_zone", timezone);
		return this;
	}
	@Override
	public String key() {
		return "date_histogram";
	}
}
