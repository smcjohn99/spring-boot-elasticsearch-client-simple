package com.smc.elastic.search.search;

import org.json.JSONArray;

public class ESSortOperator{
	String currentField;
	JSONArray jsonArray;
	protected ESSortOperator(String field) {
		currentField = field;
	}
	protected ESSortOperator(String field, JSONArray jsonArray) {
		currentField = field;
		this.jsonArray = jsonArray;
	}
	public ESSort desc() {
		return (jsonArray==null) ? new ESSort(currentField, "desc") :  new ESSort(currentField, "desc", jsonArray);
	}
	public ESSort asc() {
		return (jsonArray==null) ? new ESSort(currentField, "asc") :  new ESSort(currentField, "asc", jsonArray);
	}
}
