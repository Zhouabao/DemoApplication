package com.example.demoapplication.common

import android.annotation.SuppressLint
import android.util.Log
import com.baidu.idl.face.platform.LivenessTypeEnum
import com.blankj.utilcode.util.CrashUtils
import com.blankj.utilcode.util.ThreadUtils
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
import com.umeng.commonsdk.UMConfigure
import com.umeng.message.IUmengRegisterCallback
import com.umeng.message.PushAgent
import com.umeng.socialize.PlatformConfig
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits


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

        //初始化Umeng
        initUmeng()
        //崩溃日志
        CrashUtils.init(NimSDKOptionConfig.getAppCacheDir(this) + "/demoApplication")
        configUnits()
        configPlayer()

        DemoCache.setContext(this)
        NIMClient.init(this, UserManager.loginInfo(),NimSDKOptionConfig.getSDKOptions(this))
        initUIKit()

    }

    private fun initUmeng() {
        if (ThreadUtils.isMainThread()) {
            /**
             * 初始化common库
             * 参数1:上下文，不能为空
             * 参数2:【友盟+】 AppKey
             * 参数3:【友盟+】 Channel
             * 参数4:设备类型，UMConfigure.DEVICE_TYPE_PHONE为手机、UMConfigure.DEVICE_TYPE_BOX为盒子，默认为手机
             * 参数5:Push推送业务的secret
             */
            UMConfigure.init(
                this,
                Constants.UMENG_APPKEY,
                "Umeng",
                UMConfigure.DEVICE_TYPE_PHONE,
                Constants.UMENG_SECRET
            )

            /**
             * 设置组件化的Log开关
             * 参数: boolean 默认为false，如需查看LOG设置为true
             */
            UMConfigure.setLogEnabled(true)
            /**
             * 设置日志加密
             * 参数：boolean 默认为false（不加密）
             */
            UMConfigure.setEncryptEnabled(true)
            //获取消息推送代理示例
            val mPushAgent = PushAgent.getInstance(this)
            //注册推送服务，每次调用register方法都会回调该接口
            mPushAgent.register(object : IUmengRegisterCallback {
                override fun onSuccess(deviceToken: String?) {
                    Log.d("deviceToken", deviceToken)
                }

                override fun onFailure(p0: String?, p1: String?) {
                    Log.d("deviceToken", "=========$p0,=======$p1")

                }

            })

            //微博平台
            PlatformConfig.setSinaWeibo("3921700954", "04b48b094faeb16683c32669824ebdad", "http://sns.whalecloud.com")
            //微信平台
            PlatformConfig.setWeixin("wxdc1e388c3822c80b", "3baf1193c85774b3fd9d18447d76cab0")
            //qq空间平台
            PlatformConfig.setQQZone("100424468", "c7394704798a158208a74ab60104f0ba")
        }
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
            // 注册自定义推送消息处理，这个是可选项
//            Nimpu.registerMixPushMessageHandler(DemoMixPushMessageHandler())

            NimUIKit.init(this, buildUIKitOptions())
            // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
            NimUIKit.setLocationProvider(NimDemoLocationProvider())
            // IM 会话窗口的定制初始化。
            SessionHelper.init()
            //初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle())
            //云信相关业务初始化
            NIMInitManager.getInstance().init(true)

            //在线状态内容提供者
//            NimUIKit.setOnlineStateContentProvider(DemoOnlineStateContentProvider())

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