package com.funny.translation.translation;

import android.util.Log;

import com.funny.translation.bean.Consts;
import com.funny.translation.js.core.JsTranslateTask;
import com.funny.translation.trans.CoreTranslationTask;
import com.funny.translation.trans.TranslationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewTranslationHelper {
    private static NewTranslationHelper instance;
    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(4);
    private final HashMap<Short, Long> mLastTranslate = new HashMap<>();
    private PoolingThread mThread;
    private final LinkedList<CoreTranslationTask> mTranslateList = new LinkedList<>();
    private OnTranslateListener listener;
    private short mode = Consts.MODE_NORMAL;//翻译模式

    private final Object lock = new Object();


    private boolean mThreadFlag = true;
    private int mProgress = 0;
    private int mTotalProgress = 0;

    private NewTranslationHelper() {

    }

    public static NewTranslationHelper getInstance() {
        if (instance == null) {
            synchronized (NewTranslationHelper.class) {
                if (instance == null)
                    instance = new NewTranslationHelper();
            }
        }
        return instance;
    }

    public void setOnTranslateListener(OnTranslateListener listener) {
        this.listener = listener;
    }

    public void initTasks(ArrayList<CoreTranslationTask> translationTasks) {
        if (!mTranslateList.isEmpty())
            mTranslateList.clear();
        mTranslateList.addAll(translationTasks);

        mLastTranslate.clear();
        for (CoreTranslationTask translationTask : translationTasks) {
            short type = translationTask.getEngineKind();
            if (!mLastTranslate.containsKey(type))
                mLastTranslate.put(type, 0L);
        }

        mTotalProgress = translationTasks.size();
        mProgress = 0;

        if (mThread == null) {
            mThread = new PoolingThread();
            mThread.start();
        }


    }

    public void setMode(short mode) {
        this.mode = mode;
    }

    public void finish() {
        mThreadFlag = false;
        mThreadPool.shutdown();
    }

    public void stopTasks() {
        mTranslateList.clear();
    }

    public int getProgress() {
        return mProgress * 100 / mTotalProgress;
    }

    public boolean isTranslating() {
        return mTranslateList.size() > 0;
    }

    /**
     * 轮询线程
     */
    private class PoolingThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                while (mThreadFlag) {// 永不停息
                    if (!mTranslateList.isEmpty()) {
                        CoreTranslationTask currentTask = getATask();
                        short type = currentTask.getEngineKind();
                        if (currentTime() - mLastTranslate.get(type) >= getSleepTime(currentTask)) {
                            mThreadPool.execute(() -> {
                                try {
                                    //Log.d("NewTH", "run: beforeTrans:"+currentTask.result);
                                    translate(currentTask);
                                    Log.d("NewTH", "run: afterTrans:"+currentTask.getResult());
                                    listener.finishOne(currentTask, null);
                                } catch (TranslationException e) {
                                    currentTask.getResult().setBasicResult(e.getMessage());
                                    listener.finishOne(currentTask, e);
                                } catch (Exception e) {
                                    currentTask.getResult().setBasicResult(e.getLocalizedMessage() == null ? "" : e.getLocalizedMessage());
                                    listener.finishOne(currentTask, e);
                                } finally {
                                    mProgress++;
                                    if (mProgress == mTotalProgress) listener.finishAll();
                                }
                            });

                            synchronized (lock) {
                                mLastTranslate.put(type, currentTime());
                            }
                        } else {
                            synchronized (lock) {
                                mTranslateList.addLast(currentTask);
                            }
                        }
                    }

                    // 按需休眠
                    long sleepTime = mTranslateList.isEmpty() ? 200L : 50L;
                    sleep(sleepTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void translate(CoreTranslationTask task) throws TranslationException {
            if (task instanceof JsTranslateTask) {
                JsTranslateTask jsTranslateTask = (JsTranslateTask) task;
                jsTranslateTask.translate(mode);
            } else task.translate(mode);
        }

        private Long currentTime() {
            return System.currentTimeMillis();
        }

        private CoreTranslationTask getATask(){
            synchronized (lock){
                return mTranslateList.removeFirst();
            }
        }

        private long getSleepTime(CoreTranslationTask task) {
            if (task.isOffline()) return 1;
            else
                return task.getEngineKind() == Consts.ENGINE_BAIDU_NORMAL ? Consts.BAIDU_SLEEP_TIME : 50;
        }
    }


}
