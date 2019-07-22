package com.example.demoapplication.widgets.swipecard;

import android.content.Context;
import android.util.TypedValue;

/**
 * 介绍：一些配置
 * 界面最多显示几个View
 * 每一级View之间的Scale差异、translationY等等
 * <p>
 * <p>
 * 作者：zhangxutong
 * 邮箱：mcxtzhang@163.com
 * 主页：http://blog.csdn.net/zxt0601
 * 时间： 16/12/18.
 */

public class CardConfig {
    //屏幕上最多同时显示几个Item
    public static int MAX_SHOW_COUNT;

    //每一级Scale相差0.05f，translationY相差7dp左右
    public static float SCALE_GAP;
    public static int TRANS_Y_GAP;

    /**
     * 卡片滑动时不偏左也不偏右
     */
    public static final int SWIPING_NONE = 1;
    /**
     * 卡片向左滑动时
     */
    public static final int SWIPING_LEFT = 1 << 2;
    /**
     * 卡片向右滑动时
     */
    public static final int SWIPING_RIGHT = 1 << 3;
    /**
     * 卡片从左边滑出
     */
    public static final int SWIPED_LEFT = 1;
    /**
     * 卡片从右边滑出
     */
    public static final int SWIPED_RIGHT = 1 << 2;
    /**
     * 卡片从上边滑出
     */
    public static final int SWIPING_UP = 1 >> 2;

    public static final int SWIPED_UP = 2;

    public static final int SWIPING_DOWN = 1 >> 3;

    public static final int SWIPED_DOWN = 1 >> 2;

    public static void initConfig(Context context) {
        MAX_SHOW_COUNT = 4;
        SCALE_GAP = 0.05f;
        TRANS_Y_GAP = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
    }
}
