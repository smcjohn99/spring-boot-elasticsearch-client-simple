package com.smc.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.smc.annotation.FieldAlias;
import com.smc.annotation.FieldMap;
import com.smc.annotation.Index;
import com.smc.elastic.search.Elasticsearch;

public class SerializeUtil {
	/* Cache */
	private static final HashMap<Class<?>, HashMap<String, DtoProperties>> dtoPropertiesCache = new HashMap<>();
	private static final HashMap<Class<?>, Index> elasticsearchIndexCache = new HashMap<>();
	
	public static Index getDtoIndex(Class<?> cls) throws Exception{
		if(dtoPropertiesCache.containsKey(cls))
			return elasticsearchIndexCache.get(cls);
		Index index = cls.getAnnotation(Index.class);
		if(index == null)
			throw new Exception("Dto Not Have Index Annotation");
		elasticsearchIndexCache.put(cls, index);
		return index;
	}
	public static HashMap<String, DtoProperties> getProperties(Class<?> cls) throws Exception{
		if(dtoPropertiesCache.containsKey(cls))
			return dtoPropertiesCache.get(cls);
		HashMap<String, DtoProperties> map = new HashMap<>();
		Class<?> loopingClass = cls;
		while(!loopingClass.equals(Object.class)) {
			for(Field f:loopingClass.getDeclaredFields()) {
				DtoProperties properties = new DtoProperties();
				FieldMap annotationProperties = f.getAnnotation(FieldMap.class);
				properties.setFieldName(annotationProperties!=null && !annotationProperties.value().isEmpty() ? annotationProperties.value() : f.getName());
				properties.setFieldAnnotation(annotationProperties);
				properties.setField(f);
				properties.setFieldAlias(f.getAnnotation(FieldAlias.class));
				f.setAccessible(true);
				map.put(f.getName(), properties);
			}
			loopingClass = loopingClass.getSuperclass();
		}
		dtoPropertiesCache.put(cls, map);
		return map;
	}
	public static JSONObject serializeMapToJSON(Map<String, Object> map, Class<?> cls) throws Exception{
		JSONObject jsonObject = new JSONObject();
		HashMap<String, DtoProperties> clsProperties = SerializeUtil.getProperties(cls);
		for(String key:clsProperties.keySet()) {
			if(map.get(key) != null) {
				jsonObject.put(clsProperties.get(key).getFieldName(), map.get(key));
			}
		}
		
		return jsonObject;
	}
	public static <T> JSONObject serializeDto(T bean) throws Exception{
		JSONObject jsonObject = new JSONObject();
		HashMap<String, DtoProperties> properties = getProperties(bean.getClass());
		for(String key:properties.keySet()) {
			Field f = properties.get(key).getField();
			Object value = f.get(bean);
			if(value != null) {
				FieldMap annotationProperties = properties.get(key).getFieldAnnotation();
				if(annotationProperties!=null && annotationProperties.serializeIgnore())
					continue;
				String field = properties.get(key).getFieldName();
				if(Date.class.isAssignableFrom(f.getType())) 
					jsonObject.put(field, ((Date)value).toInstant().atZone(Elasticsearch.DATETIMEFORMATTER.getZone()).toOffsetDateTime().toString());
				else
					jsonObject.put(field, value);
			}
		}
		return jsonObject;
	}
	public static <T> T deserializeDto(JSONObject jsonObject, Class<T> cls, T obj) throws Exception{
		if(obj == null)
			obj = cls.newInstance();
		HashMap<String, DtoProperties> properties = getProperties(cls);
		if(jsonObject.has("_source") && jsonObject.get("_source").getClass() == JSONObject.class ) {
			JSONObject source = jsonObject.getJSONObject("_source");
			for(String keySet:source.keySet())
				jsonObject.put(keySet, source.get(keySet));
		}
		
		for(String key:properties.keySet()) {
			Field f = properties.get(key).field;
			if(f.get(obj)!=null)
				continue;
			ArrayList<String> fields = new ArrayList<>();
			fields.add(properties.get(key).getFieldName());
			if(properties.get(key).getFieldAlias()!=null) 
				fields.addAll(Arrays.asList(properties.get(key).getFieldAlias().value()));
			for(String field:fields) {
				JSONObject attributes = jsonObject;
				if(field.contains(".")){
					try {
						String[] innerField = field.split("\\.");
						for(int i=0;i<innerField.length;i++){ 
							if(i+1 != innerField.length) {
								if(attributes.get(innerField[i]).getClass().equals(JSONObject.class))
									attributes = attributes.getJSONObject(innerField[i]);
								else 
									attributes = new JSONObject(attributes.getString(innerField[i]));
							}
							else
								field = innerField[i];
						}
					} catch (Exception e) {}
				}
				if(attributes.has(field)) {
					if(Date.class.isAssignableFrom(f.getType())) {
						if(attributes.get(field).getClass().equals(String.class)) {
							try {
								f.set(obj, Timestamp.valueOf(OffsetDateTime.parse(attributes.getString(field)).atZoneSameInstant(Elasticsearch.DATETIMEFORMATTER.getZone()).toLocalDateTime() ));
							} catch (Exception e) {
								f.set(obj, Timestamp.valueOf(attributes.getString(field)));
							}
						}
					}
					else if(List.class.isAssignableFrom(f.getType())) {
						if(attributes.get(field).getClass().equals(JSONArray.class)) 
							f.set(obj, attributes.getJSONArray(field).toList());
						else if(!List.class.isAssignableFrom(attributes.get(field).getClass())) 
							f.set(obj, Arrays.asList(attributes.get(field)));
					}
					else if(OffsetDateTime.class.isAssignableFrom(f.getType())){
						try {
							f.set(obj, OffsetDateTime.parse(attributes.getString(field)));
						} catch (Exception e) {
							f.set(obj, OffsetDateTime.ofInstant(Timestamp.valueOf(attributes.getString(field)).toInstant(), Elasticsearch.DATETIMEFORMATTER.getZone()));
						}
					}
					else if(LocalDateTime.class.isAssignableFrom(f.getType())){
						try {
							f.set(obj, LocalDateTime.parse(attributes.getString(field)));
						} catch (Exception e) {
							f.set(obj, LocalDateTime.ofInstant(Timestamp.valueOf(attributes.getString(field)).toInstant(), Elasticsearch.DATETIMEFORMATTER.getZone()));
						}
					}
					else if(!f.getType().equals(BigDecimal.class) && attributes.get(field).getClass().equals(BigDecimal.class)){
						if(f.getType().equals(String.class))
							f.set(obj, attributes.getBigDecimal(field).toString());
						else if(Number.class.isAssignableFrom(f.getType())) 
							f.set(obj, f.getType().getMethod("valueOf", String.class).invoke(null, attributes.getBigDecimal(field).toString()) );
					}
					else if(Number.class.isAssignableFrom(f.getType()) && !attributes.get(field).getClass().equals(f.getType()) ){
						f.set(obj, f.getType().getMethod("valueOf", String.class).invoke(null, attributes.get(field).toString()));
					}
					if(f.get(obj) == null)
						f.set(obj, attributes.get(field));
					break;
				}
			}
			fields.clear();
		}
		return obj;
	}
	public static <T> T deserializeDto(Map<String, Object> map, Class<T> cls) throws Exception{
		return deserializeDto(new JSONObject(map), cls, null);
	}
	public static <T> T deserializeDto(Map<String, Object> map, Class<T> cls, T obj) throws Exception{
		return deserializeDto(new JSONObject(map), cls, obj);
	}
	public static <T> T deserializeDto(JSONObject jsonObject, Class<T> cls) throws Exception{
		return deserializeDto(jsonObject, cls, null);
	}
	public static class DtoProperties{
		public String fieldName;
		public FieldMap fieldAnnotation;
		public FieldAlias fieldAlias;
		public Field field;
		public String getFieldName() {
			return fieldName;
		}
		public FieldMap getFieldAnnotation() {
			return fieldAnnotation;
		}
		public Field getField() {
			return field;
		}
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}
		public void setFieldAnnotation(FieldMap fieldAnnotation) {
			this.fieldAnnotation = fieldAnnotation;
		}
		public void setField(Field field) {
			this.field = field;
		}
		public FieldAlias getFieldAlias() {
			return fieldAlias;
		}
		public void setFieldAlias(FieldAlias fieldAlias) {
			this.fieldAlias = fieldAlias;
		}
		
	}
}
