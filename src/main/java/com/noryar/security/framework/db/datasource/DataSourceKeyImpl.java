package com.noryar.security.framework.db.datasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;



/**
 * 功能描述：数据源key的存储控制器实现.
 * @author Leon.
 * 
 */
public class DataSourceKeyImpl implements DataSourceKey, InitializingBean {
	
	static final Logger logger = LoggerFactory.getLogger(DataSourceKeyImpl.class);
	
	// 数据源key的存储
	private static final ThreadLocal<String> DB_KEY = new ThreadLocal<String>();

	// 读(从)库
	private Map<String, String> readDateSourceMap = new ConcurrentHashMap<String, String>();

	// 写(主)库
	public String writeKey;

	private final Random random = new Random();

	private List<String> dateSourceReadKeys = Collections
			.synchronizedList(new ArrayList<String>());

	public void setReadDateSourceMap(Map<String, String> readDateSourceMap) {
		this.readDateSourceMap = Collections.synchronizedMap(readDateSourceMap);
		dateSourceReadKeys = Collections.synchronizedList(new ArrayList<String>(
				readDateSourceMap.values()));
	}
 
	public String getKey(String key) {
		return readDateSourceMap.get(key);
	}

	public String getWriteKey() {
		return writeKey;
	}

	public void setWriteKey(String writeKey) {
		this.writeKey = writeKey;
	}

	public void setWriteKey() {
		DB_KEY.set(writeKey);
		logger.debug("set data source writeKey[" + DB_KEY.get() + "]");

	}

	public void setReadKey() {
		DB_KEY.set(getRandomKey());
		logger.debug("set data source readKey[" + DB_KEY.get() + "]");
	}

	public void setKey(String key) {
		if (StringUtils.isBlank(key)
				|| StringUtils.isBlank(readDateSourceMap.get(key))) {
			throw new RuntimeException("the data source key is null");
		}
		DB_KEY.set(readDateSourceMap.get(key));
		logger.debug("set data source key[" + DB_KEY.get() + "]");
	}

	public String getKey() {
		if (DB_KEY.get() == null) {
			// throw new RuntimeException("the data source key is null");
			setReadKey();
		}
		String key = DB_KEY.get();
		logger.debug("get data source Key[" + DB_KEY.get() + "]");
		return key;
	}

	private String getRandomKey() {
		int randomInt = random.nextInt(readDateSourceMap.values().size());
		return dateSourceReadKeys.get(randomInt);
	}

	public void clearKey() {
		DB_KEY.remove();
	}

	public static void clearKeyForce() {
		DB_KEY.remove();
	}

	public void afterPropertiesSet() throws Exception {
		// default using random strategy

	}
}
