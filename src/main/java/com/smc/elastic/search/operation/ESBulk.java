package com.smc.elastic.search.operation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.smc.simple.http.request.SimpleHttpRequest;
import com.smc.simple.http.request.HttpResult;


public class ESBulk {
	List<ESOperation> operationList;
	String index;
	protected ESBulk(String index, List<ESOperation> operationList) {
		this.index = index;
		this.operationList = operationList;
	}
	protected ESBulk(String index, ESOperation... esOperations){
		this.index = index;
		operationList = Arrays.asList(esOperations);
	}
	public String getBody() {
		StringBuilder stringBuilder = new StringBuilder();
		for(ESOperation operation : operationList) 
			stringBuilder.append(operation.toJSONString(this.index));
		return stringBuilder.toString();
	}

	public HttpResult request(String url) throws IOException{
		StringBuilder stringBuilder = new StringBuilder();
		for(ESOperation operation : operationList) 
			stringBuilder.append(operation.toJSONString(this.index));
		return SimpleHttpRequest.post(url).asJson().body(stringBuilder.toString()).request();
	}
}
