package com.sdy.jitangapplication.common

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.baidu.idl.face.platform.LivenessTypeEnum
import com.blankj.utilcode.util.*
import com.google.gson.Gson
import com.ishumei.smantifraud.SmAntiFraud
import com.kotlin.base.common.BaseApplication
import com.leon.channel.helper.ChannelReaderUtil
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.UIKitOptions
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.mixpush.NIMPushClient
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.util.NIMUtil
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.CustomerMsgBean
import com.sdy.jitangapplication.nim.DemoCache
import com.sdy.jitangapplication.nim.NIMInitManager
import com.sdy.jitangapplication.nim.NimSDKOptionConfig
import com.sdy.jitangapplication.nim.event.DemoOnlineStateContentProvider
import com.sdy.jitangapplication.nim.mixpush.DemoMixPushMessageHandler
import com.sdy.jitangapplication.nim.mixpush.DemoPushContentProvider
import com.sdy.jitangapplication.nim.session.NimDemoLocationProvider
import com.sdy.jitangapplication.nim.session.SessionHelper
import com.sdy.jitangapplication.nim.sp.UserPreferences
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.dialog.VerifyForceDialog
import com.sdy.jitangapplication.ui.dialog.VerifyNormalResultDialog
import com.sdy.jitangapplication.ui.fragment.SnackBarFragment
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.tencent.bugly.Bugly
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import com.umeng.socialize.PlatformConfig
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import org.greenrobot.eventbus.EventBus


class MyApplication1 : BaseApplication() {
    init {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.setPrimaryColorsId(com.sdy.jitangapplication.R.color.colorWhite)
                .setReboundDuration(200)
            ClassicsHeader(context).setFinishDuration(200)

        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            layout.setPrimaryColorsId(com.sdy.jitangapplication.R.color.colorWhite)
                .setReboundDuration(200)
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
                    Gson().fromJson<CustomerMsgBean>(
                        customNotification.content,
                        CustomerMsgBean::class.java
                    )
                Log.d("OkHttp", "${customerMsgBean.toString()}====")
                when (customerMsgBean.type) {
                    //系统通知提醒
                    SnackBarFragment.SOMEONE_LIKE_YOU,
                    SnackBarFragment.SOMEONE_MATCH_SUCCESS,
                    SnackBarFragment.GREET_SUCCESS,
                    SnackBarFragment.FLASH_SUCCESS,
                    SnackBarFragment.CHAT_SUCCESS,
                    SnackBarFragment.HELP_CANDY,
                    SnackBarFragment.GIVE_GIFT -> {
                        if (ActivityUtils.getTopActivity() is MainActivity)
                            FragmentUtils.add(
                                (ActivityUtils.getTopActivity() as AppCompatActivity).supportFragmentManager,
                                SnackBarFragment(customerMsgBean),
                                android.R.id.content
                            )

                    }
                    //1.系统通知新的消息数量
                    1 -> {
                        EventBus.getDefault().postSticky(GetNewMsgEvent())
                        EventBus.getDefault().postSticky(UpdateHiEvent())
                        initNotificationManager(customerMsgBean.msg)
                    }
                    //2.对方删除自己,本地不删除会话列表
                    2 -> {
                        CommonFunction.dissolveRelationship(customerMsgBean.accid ?: "", true)
                    }
                    //4人脸认证不通过
//                    4 -> {
//                        //更改本地的认证状态
//                        UserManager.saveUserVerify(0)
//                        //更改本地的筛选认证状态
//                        if (SPUtils.getInstance(Constants.SPNAME).getInt("audit_only", -1) == 2) {
//                            SPUtils.getInstance(Constants.SPNAME).remove("audit_only")
//                            //发送通知更新内容
//                            EventBus.getDefault().postSticky(RefreshEvent(true))
//                        }
//                        //如果账号存在异常，就发送认证不通过弹窗
//                        if (UserManager.getAccountDanger() || UserManager.getAccountDangerAvatorNotPass()) {
//                            EventBus.getDefault()
//                                .postSticky(ForceFaceEvent(VerifyForceDialog.FORCE_FAIL_MV))
//                        } else {
//                            EventBus.getDefault().postSticky(
//                                ReVerifyEvent(
//                                    customerMsgBean.type,
//                                    customerMsgBean.msg
//                                )
//                            )
//                        }
//                    }
                    //7强制替换头像
                    //11真人头像不通过弹窗
                    7, 11 -> {
                        EventBus.getDefault()
                            .postSticky(ReVerifyEvent(customerMsgBean.type, customerMsgBean.msg))
                        UserManager.saveChangeAvator(customerMsgBean.msg)
                        UserManager.saveChangeAvatorType(customerMsgBean.type)
                    }

                    //8账号异常提示去变更账号
                    //const SHUMEI_APPROVE = 8; // 数美强制认证（没有人脸时别过）
                    //const SHUMEI_APPROVE_FACE = 81; // 数美强制认证（有人脸时别过）
                    //const SUCCESS_FORCE = 10; // 强制认证（没有人脸时别过）
                    //const SUCCESS_FORCE_FACE = 101; // 强制认证（有人脸时别过）
//                    8, 81 -> {
//                        UserManager.saveAccountDanger(true)
//                        EventBus.getDefault()
//                            .postSticky(AccountDangerEvent(AccountDangerDialog.VERIFY_NEED_ACCOUNT_DANGER))
//
//                    }
                    //91 视频认证通过(普通)
                    //94 视频认证通过(强制)
                    91, 94 -> {
                        if (UserManager.getAccountDanger()) {
                            UserManager.saveAccountDanger(false)
                        }
                        if (UserManager.getAccountDangerAvatorNotPass()) {
                            UserManager.saveAccountDangerAvatorNotPass(false)
                        }
                        UserManager.saveUserVerify(1)
                        UserManager.saveHasFaceUrl(true)
                        if (SPUtils.getInstance(Constants.SPNAME).getInt("audit_only", -1) != -1) {
                            SPUtils.getInstance(Constants.SPNAME).remove("audit_only")
                            //发送通知更新内容
                            EventBus.getDefault().postSticky(UserCenterEvent(true))
                        }
                        //const SUCCESS_MV = 91;   // 视频认证通过(普通)
                        //const FORCE_SUCCESS_MV = 94;   // 视频认证通过(强制)
                        if (customerMsgBean.type == 91) {
                            VerifyNormalResultDialog(
                                ActivityUtils.getTopActivity(),
                                VerifyNormalResultDialog.VERIFY_NORMAL_PASS
                            ).show()
                        } else {
                            EventBus.getDefault()
                                .postSticky(ForceFaceEvent(VerifyForceDialog.FORCE_SUCCESS_MV))
                        }
                    }

                    //const FAIL_AVATAR = 92;  // 视频认证失败--去替换头像(普通)
                    //const FAIL_MV = 93;      // 视频认证失败----去替换视频(普通)
                    92, 93 -> {
                        VerifyNormalResultDialog(
                            ActivityUtils.getTopActivity(),
                            customerMsgBean.type
                        ).show()
                    }

                    //const FORCE_FAIL_AVATAR = 95;  //  视频认证失败--去替换头像(强制)
                    //const FORCE_FAIL_MV = 96;      //  视频认证失败----去替换视频(强制)
                    95, 96 -> {
                        EventBus.getDefault()
                            .postSticky(ForceFaceEvent(VerifyForceDialog.FORCE_SUCCESS_MV))
                    }
                    //const SUCCESS_FORCE = 10;       // 强制认证（没有视频的）
                    //const SUCCESS_FORCE_FACE = 101; // 强制认证（有视频换头像的）
                    10, 101 -> {
                        UserManager.saveAccountDanger(customerMsgBean.type == 10)
                        UserManager.saveAccountDangerAvatorNotPass(customerMsgBean.type == 101)
                        EventBus.getDefault().postSticky(ForceFaceEvent(customerMsgBean.type))
                    }

                }
//                VerifyNormalResultDialog

//const SUCCESS_MV = 91;   // 视频认证通过(普通)
//const FAIL_AVATAR = 92;  // 视频认证失败--去替换头像(普通)
//const FAIL_MV = 93;      // 视频认证失败----去替换视频(普通)
//
//const SUCCESS_FORCE = 10;       // 强制认证（没有视频的）
//const SUCCESS_FORCE_FACE = 101; // 强制认证（有视频换头像的）
//
//const FORCE_SUCCESS_MV = 94;   // 视频认证通过(强制)
//const FORCE_FAIL_AVATAR = 95;  // 视频认证失败--去替换头像(强制)
//const FORCE_FAIL_MV = 96;      //  视频认证失败----去替换视频(强制)

            }
        }


    private fun initNotificationManager(msg: String) {
        val msgs = msg.split("\\n")

        var manager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        //8.0 以后需要加上channelId 才能正常显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "subscribe"
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
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, "subscribe")
            .setContentText(
                if (msgs.size > 1) {
                    msgs[1]
                } else {
                    msg
                }
            )
            .setContentTitle(
                if (msgs.size > 1) {
                    msgs[0]
                } else {
                    ""
                }
            )
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(com.sdy.jitangapplication.R.drawable.icon_logo)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    resources,
                    com.sdy.jitangapplication.R.drawable.icon_logo
                )
            )
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        manager.notify(1, notification)

    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
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

        //数美黑产
        initSM()

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
//        PlayerFactory.setPlayManager(SystemPlayerManager::class.java)
        PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
//        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
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
            // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
            NimUIKit.setCustomPushContentProvider(DemoPushContentProvider())
            //在线状态内容提供者
            NimUIKit.setOnlineStateContentProvider(DemoOnlineStateContentProvider())
            //初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle())
            //自定义通知监听
            NIMClient.getService(MsgServiceObserve::class.java)
                .observeCustomNotification(customNotificationObserver, true)
            //云信相关业务初始化
            NIMInitManager.getInstance().init(true)

        }
    }

    private fun buildUIKitOptions(): UIKitOptions? {
        val options = UIKitOptions()
        options.appCacheDir = UriUtils.getCacheDir(this)
        options.messageLeftBackground = R.drawable.shape_rectangle_share_square_bg_left
        options.messageRightBackground = R.drawable.shape_rectangle_chat_bg_right
        return options
    }


    private fun initSM() {
        try {//指定主进程才能执行
            if (NIMUtil.isMainProcess(this)) {
                val option = SmAntiFraud.SmOption()
                option.organization = Constants.SM_ORGANIZATION
                option.channel = ChannelReaderUtil.getChannel(this) ?: ""
                //            option.channel ="debug"
                option.publicKey = Constants.SM_PUBLICKEY
                option.ainfoKey = Constants.SM_AINFOKEY
                Log.d("smOption", "${option.organization},${option.channel}")
                SmAntiFraud.create(this, option)
            }
        } catch (e: Exception) {
            Log.d("SmAntiFraud", e.message ?: "")
        }
    }
}