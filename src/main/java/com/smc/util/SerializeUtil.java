package com.smc.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import org.hibernate.proxy.HibernateProxy;
import org.json.JSONArray;
import org.json.JSONObject;

import com.smc.annotation.FieldAlias;
import com.smc.annotation.FieldMap;
import com.smc.annotation.Index;
import com.smc.elastic.search.Elasticsearch;

public class SerializeUtil {
	/* Cache Properties Map */
	private static Map<Class<?>, HashMap<String, DtoProperty>> dtoPropertiesCache = new HashMap<>();
	private static Map<Class<?>, HashMap<String, EntityProperty>> entityPropertiesCache = new HashMap<>();
	private static Map<Class<?>, Index> elasticsearchIndexCache = new HashMap<>();
	
	public static Index getElasticsearchEntityIndex(Class<?> cls) throws Exception{
		if(elasticsearchIndexCache.containsKey(cls))
			return elasticsearchIndexCache.get(cls);
		Index index = cls.getAnnotation(Index.class);
		if(index == null)
			throw new Exception("Dto Not Have Index Annotation");
		elasticsearchIndexCache.put(cls, index);
		return index;
	}
	public static HashMap<String, DtoProperty> getProperties(Class<?> cls) throws Exception{
		if(dtoPropertiesCache.containsKey(cls))
			return dtoPropertiesCache.get(cls);
		HashMap<String, DtoProperty> map = new HashMap<>();
		Class<?> loopingClass = cls;
		while(!loopingClass.equals(Object.class)) {
			for(Field f:loopingClass.getDeclaredFields()) {
				DtoProperty properties = new DtoProperty();
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
	public static HashMap<String, EntityProperty> getEntityProperties(Class<?> cls) throws Exception{
		if(entityPropertiesCache.containsKey(cls))
			return entityPropertiesCache.get(cls);
		HashMap<String, EntityProperty> map = new HashMap<>();
		Class<?> loopingClass = cls;
		while(!loopingClass.equals(Object.class) && !HibernateProxy.class.isAssignableFrom(loopingClass)) {
			for(Field f:loopingClass.getDeclaredFields()) {
				EntityProperty properties = new EntityProperty();
				Column column = f.getAnnotation(Column.class);
				properties.setFieldName(column!=null ? column.name() : f.getName());
				properties.setColumnAnnotation(column);
				properties.setField(f);
				f.setAccessible(true);
				map.put(f.getName(), properties);
			}
			loopingClass = loopingClass.getSuperclass();
		}
		entityPropertiesCache.put(cls, map);
		return map;
	}
	/* @RequestBody Map<String, Object> to Model by Attribute Name */
	@SuppressWarnings("unchecked")
	public static <T> T MapToDto(Map<String, Object> source, Class<?> cls) throws Exception{
		Object t = cls.newInstance();
		HashMap<String, DtoProperty> properties = getProperties(cls);
		for(String key:properties.keySet()) {
			if(source.containsKey(key) && !source.get(key).toString().isEmpty()) {
				DtoProperty property = properties.get(key);
				property.getField().set(t, source.get(key));
			}
		}
		return (T)t;
	}
	/* Entity @Column To Dto @Field For Response
	 		Support Inner Attribute		*/
	public static void entityMapToDto(Object source, Object target) throws Exception{
		try {
			HashMap<String, DtoProperty> targetProperties = getProperties(target.getClass());
			HashMap<String, EntityProperty> entityProperties = getEntityProperties(source.getClass());
			for(String key:targetProperties.keySet()) {
				DtoProperty targetProperty = targetProperties.get(key);
				if(targetProperty.getField().get(target) != null)
					continue;
				ArrayList<String> fields = new ArrayList<>();
				fields.add(targetProperty.getFieldName());
				if(targetProperty.getFieldAlias()!=null) 
					fields.addAll(Arrays.asList(targetProperty.getFieldAlias().value()));
				for(String eKey:entityProperties.keySet()) {
					EntityProperty entityProperty = entityProperties.get(eKey);
					Object val = entityProperty.getField().get(source);
					if(val == null)
						continue;
					for(String f:fields) {
						String tmp = f;
						String[] tmpArray = null;
						if(f.contains(".")) {
							tmpArray = tmp.split("\\.");
							tmp = tmpArray[0];
						}
						if(tmp.equals(entityProperty.getFieldName())) {
							if(tmpArray != null) {
								try {
									JSONObject tmpJson = null;
									if(val.getClass().equals(JSONObject.class))
										tmpJson = (JSONObject)val;
									else if(val.getClass().equals(String.class))
										tmpJson = new JSONObject((String)val);
									for(int i=1;i<tmpArray.length;i++) {
										if(i+1 == tmpArray.length)  
											val = tmpJson.get(tmpArray[i]);
										else
											tmpJson  = tmpJson.getJSONObject(tmpArray[i]);
									}
								} catch (Exception e) {
									continue;
								}
							}
							val = typeConverter(val, targetProperty.getField().getType());
							targetProperty.getField().set(target, val);
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			throw new Exception("Serialize Error : "+e.getMessage());
		}
	}
	/* @RequestBody Map<String, Object> map to JSONObject by Attribute Name */
	public static JSONObject serializeMapToJSON(Map<String, Object> map, Class<?> cls) throws Exception{
		JSONObject jsonObject = new JSONObject();
		HashMap<String, DtoProperty> clsProperties = SerializeUtil.getProperties(cls);
		for(String key:clsProperties.keySet()) {
			if(map.get(key) != null && !map.get(key).toString().isEmpty()) {
				jsonObject.put(clsProperties.get(key).getFieldName(), map.get(key));
			}
		}
		return jsonObject;
	}
	/* Object map to JSONObject by @FieldMap */
	public static <T> JSONObject serializeDto(T bean) throws Exception{
		JSONObject jsonObject = new JSONObject();
		HashMap<String, DtoProperty> properties = getProperties(bean.getClass());
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
	/* JSONObject map to Object by @FieldMap */
	public static <T> T deserializeDto(JSONObject jsonObject, Class<T> cls, T obj) throws Exception{
		if(obj == null)
			obj = cls.newInstance();
		HashMap<String, DtoProperty> properties = getProperties(cls);
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
				if(attributes.has(field) && f.get(obj) == null) {
					/*if(Date.class.isAssignableFrom(f.getType())) {
						if(attributes.get(field).getClass().equals(String.class)) {
							try {
								f.set(obj, Timestamp.valueOf(OffsetDateTime.parse(attributes.getString(field)).atZoneSameInstant(ElasticSearch.DATETIMEFORMATTER.getZone()).toLocalDateTime() ));
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
							f.set(obj, OffsetDateTime.ofInstant(Timestamp.valueOf(attributes.getString(field)).toInstant(), ZoneId.of("UTC+8")));
						}
					}
					else if(LocalDateTime.class.isAssignableFrom(f.getType())){
						try {
							f.set(obj, LocalDateTime.parse(attributes.getString(field)));
						} catch (Exception e) {
							f.set(obj, LocalDateTime.ofInstant(Timestamp.valueOf(attributes.getString(field)).toInstant(), ZoneId.of("UTC+8")));
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
					else if(f.getType().equals(JSONObject.class) && !attributes.get(field).getClass().equals(JSONObject.class)) {
						try {
							f.set(obj, new JSONObject(attributes.get(field).toString()));
						} catch (Exception e) {
							f.set(obj, new JSONObject());
						}
						
					}
					else if(f.getType().equals(String.class) && attributes.get(field).getClass().equals(JSONObject.class)) {
						f.set(obj, attributes.getJSONObject(field).toString());
					}*/
					Object val = typeConverter(attributes.get(field), f.getType());
					//if(f.get(obj) == null)
					f.set(obj, val);
					break;
				}
			}
			fields.clear();
		}
		return obj;
	}
	public static <T> T typeConverter(Object source, Class<T> target) throws Exception{
		Class<?> sourceCls = source.getClass();
		Object val = source;
		
		if(sourceCls.equals(target))
			return target.cast(source);
		
		if(Number.class.isAssignableFrom(target)) {
			val = target.getMethod("valueOf", String.class).invoke(null, source.toString());
		}
		else if(List.class.isAssignableFrom(target)) {
			if(sourceCls.equals(JSONArray.class))
				val = ((JSONArray)source).toList();
			else
				val = Arrays.asList(source);
		}
		else if(target.equals(BigDecimal.class)) {
			val = new BigDecimal(source.toString());
		}
		else if(target.equals(JSONObject.class)) {
			if(Map.class.isAssignableFrom(sourceCls))
				val = new JSONObject((Map<?, ?>)source);
			else
				val = new JSONObject(source.toString());
		}
		else if(target.equals(JSONArray.class)) {
			if(Collection.class.isAssignableFrom(sourceCls))
				val = new JSONObject((Collection<?>)source);
			else
				val = new JSONArray(source.toString());
		}
		else if(target.equals(OffsetDateTime.class)) {
			if(sourceCls.equals(String.class)) {
				val = OffsetDateTime.parse((String)source);
			}
			else if(Date.class.isAssignableFrom(sourceCls)) {
				OffsetDateTime.ofInstant(((Date)source).toInstant(), Elasticsearch.DATETIMEFORMATTER.getZone());
			}
		}
		else if(target.equals(LocalDateTime.class)) {
			if(sourceCls.equals(String.class)) {
				try {
					val = LocalDateTime.parse((String)source);
				} catch (Exception e) {
					val = LocalDateTime.ofInstant(Timestamp.valueOf((String)source).toInstant(), Elasticsearch.DATETIMEFORMATTER.getZone());
				}
			}
			else if(Date.class.isAssignableFrom(sourceCls)) {
				val = LocalDateTime.ofInstant(((Date)source).toInstant(), Elasticsearch.DATETIMEFORMATTER.getZone());
			}
		}
		
		return target.cast(val);
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
	public static class DtoProperty{
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
	public static class EntityProperty{
		public String fieldName;
		public Column columnAnnotation;
		public Field field;
		public String getFieldName() {
			return fieldName;
		}
		public Column getColumnAnnotation() {
			return columnAnnotation;
		}
		public Field getField() {
			return field;
		}
		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}
		public void setColumnAnnotation(Column columnAnnotation) {
			this.columnAnnotation = columnAnnotation;
		}
		public void setField(Field field) {
			this.field = field;
		}
		
	}
}
