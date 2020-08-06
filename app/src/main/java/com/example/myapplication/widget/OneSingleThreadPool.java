package com.example.myapplication.widget;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OneSingleThreadPool {
    private Handler uiHandler;
    private ExecutorService threadPool;

    public OneSingleThreadPool() {
        this.threadPool = Executors.newSingleThreadExecutor();
//                new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue());
        this.uiHandler = new Handler(Looper.getMainLooper());
    }

    public void startLogin(final Callback callback) {
        if (threadPool == null) {
            return;
        }
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                final Thread thread = Thread.currentThread();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        new MyThread(new Callback() {
                            @Override
                            public Object getParams(Object o) {
                                return null;
                            }

                            @Override
                            public void onSuccess(Object o) {
                                if (!thread.isAlive()) {
                                    return;
                                }
                                callback.onSuccess(thread.getState()+"-》"+thread.isAlive());
                                synchronized (thread) {
                                    thread.notify();
                                }
                            }

                            @Override
                            public void onFailed(Object o) {
                                if (!thread.isAlive()) {
                                    return;
                                }
                                callback.onFailed(o);
                                synchronized (thread) {
                                    thread.notify();
                                }
                            }

                            @Override
                            public void onStart() {
                                callback.onStart();
                            }

                            @Override
                            public void onInterrupt(String s) {
                                callback.onInterrupt(s);
                            }
                        }).start();
                    }
                });

                synchronized (thread) {
                    try {
                        thread.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        uiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onInterrupt("线程池线程Thread 中断");
                            }
                        });
                    }
                }
            }
        });
    }

    public void stopAll() {
        if (threadPool == null) {
            return;
        }
        threadPool.shutdownNow();
    }

    public interface Callback<P, T, K, PR, TR, KR> {
        PR getParams(P p);

        void onSuccess(T t);

        void onFailed(K k);

        void onStart();

        void onInterrupt(String s);
    }

    public class MyThread extends Thread {
        Callback callback;


        public MyThread(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            super.run();
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onStart();
                }
            });
            try {
                sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onInterrupt("MyThread 中断");
                    }
                });
            }
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(1);
                }
            });

        }
    }

}
