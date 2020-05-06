package com.smc.elastic.search.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

public abstract class Aggregation{
	//String key;
	protected JSONObject fields;
	private List<Aggregation> aggregations;
	private String name;
	protected Aggregation() {
		//this.key = key;
		fields = new JSONObject();
	}
	public abstract String key();
	public Aggregation as(String name) {
		this.name = name;
		return this;
	}
	public Aggregation withAggregation(Aggregation... aggs) {
		aggregations = new ArrayList<Aggregation>(Arrays.asList(aggs));
		return this;
	}
	public String getName() {
		return name;
	}
	public JSONObject toDoc() {
		JSONObject jsonObject = new JSONObject().put(this.key(), fields) ;
		if(this.aggregations!=null) {
			JSONObject temp = new JSONObject();
			for(Aggregation aggregation:this.aggregations)
				temp.put(aggregation.getName(), aggregation.toDoc());
			jsonObject.put("aggs", temp);
		}
		return jsonObject;
	};
}
