package com.example.demoapplication.common

import android.annotation.SuppressLint
import android.os.Environment
import cn.bingoogolapple.swipebacklayout.BGASwipeBackHelper
import com.blankj.utilcode.util.CrashUtils
import com.example.demoapplication.R
import com.kotlin.base.common.BaseApplication
import com.mob.MobSDK
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import java.io.File

class MyApplication : BaseApplication() {
    init {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.setPrimaryColorsId(R.color.colorWhite)
            ClassicsHeader(context)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            ClassicsFooter(context).setDrawableSize(20F)
        }
    }


    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        MobSDK.init(this)
        CrashUtils.init(File(Environment.getExternalStorageDirectory().absolutePath.plus(File.separator).plus("demoapplicaiton")))
        configUnits()
        configPlayer()

        /**
         * 必须在 Application 的 onCreate 方法中执行 BGASwipeBackHelper.init 来初始化滑动返回
         * 第一个参数：应用程序上下文
         * 第二个参数：如果发现滑动返回后立即触摸界面时应用崩溃，请把该界面里比较特殊的 View 的 class 添加到该集合中，目前在库中已经添加了 WebView 和 SurfaceView
         */
        BGASwipeBackHelper.init(this, null);
    }

    private fun configPlayer() {
//        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3)
    }


    private fun configUnits() {
        AutoSizeConfig
            .getInstance()
            .unitsManager
            .setSupportDP(true)
            .supportSubunits = Subunits.PT

    }
}