package com.smc.model;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.Min;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class QueryModel {
	private String from, to;
	@Min(value=0L)
	private Integer start;
	@Min(value=1L)
	private Integer pageLength;
	private Map<String, Object> queryProperties;
	
	public QueryModel() {
		queryProperties = new HashMap<String, Object>();
		start = 0;
		pageLength = 10;
	}
	@JsonAnyGetter
	public Map<String, Object> getQueryProperties() {
		return queryProperties;
	}
	@JsonAnySetter
	public void addQuery(String key, Object value) {
		this.queryProperties.put(key, value);
	}
	public String getFrom() {
		return from;
	}
	public String getTo() {
		return to;
	}
	public Integer getStart() {
		return start;
	}
	public Integer getPageLength() {
		return pageLength;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public void setStart(Integer start) {
		this.start = start;
	}
	public void setPageLength(Integer pageLength) {
		this.pageLength = pageLength;
	}
	
	
}
