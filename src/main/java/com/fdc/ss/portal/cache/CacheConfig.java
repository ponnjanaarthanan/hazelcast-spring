package com.fdc.ss.portal.cache;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:cache.properties")
@ConfigurationProperties(prefix="cache")
public class CacheConfig {
	private int networkConfigPort;
	private String multicastGroup;
	private int multicastPort;
	private String clusterName;
	private int backupCount;
	private String mapStoreLocation;
	private String mapStoreName = "*";
	private int writeDelaySeconds = 1;
	private String machines;

	private int timeToLiveInSeconds;
	private int timeToIdleInSeconds;

	private String tcpipMember;
	
	private String wanName;
	private String wanReplicationClass;
	private String wanEndPoints;


	private String interfaces;

	public int getNetworkConfigPort() {
		return networkConfigPort;
	}

	public void setNetworkConfigPort(int networkConfigPort) {
		this.networkConfigPort = networkConfigPort;
	}

	public String getMulticastGroup() {
		return multicastGroup;
	}

	public void setMulticastGroup(String multicastGroup) {
		this.multicastGroup = multicastGroup;
	}

	public int getMulticastPort() {
		return multicastPort;
	}

	public void setMulticastPort(int multicastPort) {
		this.multicastPort = multicastPort;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public int getBackupCount() {
		return backupCount;
	}

	public void setBackupCount(int backupCount) {
		this.backupCount = backupCount;
	}

	public String getMapStoreLocation() {
		return mapStoreLocation;
	}

	public void setMapStoreLocation(String mapStoreLocation) {
		this.mapStoreLocation = mapStoreLocation;
	}

	public String getMapStoreName() {
		return mapStoreName;
	}

	public void setMapStoreName(String mapStoreName) {
		this.mapStoreName = mapStoreName;
	}

	public int getWriteDelaySeconds() {
		return writeDelaySeconds;
	}

	public void setWriteDelaySeconds(int writeDelaySeconds) {
		this.writeDelaySeconds = writeDelaySeconds;
	}

	public String getMachines() {
		return machines;
	}

	public void setMachines(String machines) {
		this.machines = machines;
	}

	public String getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(String interfaces) {
		this.interfaces = interfaces;
	}

	public int getTimeToLiveInSeconds() {
		return timeToLiveInSeconds;
	}

	public void setTimeToLiveInSeconds(int timeToLiveInSeconds) {
		this.timeToLiveInSeconds = timeToLiveInSeconds;
	}

	public int getTimeToIdleInSeconds() {
		return timeToIdleInSeconds;
	}

	public void setTimeToIdleInSeconds(int timeToIdleInSeconds) {
		this.timeToIdleInSeconds = timeToIdleInSeconds;
	}

	public String getTcpipMember() {
		return tcpipMember;
	}

	public void setTcpipMember(String tcpipMember) {
		this.tcpipMember = tcpipMember;
	}

	public String getWanName() {
		return wanName;
	}

	public void setWanName(String wanName) {
		this.wanName = wanName;
	}

	public String getWanReplicationClass() {
		return wanReplicationClass;
	}

	public void setWanReplicationClass(String wanReplicationClass) {
		this.wanReplicationClass = wanReplicationClass;
	}

	public String getWanEndPoints() {
		return wanEndPoints;
	}

	public void setWanEndPoints(String wanEndPoints) {
		this.wanEndPoints = wanEndPoints;
	}

}
