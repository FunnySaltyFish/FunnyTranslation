package com.funny.translation.translation;

import com.funny.translation.bean.Consts;
import com.funny.translation.js.TranslationCustom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewTranslationHelper {
    private static NewTranslationHelper instance;
    private final ExecutorService mThreadPool = Executors.newFixedThreadPool(8);
    private final HashMap<Short, Long> mLastTranslate = new HashMap<>();
    private PoolingThread mThread;
    private final LinkedList<BasicTranslationTask> mTranslateList = new LinkedList<>();
    private OnTranslateListener listener;
    private short mode = Consts.MODE_NORMAL;//翻译模式


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

    public void initTasks(ArrayList<BasicTranslationTask> translationTasks) {
        if (!mTranslateList.isEmpty())
            mTranslateList.clear();
        mTranslateList.addAll(translationTasks);

        mLastTranslate.clear();
        for (BasicTranslationTask translationTask : translationTasks) {
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

    public void finish(){
        mThreadFlag = false;
    }

    public void stopTasks(){
        mTranslateList.clear();
    }

    public int getProgress(){
        return mProgress * 100 / mTotalProgress ;
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
                        BasicTranslationTask currentTask = mTranslateList.removeFirst();
                        short type = currentTask.getEngineKind();
                        if (currentTime() - mLastTranslate.get(type) >= getSleepTime(currentTask)) {
                            mThreadPool.execute(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        translate(currentTask);
                                        listener.finishOne(currentTask,null);
                                    }catch(Exception e){
                                        listener.finishOne(currentTask,e);
                                    }finally {
                                        mProgress++;
                                        if(mProgress == mTotalProgress)listener.finishAll();
                                    }
                                }
                            });
                            mLastTranslate.put(type, currentTime());
                        } else {
                            mTranslateList.addLast(currentTask);
                        }
                    }

                    // 按需休眠
                    long sleepTime = mTranslateList.isEmpty() ? 100L : 10L;
                    sleep(sleepTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void translate(BasicTranslationTask task) throws TranslationException {
            if (task instanceof TranslationCustom) {
                TranslationCustom custom = (TranslationCustom) task;
                custom.translate(mode);
            } else task.translate(mode);
        }

        private Long currentTime() {
            return System.currentTimeMillis();
        }

        private long getSleepTime(BasicTranslationTask task){
            if(task.isOffline())return 1;
            else return task.getEngineKind() == Consts.ENGINE_BAIDU_NORMAL ? Consts.BAIDU_SLEEP_TIME : 50;
        }
    }
}
