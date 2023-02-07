package com.akka.config.ha.protocol;

import io.etcd.jetcd.watch.WatchEvent;

public class EtcdEvent {
    private final String key;

    private final String value;

    private final EtcdEventType etcdEventType;


    public EtcdEvent(String key, String value) {
        this(key, value, null);
    }

    public EtcdEvent(String key, String value, EtcdEventType tcdEventType) {
        this.key = key;
        this.value = value;
        this.etcdEventType = tcdEventType;
    }


    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public EtcdEventType getEtcdEventType() {
        return etcdEventType;
    }



    public enum EtcdEventType {
        CREATE(),
        UPDATE(),
        DELETE(),
        UNRECOGNIZED();



        public EtcdEventType valueOf(WatchEvent.EventType eventType) {
            switch (eventType) {
                case PUT:
                    return CREATE;
                case DELETE:
                    return DELETE;
                case UNRECOGNIZED:
                    return UNRECOGNIZED;
                default:
                    return null;
            }
        }
    }

}
