package com.smc.elastic.search.operation;

import java.util.Date;

import org.json.JSONObject;

public class ESUpdate extends ESOperationWithBody{
	public ESUpdate(String body, String id) {
		super(body, id);
	}
	@Override
	public ESUpdate setTimeStamp(Date date) {
		this.date = date;
		return this;
	}
	@Override
	public String operatorName() {
		return "update";
	}
	@Override
	public String toJSONString(String index) {
		return new JSONObject().put(this.operatorName(), new JSONObject().put("_index", index).put("_id", id)).toString()+"\r\n"
				+ new JSONObject().put("doc", new JSONObject(body)) + "\r\n";
	}
}
