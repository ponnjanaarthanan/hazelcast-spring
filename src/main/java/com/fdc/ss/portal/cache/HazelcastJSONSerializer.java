package com.fdc.ss.portal.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.nio.serialization.ByteArraySerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HazelcastJSONSerializer implements ByteArraySerializer {
	private static final Logger LOG = LoggerFactory.getLogger(HazelcastJSONSerializer.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
	}

	@Override
	public int getTypeId() {
		return 100;
	}

	@Override
	public void destroy() {
	}

	@Override
	public byte[] write(Object o) throws IOException {
		return mapper.writeValueAsBytes(o);
	}

	@Override
	public Object read(byte[] bytes) throws IOException {
		JsonNode node = mapper.readTree(bytes);
		String type = node.get(0).asText();
		Class<?> cls;
		try {
			cls = Class.forName(type);
		} catch (ClassNotFoundException e) {
			LOG.error("Could not retrieve the deserialized type of byte array", e);
			return null;
		}
		return mapper.readValue(bytes, cls);
	}

}
