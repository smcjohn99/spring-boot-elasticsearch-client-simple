package com.smc;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResponseMsg {
	private String errorCode;
	private String errorMessage;
	private Object data;
	@JsonIgnore
	private HashMap<String,Object> extraProperties;
	private ResponseMsg(String msg) {
		this.errorMessage = "No Error";
		this.errorCode = "0";
		this.extraProperties = new HashMap<>();
	}
	public static ResponseMsgBuilder builder(String msg) {
		return new ResponseMsgBuilder(msg);
	}
	
	public String getErrorCode() {
		return errorCode;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public Object getData() {
		return data;
	}
	@JsonAnyGetter
	public Map<String, Object> getExtraProperties() {
	    return extraProperties;
	}
	public static class ResponseMsgBuilder{
		public ResponseMsg obj;
		public ResponseMsgBuilder(String msg){
			obj = new ResponseMsg(msg);
		}
		public ResponseMsgBuilder setErrorMsg(String msg) {
			obj.errorMessage = msg;
			return this;
		}
		public ResponseMsgBuilder setErrorCode(String errorCode) {
			obj.errorCode = errorCode;
			return this;
		}
		public ResponseMsgBuilder setData(Object data) {
			obj.data = data;
			return this;
		}
		public ResponseMsgBuilder addProperty(String key, Object value) {
			obj.extraProperties.put(key, value);
			return this;
		}
		public ResponseMsgBuilder addProperties(HashMap<String, Object> properties) {
			obj.extraProperties.putAll(properties);
			return this;
		}
		public ResponseMsg build() {
			return this.obj;
		}
	}
}
