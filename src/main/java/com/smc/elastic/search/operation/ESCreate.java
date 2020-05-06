package com.smc.elastic.search.operation;

import java.util.Date;

public class ESCreate extends ESOperationWithBody{
	public ESCreate(String body, String id) {
		super(body, id);
	}
	@Override
	public ESCreate setTimeStamp(Date date) {
		this.date = date;
		return this;
	}
	@Override
	public String operatorName() {
		return "create";
	}

}
