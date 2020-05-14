package com.smc.elastic.search.operation;

import org.json.JSONObject;

import com.smc.elastic.search.dto.ElasticsearchBaseDto;
import com.smc.util.SerializeUtil;



public class ESUpdateByDto implements ESDtoOperation{
	ElasticsearchBaseDto<?> dto;
	public ESUpdateByDto(ElasticsearchBaseDto<?> dto) {
		this.dto = dto;
	}
	@Override
	public String operatorName() {
		return "update";
	}
	@Override
	public String toString() {
		try {
			String index = SerializeUtil.getElasticsearchEntityIndex(dto.getClass()) != null ? SerializeUtil.getElasticsearchEntityIndex(dto.getClass()).value() : dto.getIndex();
			return new JSONObject().put(this.operatorName(), new JSONObject().put("_index", index).put("_id", dto.getId())).toString()+"\r\n"
					+ new JSONObject().put("doc", SerializeUtil.serializeDto(dto)) + "\r\n";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
