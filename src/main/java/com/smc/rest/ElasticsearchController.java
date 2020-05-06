package com.smc.rest;

import java.util.ArrayList;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.smc.ResponseMsg;
import com.smc.elastic.search.Elasticsearch;
import com.smc.elastic.search.dto.TestIndex;
import com.smc.elastic.search.operation.ESCreateByDto;
import com.smc.elastic.search.operation.ESRequest;
import com.smc.simple.http.request.HttpResult;
import com.smc.util.SerializeUtil;

@RestController
public class ElasticsearchController {
	@Autowired Elasticsearch elasticsearch;
	@RequestMapping("query")
	public ResponseMsg test() throws Exception{
		TestIndex index = new TestIndex();
		index.setField1("FIELD 1587721457608");
		
		JSONArray array = elasticsearch.findByDto(index).getHits();
		ArrayList<TestIndex> ls = new ArrayList<>();
		for(int i=0;i<array.length();i++) {
			ls.add(SerializeUtil.deserializeDto(array.getJSONObject(i), TestIndex.class));
		}
		return ResponseMsg.builder("OK").setData(ls).build();
	}
	@RequestMapping("insert")
	public ResponseMsg insert() throws Exception{
		TestIndex index = new TestIndex();
		index.setId(System.currentTimeMillis()+"").setField1("FIELD "+System.currentTimeMillis());
		HttpResult ro = elasticsearch.bulkRequest(ESRequest.dtoBulk(new ESCreateByDto(index)));
		System.out.println(ro.getBody());
		return ResponseMsg.builder("OK").build();
	}
}
