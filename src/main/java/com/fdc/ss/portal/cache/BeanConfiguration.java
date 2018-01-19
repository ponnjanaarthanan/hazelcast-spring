package com.fdc.ss.portal.cache;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EntryListenerConfig;
import com.hazelcast.config.GlobalSerializerConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.ListConfig;
import com.hazelcast.config.ListenerConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.WanReplicationConfig;
import com.hazelcast.config.WanReplicationRef;
import com.hazelcast.config.WanTargetClusterConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.merge.PassThroughMergePolicy;

@Configuration
public class BeanConfiguration {
	
	private final static Log LOG = LogFactory.getLog(BeanConfiguration.class);
	
	@Autowired
	CacheConfig cacheConfig;

	public String showConfiguration() {
        return new StringBuffer("networkPort: ").append(cacheConfig.getNetworkConfigPort()).append(", multicastGroup: ")
                .append(cacheConfig.getMulticastGroup()).append(", multicastPort: ")
                .append(cacheConfig.getMulticastPort()) .append(", clusterName: ")
                .append(cacheConfig.getClusterName()).append(", backupCount: ")
                .append(cacheConfig.getBackupCount()).append(", mapStoreLocation: ")
                .append(cacheConfig.getMapStoreLocation()).append(", writeDelaySeconds: ")
                .append(cacheConfig.getWriteDelaySeconds())
                .append(", interface: ").append(cacheConfig.getInterfaces())
                .append(", tcpip: ").append(cacheConfig.getTcpipMember())
                .append(", timetolive: ").append(cacheConfig.getTimeToLiveInSeconds())
                .append(", timetoidle: ").append(cacheConfig.getTimeToIdleInSeconds())
                .append(", wan endpoints: ").append(cacheConfig.getWanEndPoints())
                .toString();
    }
	
	@Bean
    HazelcastInstance getHazelcastInstance() {
    	
		final Config config = new Config();
		
        LOG.info(showConfiguration());

        config.getSerializationConfig().setGlobalSerializerConfig(new GlobalSerializerConfig().setImplementation(new HazelcastJSONSerializer()));
        GroupConfig groupConfig = new GroupConfig();
        groupConfig.setName(cacheConfig.getClusterName()).setPassword("dev-pass");
        config.setGroupConfig(groupConfig);

        config.getNetworkConfig().setPort(cacheConfig.getNetworkConfigPort());
        config.getNetworkConfig().setPortAutoIncrement(true);

        MapConfig mapConfig = new MapConfig(cacheConfig.getMapStoreName())
                .setBackupCount(cacheConfig.getBackupCount())
                .setReadBackupData(false)
                .setMergePolicy("hz.LATEST_UPDATE")
                .setTimeToLiveSeconds(cacheConfig.getTimeToLiveInSeconds())
                .setMaxIdleSeconds(cacheConfig.getTimeToIdleInSeconds());
        		
        mapConfig.addEntryListenerConfig(new EntryListenerConfig(new CacheEntryListener(), true, false));
        
        /* Wan Replication */
        if(cacheConfig.getWanEndPoints() != null){
            WanReplicationConfig wanConfig = new WanReplicationConfig();
            wanConfig.setName(cacheConfig.getWanName());
            WanTargetClusterConfig clusterConfig = new WanTargetClusterConfig();
            clusterConfig
            		.setGroupName(cacheConfig.getClusterName())
            		.setGroupPassword("dev-pass")
            		.setReplicationImpl(cacheConfig.getWanReplicationClass())
            		.addEndpoint(cacheConfig.getWanEndPoints());
            wanConfig.addTargetClusterConfig(clusterConfig);
            config.addWanReplicationConfig(wanConfig);        	

            /* Wan Replicate Reference */
            WanReplicationRef wRef = new WanReplicationRef();
            wRef.setMergePolicy(PassThroughMergePolicy.class.getName())
            			.setName(cacheConfig.getWanName())
            			.setRepublishingEnabled(false);
            mapConfig.setWanReplicationRef(wRef);
        }
        
        /* Wan Replication 
        if(cacheConfig.getWanEndPoints() != null){
            WanReplicationConfig wanConfig = new WanReplicationConfig();
            wanConfig.setName(cacheConfig.getWanName());
            WanPublisherConfig publisherConfig = new WanPublisherConfig();
            publisherConfig
            	.setClassName(cacheConfig.getWanReplicationClass())
            	.setGroupName(cacheConfig.getClusterName())
            	.setImplementation(cacheConfig.getWanReplicationClass())
            	.setQueueCapacity(1000)
            	.setQueueFullBehavior(WANQueueFullBehavior.THROW_EXCEPTION);
            
            Map<String, Comparable> props = publisherConfig.getProperties();
            props.put("snapshot.enabled", false);
            props.put("response.timeout.millis", 60000);
            props.put("ack.type", WanAcknowledgeType.ACK_ON_OPERATION_COMPLETE.toString());
            props.put("endpoints",cacheConfig.getWanEndPoints());
            props.put("group.password", "dev-pass");
            props.put("discovery.period", "20");
            props.put("executorThreadCount", "2");
           	
            wanConfig.addWanPublisherConfig(publisherConfig);
            config.addWanReplicationConfig(wanConfig);        	

            WanReplicationRef wRef = new WanReplicationRef();
            wRef.setMergePolicy(PassThroughMergePolicy.class.getName())
            			.setName(cacheConfig.getWanName())
            			.setRepublishingEnabled(false);
            mapConfig.setWanReplicationRef(wRef);
        }
        */	

        if (cacheConfig.getMapStoreLocation() != null && !cacheConfig.getMapStoreLocation().isEmpty()) {
            MapStoreConfig mapStoreConfig = new MapStoreConfig()
                    .setEnabled(true)
                    .setWriteDelaySeconds(cacheConfig.getWriteDelaySeconds())
                    .setClassName(cacheConfig.getMapStoreLocation());
            mapConfig.setMapStoreConfig(mapStoreConfig);
        }

        config.addMapConfig(mapConfig);

        ListConfig listConfig = new ListConfig(cacheConfig.getClusterName()).setBackupCount(cacheConfig.getBackupCount());
        config.addListConfig(listConfig);

        final NetworkConfig network = config.getNetworkConfig();
        final JoinConfig joinConfig = network.getJoin();
        joinConfig.getMulticastConfig().setEnabled(true);
        joinConfig.getMulticastConfig().setMulticastGroup(cacheConfig.getMulticastGroup()).setMulticastPort(cacheConfig.getMulticastPort()).setEnabled(true);

        /* Trying Tcp Ip Connection instead of multi-casting */
        if(cacheConfig.getTcpipMember() != null && cacheConfig.getTcpipMember().length() > 0) {
        	joinConfig.getMulticastConfig().setEnabled(false);
        	joinConfig.getTcpIpConfig().setEnabled(true);
        	Arrays.asList(cacheConfig.getTcpipMember().split(",")).forEach(ip -> joinConfig.getTcpIpConfig().addMember(ip));
        }


        /* Fixing the network interface */
        if(cacheConfig.getInterfaces() != null && cacheConfig.getInterfaces().length() > 0){
        	network.getInterfaces().setEnabled(true);
        	Arrays.asList(cacheConfig.getInterfaces().split(",")).forEach(ip -> network.getInterfaces().addInterface(ip));
        }
        
        config.addListenerConfig(
        		new ListenerConfig( "com.fdc.ss.portal.cache.MergeLifecycleListener" ) );
        config.setProperty("hazelcast.logging.type", "log4j");
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        return instance;
    }
	
}
