package com.kotlin.base.common

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex


/*
    Application 基类
 */
open class BaseApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)

        context = this

    }


    /*
        全局伴生对象
     */
    companion object {
        lateinit var context: Context
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}
