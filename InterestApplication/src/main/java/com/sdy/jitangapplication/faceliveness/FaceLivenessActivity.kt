package com.sdy.jitangapplication.faceliveness

import android.content.BroadcastReceiver
import android.content.Context
import android.graphics.*
import android.hardware.Camera
import android.hardware.Camera.ErrorCallback
import android.hardware.Camera.PreviewCallback
import android.media.AudioManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.baidu.idl.face.platform.*
import com.baidu.idl.face.platform.model.ImageInfo
import com.baidu.idl.face.platform.ui.FaceSDKResSettings
import com.baidu.idl.face.platform.ui.utils.BrightnessUtils
import com.baidu.idl.face.platform.ui.utils.CameraUtils
import com.baidu.idl.face.platform.ui.utils.VolumeUtils
import com.baidu.idl.face.platform.ui.widget.FaceDetectRoundView
import com.baidu.idl.face.platform.utils.APIUtils
import com.baidu.idl.face.platform.utils.Base64Utils
import com.baidu.idl.face.platform.utils.CameraPreviewUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.activity_face_liveness.*
import java.util.*


/**
 * 活体检测接口
 */
open class FaceLivenessActivity : BaseActivity(), SurfaceHolder.Callback,
    PreviewCallback, ErrorCallback, VolumeUtils.VolumeCallback, ILivenessStrategyCallback,
    ILivenessViewCallback {
    // View
    protected var mSurfaceView: SurfaceView? = null
    protected var mSurfaceHolder: SurfaceHolder? = null

    // 人脸信息
    protected lateinit var mFaceConfig: FaceConfig
    protected var mILivenessStrategy: ILivenessStrategy? = null

    // 显示Size
    private val mPreviewRect = Rect()
    protected var mDisplayWidth = 0
    protected var mDisplayHeight = 0
    protected var mSurfaceWidth = 0
    protected var mSurfaceHeight = 0

    // 状态标识
    @Volatile
    protected var mIsEnableSound = true
    protected var mIsCreateSurface = false
    protected var mIsCompletion = false

    // 相机
    protected var mCamera: Camera? = null
    protected var mCameraParam: Camera.Parameters? = null
    protected var mCameraId = 0
    protected var mPreviewWidth = 0
    protected var mPreviewHight = 0
    protected var mPreviewDegree = 0

    // 监听系统音量广播
    protected var mVolumeReceiver: BroadcastReceiver? = null
    var mBmpStr: String = ""
    private var mLivenessType: LivenessTypeEnum? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setScreenBright()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_face_liveness)
        val dm = DisplayMetrics()
        val display = this.windowManager.defaultDisplay
        display.getMetrics(dm)
        mDisplayWidth = dm.widthPixels
        mDisplayHeight = dm.heightPixels
        FaceSDKResSettings.initializeResId()
        mFaceConfig = FaceSDKManager.getInstance().faceConfig
        val am =
            getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val vol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        mIsEnableSound = if (vol > 0) mFaceConfig.isSound() else false

        mSurfaceView = SurfaceView(this)
        mSurfaceHolder = mSurfaceView!!.holder
        mSurfaceHolder!!.setSizeFromLayout()
        mSurfaceHolder!!.addCallback(this)
        mSurfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        val w = mDisplayWidth
        val h = mDisplayHeight
        val cameraFL = FrameLayout.LayoutParams(
            (w * FaceDetectRoundView.SURFACE_RATIO).toInt(),
            (h * FaceDetectRoundView.SURFACE_RATIO).toInt(),
            Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        )
        mSurfaceView!!.layoutParams = cameraFL
        mFrameLayout.addView(mSurfaceView)
    }

    /**
     * 设置屏幕亮度
     */
    private fun setScreenBright() {
        val currentBright =
            BrightnessUtils.getScreenBrightness(this)
        BrightnessUtils.setBrightness(
            this,
            currentBright + 100
        )
    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onRestart() {
        super.onRestart()
        Log.e(TAG, "onRestart")
    }

    public override fun onPause() {
        if (mILivenessStrategy != null) {
            mILivenessStrategy!!.reset()
        }
        VolumeUtils.unRegisterVolumeReceiver(this, mVolumeReceiver)
        mVolumeReceiver = null
        mFaceDetectRoundView.setProcessCount(
            0,
            mFaceConfig!!.livenessTypeList.size
        )
        super.onPause()
        stopPreview()
        mIsCompletion = false
    }

    public override fun onStop() {
        super.onStop()
    }

    override fun finish() {
        super.finish()
    }

    override fun volumeChanged() {
        try {
            val am =
                this.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (am != null) {
                val cv = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                mIsEnableSound = cv > 0
                //                mSoundView.setImageResource(mIsEnableSound
//                        ? R.mipmap.icon_titlebar_voice2 : R.mipmap.icon_titlebar_voice1);
                if (mILivenessStrategy != null) {
                    mILivenessStrategy!!.setLivenessStrategySoundEnable(mIsEnableSound)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun open(): Camera? {
        val camera: Camera
        val numCameras = Camera.getNumberOfCameras()
        if (numCameras == 0) {
            return null
        }
        var index = 0
        while (index < numCameras) {
            val cameraInfo =
                Camera.CameraInfo()
            Camera.getCameraInfo(index, cameraInfo)
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                break
            }
            index++
        }
        if (index < numCameras) {
            camera = Camera.open(index)
            mCameraId = index
        } else {
            camera = Camera.open(0)
            mCameraId = 0
        }
        return camera
    }

    protected fun startPreview() {
        volumeControlStream = AudioManager.STREAM_MUSIC
        mVolumeReceiver = VolumeUtils.registerVolumeReceiver(this, this)

        mFaceDetectRoundView.isVisible = true
        mFrameLayout.isVisible = true
        faceType.isVisible = true
        faceNotice.isVisible = true
        faceCoverIv.isInvisible = true
        faceBeginRl.isVisible = false

        faceType!!.text = "请将脸移入取景框"
        faceNotice!!.text = ""


        if (mSurfaceView != null && mSurfaceView!!.holder != null) {
            mSurfaceHolder = mSurfaceView!!.holder
            mSurfaceHolder!!.addCallback(this)
        }
        if (mCamera == null) {
            try {
                mCamera = open()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (mCamera == null) {
            return
        }
        if (mCameraParam == null) {
            mCameraParam = mCamera!!.parameters
        }
        mCameraParam!!.pictureFormat = PixelFormat.JPEG
        val degree = displayOrientation(this)
        mCamera!!.setDisplayOrientation(degree)
        // 设置后无效，camera.setDisplayOrientation方法有效
        mCameraParam!!["rotation"] = degree
        mPreviewDegree = degree
        val point = CameraPreviewUtils.getBestPreview(
            mCameraParam,
            Point(mDisplayWidth, mDisplayHeight)
        )
        mPreviewWidth = point.x
        mPreviewHight = point.y
        Log.e(
            TAG,
            "x = $mPreviewWidth y = $mPreviewHight"
        )
        // Preview 768,432
        if (mILivenessStrategy != null) {
            mILivenessStrategy!!.setPreviewDegree(degree)
        }
        mPreviewRect[0, 0, mPreviewHight] = mPreviewWidth
        mCameraParam!!.setPreviewSize(mPreviewWidth, mPreviewHight)
        mCamera!!.parameters = mCameraParam
        try {
            mCamera!!.setPreviewDisplay(mSurfaceHolder)
            mCamera!!.stopPreview()
            mCamera!!.setErrorCallback(this)
            mCamera!!.setPreviewCallback(this)
            mCamera!!.startPreview()
        } catch (e: RuntimeException) {
            e.printStackTrace()
            CameraUtils.releaseCamera(mCamera)
            mCamera = null
        } catch (e: Exception) {
            e.printStackTrace()
            CameraUtils.releaseCamera(mCamera)
            mCamera = null
        }
    }

    protected fun stopPreview() {
        if (mCamera != null) {
            try {
                mCamera!!.setErrorCallback(null)
                mCamera!!.setPreviewCallback(null)
                mCamera!!.stopPreview()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                CameraUtils.releaseCamera(mCamera)
                mCamera = null
            }
        }
        if (mSurfaceHolder != null) {
            mSurfaceHolder!!.removeCallback(this)
        }
        if (mILivenessStrategy != null) {
            mILivenessStrategy = null
        }
    }

    private fun displayOrientation(context: Context): Int {
        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val rotation = windowManager.defaultDisplay.rotation
        var degrees = 0
        degrees = when (rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        var result = (0 - degrees + 360) % 360
        if (APIUtils.hasGingerbread()) {
            val info = Camera.CameraInfo()
            Camera.getCameraInfo(mCameraId, info)
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360
            } else {
                result = (info.orientation - degrees + 360) % 360
            }
        }
        return result
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mIsCreateSurface = true
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
        mSurfaceWidth = width
        mSurfaceHeight = height
        if (holder.surface == null) {
            return
        }
//        startPreview()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mIsCreateSurface = false
    }

    override fun onPreviewFrame(
        data: ByteArray,
        camera: Camera
    ) {
        if (mIsCompletion) {
            return
        }
        if (mILivenessStrategy == null) {
            mILivenessStrategy = FaceSDKManager.getInstance().getLivenessStrategyModule(this)
            mILivenessStrategy!!.setPreviewDegree(mPreviewDegree)
            mILivenessStrategy!!.setLivenessStrategySoundEnable(mIsEnableSound)
            val detectRect = FaceDetectRoundView.getPreviewDetectRect(
                mDisplayWidth, mPreviewHight, mPreviewWidth
            )
            mILivenessStrategy!!.setLivenessStrategyConfig(
                mFaceConfig!!.livenessTypeList, mPreviewRect, detectRect, this
            )
        }
        mILivenessStrategy!!.livenessStrategy(data)
    }

    override fun onError(error: Int, camera: Camera) {}
    override fun onLivenessCompletion(
        status: FaceStatusNewEnum,
        message: String,
        base64ImageCropMap: HashMap<String, ImageInfo>,
        base64ImageSrcMap: HashMap<String, ImageInfo>,
        currentLivenessCount: Int
    ) {
        if (mIsCompletion) {
            return
        }
        onRefreshView(status, message, currentLivenessCount)
        if (status == FaceStatusNewEnum.OK) {
            mIsCompletion = true
            saveImage(base64ImageCropMap, base64ImageSrcMap)
            // saveAllImage(base64ImageCropMap, base64ImageSrcMap);
        }
    }

    private fun onRefreshView(
        status: FaceStatusNewEnum,
        message: String,
        currentLivenessCount: Int
    ) {
        when (status) {
            FaceStatusNewEnum.OK,
            FaceStatusNewEnum.FaceLivenessActionComplete,
            FaceStatusNewEnum.DetectRemindCodeTooClose,
            FaceStatusNewEnum.DetectRemindCodeTooFar,
            FaceStatusNewEnum.DetectRemindCodeBeyondPreviewFrame,
            FaceStatusNewEnum.DetectRemindCodeNoFaceDetected -> {
                // onRefreshTipsView(false, message);
                faceNotice.text = message
                faceNotice.setTextColor(Color.parseColor("#fffb1919"))
            }
            FaceStatusNewEnum.FaceLivenessActionTypeLiveEye,
            FaceStatusNewEnum.FaceLivenessActionTypeLiveMouth,
            FaceStatusNewEnum.FaceLivenessActionTypeLivePitchUp,
            FaceStatusNewEnum.FaceLivenessActionTypeLivePitchDown,
            FaceStatusNewEnum.FaceLivenessActionTypeLiveYawLeft,
            FaceStatusNewEnum.FaceLivenessActionTypeLiveYawRight,
            FaceStatusNewEnum.FaceLivenessActionTypeLiveYaw -> {
                faceType.text = message
            }
            FaceStatusNewEnum.DetectRemindCodePitchOutofUpRange,
            FaceStatusNewEnum.DetectRemindCodePitchOutofDownRange,
            FaceStatusNewEnum.DetectRemindCodeYawOutofLeftRange,
            FaceStatusNewEnum.DetectRemindCodeYawOutofRightRange -> {
                faceNotice.text = message
                faceNotice.setTextColor(Color.parseColor("#fffb1919"))
            }
            FaceStatusNewEnum.FaceLivenessActionCodeTimeout -> {
                //todo 提醒动作超时
                faceNotice.text = message
                faceNotice.setTextColor(Color.parseColor("#fffb1919"))
            }
            else -> {
                faceNotice.text = message
                faceNotice.setTextColor(Color.parseColor("#fffb1919"))
            }
        }
    }

    private fun saveImage(
        imageCropMap: HashMap<String, ImageInfo>?,
        imageSrcMap: HashMap<String, ImageInfo>?
    ) {
        if (imageCropMap != null && imageCropMap.size > 0) {
            val list1: List<Map.Entry<String, ImageInfo>> =
                ArrayList<Map.Entry<String, ImageInfo>>(imageCropMap.entries)
            Collections.sort(list1) { o1, o2 ->
                val key1 = o1.key.split("_".toRegex()).toTypedArray()
                val score1 = key1[2]
                val key2 = o2.key.split("_".toRegex()).toTypedArray()
                val score2 = key2[2]
                // 降序排序
                java.lang.Float.valueOf(score2).compareTo(java.lang.Float.valueOf(score1))
            }

            // TODO:发送加密的base64字符串
//            int secType = mFaceConfig.getSecType();
//            String base64;
//            if (secType == 0) {
//                base64 = mBmpStr;
//            } else {
//                base64 = list1.get(0).getValue().getSecBase64();
//            }
//            SecRequest.sendMessage(FaceDetectActivity.this, base64, secType);
        }
        if (imageSrcMap != null && imageSrcMap.size > 0) {
            val list2: List<Map.Entry<String, ImageInfo>> =
                ArrayList<Map.Entry<String, ImageInfo>>(imageSrcMap.entries)
            Collections.sort(list2) { o1, o2 ->
                val key1 =
                    o1.key.split("_".toRegex()).toTypedArray()
                val score1 = key1[2]
                val key2 =
                    o2.key.split("_".toRegex()).toTypedArray()
                val score2 = key2[2]
                // 降序排序
                java.lang.Float.valueOf(score2)
                    .compareTo(java.lang.Float.valueOf(score1))
            }
            mBmpStr = list2[0].value.base64
            // TODO:发送底层加密的base64字符串
//            int secType = mFaceConfig.getSecType();
//            String base64;
//            if (secType == 0) {
//                base64 = mBmpStr;
//            } else {
//                base64 = list2.get(0).getValue().getSecBase64();
//            }
//            SecRequest.sendMessage(FaceDetectActivity.this, base64, secType);
        }
    }

    override fun setCurrentLiveType(liveType: LivenessTypeEnum) {
        mLivenessType = liveType
    }

    override fun viewReset() {
        mFaceDetectRoundView.setProcessCount(0, 1)
    }

    companion object {
        val TAG = FaceLivenessActivity::class.java.simpleName
        fun base64ToBitmap(base64Data: String?): Bitmap {
            val bytes = Base64Utils.decode(
                base64Data,
                Base64Utils.NO_WRAP
            )
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}
