package com.sdy.jitangapplication.nim.uikit.impl.cache;

import com.sdy.jitangapplication.nim.uikit.api.UIKitOptions;
import com.sdy.jitangapplication.nim.uikit.common.framework.NimSingleThreadExecutor;
import com.sdy.jitangapplication.nim.uikit.common.util.log.LogUtil;
import com.sdy.jitangapplication.nim.uikit.impl.NimUIKitImpl;

import java.util.List;

/**
 * UIKit缓存数据管理类
 * <p/>
 * Created by huangjun on 2015/10/19.
 */
public class DataCacheManager {

    private static final String TAG = DataCacheManager.class.getSimpleName();

    /**
     * App初始化时向SDK注册数据变更观察者
     */
    public static void observeSDKDataChanged(boolean register) {
        UIKitOptions options = NimUIKitImpl.getOptions();
        if (options.buildNimUserCache) {
            NimUserInfoCache.getInstance().registerObservers(register);
        }


    }

    /**
     * 本地缓存构建(异步)
     */
    public static void buildDataCacheAsync() {
        NimSingleThreadExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                buildDataCache();
                LogUtil.i(TAG, "build data cache completed");
                NimUIKitImpl.notifyCacheBuildComplete();
            }
        });
    }

    /**
     * 本地缓存构建（同步）
     */
    public static void buildDataCache() {
        // clear
        clearDataCache();

        UIKitOptions options = NimUIKitImpl.getOptions();
        // build user/friend/team data cache
        if (options.buildNimUserCache) {
            NimUserInfoCache.getInstance().buildCache();
        }


        // chat room member cache 在进入聊天室之后构建
    }

    /**
     * 清空缓存（同步）
     */
    public static void clearDataCache() {
        UIKitOptions options = NimUIKitImpl.getOptions();

        // clear user/friend/team data cache

        if (options.buildNimUserCache) {
            NimUserInfoCache.getInstance().clear();
        }


    }


    /**
     * 输出缓存数据变更日志
     */
    public static void Log(List<String> accounts, String event, String logTag) {
        StringBuilder sb = new StringBuilder();
        sb.append(event);
        sb.append(" : ");
        for (String account : accounts) {
            sb.append(account);
            sb.append(" ");
        }
        sb.append(", total size=" + accounts.size());

        LogUtil.i(logTag, sb.toString());
    }
}
