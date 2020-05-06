package com.smc.elastic.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchAutoConfiguartion {
	
	@Bean
	@ConditionalOnProperty("elastic.search.url")
	@ConditionalOnMissingBean(Elasticsearch.class)
	public Elasticsearch getElasticSearch(@Value("${elastic.search.url}") String url) {
		System.out.println("Elasticsearch Init");
		Elasticsearch elasticsearch = new Elasticsearch(url);
		elasticsearch.connectToServer();
		return elasticsearch;
	}
	
	@Bean
	@ConditionalOnProperty("elastic.search.heartbeart.enabled")
	public ElasticsearchScheduler getElasticSearchScheduler() {
		System.out.println("ElasticsearchScheduler Init");
		return new ElasticsearchScheduler();
	}
}
