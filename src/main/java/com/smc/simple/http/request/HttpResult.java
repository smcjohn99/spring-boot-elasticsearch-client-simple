package com.smc.simple.http.request;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONObject;

public class HttpResult{
	int statusCode;
	String body, url;
	InputStream inputStream;
	public boolean isSuccess() {
		return statusCode == 200;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public String getBody() {
		return getContent();
	}
	public boolean isJsonArray() {
		try {
			new JSONArray(getContent());
			return true;
		}
		catch(Exception e) {}
		return false;
	}
	public boolean isJsonObject() {
		try {
			new JSONObject(getContent());
			return true;
		}
		catch(Exception e) {}
		return false;
	}
	public JSONObject getAsJsonObject() {
		try {
			return new JSONObject(getContent());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONObject();
	}
	public JSONArray getAsJsonArray() {
		try {
			return new JSONArray(getContent());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new JSONArray();
	}
	public String getUrl() {
		return url;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	protected String getContent() {
		if(body==null){
			StringBuffer sb = new StringBuffer();
			new BufferedReader(new InputStreamReader(this.inputStream)).lines().forEach(str->{
				sb.append(str);
			});
			body = sb.toString();
			return body;
		}
		else
			return body;
	}
	
}