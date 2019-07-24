package com.example.demoapplication.common

import android.annotation.SuppressLint
import android.os.Environment
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