package com.smc.elastic.search.search;

import org.json.JSONArray;
import org.json.JSONObject;

public class ESSort {
	JSONArray jsonArray;
	public static ESSortOperator sort(String field) {
		return new ESSortOperator(field);
	}
	protected ESSort(String field, String direction) {
		this.jsonArray = new JSONArray().put(new JSONObject().put(field, direction));
	}
	protected ESSort(String field, String direction, JSONArray jsonArray) {
		this.jsonArray = jsonArray.put(new JSONObject().put(field, direction));
	}
	public ESSortOperator and(String field) {
		return new ESSortOperator(field, jsonArray);
	}
	public JSONArray toDoc() {
		return this.jsonArray;
	}
}