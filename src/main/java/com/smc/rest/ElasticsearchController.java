package com.smc.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smc.ResponseMsg;

@RestController
public class ElasticsearchController {
	@Autowired ApplicationContext context;
	@RequestMapping("test")
	public ResponseMsg test() throws Exception{
		return ResponseMsg.builder("OK").build();
	}
}
