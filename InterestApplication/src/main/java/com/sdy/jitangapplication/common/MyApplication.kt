package com.sdy.jitangapplication.common

import android.annotation.SuppressLint
import com.baidu.idl.face.platform.LivenessTypeEnum
import com.blankj.utilcode.util.CrashUtils
import com.blankj.utilcode.util.ThreadUtils
import com.kotlin.base.common.BaseApplication
import com.netease.nim.uikit.R
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.UIKitOptions
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.mixpush.NIMPushClient
import com.netease.nimlib.sdk.util.NIMUtil
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.nim.NIMInitManager
import com.sdy.jitangapplication.nim.NimSDKOptionConfig
import com.sdy.jitangapplication.nim.mixpush.DemoMixPushMessageHandler
import com.sdy.jitangapplication.nim.mixpush.DemoPushContentProvider
import com.sdy.jitangapplication.nim.session.NimDemoLocationProvider
import com.sdy.jitangapplication.nim.session.SessionHelper
import com.sdy.jitangapplication.nim.sp.UserPreferences
import com.sdy.jitangapplication.utils.UserManager
import com.tencent.bugly.Bugly
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits


class MyApplication : BaseApplication() {
    init {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.setPrimaryColorsId(com.sdy.jitangapplication.R.color.colorWhite)
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

        //初始化Umeng
        initUmeng()
        //崩溃日志
        CrashUtils.init(NimSDKOptionConfig.getAppCacheDir(this) + "/demoApplication")
        //自适应size
        configUnits()
        //gsyvideoplayer
        configPlayer()
        //nim
        initUIKit()
        //bugly初始化
        Bugly.init(this, Constants.BUGLY_APP_ID, false)

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
            UMConfigure.init(this, Constants.UMENG_APPKEY, "Umeng", UMConfigure.DEVICE_TYPE_PHONE, Constants.UMENG_SECRET)

            /**
             * 设置组件化的Log开关
             * 参数: boolean 默认为false，如需查看LOG设置为true
             */
            UMConfigure.setLogEnabled(Constants.TEST)
            /**
             * 设置日志加密
             * 参数：boolean 默认为false（不加密）
             */
            UMConfigure.setEncryptEnabled(true)
            // 选用AUTO页面采集模式
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)

            //微博平台
            PlatformConfig.setSinaWeibo(Constants.SINA_APP_KEY, Constants.SINA_APP_SECRET, "http://sns.whalecloud.com")
            //微信平台
            PlatformConfig.setWeixin(Constants.WECHAT_APP_ID, Constants.WECHAT_APP_KEY)
            //qq空间平台
            PlatformConfig.setQQZone(Constants.QQ_APP_KEY, Constants.QQ_APP_SECRET)



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
        DemoCache.setContext(this)
        NIMClient.init(this, UserManager.loginInfo(), NimSDKOptionConfig.getSDKOptions(this))

        if (NIMUtil.isMainProcess(this)) {

            // 注册自定义推送消息处理，这个是可选项
            NIMPushClient.registerMixPushMessageHandler(DemoMixPushMessageHandler())

            NimUIKit.init(this, buildUIKitOptions())
            // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
            NimUIKit.setLocationProvider(NimDemoLocationProvider())
            // IM 会话窗口的定制初始化。
            SessionHelper.init()
            //初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle())
            //云信相关业务初始化
            NIMInitManager.getInstance().init(true)

            // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
            NimUIKit.setCustomPushContentProvider(DemoPushContentProvider())



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