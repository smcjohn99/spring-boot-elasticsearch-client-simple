package com.smc.elastic.search.dto;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smc.annotation.FieldMap;

public class ElasticSearchResultDto {
	@FieldMap(value="took")
	public Integer took;
	@FieldMap(value="timed_out")
	public Boolean timedOut;
	@FieldMap(value="hits.hits")
	public JSONArray hits;
	@FieldMap(value="hits.total.value")
	public Integer total;
	@FieldMap(value="aggregations")
    public JSONObject aggregations;
    public Integer getTook() {
		return took;
	}
	public void setTook(Integer took) {
		this.took = took;
	}
    public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
    }
    public Boolean isTimedOut() {
		return timedOut;
	}
	public void setTimedOut(Boolean timedOut) {
		this.timedOut = timedOut;
    }
    public JSONArray getHits() {
		return hits;
	}
	public void setHits(JSONArray hits) {
		this.hits = hits;
    }
    public JSONObject getAggregations() {
		return aggregations;
	}
	public void setAggregations(JSONObject aggregations) {
		this.aggregations = aggregations;
	}
}