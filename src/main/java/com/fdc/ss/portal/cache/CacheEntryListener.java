package com.fdc.ss.portal.cache;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;

public class CacheEntryListener implements EntryListener<Serializable, Serializable> {
	
	private final static Log LOG = LogFactory.getLog(CacheEntryListener.class);

	@Override
	public void entryAdded(EntryEvent<Serializable, Serializable> arg0) {
		LOG.info("Entry Added : " + arg0);
	}

	@Override
	public void entryUpdated(EntryEvent<Serializable, Serializable> arg0) {
		LOG.info("Entry Updated : " + arg0);
	}

	@Override
	public void entryRemoved(EntryEvent<Serializable, Serializable> arg0) {
		LOG.info("Entry Removed : " + arg0);
	}

	@Override
	public void entryEvicted(EntryEvent<Serializable, Serializable> arg0) {
		LOG.info("Entry Evicted : " + arg0);
	}

	@Override
	public void mapCleared(MapEvent arg0) {
		LOG.info("Map Cleared : " + arg0);

	}

	@Override
	public void mapEvicted(MapEvent arg0) {
		LOG.info("Map Evicted : " + arg0);
	}

}
