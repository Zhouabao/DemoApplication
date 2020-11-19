package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.baidu.idl.face.platform.FaceEnvironment
import com.baidu.idl.face.platform.FaceSDKManager
import com.baidu.idl.face.platform.FaceStatusNewEnum
import com.baidu.idl.face.platform.listener.IInitCallback
import com.baidu.idl.face.platform.model.ImageInfo
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.AppUtils
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
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.MyApplication
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.AccountDangerEvent
import com.sdy.jitangapplication.faceliveness.FaceLivenessActivity
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import kotlinx.android.synthetic.main.activity_face_liveness.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import java.io.ByteArrayOutputStream


/**
 * 身份验证
 */
class IDVerifyActivity : FaceLivenessActivity() {
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
                        .setTitle(context1.getString(R.string.verify_notice))
                        .setContent(context1.getString(R.string.verify_compare_with_avatar))
                        .setConfirmText(context1.getString(R.string.ok))
                        .setCancelText(context1.getString(R.string.cancel))
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
                    .setTitle(context1.getString(R.string.verify_notice))
                    .setContent(context1.getString(R.string.verify_compare_with_avatar))
                    .setConfirmText(context1.getString(R.string.ok))
                    .setCancelText(context1.getString(R.string.cancel))
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.immersive(this)
        setSwipeBackEnable(type != TYPE_LIVE_CAPTURE)
        GlideUtil.loadCircleImg(this, UserManager.getAvator(), faceCoverIv)

        startFaceBtn.clickWithTrigger {
//            onResume()
            if (initFace)
                startPreview()
            else
                initVerify()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.isGranted(
                *PermissionConstants.getPermissions(PermissionConstants.CAMERA),
                *PermissionConstants.getPermissions(PermissionConstants.STORAGE)
            )
        ) {
            PermissionUtils.permission(PermissionConstants.CAMERA, PermissionConstants.STORAGE)
                .callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        initVerify()
                    }

                    override fun onDenied() {
                        CommonFunction.toast(getString(R.string.permission_camera))
                        finish()
                    }
                })
                .request()
        } else {
            initVerify()
        }


    }

    /**
     * 初始化人脸config
     */
    private var initFace = false


    private fun initVerify() {
        Log.d("VVV", "MD5 = ${AppUtils.getAppSignatureMD5()}")
        Log.d("VVV", "SHA1 = ${AppUtils.getAppSignatureSHA1()}")

        // 根据需求添加活体动作
        setFaceConfig()
        FaceSDKManager.getInstance().initialize(this, Constants.licenseID,
            Constants.licenseFileName, object : IInitCallback {
                override fun initSuccess() {
                    runOnUiThread {
                        initFace = true
                        Log.e(TAG, "初始化成功")
                    }
                }

                override fun initFailure(p0: Int, p1: String?) {
                    runOnUiThread {

                        Log.e(TAG, "初始化失败 = $p0  , $p1")
                    }
                }

            })
    }

    private fun setFaceConfig() {
        val config = FaceSDKManager.getInstance().faceConfig
        // SDK初始化已经设置完默认参数（推荐参数），也可以根据实际需求进行数值调整
        // 设置可检测的最小人脸阈值
//        config.minFaceSize = FaceEnvironment.VALUE_MIN_FACE_SIZE
        config.minFaceSize = 120
        // 设置可检测到人脸的阈值
        config.notFaceValue = FaceEnvironment.VALUE_NOT_FACE_THRESHOLD
        // 设置模糊度阈值
        config.blurnessValue = FaceEnvironment.VALUE_BLURNESS
        // 设置光照阈值（范围0-255）
        config.brightnessValue = FaceEnvironment.VALUE_BRIGHTNESS
        // 设置遮挡阈值
        config.occlusionValue = FaceEnvironment.VALUE_OCCLUSION
        // 设置人脸姿态角阈值
        config.headPitchValue = 45
        config.headYawValue = 45
        // 设置闭眼阈值
        config.eyeClosedValue = FaceEnvironment.VALUE_CLOSE_EYES
        // 设置图片缓存数量
        config.cacheImageNum = FaceEnvironment.VALUE_CACHE_IMAGE_NUM
        // 设置口罩判断开关以及口罩阈值
        config.isOpenMask = FaceEnvironment.VALUE_OPEN_MASK
        config.maskValue = FaceEnvironment.VALUE_MASK_THRESHOLD
        // 设置活体动作，通过设置list，LivenessTypeEunm.Eye, LivenessTypeEunm.Mouth,
        // LivenessTypeEunm.HeadUp, LivenessTypeEunm.HeadDown, LivenessTypeEunm.HeadLeft,
        // LivenessTypeEunm.HeadRight, LivenessTypeEunm.HeadLeftOrRight
        config.livenessTypeList = MyApplication.livenessList
        // 设置动作活体是否随机
        config.isLivenessRandom = false
        // 设置开启提示音
        config.isSound = false
        // 原图缩放系数
        config.scale = FaceEnvironment.VALUE_SCALE
        // 抠图高的设定，为了保证好的抠图效果，我们要求高宽比是4：3，所以会在内部进行计算，只需要传入高即可
        config.cropHeight = FaceEnvironment.VALUE_CROP_HEIGHT
        // 抠图人脸框与背景比例
        config.enlargeRatio = FaceEnvironment.VALUE_CROP_ENLARGERATIO
        // 加密类型，0：Base64加密，上传时image_sec传false；1：百度加密文件加密，上传时image_sec传true
        config.secType = FaceEnvironment.VALUE_SEC_TYPE
        FaceSDKManager.getInstance().faceConfig = config

    }

    override fun onLivenessCompletion(
        status: FaceStatusNewEnum, message: String,
        base64ImageCropMap: HashMap<String, ImageInfo>,
        base64ImageSrcMap: HashMap<String, ImageInfo>, currentLivenessCount: Int
    ) {
        super.onLivenessCompletion(
            status,
            message,
            base64ImageCropMap,
            base64ImageSrcMap,
            currentLivenessCount
        )
        if (status == FaceStatusNewEnum.OK && mIsCompletion) {
            onPause()
            uploadFaceImg()
        } else if (status == FaceStatusNewEnum.FaceLivenessActionCodeTimeout
            || status == FaceStatusNewEnum.DetectRemindCodeTimeout
        ) {
            onPause()
            faceNotice.text = getString(R.string.take_time_out)
            faceNotice.setTextColor(Color.parseColor("#fffb1919"))
            CommonFunction.toast(getString(R.string.take_time_out))
            onResume()
        }
    }

    private fun uploadFaceImg() {
        if (mBmpStr.isNotEmpty()) {
            val fileKey =
                "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME)
                    .getString(
                        "accid"
                    )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                    16
                )}"
            uploadProfile(bitmap2Bytes(mBmpStr), fileKey)
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
                    CommonFunction.toast(getString(R.string.verify_commit_fail))
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
                        OpenVipActivity.start(
                            this@IDVerifyActivity,
                            intent.getSerializableExtra("morematchbean") as MoreMatchBean?,
                            OpenVipActivity.FROM_REGISTER_OPEN_VIP
                        )

                    } else
                        when (t.code) {
                            200 -> {
                                CommonFunction.toast(getString(R.string.verify_commit_success))
                                UserManager.saveUserVerify(2)
                                UserManager.saveHasFaceUrl(true)
                                setResult(Activity.RESULT_OK, intent.putExtra("verify", 2))
                                finish()
                                if (intent.getIntExtra(
                                        "type",
                                        TYPE_ACCOUNT_NORMAL
                                    ) == TYPE_ACCOUNT_DANGER
                                ) {
                                    EventBus.getDefault().postSticky(
                                        AccountDangerEvent(
                                            AccountDangerDialog.VERIFY_ING
                                        )
                                    )
                                }
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
                    CommonFunction.toast(getString(R.string.verify_commit_fail))
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
