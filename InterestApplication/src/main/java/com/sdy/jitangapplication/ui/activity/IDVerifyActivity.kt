package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.baidu.idl.face.platform.FaceEnvironment
import com.baidu.idl.face.platform.FaceSDKManager
import com.baidu.idl.face.platform.FaceStatusEnum
import com.baidu.idl.face.platform.LivenessTypeEnum
import com.baidu.idl.face.platform.ui.FaceLivenessActivity
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.common.AppManager
import com.kotlin.base.common.BaseApplication.Companion.context
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.utils.NetWorkUtils
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.baselibrary.widgets.swipeback.SwipeBackLayout
import com.sdy.baselibrary.widgets.swipeback.Utils
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityBase
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityHelper
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.MyApplication
import com.sdy.jitangapplication.event.AccountDangerEvent
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.ui.dialog.AccountDangerDialog
import com.sdy.jitangapplication.ui.dialog.HumanVerfiyDialog
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import java.io.ByteArrayOutputStream


/**
 * 身份验证
 */
class IDVerifyActivity : FaceLivenessActivity(), SwipeBackActivityBase {
    private lateinit var mHelper: SwipeBackActivityHelper


    companion object {
        const val TYPE_ACCOUNT_DANGER = 1 //账户异常发起
        const val TYPE_ACCOUNT_NORMAL = 2  //用户主动发起
        const val TYPE_LIVE_CAPTURE = 3  //活体检测

        fun startActivity(
            context1: Context,
            type: Int = TYPE_ACCOUNT_NORMAL
        ) {
            if (type == TYPE_LIVE_CAPTURE) {
                context1.startActivity<IDVerifyActivity>("type" to type)
            } else
                if (!UserManager.isHasFaceUrl()) {
                    CommonAlertDialog.Builder(context1)
                        .setIconVisible(false)
                        .setCancelIconIsVisibility(true)
                        .setTitle("认证提醒")
                        .setContent("审核将与用户头像做比对，请确认头像为本人\n验证信息只用作审核，不会对外展示")
                        .setConfirmText("确定")
                        .setCancelText("取消")
                        .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                            override fun onClick(dialog: Dialog) {
                                dialog.dismiss()
                                context1.startActivity<IDVerifyActivity>(
                                    "type" to type
                                )
                            }
                        })
                        .setOnCancelListener(object : CommonAlertDialog.OnConfirmListener,
                            CommonAlertDialog.OnCancelListener {
                            override fun onClick(dialog: Dialog) {
                                dialog.dismiss()
                            }

                        })
                        .create()
                        .show()
                } else {
                    HumanVerfiyDialog(context1, type, true).show()
                }
        }


        fun startActivityForResult(
            context1: Activity,
            type: Int = TYPE_ACCOUNT_NORMAL, requestCode: Int
        ) {

            if (!UserManager.isHasFaceUrl()) {
                CommonAlertDialog.Builder(context1)
                    .setIconVisible(false)
                    .setCancelIconIsVisibility(true)
                    .setTitle("认证提醒")
                    .setContent("审核将与用户头像做比对，请确认头像为本人\n验证信息只用作审核，不会对外展示")
                    .setConfirmText("确定")
                    .setCancelText("取消")
                    .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                        override fun onClick(dialog: Dialog) {
                            dialog.dismiss()
                            context1.startActivityForResult<IDVerifyActivity>(
                                requestCode,
                                "type" to type
                            )
                        }
                    })
                    .setOnCancelListener(object : CommonAlertDialog.OnConfirmListener,
                        CommonAlertDialog.OnCancelListener {
                        override fun onClick(dialog: Dialog) {
                            dialog.dismiss()
                        }

                    })
                    .create()
                    .show()
            } else {
                HumanVerfiyDialog(context1, type, true).show()
            }
        }
    }

    private val type by lazy { intent.getIntExtra("type", TYPE_ACCOUNT_NORMAL) }

    override fun getSwipeBackLayout(): SwipeBackLayout {
        return mHelper.swipeBackLayout
    }

    override fun setSwipeBackEnable(enable: Boolean) {
        swipeBackLayout.setEnableGesture(enable)
    }

    override fun scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this)
        swipeBackLayout.scrollToFinishActivity()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        mHelper.onPostCreate()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setFaceConfig()
        super.onCreate(savedInstanceState)
        StatusBarUtil.immersive(this)
        AppManager.instance.addActivity(this)
        mHelper = SwipeBackActivityHelper(this)
        mHelper.onActivityCreate()
        setSwipeBackEnable(type != TYPE_LIVE_CAPTURE)
        mCloseView.isVisible = type != TYPE_LIVE_CAPTURE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            (!PermissionUtils.isGranted(PermissionConstants.CAMERA) ||
                    !PermissionUtils.isGranted(PermissionConstants.STORAGE))
        ) {
            PermissionUtils.permission(PermissionConstants.CAMERA)
                .callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        if (!PermissionUtils.isGranted(PermissionConstants.STORAGE)) {
                        }
                        PermissionUtils.permission(PermissionConstants.STORAGE)
                            .callback(object : PermissionUtils.SimpleCallback {
                                override fun onGranted() {
                                    initVerify()
                                }

                                override fun onDenied() {
                                    CommonFunction.toast("文件存储权限被拒,请允许权限后再进行认证.")
                                    finish()
                                }

                            })
                            .request()
                    }

                    override fun onDenied() {
                        CommonFunction.toast("相机权限被拒,请允许权限后再进行认证.")
                        finish()
                    }
                })
                .request()
        } else {
            initVerify()
        }
    }

    private fun initVerify() {
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)
        mImageLayout.visibility = View.GONE
        // 根据需求添加活体动作
        MyApplication.livenessList.clear()
        MyApplication.livenessList.add(LivenessTypeEnum.Eye)
        MyApplication.livenessList.add(LivenessTypeEnum.HeadLeft)
        MyApplication.livenessList.add(LivenessTypeEnum.HeadRight)

        // 为了android和ios 区分授权，appId=appname_face_android ,其中appname为申请sdk时的应用名
        // 应用上下文
        // 申请License取得的APPID
        // assets目录下License文件名
        FaceSDKManager.getInstance()
            .initialize(this, Constants.licenseID, Constants.licenseFileName)

    }

    private fun setFaceConfig() {
        val config = FaceSDKManager.getInstance().faceConfig
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整
        config.setLivenessTypeList(MyApplication.livenessList)
        //设置就活体动作是否随机
        config.setLivenessRandom(MyApplication.isLivewnessRandom)
        //设置模糊度范围(0-1)推荐小于0.7
        config.setBlurnessValue(FaceEnvironment.VALUE_BLURNESS)
        //光照范围(0-1)推荐大于40
        config.setBrightnessValue(FaceEnvironment.VALUE_BRIGHTNESS)
        //裁剪人脸大小
        config.setCropFaceValue(FaceEnvironment.VALUE_MIN_FACE_SIZE)
        //人脸yaw,pitch,row角度，范围(-45,45)，推荐-15-15
        //低头抬头角度
        config.setHeadPitchValue(45)
        //偏头角度
        config.setHeadRollValue(45)
        //左右角度
        config.setHeadYawValue(45)
        //最小检测人脸80-200 越小越耗性能,推荐120-200
        config.setMinFaceSize(120)

        config.setNotFaceValue(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD)
        //人脸遮挡范围(0-1) 推荐小于0.5
        config.setOcclusionValue(FaceEnvironment.VALUE_OCCLUSION)
        //s是否进行质量检测
        config.setCheckFaceQuality(true)
        //人脸检测使用线程数量
        config.setFaceDecodeNumberOfThreads(2)
        //是否开启提示声音
        config.setSound(false)
        FaceSDKManager.getInstance().faceConfig = config

    }

    override fun onLivenessCompletion(
        status: FaceStatusEnum?,
        message: String?,
        images: HashMap<String, String>?
    ) {
        super.onLivenessCompletion(status, message, images)
        if (status == FaceStatusEnum.OK && mIsCompletion) {
            loadingDialog.show()
            if (images != null && images.size > 0) {
                val fileKey =
                    "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME)
                        .getString(
                            "accid"
                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                        16
                    )}"

                if (images["bestImage0"].isNullOrEmpty()) {
                    uploadProfile(bitmap2Bytes(mILivenessStrategy.bestFaceImage), fileKey)
                } else {
                    uploadProfile(bitmap2Bytes(images["bestImage0"]!!), fileKey)
                }

            }
        } else if (status == FaceStatusEnum.Error_DetectTimeout
            || status == FaceStatusEnum.Error_LivenessTimeout ||
            status == FaceStatusEnum.Error_Timeout
        ) {
            CommonFunction.toast("采集超时,请退出重试")
        }
    }


    /*-------------------by服务端-----------------------*/

    /**
     * 上传照片
     * imagePath 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     * face_source_type是否是消息过来的上传 1是 0否
     */
    private fun uploadProfile(filePath: ByteArray, imagePath: String) {
        if (!NetWorkUtils.isNetWorkAvailable(this)) {
            return
        }
        QNUploadManager.getInstance().put(
            filePath, imagePath, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                if (info != null && info.isOK) {
                    savePersonal(
                        hashMapOf(
                            "token" to UserManager.getToken(),
                            "accid" to UserManager.getAccid(),
                            "face" to key,
                            "face_source_type" to intent.getIntExtra("face_source_type", 0)
                        )
                    )
                } else {
                    CommonFunction.toast("认证审核提交失败，请重新进入认证")
                }
            }, null
        )
    }

    /**
     * 保存个人信息
     */

    private fun savePersonal(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .savePersonal(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    loadingDialog.dismiss()
                    if (type == TYPE_LIVE_CAPTURE) {
                        UserManager.startToFlow(
                            this@IDVerifyActivity,
                            intent.getSerializableExtra("morematchbean") as MoreMatchBean?
                        )
                    } else
                        when (t.code) {
                            200 -> {
                                CommonFunction.toast("审核提交成功")
                                UserManager.saveUserVerify(2)
                                UserManager.saveHasFaceUrl(true)
                                setResult(Activity.RESULT_OK, intent.putExtra("verify", 2))
                                finish()
                                if (intent.getIntExtra(
                                        "type",
                                        TYPE_ACCOUNT_NORMAL
                                    ) == TYPE_ACCOUNT_DANGER
                                ) {
                                }
                                EventBus.getDefault().postSticky(
                                    AccountDangerEvent(
                                        AccountDangerDialog.VERIFY_ING
                                    )
                                )
                            }
                            403 -> UserManager.startToLogin(context as Activity)
                            else -> {
                                CommonFunction.toast(t.msg)
                            }
                        }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(this@IDVerifyActivity).show()
                    }
                    loadingDialog.dismiss()
                    CommonFunction.toast("认证审核提交失败，请重新进入认证")
                }
            })

    }


    /*-------------------by客户端-----------------------*/
    private val loadingDialog by lazy { LoadingDialog(this) }

    /**
     * 将图片转变为字节数组
     */
    private fun bitmap2Bytes(base64Url: String): ByteArray {
        val baos = ByteArrayOutputStream()
        base64ToBitmap(base64Url).compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        AppManager.instance.finishActivity(this)
        FaceSDKManager.release()
    }

    override fun onBackPressed() {
        if (type != TYPE_LIVE_CAPTURE)
            super.onBackPressed()
    }
}
