package com.kotlin.base.common

import android.app.Application
import android.content.Context
import com.alibaba.android.arouter.launcher.ARouter


/*
    Application 基类
 */
open class BaseApplication : Application() {


    override fun onCreate() {
        super.onCreate()


        context = this

        //ARouter初始化
        ARouter.openLog()    // 打印日志
        ARouter.openDebug()
        ARouter.init(this)
    }


    /*
        全局伴生对象
     */
    companion object {
        lateinit var context: Context
    }

    override fun onTerminate() {
        super.onTerminate()
        ARouter.getInstance().destroy()
    }
}
