package com.smc.elastic.search.operation;

public class ESDelete extends ESOperationWithoutBody{
	public ESDelete(String id) {
		super(id);
	}
	@Override
	public String operatorName() {
		return "delete";
	}
}
