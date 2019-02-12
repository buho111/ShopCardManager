package com.doctormiyabi.shopcardmanager.model.usecase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TaskQueue {
    // A managed pool of threads
    private final ThreadPoolExecutor mTaskThreadPool;

    // Gets the number of available cores
    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    // Sets the amount of time an idle thread waits before terminating
    private static final int KEEP_ALIVE_TIME = 1;
    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    // A queue of Runnables for the pool
    private final BlockingQueue<Runnable> mPoolWorkQueue;

    private static TaskQueue sInstance = null;
    static {
        sInstance = new TaskQueue();
    }

    private TaskQueue() {

        //LogUtils.th(this.getClass(), "Number of Cores => " + NUMBER_OF_CORES);

        mPoolWorkQueue = new LinkedBlockingQueue<Runnable>();

        // Creates a thread pool manager
        mTaskThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES,
                NUMBER_OF_CORES,
                KEEP_ALIVE_TIME,
                KEEP_ALIVE_TIME_UNIT,
                mPoolWorkQueue);
    }

    /**
     * Returns the TaskQueue object
     * @return The global TaskQueue object
     */
    public static TaskQueue getInstance() {
        return sInstance;
    }

    public void addTask(Runnable r) {
        sInstance.mTaskThreadPool.execute(r);
    }
}
