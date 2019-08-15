package com.example.demoapplication.common

import android.annotation.SuppressLint
import android.os.Environment
import com.baidu.idl.face.platform.LivenessTypeEnum
import com.blankj.utilcode.util.CrashUtils
import com.example.demoapplication.nim.DemoCache
import com.example.demoapplication.nim.NIMInitManager
import com.example.demoapplication.nim.NimSDKOptionConfig
import com.example.demoapplication.nim.session.NimDemoLocationProvider
import com.example.demoapplication.nim.session.SessionHelper
import com.example.demoapplication.nim.sp.UserPreferences
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.common.BaseApplication
import com.mob.MobSDK
import com.netease.nim.uikit.R
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.UIKitOptions
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.util.NIMUtil
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import java.io.File


class MyApplication : BaseApplication() {
    init {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.setPrimaryColorsId(com.example.demoapplication.R.color.colorWhite)
            ClassicsHeader(context)

        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            ClassicsFooter(context).setDrawableSize(20F)
        }

    }

    companion object {
        public var livenessList = mutableListOf<LivenessTypeEnum>()
        public var isLivewnessRandom = false

    }


    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        MobSDK.init(this)
        CrashUtils.init(File(Environment.getExternalStorageDirectory().absolutePath.plus(File.separator).plus("demoapplicaiton")))
        configUnits()
        configPlayer()

        DemoCache.setContext(this)
        NIMClient.init(this, UserManager.loginInfo(), UserManager.options(this))
        initUIKit()

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


    private fun initUIKit() {
        if (NIMUtil.isMainProcess(this)) {
            NimUIKit.init(this, buildUIKitOptions())
            // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
            NimUIKit.setLocationProvider(NimDemoLocationProvider())
            // IM 会话窗口的定制初始化。
            SessionHelper.init()
            //初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle())
            //云信相关业务初始化
            NIMInitManager.getInstance().init(true)
        }
    }

    private fun buildUIKitOptions(): UIKitOptions? {
        val options = UIKitOptions()
        options.appCacheDir = NimSDKOptionConfig.getAppCacheDir(this) + "/demoApplication"
        options.messageLeftBackground = R.drawable.shape_rectangle_share_square_bg_left
        options.messageRightBackground = R.drawable.shape_rectangle_share_square_bg
        return options
    }
}