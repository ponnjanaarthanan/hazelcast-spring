package com.fdc.ss.portal.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hazelcast.core.LifecycleEvent;
import com.hazelcast.core.LifecycleListener;

public class MergeLifecycleListener implements LifecycleListener {
	
	private final static Log LOG = LogFactory.getLog(MergeLifecycleListener.class);

	@Override
	public void stateChanged(LifecycleEvent event) {
			System.out.println("-------------------------------------------------------------");
			LOG.error(event);		
	}

}
