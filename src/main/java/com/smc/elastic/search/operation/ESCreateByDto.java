package com.smc.elastic.search.operation;

import org.json.JSONObject;

import com.smc.elastic.search.dto.ElasticsearchBaseDto;
import com.smc.util.SerializeUtil;


public class ESCreateByDto implements ESDtoOperation{
	public ElasticsearchBaseDto<?> dto;
	public ESCreateByDto(ElasticsearchBaseDto<?> dto) {
		this.dto=dto;
	}
	@Override
	public String operatorName() {
		return "create";
	}
	@Override
	public String toString() {
		try {
			String index = SerializeUtil.getElasticsearchEntityIndex(dto.getClass()) != null ? SerializeUtil.getElasticsearchEntityIndex(dto.getClass()).value() : dto.getIndex();
			return new JSONObject().put(this.operatorName(), new JSONObject().put("_index", index).put("_id", dto.getId())).toString()+"\r\n"
					+ SerializeUtil.serializeDto(dto).toString() + "\r\n";
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
