package com.akka.config.ha.etcd;/* 
    create qiangzhiwei time 2023/2/5
 */

import com.akka.config.ha.listener.DataListener;
import com.akka.tools.api.LifeCycle;
import io.etcd.jetcd.*;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import io.grpc.stub.StreamObserver;
import javafx.util.Pair;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;


public class EtcdClient implements LifeCycle {

    private ClientBuilder clientBuilder;

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
        this.clientBuilder = Client.builder().endpoints(config.getEndpoints().split(","));
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
        }
    }



    public void watch(String key, DataListener listener) {
        this.watchClient.watch(createByteSequence(key), new Watch.Listener() {

            @Override
            public void onNext(WatchResponse response) {
                final List<WatchEvent> events = response.getEvents();

            }

            @Override
            public void onError(Throwable throwable) {
                listener.onException(throwable);
            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public void put(String key, String value) throws ExecutionException, InterruptedException {
        this.kvClient.put(createByteSequence(key), createByteSequence(value)).get();
    }

    public Pair<String, String> get(String key) throws ExecutionException, InterruptedException {
        if (kvClient != null) {
            final GetOption getOption = GetOption.newBuilder().isPrefix(false).build();
            final List<KeyValue> kvs = this.kvClient.get(createByteSequence(key), getOption).get().getKvs();
            return new Pair<>(kvs.get(0).getKey().toString(), kvs.get(0).getValue().toString());
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
        return this.electionClient.campaign(createByteSequence(key), leaseId, createByteSequence(member))
                .get()
                .getLeader()
                .getName()
                .toString();
    }

    private void checkOrCreateLeaseId() throws ExecutionException, InterruptedException {
        if (leaseId == null && leaseClient != null) {
            final CompletableFuture<LeaseGrantResponse> grantFuture = this.leaseClient.grant(config.getLeaseLiveTimeout(),
                    config.getCreateLeaseIdTimeout(), TimeUnit.MILLISECONDS);
            leaseId = grantFuture.get().getID();
            return;
        }

        if (leaseId == null) {
            throw new RuntimeException("create leaseId failed, campaign leader failed");
        }

    }


    private ByteSequence createByteSequence(String value) {
        return ByteSequence.from(value, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        EtcdClient etcdClient = new EtcdClient(new EtcdConfig());
        etcdClient.start();

        new Thread(() -> {

            try {
                final long id = etcdClient.leaseClient.grant(10).get().getID();
                etcdClient.leaseClient.keepAlive(id, new StreamObserver<LeaseKeepAliveResponse>() {
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
                final ByteSequence from = ByteSequence.from("/root/akka/leader", StandardCharsets.UTF_8);
                ByteSequence proposal = ByteSequence.from("member01", StandardCharsets.UTF_8);
                final String string = etcdClient.electionClient.campaign(from, id, proposal).get().getLeader().getKey().toString();
                System.out.println("11: " + string);
                TimeUnit.SECONDS.sleep(30);
                System.out.println("leader1 end");
                etcdClient.leaseClient.revoke(id);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }).start();

        new Scanner(System.in).nextLine();

        new Thread(() -> {

            try {
                final long id = etcdClient.leaseClient.grant(10).get().getID();
                etcdClient.leaseClient.keepAlive(id, new StreamObserver<LeaseKeepAliveResponse>() {
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
                final ByteSequence from = ByteSequence.from("/root/akka/leader", StandardCharsets.UTF_8);
                ByteSequence proposal = ByteSequence.from("member02", StandardCharsets.UTF_8);
                final String string = etcdClient.electionClient.campaign(from, id, proposal).get().getLeader().getKey().toString();
                System.out.println("22: " + string);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

        }).start();

        new CountDownLatch(1).await();
    }
}
