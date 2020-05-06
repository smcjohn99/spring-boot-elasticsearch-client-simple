package com.smc.elastic.search;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.smc.annotation.Index;
import com.smc.elastic.search.dto.ElasticSearchResultDto;
import com.smc.elastic.search.dto.ElasticsearchBaseDto;
import com.smc.elastic.search.operation.ESDeleteByQuery;
import com.smc.elastic.search.operation.ESDtoBulk;
import com.smc.elastic.search.search.Bool;
import com.smc.elastic.search.search.Condition;
import com.smc.elastic.search.search.ESBool;
import com.smc.elastic.search.search.ESCondition;
import com.smc.elastic.search.search.ESSearch;
import com.smc.elastic.search.search.ESSort;
import com.smc.simple.http.request.HttpResult;
import com.smc.simple.http.request.SimpleHttpRequest;
import com.smc.util.SerializeUtil;
import com.smc.util.SerializeUtil.DtoProperties;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;


@SuppressWarnings("serial")
public class Elasticsearch {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	public String url;
	public volatile Boolean isConnect = new Boolean(false); // Check Reachable
	public static DateTimeFormatter DATETIMEFORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("+08:00"));

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
							Index indexAnnotation = SerializeUtil.getDtoIndex(cls);
							if(!alias.has(indexAnnotation.value()) && indexAnnotation.createIndex()) {
								JSONObject mapping = new JSONObject();
								for(Map.Entry<String, DtoProperties> entry : SerializeUtil.getProperties(cls).entrySet()) {
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
	public ElasticSearchResultDto findByDto(ElasticsearchBaseDto<?> dto) throws Exception{
		return this.findByDto(dto, null);
	}
	public ElasticSearchResultDto findByDto(ElasticsearchBaseDto<?> dto, ESSort sort) throws Exception{
		if(!isConnect)
			throw new Exception("Elasticsearch is unreachable");
			
		ESSearch esSearch = parseMapToESSearch(SerializeUtil.serializeDto(dto).toMap(), dto.getClass());
		System.out.println(esSearch.getBody().toString());
		if(sort!=null) esSearch.sort(sort);
		
		return this.findByESSearch(esSearch, SerializeUtil.getDtoIndex(dto.getClass()).value());
	}
	public ElasticSearchResultDto findByMapAttributes(HashMap<String, Object> body, Class<?> cls) throws Exception{
		return this.findByMapAttributes(body, cls, null);
	}
	public ElasticSearchResultDto findByMapAttributes(HashMap<String, Object> body, Class<?> cls, ESSort sort) throws Exception{
		if(!isConnect)
			throw new Exception("Elasticsearch is unreachable");
			
		ESSearch esSearch = parseMapToESSearch(body, cls);
		if(sort!=null) esSearch.sort(sort);
		
		return this.findByESSearch(esSearch, SerializeUtil.getDtoIndex(cls).value());
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
	public static ElasticSearchResultDto parseToResponseDto(JSONObject jsonObject) throws Exception{
		return SerializeUtil.deserializeDto(jsonObject, ElasticSearchResultDto.class);
	}
	
	public static ESSearch parseMapToESSearch(Map<String, Object> body, Class<?> cls) throws Exception{
		//HashMap<String, DtoProperties> fieldMapper = SerializeUtil.getProperties(cls);
		ESSearch esSearch = new ESSearch().size(10).from(0);
		ArrayList<Condition> conditions = new ArrayList<>();
		for(Map.Entry<String, Object> entry:body.entrySet()) {
			if(entry.getValue()==null || entry.getValue().toString().isEmpty())
				continue;
			String key = entry.getKey();
			if(key.equals("size"))
				esSearch.from(Integer.parseInt(entry.getValue().toString()));
			else if(key.equals("from"))
				esSearch.size(Integer.parseInt(entry.getValue().toString()));
			else {
				String field = key;
				if(body.get(key) instanceof List) {
					ArrayList<Condition> shoulds = new ArrayList<>();
					
					@SuppressWarnings("unchecked")
					List<String> strs = (List<String>)body.get(key);
					for(String str:strs) {
						if(str.contains("*"))
							shoulds.add(ESCondition.wildcard(field).like(str));
						else
							shoulds.add(ESCondition.match(field).is(str));
					}
					conditions.add(Bool.should(shoulds.toArray(new Condition[shoulds.size()])));
				}
				else {
					if(body.get(key).toString().isEmpty())
						continue;
					
					if(body.get(key).toString().contains("*"))
						conditions.add(ESCondition.wildcard(field).like(body.get(key)));
					else
						conditions.add(ESCondition.match(field).is(body.get(key)));
				}
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
