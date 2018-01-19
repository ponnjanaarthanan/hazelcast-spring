package com.fdc.ss.portal.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hazelcast.core.HazelcastInstance;

@Controller
@RequestMapping("/cache")
public class RestController {
	
	private final static Log LOG = LogFactory.getLog(RestController.class);
	
	private final static String MAP_NAME = "MAP_NAME";
	
	@Autowired
	HazelcastInstance cacheInstance;
	
	@RequestMapping(value="/put/{key}/{value}", method=RequestMethod.GET)
	public void putCache(@PathVariable("key") String key, @PathVariable("value") String value) {
		cacheInstance.getMap(MAP_NAME).put(key, value);
	}

	@RequestMapping(value="/get/{key}", method=RequestMethod.GET)
	@ResponseBody
	public Object getCache(@PathVariable("key") String key) {
		return cacheInstance.getMap(MAP_NAME).get(key);
	}

}
