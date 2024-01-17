package com.website.util;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ahl
 * 节流
 * 使用场景：在回调比较频繁的情况下，用户可能只需要在某段时间内处理第一个或最后一个回调即可，例如按钮的快速点击，关键字搜索，点击指定次数等
 * 其中groupKey参数主要是为了标识是否是同个业务场景（比如点击10次才生效的话那么这10次的groupKey就必须保持一致），一般用当前线程id表示,
 * 当使用当前线程id不能满足的话则可以由用户自行设定
 */
public class ThrottleUtil {

    private static ScheduledExecutorService mScheduledExecutorService;
    private static volatile boolean mIsInit = false;

    private static final HashMap<Long, Long> mCacheLastTime = new HashMap<>();
    private static final HashMap<Long, Boolean> mCacheSchedule = new HashMap<>();
    private static final HashMap<Long, CallbackParam> mCacheCallback = new HashMap<>();
    private static final HashMap<Long, Integer> mCacheCount = new HashMap<>();

    private static void init() {
        if (mIsInit) {
            return;
        }
        mScheduledExecutorService = new ScheduledThreadPoolExecutor(1, new ThreadPoolExecutor.DiscardPolicy());
        mIsInit = true;
    }

    /**
     * 用户自定义调度服务
     *
     * @param scheduledExecutorService
     */
    public static void config(ScheduledExecutorService scheduledExecutorService) {
        if (scheduledExecutorService == null) {
            return;
        }
        mScheduledExecutorService = scheduledExecutorService;
    }

    /**
     * 在某段时间内，只在第一次返回true
     * 默认以线程id作为同一组的key,如果不是同一线程的调用此方法，则请使用{@link ThrottleUtil#throttleFirst(long, long)}传入groupKey
     *
     * @param ms 单位毫秒
     * @return 返回true则执行
     */
    public static boolean throttleFirst(long ms) {
        return throttleFirst(ms, Thread.currentThread().getId());
    }

    /**
     * 在某段时间内，只在第一次返回true
     *
     * @param ms       单位毫秒
     * @param groupKey 同一处回调保持groupKey一致即可
     * @return 返回true则执行
     */
    public static synchronized boolean throttleFirst(long ms, long groupKey) {
        if (mCacheLastTime.get(groupKey) == null) {
            mCacheLastTime.put(groupKey, 0L);
        }
        long now = System.currentTimeMillis();
        if (now - mCacheLastTime.get(groupKey) > ms) {
            mCacheLastTime.put(groupKey, now);
            return true;
        }
        return false;
    }

    /**
     * 在指定时间内调用此方法指定次数则返回true
     * 例如，用于在2秒内点击按钮5次则触发事件的业务场景
     *
     * @param count 指定次数
     * @param ms    指定时间，单位毫秒
     * @return
     */
    public static synchronized boolean throttleCount(int count, long ms) {
        return throttleCount(count, ms, Thread.currentThread().getId());
    }

    /**
     * 在指定时间内调用此方法指定次数则返回true
     * 例如，用于在2秒内点击按钮5次则触发事件的业务场景
     *
     * @param count    指定次数
     * @param ms       指定时间，单位毫秒
     * @param groupKey 同一业务的保持groupKey一致即可
     * @return
     */
    public static synchronized boolean throttleCount(int count, long ms, long groupKey) {
        if (mCacheLastTime.get(groupKey) == null) {
            mCacheLastTime.put(groupKey, 0L);
        }
        if (mCacheCount.get(groupKey) == null) {
            mCacheCount.put(groupKey, 0);
        }
        long now = System.currentTimeMillis();
        if (mCacheCount.get(groupKey) == 0) {
            mCacheLastTime.put(groupKey, now);
        }
        if (now - mCacheLastTime.get(groupKey) < ms) {
            int tmp = mCacheCount.get(groupKey);
            tmp++;
            mCacheCount.put(groupKey, tmp);
            if (tmp >= count) {
                mCacheCount.put(groupKey, 0);
                return true;
            }
        } else {
            mCacheCount.put(groupKey, 0);
        }
        return false;
    }

    /**
     * 在某段时间内，只触发第一次
     *
     * @param callback 触发回调
     * @param ms       单位毫秒
     */
    public static void throttleFirst(Runnable callback, long ms) {
        throttleFirst(callback, ms, Thread.currentThread().getId());
    }

    /**
     * 在某段时间内，只触发第一次
     *
     * @param callback 触发回调
     * @param ms       单位毫秒
     */
    public static void throttleFirst(Runnable callback, long ms, long groupKey) {
        if (throttleFirst(ms, groupKey)) {
            callback.run();
        }
    }

    /**
     * 在某段时间内，只触发第一次
     *
     * @param callback 触发回调
     * @param userData 用户自定义数据（可为null），会在回调中原样返回
     * @param ms       单位毫秒
     * @param groupKey 同一处回调保持groupKey一致即可
     * @param <T>
     */
    public static <T> void throttleFirst(ICallback<T> callback, T userData, long ms, long groupKey) {
        if (throttleFirst(ms, groupKey)) {
            callback.callback(userData);
        }
    }

    /**
     * 在某段时间内，只触发最后一次
     *
     * @param callback 触发回调
     * @param ms       单位毫秒
     */
    public static void throttleLast(Runnable callback, long ms) {
        throttleLast(callback, ms, Thread.currentThread().getId());
    }

    /**
     * 在某段时间内，只触发最后一次
     *
     * @param callback 触发回调
     * @param ms       单位毫秒
     * @param groupKey 同一处回调保持groupKey一致即可
     */
    public static synchronized void throttleLast(Runnable callback, long ms, long groupKey) {
        throttleLast(userData -> callback.run(), null, ms, groupKey);
    }

    /**
     * 在某段时间内，只触发最后一次
     *
     * @param callback 触发回调
     * @param userData 用户自定义数据（可为null），会在回调中原样返回
     * @param ms       单位毫秒
     * @param groupKey 同一处回调保持groupKey一致即可
     * @param <T>
     */
    public static synchronized <T> void throttleLast(ICallback<T> callback, T userData, long ms, long groupKey) {
        if (mCacheSchedule.get(groupKey) == null) {
            mCacheSchedule.put(groupKey, false);
        }
        if (!mIsInit) {
            init();
        }
        mCacheCallback.put(groupKey, new CallbackParam(callback, userData));
        if (!mCacheSchedule.get(groupKey)) {
            mCacheSchedule.put(groupKey, true);
            mScheduledExecutorService.schedule(() -> {
                mCacheSchedule.put(groupKey, false);
                CallbackParam cp = mCacheCallback.remove(groupKey);
                if (cp != null && cp.iCallback != null && cp.iCallback.get() instanceof ICallback) {
                    ((ICallback) cp.iCallback.get()).callback(cp.userData);
                }
            }, ms, TimeUnit.MILLISECONDS);
        }
    }


    /**
     * 在某段时间内，只触发第一次和最后一次
     * 默认以线程id为groupKey
     *
     * @param callback 触发回调
     * @param ms       单位毫秒
     */
    public static void throttleFirstAndLast(Runnable callback, long ms) {
        throttleFirstAndLast(callback, ms, Thread.currentThread().getId());
    }

    /**
     * 在某段时间内，只触发第一次和最后一次
     *
     * @param callback 触发的回调
     * @param ms       单位毫秒
     * @param groupKey 同一处回调保持groupKey一致即可
     */
    public static synchronized void throttleFirstAndLast(Runnable callback, long ms, long groupKey) {
        throttleFirstAndLast(userData -> callback.run(), null, ms, groupKey);
    }

    /**
     * 在某段时间内，只触发第一次和最后一次
     *
     * @param callback 触发的回调
     * @param userData 用户自定义数据（可为null），会在回调中原样返回
     * @param ms       单位毫秒
     * @param groupKey 同一处回调保持groupKey一致即可
     * @param <T>
     */
    public static synchronized <T> void throttleFirstAndLast(ICallback<T> callback, T userData, long ms, long groupKey) {
        if (callback == null) {
            return;
        }
        if (mCacheSchedule.get(groupKey) == null) {
            mCacheSchedule.put(groupKey, false);
        }
        if (mCacheCount.get(groupKey) == null) {
            mCacheCount.put(groupKey, 0);
        }
        if (!mIsInit) {
            init();
        }
        if (throttleFirst(ms)) {
            mCacheCount.put(groupKey, 0);
            callback.callback(userData);
        } else {
            mCacheCount.put(groupKey, mCacheCount.get(groupKey) + 1);
        }

        mCacheCallback.put(groupKey, new CallbackParam(callback, userData));
        if (!mCacheSchedule.get(groupKey)) {
            mCacheSchedule.put(groupKey, true);
            mScheduledExecutorService.schedule(() -> {
                mCacheSchedule.put(groupKey, false);
                if (mCacheCount.get(groupKey) != 0) {
                    CallbackParam cp = mCacheCallback.remove(groupKey);
                    if (cp != null && cp.iCallback != null && cp.iCallback.get() instanceof ICallback) {
                        ((ICallback) cp.iCallback.get()).callback(cp.userData);
                    }
                }

            }, ms, TimeUnit.MILLISECONDS);
        }
    }

    public interface ICallback<T> {
        /**
         * 回调
         *
         * @param userData 可为null
         */
        void callback(T userData);
    }

    public static class CallbackParam<T> {
        public WeakReference<ICallback> iCallback;
        public T userData;

        public CallbackParam(ICallback iCallback, T userData) {
            this.iCallback = new WeakReference(iCallback);
            this.userData = userData;
        }
    }
}