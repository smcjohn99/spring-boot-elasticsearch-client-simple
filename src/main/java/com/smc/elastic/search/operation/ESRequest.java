package com.smc.elastic.search.operation;

import java.util.List;

import com.smc.elastic.search.search.ESBool;


//John.Suen
public class ESRequest {
	public static ESBulk bulk(String index, List<ESOperation> operationList){ 
		return new ESBulk(index, operationList);
	} 
	public static ESBulk bulk(String index, ESOperation... esOperations){ 
		return new ESBulk(index, esOperations);
	}
	public static ESDtoBulk dtoBulk(List<ESDtoOperation> esOperations){ 
		return new ESDtoBulk(esOperations);
	}
	public static ESDtoBulk dtoBulk(ESDtoOperation... esOperations){ 
		return new ESDtoBulk(esOperations);
	}
	public static ESDeleteByQuery deleteByQuery(ESBool esQuery) {
		return new ESDeleteByQuery(esQuery);
	}
}
