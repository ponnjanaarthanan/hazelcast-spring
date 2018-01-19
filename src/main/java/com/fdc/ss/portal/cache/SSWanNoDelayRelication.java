package com.fdc.ss.portal.cache;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hazelcast.cluster.impl.operations.AuthorizationOperation;
import com.hazelcast.instance.Node;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.Connection;
import com.hazelcast.nio.ConnectionManager;
import com.hazelcast.nio.Packet;
import com.hazelcast.nio.serialization.Data;
import com.hazelcast.spi.InvocationBuilder;
import com.hazelcast.spi.Operation;
import com.hazelcast.spi.OperationService;
import com.hazelcast.util.AddressUtil;
import com.hazelcast.util.AddressUtil.AddressHolder;
import com.hazelcast.wan.ReplicationEventObject;
import com.hazelcast.wan.WanReplicationEndpoint;
import com.hazelcast.wan.WanReplicationEvent;
import com.hazelcast.wan.WanReplicationService;

public class SSWanNoDelayRelication implements Runnable, WanReplicationEndpoint {

    private static final int RETRY_CONNECTION_MAX = 10;
    private static final int RETRY_CONNECTION_SLEEP_MILLIS = 1000;
    private static final Log LOG = LogFactory.getLog(SSWanNoDelayRelication.class);

    private Node node;
    private String groupName;
    private String password;
    private final LinkedBlockingQueue<String> addressQueue = new LinkedBlockingQueue<String>();
    private final LinkedList<WanReplicationEvent> failureQ = new LinkedList<WanReplicationEvent>();
    private final BlockingQueue<WanReplicationEvent> eventQueue = new ArrayBlockingQueue<WanReplicationEvent>(100000);
    private volatile boolean running = true;

    public void init(Node node, String groupName, String password, String... targets) {
        this.node = node;       
        this.groupName = groupName;
        this.password = password;
        addressQueue.addAll(Arrays.asList(targets));
        node.nodeEngine.getExecutionService().execute("hz:wan", this);
    }

    @Override
    public void publishReplicationEvent(String serviceName, ReplicationEventObject eventObject) {
        WanReplicationEvent replicationEvent = new WanReplicationEvent(serviceName, eventObject);

        
        LOG.info("Wan Publish Event : " + serviceName );
        //if the replication event is published, we are done.
        if (eventQueue.offer(replicationEvent)) {
            return;
        }

        //the replication event could not be published because the eventQueue is full. So we are going
        //to drain one item and then offer it again.
        //todo: isn't it dangerous to drop a ReplicationEvent?
        eventQueue.poll();

        if (!eventQueue.offer(replicationEvent)) {
            LOG.warn("Could not publish replication event: " + replicationEvent);
        }
    }

    public void shutdown() {
        running = false;
    }

    public void run() {
        Connection conn = null;
        while (running) {
            try {
                WanReplicationEvent event = (failureQ.size() > 0) ? failureQ.removeFirst() : eventQueue.take();
                if (conn == null) {
                    conn = getConnection();
                    if (conn != null) {
                        conn = authorizeConnection(conn);
                    }
                }
                if (conn != null && conn.isAlive()) {
                	LOG.info("TCP Connection is not null");
                    Data data = node.nodeEngine.getSerializationService().toData(event);
                    Packet packet = new Packet(data);
                    packet.setHeader(Packet.HEADER_WAN_REPLICATION);
                    node.nodeEngine.getPacketTransceiver().transmit(packet, conn);
                    LOG.info("Wan Replication packet succesfully sent ");
                } else {
                	LOG.info("TCP Connection is null or not alive");
                    failureQ.addFirst(event);
                    conn = null;
                }
            } catch (InterruptedException e) {
                running = false;
            } catch (Throwable e) {
                if (LOG != null) {
                    LOG.warn(e);
                }
                conn = null;
            }
        }
    }

    @SuppressWarnings("BusyWait")
    Connection getConnection()
            throws InterruptedException {
        final int defaultPort = node.getConfig().getNetworkConfig().getPort();
        while (running) {
            String targetStr = addressQueue.take();
            try {
                final AddressHolder addressHolder = AddressUtil.getAddressHolder(targetStr, defaultPort);
                final Address target = new Address(addressHolder.getAddress(), addressHolder.getPort());
                final ConnectionManager connectionManager = node.getConnectionManager();
                Connection conn = connectionManager.getOrConnect(target);
                for (int i = 0; i < RETRY_CONNECTION_MAX; i++) {
                    if (conn == null) {
                        Thread.sleep(RETRY_CONNECTION_SLEEP_MILLIS);
                    } else {
                        return conn;
                    }
                    conn = connectionManager.getConnection(target);
                }
            } catch (Throwable e) {
                Thread.sleep(RETRY_CONNECTION_SLEEP_MILLIS);
            } finally {
                addressQueue.offer(targetStr);
            }
        }
        return null;
    }

    public boolean checkAuthorization(String groupName, String groupPassword, Address target) {
        Operation authorizationCall = new AuthorizationOperation(groupName, groupPassword);
        OperationService operationService = node.nodeEngine.getOperationService();
        String serviceName = WanReplicationService.SERVICE_NAME;
        InvocationBuilder invocationBuilder = operationService.createInvocationBuilder(serviceName, authorizationCall, target);
        Future<Boolean> future = invocationBuilder.setTryCount(1).invoke();
        try {
            return future.get();
        } catch (Exception ignored) {
            LOG.debug(ignored);
        }
        return false;
    }

    private Connection authorizeConnection(Connection conn) {
        boolean authorized = checkAuthorization(groupName, password, conn.getEndPoint());
        if (!authorized) {
            conn.close();
            if (LOG != null) {
                LOG.error("Invalid groupName or groupPassword! ");
            }
            return null;
        }
        return conn;
    }
}
