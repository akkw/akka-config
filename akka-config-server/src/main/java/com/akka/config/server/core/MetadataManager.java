package com.akka.config.server.core;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.protocol.Metadata;
import com.alibaba.fastjson2.JSON;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MetadataManager {
    private final Map<String, Map<String, Metadata>> metadata = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ReentrantReadWriteLock>> locks = new ConcurrentHashMap<>();

    public Map<String, Metadata> getMetadataMap(String namespace) {
        return metadata.get(namespace);
    }

    public Metadata getMetadata(String namespace, String environment) {
        final Map<String, Metadata> metadataMap = metadata.get(namespace);
        if (metadataMap != null) {
            return metadataMap.get(environment);
        }
        return null;
    }

    public ReentrantReadWriteLock getMetadataLock(String namespace, String environment) {
        final Map<String, ReentrantReadWriteLock> metadataMap = locks.get(namespace);
        if (metadataMap != null) {
            return metadataMap.get(environment);
        }
        return null;
    }

    public void createOrUpdateMetadata(String namespace, String environment, Metadata metadata) {
        final Map<String, ReentrantReadWriteLock> envLockMap = this.locks.computeIfAbsent(namespace, (x) -> {
            return new ConcurrentHashMap<>();
        });

        final Map<String, Metadata> envMetadataMap = this.metadata.computeIfAbsent(namespace, (x) -> {
            return  new ConcurrentHashMap<>();
        });
        envMetadataMap.put(environment, metadata);

        if (!envLockMap.containsKey(environment)) {
            envLockMap.put(environment, new ReentrantReadWriteLock());
        }

    }
}
