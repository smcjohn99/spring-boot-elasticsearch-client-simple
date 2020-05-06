package com.smc.elastic.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

public class ElasticsearchScheduler {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired Elasticsearch elasticsearch;

	@Scheduled(fixedDelay = 1000 * 60 * 5)
	public void heartBeatChecking() {
		elasticsearch.heartbeatChecking();
	}
}
