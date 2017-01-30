package com.openlibin.net.ext;

/*
 * every producer and consumer must implement this interface
 * Barrier is just a object lock for the synchronization between producer and consumer
 */
public interface BarrierHolder {
	Object getBarrier();
}
