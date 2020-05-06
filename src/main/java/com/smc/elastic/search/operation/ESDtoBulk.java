package com.smc.elastic.search.operation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.smc.simple.http.request.SimpleHttpRequest;
import com.smc.simple.http.request.HttpResult;

public class ESDtoBulk {
	List<ESDtoOperation> operationList;
	protected ESDtoBulk(List<ESDtoOperation> operationList) {
		this.operationList = operationList;
	}
	protected ESDtoBulk(ESDtoOperation... esOperations){
		operationList = Arrays.asList(esOperations);
	}
	public String getBody() {
		StringBuilder stringBuilder = new StringBuilder();
		for(ESDtoOperation operation : operationList) 
			stringBuilder.append(operation.toString());
		return stringBuilder.toString();
	}
	
	public HttpResult request(String url) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		for(ESDtoOperation operation : operationList) 
			stringBuilder.append(operation.toString());
		return SimpleHttpRequest.post(url).asJson().body(stringBuilder.toString()).request();
	}
}
