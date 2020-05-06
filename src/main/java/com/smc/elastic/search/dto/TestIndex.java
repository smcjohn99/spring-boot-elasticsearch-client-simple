package com.smc.elastic.search.dto;

import com.smc.annotation.FieldMap;
import com.smc.annotation.Index;

@Index(value="test_index", createIndex=true)
public class TestIndex extends ElasticsearchBaseDto<TestIndex>{
	@FieldMap("field_one")
	public String field1;

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}
	
}