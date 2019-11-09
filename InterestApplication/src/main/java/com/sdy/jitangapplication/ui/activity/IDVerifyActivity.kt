package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import com.baidu.idl.face.platform.FaceEnvironment
import com.baidu.idl.face.platform.FaceSDKManager
import com.baidu.idl.face.platform.FaceStatusEnum
import com.baidu.idl.face.platform.LivenessTypeEnum
import com.baidu.idl.face.platform.ui.FaceLivenessActivity
import com.baidu.idl.face.platform.utils.Base64Utils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.kotlin.base.common.AppManager
import com.kotlin.base.common.BaseApplication.Companion.context
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.utils.NetWorkUtils
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.baselibrary.widgets.swipeback.SwipeBackLayout
import com.sdy.baselibrary.widgets.swipeback.Utils
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityBase
import com.sdy.baselibrary.widgets.swipeback.app.SwipeBackActivityHelper
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.MyApplication
import com.sdy.jitangapplication.model.AccessTokenBean
import com.sdy.jitangapplication.model.MatchFaceBean
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
import com.zhy.http.okhttp.OkHttpUtils
import com.zhy.http.okhttp.callback.Callback
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL


/**
 * 身份验证
 */
class IDVerifyActivity : FaceLivenessActivity(), SwipeBackActivityBase {
    private lateinit var mHelper: SwipeBackActivityHelper

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
        AppManager.instance.addActivity(this)
        mHelper = SwipeBackActivityHelper(this)
        mHelper.onActivityCreate()
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT)

        mImageLayout.visibility = View.VISIBLE
        // 根据需求添加活体动作
        MyApplication.livenessList.clear()
        MyApplication.livenessList.add(LivenessTypeEnum.Eye)
//        MyApplication.livenessList.add(LivenessTypeEnum.HeadDown)
        MyApplication.livenessList.add(LivenessTypeEnum.HeadLeft)
        MyApplication.livenessList.add(LivenessTypeEnum.HeadRight)
//        MyApplication.livenessList.add(LivenessTypeEnum.Mouth)
//        MyApplication.livenessList.add(LivenessTypeEnum.HeadUp)
//        MyApplication.livenessList.add(LivenessTypeEnum.HeadLeftOrRight)

        // 为了android和ios 区分授权，appId=appname_face_android ,其中appname为申请sdk时的应用名
        // 应用上下文
        // 申请License取得的APPID
        // assets目录下License文件名
        FaceSDKManager.getInstance().initialize(this, Constants.licenseID, Constants.licenseFileName)


        CommonFunction.toast("审核将与用户头像做比对，请确认头像为本人")
        //获取accesstoken
//        getAccessToken()
        //获取图片的base64
//        Thread(runnable).start()
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
        config.setCropFaceValue(FaceEnvironment.VALUE_CROP_FACE_SIZE)
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
                    "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
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
     */
    private fun uploadProfile(filePath: ByteArray, imagePath: String) {
        if (!NetWorkUtils.isNetWorkAvailable(this)) {
            return
        }
        QNUploadManager.getInstance().put(
            filePath, imagePath, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                Log.d("OkHttp", "token = ${SPUtils.getInstance(Constants.SPNAME).getString("qntoken")}")
                Log.d("OkHttp", "key=$key\ninfo=$info\nresponse=$response")
                if (info != null && info.isOK) {
                    savePersonal(
                        hashMapOf(
                            "token" to UserManager.getToken(),
                            "accid" to UserManager.getAccid(),
                            "face" to key
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
        params.putAll(UserManager.getBaseParams())
        RetrofitFactory.instance.create(Api::class.java)
            .savePersonal(params)
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    loadingDialog.dismiss()
                    when {
                        t.code == 200 -> {
                            CommonFunction.toast("审核提交成功")
                            UserManager.saveUserVerify(2)
                            setResult(Activity.RESULT_OK)
//                            finish()
                        }
                        t.code == 403 -> UserManager.startToLogin(context as Activity)
                        else -> {
                            CommonFunction.toast("认证审核提交失败，请重新进入认证")
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {

                    }
                    loadingDialog.dismiss()
                    CommonFunction.toast("认证审核提交失败，请重新进入认证")
                }
            })

    }


    /*-------------------by客户端-----------------------*/
    private var accessToken = ""
    private var avatorString: String? = null
    /**
     * 获取人脸验证的token
     * https://aip.baidubce.com/oauth/2.0/token
     * grant_type： 必须参数，固定为 client_credentials；
    client_id： 必须参数，应用的API Key；
    client_secret： 必须参数，应用的Secret Key；
     */
    private fun getAccessToken() {
        OkHttpUtils.post().url("https://aip.baidubce.com/oauth/2.0/token")
            .addParams("grant_type", "client_credentials")
            .addParams("client_id", Constants.apiKey)
            .addParams("client_secret", Constants.secretKey)
            .build()
            .execute(object : Callback<AccessTokenBean?>() {
                override fun onResponse(response: AccessTokenBean?, id: Int) {
                    if (response != null) {
                        accessToken = response.access_token ?: ""
                    }
                }

                override fun parseNetworkResponse(response: Response?, id: Int): AccessTokenBean? {
                    if (response != null) {
                        return Gson().fromJson(response.body()!!.string(), AccessTokenBean::class.java)
                    }
                    return null
                }

                override fun onError(call: Call?, e: Exception?, id: Int) {

                }

            })
    }

    private val loadingDialog by lazy { LoadingDialog(this) }

    /**
     * 获取人脸验证的token
     * https://aip.baidubce.com/rest/2.0/face/v2/match
     * access_token
     * images	是	string	分别base64编码后的2张图片数据，需urlencode，半角逗号分隔，单次请求最大不超过20M
     * ext_fields	否	string	返回质量信息，取值固定，目前支持qualities(质量检测)(对所有图片都会做改处理)
    image_liveness	否	string	返回的活体信息，“faceliveness,faceliveness” 表示对比对的两张图片都做活体检测；“,faceliveness” 表示对第一张图片不做活体检测、第二张图做活体检测；“faceliveness,” 表示对第一张图片做活体检测、第二张图不做活体检测；
    注：需要用于判断活体的图片，图片中的人脸像素面积需要不小于100px*100px，人脸长宽与图片长宽比例，不小于1/3
    types	否	string	请求对比的两张图片的类型，示例：“7，13”
    7表示生活照：通常为手机、相机拍摄的人像图片、或从网络获取的人像图片等
    11表示身份证芯片照：二代身份证内置芯片中的人像照片
    12表示带水印证件照：一般为带水印的小图，如公安网小图
    13表示证件照片：如拍摄的身份证、工卡、护照、学生证等证件图片，注：需要确保人脸部分不可太小，通常为100px*100px
     */
    private fun matchFace(img1: String?, img2: String?) {
        if (img1.isNullOrEmpty() || img2.isNullOrEmpty()) {
            return
        }
        val images = arrayListOf<HashMap<String, String>>()
        val map1 = hashMapOf<String, String>()
        map1.put("image", img1)
        map1.put("image_type", "BASE64")
        map1.put("face_type", "LIVE")
        map1.put("quality_control", "LOW")
        map1.put("liveness_control", "NORMAL")

        val map2 = hashMapOf<String, String>()
        map2.put("image", img2)
        map2.put("image_type", "BASE64")
        map2.put("face_type", "LIVE")
        map2.put("quality_control", "LOW")
        map2.put("liveness_control", "NORMAL")
        images.add(map1)
        images.add(map2)
        val params = GsonUtils.toJson(images)



        OkHttpUtils.postString()
            .url("https://aip.baidubce.com/rest/2.0/face/v3/match?access_token=${accessToken}")
            .addHeader("Content-Type", "application/json")
            .content(params)
            .build()
            .execute(object : Callback<MatchFaceBean?>() {
                override fun onBefore(request: Request?, id: Int) {
                }

                override fun onResponse(response: MatchFaceBean?, id: Int) {
                    loadingDialog.dismiss()

                    Log.d("OkHttp", response.toString())
                    if (response?.result?.score != null && response?.result.score >= 80) {
                        loadingDialog.dismiss()
                        CommonFunction.toast("认证成功！")
//                        finish()
                    } else {
                        CommonFunction.toast("认证失败！")
                    }
                }

                override fun parseNetworkResponse(response: Response?, id: Int): MatchFaceBean? {
                    if (response != null) {
                        Log.d("OkHttp", response.toString())
                        return Gson().fromJson(response.body()!!.string(), MatchFaceBean::class.java)
                    }
                    return null
                }

                override fun onError(call: Call?, e: java.lang.Exception?, id: Int) {
                    loadingDialog.dismiss()
                    CommonFunction.toast("认证失败！")

                    Log.d("OkHttp", "${id},${e?.message}")
                }
            })

    }


    /**
     * 获取网络图片并转为Base64编码
     *
     * @param url
     * 网络图片路径
     * @return base64编码
     * @throws Exception
     */
    private val runnable = Runnable {
        try {
            val u = URL(UserManager.getAvator())
            // 打开图片路径
            val conn = u.openConnection() as HttpURLConnection
            // 设置请求方式为GET
            conn.requestMethod = "GET"
            // 设置超时响应时间为5秒
            conn.connectTimeout = 5000
            // 通过输入流获取图片数据
            val inStream = conn.inputStream

            val outStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var len = 0

            do {
                outStream.write(buffer, 0, len)
                len = inStream.read(buffer)
            } while (len != -1)
            inStream.close()
            // 读取图片字节数组
            val data = outStream.toByteArray()
            // 对字节数组Base64编码
            avatorString = Base64Utils.encodeToString(data, Base64Utils.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


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

    }
}
