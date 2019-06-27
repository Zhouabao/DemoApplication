package com.example.demoapplication.common

import com.kotlin.base.common.BaseApplication
import com.tencent.ugc.TXUGCBase
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits

class MyApplication : BaseApplication() {

    val ugcLicenseUrl = "http://license.vod2.myqcloud.com/license/v1/09557a438c98b1e9e28a6d0c3b052ecc/TXUgcSDK.licence"
    val ugcKey = "dcede96f01ea0cb45e1bd7a3017ea144"


    override fun onCreate() {
        super.onCreate()
        TXUGCBase.getInstance().setLicence(this, ugcLicenseUrl, ugcKey)
        configUnits()
    }


    private fun configUnits() {
        AutoSizeConfig
            .getInstance()
            .unitsManager
            .setSupportDP(true)
            .supportSubunits = Subunits.PT

    }
}