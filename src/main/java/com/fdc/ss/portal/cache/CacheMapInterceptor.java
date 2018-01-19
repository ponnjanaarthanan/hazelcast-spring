package com.fdc.ss.portal.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hazelcast.map.MapInterceptor;

public class CacheMapInterceptor implements MapInterceptor {
	
	private static final long serialVersionUID = 1L;
	
	private final static Log LOG = LogFactory.getLog(CacheMapInterceptor.class);

	@Override
	public Object interceptGet(Object value) {
		return value;
	}

	@Override
	public void afterGet(Object value) {
		LOG.info(value != null ? value.toString() : "No Value Found");
	}

	@Override
	public Object interceptPut(Object oldValue, Object newValue) {
		return newValue;
	}

	@Override
	public void afterPut(Object value) {

	}

	@Override
	public Object interceptRemove(Object removedValue) {
		return removedValue;
	}

	@Override
	public void afterRemove(Object value) {

	}

}
