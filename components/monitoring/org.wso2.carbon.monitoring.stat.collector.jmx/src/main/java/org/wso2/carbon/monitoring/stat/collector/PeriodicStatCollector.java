package org.wso2.carbon.monitoring.stat.collector;

import org.wso2.carbon.monitoring.core.publisher.api.Publisher;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class is a wrapper which executes the DataCollector periodically.
 */
public abstract class PeriodicStatCollector implements Runnable {

	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	private long startupDelay = 20000;
	private long period = 15000;
	private boolean running = false;
	private ScheduledFuture<?> scheduledFuture;

	protected Publisher publisher;

	public void start() {
		if (!running) {
			scheduledFuture = executor.scheduleAtFixedRate(this, startupDelay, period, TimeUnit.MILLISECONDS);
			running = true;
		}
	}

	public void stop() {
		scheduledFuture.cancel(false);
		running = false;
	}

}
