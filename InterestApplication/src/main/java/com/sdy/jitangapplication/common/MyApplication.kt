package com.sdy.jitangapplication.common

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.baidu.idl.face.platform.LivenessTypeEnum
import com.blankj.utilcode.util.*
import com.google.gson.Gson
import com.ishumei.smantifraud.SmAntiFraud
import com.kotlin.base.common.BaseApplication
import com.leon.channel.helper.ChannelReaderUtil
import com.netease.nim.uikit.R
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.UIKitOptions
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.mixpush.NIMPushClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.util.NIMUtil
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivity
import com.sdy.jitangapplication.event.GetNewMsgEvent
import com.sdy.jitangapplication.event.ReVerifyEvent
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.model.CustomerMsgBean
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.nim.NIMInitManager
import com.sdy.jitangapplication.nim.NimSDKOptionConfig
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.activity.MessageInfoActivity
import com.sdy.jitangapplication.nim.mixpush.DemoMixPushMessageHandler
import com.sdy.jitangapplication.nim.mixpush.DemoPushContentProvider
import com.sdy.jitangapplication.nim.session.NimDemoLocationProvider
import com.sdy.jitangapplication.nim.session.SessionHelper
import com.sdy.jitangapplication.nim.sp.UserPreferences
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.activity.MessageHiActivity
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.player.SystemPlayerManager
import com.tencent.bugly.Bugly
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import org.greenrobot.eventbus.EventBus


class MyApplication : BaseApplication() {
    init {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.setPrimaryColorsId(com.sdy.jitangapplication.R.color.colorWhite).setReboundDuration(200)
            ClassicsHeader(context).setFinishDuration(200)

        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            layout.setPrimaryColorsId(com.sdy.jitangapplication.R.color.colorWhite).setReboundDuration(200)
            ClassicsFooter(context).setFinishDuration(200).setDrawableSize(20F)
        }

    }

    companion object {
        public var livenessList = mutableListOf<LivenessTypeEnum>()
        public var isLivewnessRandom = false

    }


    /**
     * 系统通知监听
     */
    private var customNotificationObserver: Observer<CustomNotification> =
        Observer { customNotification ->
            if (customNotification.content != null) {
                val customerMsgBean =
                    Gson().fromJson<CustomerMsgBean>(customNotification.content, CustomerMsgBean::class.java)
                Log.d("OkHttp", "${customerMsgBean.type}====,${customerMsgBean.msg}==================================")
                when (customerMsgBean.type) {
                    1 -> {//系统通知新的消息数量
                        EventBus.getDefault().postSticky(GetNewMsgEvent())
                        EventBus.getDefault().postSticky(UpdateHiEvent())
                        initNotificationManager(customerMsgBean.msg)
                    }
                    2 -> {//对方删除自己,本地删除会话列表
                        NIMClient.getService(MsgService::class.java)
                            .deleteRecentContact2(customerMsgBean.accid ?: "", SessionTypeEnum.P2P)
                        // 删除与某个聊天对象的全部消息记录
                        NIMClient.getService(MsgService::class.java)
                            .clearChattingHistory(customerMsgBean.accid ?: "", SessionTypeEnum.P2P)
                        if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(MessageInfoActivity::class.java.newInstance()))
                            ActivityUtils.finishActivity(MessageInfoActivity::class.java)
                        if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(ChatActivity::class.java.newInstance()))
                            ActivityUtils.finishActivity(ChatActivity::class.java)
                        if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(MessageHiActivity::class.java.newInstance()))
                            ActivityUtils.finishActivity(MessageHiActivity::class.java)
                        EventBus.getDefault().postSticky(UpdateHiEvent())

                    }
                    3 -> {
                        //新的招呼刷新界面
                        EventBus.getDefault().postSticky(UpdateHiEvent())
                    }
                    //4人脸认证不通过
                    4 -> {
                        //更改本地的认证状态
                        UserManager.saveUserVerify(0)
                        //更改本地的筛选认证状态
                        if (SPUtils.getInstance(Constants.SPNAME).getInt("audit_only", -1) == 2) {
                            SPUtils.getInstance(Constants.SPNAME).remove("audit_only")
                            //发送通知更新内容
                            EventBus.getDefault().postSticky(RefreshEvent(true))
                        }
                        EventBus.getDefault().postSticky(ReVerifyEvent(customerMsgBean.type, customerMsgBean.msg))
                    }
                    //7强制替换头像
                    7 -> {
                        EventBus.getDefault().postSticky(ReVerifyEvent(customerMsgBean.type, customerMsgBean.msg))
                        UserManager.saveChangeAvator(customerMsgBean.msg)
                    }


                }


            }
        }


    private fun initNotificationManager(msg: String) {
        val manager = getSystemService(SwipeBackActivity.NOTIFICATION_SERVICE) as NotificationManager
        //8.0 以后需要加上channelId 才能正常显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "default"
            val channelName = "默认通知"
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }


        //为了版本兼容  选择V7包下的NotificationCompat进行构造
        val builder = NotificationCompat.Builder(this, "default")
        //Ticker是状态栏显示的提示
//        builder.setTicker("通知")
        //第一行内容  通常作为通知栏标题
//        builder.setContentTitle("积糖")
        //第二行内容 通常是通知正文
        builder.setContentText(msg)
        //第三行内容 通常是内容摘要什么的 在低版本机器上不一定显示
//        builder.setSubText("这里显示的是通知第三行内容！")
        //ContentInfo 在通知的右侧 时间的下面 用来展示一些其他信息
        //builder.setContentInfo("2");
        //number设计用来显示同种通知的数量和ContentInfo的位置一样，如果设置了ContentInfo则number会被隐藏
//        builder.setNumber(2)
        //可以点击通知栏的删除按钮删除
        builder.setAutoCancel(true)
        //系统状态栏显示的小图标
        builder.setSmallIcon(com.sdy.jitangapplication.R.drawable.icon_logo)
        //下拉显示的大图标
        builder.setLargeIcon(
            BitmapFactory.decodeResource(
                resources,
                com.sdy.jitangapplication.R.drawable.icon_logo
            )
        )
        val intent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this, 1, intent, 0)
        //点击跳转的intent
        builder.setContentIntent(pIntent)
        //通知默认的声音 震动 呼吸灯
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        val notification = builder.build()
        manager.notify(1, notification)

    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        //数美黑产
        initSM()
        //初始化Umeng
        initUmeng()
        //崩溃日志
        CrashUtils.init(UriUtils.getCacheDir(this))
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
//            UMConfigure.init(
//                this,
//                UMConfigure.DEVICE_TYPE_PHONE,
//                Constants.UMENG_SECRET
//            )
            var channel = "test"
            if (ChannelReaderUtil.getChannel(this) != null) {
                channel = ChannelReaderUtil.getChannel(this)
            }
            UMConfigure.init(this, null, channel, UMConfigure.DEVICE_TYPE_PHONE, null)

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
            PlatformConfig.setSinaWeibo(
                Constants.SINA_APP_KEY,
                Constants.SINA_APP_SECRET,
                "http://sns.whalecloud.com"
            )
            //微信平台
            PlatformConfig.setWeixin(Constants.WECHAT_APP_ID, Constants.WECHAT_APP_KEY)
            //qq空间平台
            PlatformConfig.setQQZone(Constants.QQ_APP_KEY, Constants.QQ_APP_SECRET)


        }
    }

    private fun configPlayer() {
        //系统内核模式
        PlayerFactory.setPlayManager(SystemPlayerManager::class.java)
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
            NIMClient.getService(MsgServiceObserve::class.java)
                .observeCustomNotification(customNotificationObserver, true)

        }
    }

    private fun buildUIKitOptions(): UIKitOptions? {
        val options = UIKitOptions()
        options.appCacheDir = UriUtils.getCacheDir(this)
        options.messageLeftBackground = R.drawable.shape_rectangle_share_square_bg_left
        options.messageRightBackground = R.drawable.shape_rectangle_share_square_bg
        return options
    }




    private fun initSM(){
        //指定主进程才能执行
        if (NIMUtil.isMainProcess(this)) {
            val option = SmAntiFraud.SmOption()
            option.organization = Constants.SM_ORGANIZATION
            option.channel = ChannelReaderUtil.getChannel(this) ?: ""
//            option.channel ="debug"
            option.publicKey = Constants.SM_PUBLICKEY
            option.ainfoKey = Constants.SM_AINFOKEY
            Log.d("smOption","${option.organization},${option.channel}")
            SmAntiFraud.create(this,option)
            //注意！！获取deviceid，这个接口在需要使用deviceid时地方调用
            val deviceId = SmAntiFraud.getDeviceId()
            Log.d("smOption","${deviceId}")
        }
    }
}