package com.smc.elastic.search.search;

import org.json.JSONObject;


public class ESSearch {
	public static String SORT_DESC = "desc", SORT_ASC = "asc";
	private JSONObject body;
	private ESBool query;
	private ESSort sort;
	public ESSearch() {
		body = new JSONObject();
	}
	public ESSearch from(int from) {
		body.put("from", from);
		return this;
	}
	public ESSearch size(int size) {
		if(size > 10000) size = 10000;
		body.put("size", size);
		return this;
	}
	public ESSearch query(ESBool query) {
		this.query = query;
		//body.put("query", query.toDoc());
		return this;
	}
	public ESSearch sort(ESSort sort) {
		this.sort = sort;
		//body.put("sort", sort.toDoc());
		return this;
	}
	public ESSearch aggregations(Aggregation... aggs) {
		JSONObject jsonObject = new JSONObject();
		for(Aggregation aggregation:aggs)
			jsonObject.put(aggregation.getName(), aggregation.toDoc() );
		body.put("aggs", jsonObject);
		return this;
	}
	public ESBool getQuery() {
		return query;
	}
	public ESSort getSort() {
		return sort;
	}
	public JSONObject getBody(){
		if(query!=null)
			this.body.put("query", query.toDoc());
		if(sort!=null)
			this.body.put("sort", sort.toDoc());
		return this.body;
	}
}

