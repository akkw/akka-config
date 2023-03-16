package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/16
 */

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class Once {

    private AtomicBoolean down;

    private Once(AtomicBoolean once) {
        this.down = once;
    }

    private Once() {
    }

    public static Once create() {
        return new Once(new AtomicBoolean(false));
    }

    public synchronized boolean isDown() {
        return down.get();
    }


    public synchronized void down() {
        this.down.compareAndSet(false, true);
    }
}
