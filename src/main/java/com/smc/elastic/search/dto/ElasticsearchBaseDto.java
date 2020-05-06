package com.smc.elastic.search.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smc.annotation.FieldMap;

@SuppressWarnings("unchecked")
public class ElasticsearchBaseDto<T extends ElasticsearchBaseDto<T>> {
	@FieldMap(value="_index", serializeIgnore=true)
	@JsonIgnore
    public String index;
	@FieldMap(value="_type", serializeIgnore=true)
	@JsonIgnore
    public String docType;
    @FieldMap(value="_id", serializeIgnore=true)
    @JsonIgnore
    public String id;
    public OffsetDateTime timestamp;
    
	public String getIndex() {
		return index;
	}
	public String getDocType() {
		return docType;
	}
	
	public T setIndex(String index) {
		this.index = index;
		return (T)this;
	}
	public T setDocType(String docType) {
		this.docType = docType;
		return (T)this;
	}
	public String getId() {
		return id;
	}
	public OffsetDateTime getTimestamp() {
		return timestamp;
	}
	public T setId(String id) {
		this.id = id;
		return (T)this;
	}
	public T setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
		return (T)this;
	}
    

}