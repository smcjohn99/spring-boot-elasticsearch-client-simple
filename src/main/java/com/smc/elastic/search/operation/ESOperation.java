package com.smc.elastic.search.operation;


public interface ESOperation {
	public String operatorName();
	public String toJSONString(String index);
}
