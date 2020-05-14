package com.smc.elastic.search;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.smc.annotation.Index;
import com.smc.elastic.search.dto.ElasticSearchResultDto;
import com.smc.elastic.search.dto.ElasticsearchBaseDto;
import com.smc.elastic.search.operation.ESDeleteByQuery;
import com.smc.elastic.search.operation.ESDtoBulk;
import com.smc.elastic.search.search.Condition;
import com.smc.elastic.search.search.ESBool;
import com.smc.elastic.search.search.ESCondition;
import com.smc.elastic.search.search.ESSearch;
import com.smc.model.QueryModel;
import com.smc.simple.http.request.HttpResult;
import com.smc.simple.http.request.SimpleHttpRequest;
import com.smc.util.SerializeUtil;
import com.smc.util.SerializeUtil.DtoProperty;


@SuppressWarnings("serial")
public class Elasticsearch {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	public String url;
	public volatile Boolean isConnect = new Boolean(false); // Check Reachable
	public static ZoneId zoneId = ZoneId.of("Asia/Hong_Kong");
	public static ZoneOffset zoneOffset = ZoneOffset.of("+08:00");
	public static DateTimeFormatter DATETIMEFORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId);
	public static DateTimeFormatter MODEL_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId);
	public Elasticsearch(String url) {
		this.url = url;
	}
	public static HashMap<Class<?>, String> classToType = new HashMap<Class<?>, String>(){{
		put(Timestamp.class, "date");
		put(Date.class, "date");
		put(OffsetDateTime.class, "date");
		put(String.class, "keyword");
		put(Integer.class, "integer");
		put(Long.class, "long");
		put(Double.class, "double");
		put(Boolean.class, "boolean");
	}};
	
	public void heartbeatChecking() {
		try {
			synchronized(isConnect) {
				logger.info("ElasticSearch -> HeartBeat Checking");
				HttpResult response = SimpleHttpRequest.get(url).request();
				if(response.isSuccess()) {
					isConnect = true;
				}
				else
					throw new Exception("Connection Failed");
			}
		} catch (Exception e) {
			logger.error("ElasticSearch -> HeartBeat Checking Failed : "+e.toString());
			isConnect = false;
		}
	}
	
	public void connectToServer() {
		try {
			synchronized(isConnect) {
				logger.info("ElasticSearch -> Connecting");
				HttpResult response = SimpleHttpRequest.get(url+PATH.ALIASES).request();
				if(response.isSuccess()) {
					logger.info("ElasticSearch -> Connected");
					isConnect = true;
					try {
						JSONObject alias = response.getAsJsonObject();
						
						ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
						scanner.addIncludeFilter(new AnnotationTypeFilter(Index.class));
						for (BeanDefinition bd : scanner.findCandidateComponents("com.smc.elastic.search.dto")) {
							Class<?> cls = Class.forName(bd.getBeanClassName());
							Index indexAnnotation = SerializeUtil.getElasticsearchEntityIndex(cls);
							if(!alias.has(indexAnnotation.value()) && indexAnnotation.createIndex()) {
								JSONObject mapping = new JSONObject();
								for(Map.Entry<String, DtoProperty> entry : SerializeUtil.getProperties(cls).entrySet()) {
									if(entry.getValue().getFieldAnnotation()!=null && entry.getValue().getFieldAnnotation().serializeIgnore())
										continue;
									JSONObject fieldAttr = new JSONObject();
									fieldAttr.put("type", classToType.get(entry.getValue().getField().getType()));
									if(entry.getValue().getField().getType().equals(String.class))
										fieldAttr.put("ignore_above", "65536");
									mapping.put(entry.getValue().getFieldName(), fieldAttr);
								} 
								JSONObject body = new JSONObject().put("mappings", new JSONObject().put("properties", mapping));
								if(!SimpleHttpRequest.put(url+"/"+indexAnnotation.value()).asJson().body(body.toString()).request().isSuccess())
									throw new Exception("Create Index Error");
								logger.info("ElasticSearch -> Create Index : "+cls.getSimpleName());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else
					throw new Exception("Connect Failed");
			}
		} catch (Exception e) {
			logger.error("ElasticSearch -> Connect Failed : "+e.toString());
			isConnect = false;
		}
	}
	public HttpResult deleteByQuery(ESDeleteByQuery query, String index) throws Exception{
		if(!isConnect) 
			throw new Exception("Elasticsearch is unreachable");
		
		return query.request(url+PATH.getDeletePath(index) );
	}
	public HttpResult bulkRequest(ESDtoBulk bulk) throws Exception{
		if(!isConnect) 
			throw new Exception("Elasticsearch is unreachable");
		
		return bulk.request(url+PATH.BULK);
	}
	public ElasticSearchResultDto findByESSearch(ESSearch esSearch, String index) throws Exception{
		if(!isConnect)
			throw new Exception("Elasticsearch is unreachable");
		HttpResult requestObject = SimpleHttpRequest.post(url+PATH.getSearchPath(index))
				.asJson().body(esSearch.getBody().toString()).request();
		
		if(!requestObject.isSuccess() || requestObject.getAsJsonObject().has("error")) {
			String exceptionMsg = !requestObject.getAsJsonObject().has("error") ? "Error Occured" : requestObject.getAsJsonObject().getJSONObject("error").getString("reason");
			throw new Exception(exceptionMsg);
		}
		return parseToResponseDto(requestObject.getAsJsonObject());
	}
	public <T extends ElasticsearchBaseDto<?>> ElasticSearchResultDto findByEntity(T entity) throws Exception{
		if(!isConnect)
			throw new Exception("Elasticsearch is unreachable");
		
		ESSearch esSearch = Elasticsearch.parseEntityToESSearch(entity).size(100);
		
		HttpResult requestObject = SimpleHttpRequest.post(url+PATH.getSearchPath(SerializeUtil.getElasticsearchEntityIndex(entity.getClass()).value()))
				.asJson().body(esSearch.getBody().toString()).request();
		if(!requestObject.isSuccess() || requestObject.getAsJsonObject().has("error")) {
			String exceptionMsg = !requestObject.getAsJsonObject().has("error") ? "Error Occured" : requestObject.getAsJsonObject().getJSONObject("error").getString("reason");
			throw new Exception(exceptionMsg);
		}
		return parseToResponseDto(requestObject.getAsJsonObject());
	}
	public static ElasticSearchResultDto parseToResponseDto(JSONObject jsonObject) throws Exception{
		return SerializeUtil.deserializeDto(jsonObject, ElasticSearchResultDto.class);
	}
	public static <T extends ElasticsearchBaseDto<?>> ESSearch modelToESSearch(QueryModel model, Class<? extends ElasticsearchBaseDto<?>> cls) throws Exception{
		ElasticsearchBaseDto<?> tmp = SerializeUtil.MapToDto(model.getQueryProperties(), cls);
		
		ESSearch esSearch = Elasticsearch.parseEntityToESSearch(tmp).from(model.getStart()).size(model.getPageLength());
		
		if(model.getFrom()!=null && !model.getFrom().isEmpty()) {
			esSearch.getQuery().must(
					ESCondition.range("timestamp").gte(LocalDateTime.from(MODEL_TIMESTAMP_FORMATTER.parse(model.getFrom())).atOffset(zoneOffset))
				);
		}
		if(model.getTo()!=null && !model.getTo().isEmpty()) {
			esSearch.getQuery().must(
					ESCondition.range("timestamp").lte(LocalDateTime.from(MODEL_TIMESTAMP_FORMATTER.parse(model.getTo())).atOffset(zoneOffset))
				);
		}
		return esSearch;
	}
	public static <T extends ElasticsearchBaseDto<?>> ESSearch parseEntityToESSearch(T entity) throws Exception{
		HashMap<String, DtoProperty> fieldMapper = SerializeUtil.getProperties(entity.getClass());
		ESSearch esSearch = new ESSearch();
		ArrayList<Condition> conditions = new ArrayList<>();
		for(String fieldName:fieldMapper.keySet()) {
			DtoProperty dtoProperty = fieldMapper.get(fieldName);
			Object val = dtoProperty.getField().get(entity);
			if(val != null && !val.toString().isEmpty()) {
				if(val.toString().contains("*"))
					conditions.add(ESCondition.wildcard(dtoProperty.getFieldName()).like(val));
				else
					conditions.add(ESCondition.match(dtoProperty.getFieldName()).is(val));
			}
		}
		esSearch.query(new ESBool().must(conditions.toArray(new Condition[conditions.size()])));
		return esSearch;
	}
	public static class PATH{
		public static String BULK = "/_bulk";
		public static String SEARCH = "/{index}/_search";
		public static String ALIASES = "/_aliases";
		public static String DELETEBYQUERY = "/{index}/_delete_by_query";
		public static String getSearchPath(String index) {
			return SEARCH.replace("{index}", index);
		}
		public static String getDeletePath(String index) {
			return DELETEBYQUERY.replace("{index}", index);
		}
	}
}
