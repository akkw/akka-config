package com.akka.config.ha.etcd;

/*
    create qiangzhiwei time 2023/2/5
 */

import com.akka.config.ha.listener.DataListener;
import com.akka.config.ha.protocol.EtcdEvent;
import com.akka.tools.api.LifeCycle;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.Election;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.api.Compare;
import io.etcd.jetcd.election.NoLeaderException;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import io.grpc.stub.StreamObserver;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EtcdClient implements LifeCycle {

    private static final Logger logger = LoggerFactory.getLogger(EtcdClient.class);
    private final ClientBuilder clientBuilder;

    private Client client;

    private final EtcdConfig config;

    private KV kvClient;

    private Lock lockClient;

    private Watch watchClient;

    private Lease leaseClient;

    private Election electionClient;

    private Long leaseId;

    private ExecutorService publishEventExecutor;

    public EtcdClient(EtcdConfig config) {
        this.config = config;
        this.clientBuilder = Client.builder()
                .endpoints(config.getEndpoints().split(","))
                .user(createByteSequence(config.getUsername()))
                .password(createByteSequence(config.getPassword()))
                .waitForReady(false);
    }


    @Override
    public void start() {
        this.client = clientBuilder.build();
        this.kvClient = client.getKVClient();
        this.lockClient = client.getLockClient();
        this.watchClient = client.getWatchClient();
        this.leaseClient = client.getLeaseClient();
        this.electionClient = client.getElectionClient();
    }

    @Override
    public void stop() {
        if (leaseId != null) {
            leaseClient.revoke(leaseId);
            leaseClient.close();
        }
        if (kvClient != null) {
            kvClient.close();
        }
        if (lockClient != null) {
            lockClient.close();
        }
        if (watchClient != null) {
            watchClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    public String lock(String key, long timeoutMs) throws ExecutionException, InterruptedException, TimeoutException {

        if (key == null || "".equals(key)) {
            throw new NullPointerException();
        }

        if (lockClient == null) {
            throw new NullPointerException();
        }
        checkOrCreateLeaseId();
        ByteSequence lockKey = createByteSequence(key);
        return lockClient.lock(lockKey, leaseId).get(timeoutMs, TimeUnit.MILLISECONDS).getKey().toString();
    }
    public String lock(String key) throws ExecutionException, InterruptedException {

        if (key == null || "".equals(key)) {
            throw new NullPointerException();
        }

        if (lockClient == null) {
            throw new NullPointerException();
        }
        checkOrCreateLeaseId();
        ByteSequence lockKey = createByteSequence(key);
        return lockClient.lock(lockKey, leaseId).get().getKey().toString();
    }

    public String unlock(String key) throws ExecutionException, InterruptedException, TimeoutException {

        if (key == null || "".equals(key)) {
            throw new NullPointerException();
        }

        if (lockClient == null) {
            throw new NullPointerException();
        }

        ByteSequence lockKey = createByteSequence(key);
        return lockClient.unlock(lockKey).get(getConfig().getTimeoutMs(), TimeUnit.MILLISECONDS).toString();
    }


    public void watch(String key, DataListener listener, ExecutorService publishEventExecutor) {

        if (key == null || listener == null) {
            throw new NullPointerException();
        }
        Watch.Listener watchListener = new Watch.Listener() {
            @Override
            public void onNext(WatchResponse response) {

                final List<WatchEvent> events = response.getEvents();
                for (WatchEvent we : events) {
                    Runnable publishEventRunnable = publishEventRunnable(we, listener);
                    if (publishEventRunnable == null) {
                        logger.error("EtcdClient.watch onNext event continue, event: {}", we);
                        continue;
                    }

                    ExecutorService thisPublishEventExecutor = publishEventExecutor;
                    if (publishEventExecutor == null) {
                        thisPublishEventExecutor = EtcdClient.this.publishEventExecutor;
                    }
                    thisPublishEventExecutor.execute(publishEventRunnable);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                listener.onException(throwable);
            }

            @Override
            public void onCompleted() {

            }
        };

        this.watchClient.watch(createByteSequence(key), watchListener);
    }

    public void watch(String key, DataListener listener) {

        if (key == null || listener == null) {
            throw new NullPointerException();
        }


        Watch.Listener watchListener = new Watch.Listener() {
            @Override
            public void onNext(WatchResponse response) {

                final List<WatchEvent> events = response.getEvents();
                for (WatchEvent we : events) {
                    Runnable publishEventRunnable = publishEventRunnable(we, listener);

                    if (publishEventRunnable == null) {
                        logger.error("EtcdClient.watch onNext event continue, event: {}", we);
                        continue;
                    }

                    EtcdClient.this.publishEventExecutor.execute(publishEventRunnable);
                }

            }

            @Override
            public void onError(Throwable throwable) {
                listener.onException(throwable);
            }

            @Override
            public void onCompleted() {

            }
        };

        this.watchClient.watch(createByteSequence(key), watchListener);
    }

    private Runnable publishEventRunnable(WatchEvent watchEvent, DataListener listener) {
        if (watchEvent == null || listener == null) {
            return null;
        }

        final EtcdEvent etcdEvent;
        if (watchEvent.getEventType() != WatchEvent.EventType.PUT) {
            if (watchEvent.getEventType() == WatchEvent.EventType.DELETE) {
                etcdEvent = new EtcdEvent(watchEvent.getKeyValue().toString(), watchEvent.getKeyValue().toString(), EtcdEvent.EtcdEventType.DELETE);
            } else {
                etcdEvent = new EtcdEvent(watchEvent.getKeyValue().toString(), watchEvent.getKeyValue().toString(), EtcdEvent.EtcdEventType.UNRECOGNIZED);
            }
        } else {
            if (watchEvent.getPrevKV() != null) {
                etcdEvent = new EtcdEvent(watchEvent.getKeyValue().toString(), watchEvent.getKeyValue().toString(), EtcdEvent.EtcdEventType.UPDATE);
            } else {
                etcdEvent = new EtcdEvent(watchEvent.getKeyValue().toString(), watchEvent.getKeyValue().toString(), EtcdEvent.EtcdEventType.CREATE);
            }
        }
        return new Runnable() {
            @Override
            public void run() {
                listener.onEvent(etcdEvent);
            }
        };
    }

    public void put(String key, String value) throws ExecutionException, InterruptedException, TimeoutException {
        this.kvClient.put(createByteSequence(key), createByteSequence(value)).get(getConfig().getTimeoutMs(), TimeUnit.MILLISECONDS);
    }

    public void del(String key) throws ExecutionException, InterruptedException, TimeoutException {
        this.kvClient.delete(createByteSequence(key)).get(config.getTimeoutMs(), TimeUnit.MILLISECONDS);
    }

    public boolean putIfAbsent(String key, String value) throws ExecutionException, InterruptedException {
        final TxnResponse txnResponse = this.kvClient.txn()
                .If(new Cmp(createByteSequence(key), Cmp.Op.EQUAL, new CmpTarget<Long>(Compare.CompareTarget.VERSION, 0L) {
                    @Override
                    public Compare.CompareTarget getTarget() {
                        return Compare.CompareTarget.VERSION;
                    }
                }))
                .Then(Op.PutOp.put(createByteSequence(key), createByteSequence(value), PutOption.DEFAULT))
                .Else()
                .commit().get();
        return txnResponse.isSucceeded();
    }

    public Pair<String, String> get(String key) throws ExecutionException, InterruptedException {
        if (kvClient != null) {
            final GetOption getOption = GetOption.newBuilder().isPrefix(false).build();
            final List<KeyValue> kvs = this.kvClient.get(createByteSequence(key), getOption).get().getKvs();
            if (kvs != null && kvs.size() != 0) {
                return new Pair<>(kvs.get(0).getKey().toString(), kvs.get(0).getValue().toString());
            }
        }
        return null;
    }

    public List<Pair<String, String>> getPrefix(String key) throws ExecutionException, InterruptedException {
        if (kvClient != null) {
            final GetOption getOption = GetOption.newBuilder().isPrefix(true).build();
            final List<KeyValue> getKvs = this.kvClient.get(createByteSequence(key), getOption)
                    .get()
                    .getKvs();

            List<Pair<String, String>> kvsResult = new ArrayList<>();
            for (KeyValue kv : getKvs) {
                kvsResult.add(new Pair<>(kv.getKey().toString(), kv.getValue().toString()));
            }
            return kvsResult;
        }
        return null;
    }

    public String leader(String key, String member) throws ExecutionException, InterruptedException {
        checkOrCreateLeaseId();
        final String leaderKey = this.electionClient.campaign(createByteSequence(key), leaseId, createByteSequence(member))
                .get()
                .getLeader()
                .getName()
                .toString();
        this.leaseClient.keepAlive(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {
            @Override
            public void onNext(LeaseKeepAliveResponse value) {

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
        return leaderKey;
    }


    public String leaderTimeout(String key, String member, long timeMilli) throws ExecutionException, InterruptedException, TimeoutException {
        checkOrCreateLeaseId();
        return this.electionClient.campaign(createByteSequence(key), leaseId, createByteSequence(member))
                .get(timeMilli, TimeUnit.MILLISECONDS)
                .getLeader()
                .getName()
                .toString();
    }

    private void checkOrCreateLeaseId() throws ExecutionException, InterruptedException {
        if (leaseId == null && leaseClient != null) {
            final CompletableFuture<LeaseGrantResponse> grantFuture = this.leaseClient.grant(config.getLeaseLiveTimeout(),
                    config.getCreateLeaseIdTimeout(), TimeUnit.MILLISECONDS);
            leaseId = grantFuture.get().getID();
            leaseClient.keepAlive(leaseId, new StreamObserver<LeaseKeepAliveResponse>() {
                long intervalTime = 30 * 10;
                long lastPrintTime = System.currentTimeMillis();
                @Override
                public void onNext(LeaseKeepAliveResponse value) {
                    final long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastPrintTime > intervalTime) {
                        logger.info("leaseId keepAlive, leaseId: {}, ttl: {}", value.getID(), value.getTTL());
                    }
                    lastPrintTime = currentTimeMillis;
                }

                @Override
                public void onError(Throwable t) {
                    leaseClient.revoke(leaseId);
                    leaseId = null;
                    logger.error("leaseId keepAlive failed", t);
                }

                @Override
                public void onCompleted() {
                    leaseClient.revoke(leaseId);
                    logger.info("leaseId Completed: {}", Long.toHexString(leaseId));
                    leaseId = null;
                }
            });
            return;
        }

        if (leaseId == null) {
            throw new RuntimeException("create leaseId failed, campaign leader failed");
        }

    }

    public boolean checkLeader(String key) {
        boolean existLeader = false;
        try {
            final String leaderKey = electionClient.leader(createByteSequence(key)).get().getKv().getKey().toString();

            if (leaderKey != null) {
                existLeader = true;
            }
        } catch (Exception e) {
            if (e.getCause() instanceof NoLeaderException) {
                logger.error("the [{}] does not exist as a leader", key);
            }
        }
        return existLeader;
    }


    private ByteSequence createByteSequence(String value) {
        return ByteSequence.from(value, StandardCharsets.UTF_8);
    }

    public EtcdConfig getConfig() {
        return config;
    }
}
