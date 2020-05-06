package com.smc.elastic.search.operation;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

public abstract class ESOperationWithBody implements ESOperation {
	String body, id;
	Date date;
	public ESOperationWithBody(String body, String id) {
		this.body = body;
		this.id = id;
	}
	public ESOperationWithBody setTimeStamp(Date date) {
		this.date = date;
		return this;
	}
	@Override
	public String toJSONString(String index) {
		
		return new JSONObject().put(this.operatorName(), new JSONObject().put("_index", index).put("_id", id)).toString()+"\r\n"
				+ new JSONObject(body).put("timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+08:00'").format(date)).toString() + "\r\n";
	}
	
}
