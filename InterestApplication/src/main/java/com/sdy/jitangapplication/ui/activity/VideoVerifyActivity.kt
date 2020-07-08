package com.sdy.jitangapplication.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ImageFormat
import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.kotlin.base.utils.NetWorkUtils
import com.luck.picture.lib.config.PictureMimeType

import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.FemaleVideoEvent
import com.sdy.jitangapplication.event.UpdateApproveEvent
import com.sdy.jitangapplication.model.CopyMvBean
import com.sdy.jitangapplication.model.VideoVerifyBannerBean
import com.sdy.jitangapplication.nim.uikit.common.ToastHelper
import com.sdy.jitangapplication.nim.uikit.common.media.imagepicker.camera.CameraPreview
import com.sdy.jitangapplication.nim.uikit.common.media.imagepicker.camera.CameraUtils
import com.sdy.jitangapplication.nim.uikit.common.media.imagepicker.camera.ConfirmationDialog
import com.sdy.jitangapplication.nim.uikit.common.media.imagepicker.camera.ErrorDialog
import com.sdy.jitangapplication.nim.uikit.common.util.sys.TimeUtil
import com.sdy.jitangapplication.presenter.VideoVerifyPresenter
import com.sdy.jitangapplication.presenter.view.VideoVerifyView
import com.sdy.jitangapplication.ui.dialog.VerifyForceDialog
import com.sdy.jitangapplication.ui.dialog.VideoIntroduceBeforeDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_video_verify.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.textColor
import java.io.File
import java.io.IOException


/**
 * 视频认证界面
 */
class VideoVerifyActivity : BaseMvpActivity<VideoVerifyPresenter>(), VideoVerifyView,
    OnLazyClickListener {
    companion object {
        val RATIO = 500 / 375F //拍摄比例
        val TAG = VideoVerifyActivity::class.java.simpleName
        val VIDEO_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        const val VIDEO_PERMISSIONS_REQUEST_CODE = 1
        const val RESULT_CODE_RECORD_VIDEO = 1006
        const val RESULT_CODE_CHOOSE_VIDEO = 1007
        const val RESULT_CODE_CONFIRM_VIDEO = 1008
        const val RESULT_CODE_CONFIRM_IMAGE = 1009
        const val EXTRA_RESULT_ITEMS = "extra_result_items"
        const val RESULT_EXTRA_CONFIRM_IMAGES = "RESULT_EXTRA_CONFIRM_IMAGES"


        const val RECORD_MAX_TIME = 30 //录制的总时长秒数，单位秒，默认30秒
        const val RECORD_MIN_TIME = 5 //最小录制时长，单位秒，默认1秒
        const val REQUEST_VIDEO_PERMISSIONS = 1
        const val PERMISSIONS_FRAGMENT_DIALOG = "permission_dialog"

        fun start(context1: Context, requestCode: Int = -1) {
            if (Build.VERSION.SDK_INT < 21) {
                ToastHelper.showToast(context1, "当前系统版本暂不支持视频拍摄功能")
                return
            }
            VideoIntroduceBeforeDialog(context1, requestCode).show()

        }
    }

    private lateinit var mainHandler: Handler
    private var mCamera: Camera? = null
    private var cameraId: Int = 0
    private var mPreview: CameraPreview? = null
    private var mMediaRecorder: MediaRecorder? = null
    private var longPressRunnable: LongPressRunnable? = null
    private var isAction = false
    private var isRecording = false
    private var isCameraFront = true //当前是否是前置摄像头
    private var currentTime = 0
    private var videoSavePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_verify)
        initView()
        switchMvCopy()

    }

    private var switchIndex = -1
    private val mvCopy: MutableList<VideoVerifyBannerBean> by lazy {
        (intent.getSerializableExtra("copyMv") as CopyMvBean?)?.list ?: mutableListOf()
    }

    private fun initView() {
        mPresenter = VideoVerifyPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        StatusBarUtil.immersive(this)
        mainHandler = Handler()


//        setupTouchListener()
        btnBack.setOnClickListener(this)
        switchRecordContentBtn.setOnClickListener(this)
        captureButton.setOnClickListener(this)
        chooseVideoBtn.setOnClickListener(this)
        turnCameraBtn.setOnClickListener(this)
        longPressRunnable = LongPressRunnable()

        llTitle.setBackgroundResource(R.color.colorTransparent)
        hotT1.text = "视频介绍"
        hotT1.textColor = Color.WHITE
        btnBack.setImageResource(R.drawable.icon_back_white)
    }

    fun setVideoRatio() {
        val params = camera_preview.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
        params.height = (RATIO * ScreenUtils.getScreenWidth()).toInt()
        camera_preview.layoutParams = params
    }

    override fun onResume() {
        super.onResume()
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions()
            return
        } else {
            setVideoRatio()
            setupSurfaceIfNeeded()
            setupCamera()
        }
    }

    private fun setupSurfaceIfNeeded() {
        if (mPreview != null) {
            return
        }
        // Create our Preview view and set it as the content of our activity.
        mPreview = CameraPreview(this)
        //设置界面的大小
        mPreview?.holder?.setFixedSize(
            (RATIO * ScreenUtils.getScreenWidth()).toInt(),
            ScreenUtils.getScreenWidth()
        )

        camera_preview.addView(mPreview)
    }

    private fun setupCamera() {
        // Create an instance of Camera
        val pair = CameraUtils.getCameraInstance(isCameraFront)
        mCamera = pair.first
        cameraId = pair.second
        if (mCamera == null) {
            ToastHelper.showToast(this, "设备异常")
            finish()
        }
        // get Camera parameters
        val params = mCamera!!.parameters
        val focusModes = params.supportedFocusModes
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // Autofocus mode is supported
            // set the focus mode
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
        }
        val choosePictureSize = CameraUtils.choosePictureSize(params.supportedPictureSizes)
        params.setPictureSize(choosePictureSize.width, choosePictureSize.getHeight())
//        params.setPreviewSize(choosePictureSize.width, choosePictureSize.getHeight())
        params.pictureFormat = ImageFormat.JPEG
        params.setRotation(CameraUtils.getPictureRotation(this, cameraId))
        val displayOrientation = CameraUtils.getDisplayOrientation(this, cameraId, mCamera, false)
        mCamera?.setDisplayOrientation(displayOrientation)
        // set Camera parameters
        mCamera?.parameters = params
        mPreview?.setCamera(mCamera, isCameraFront)
    }


    override fun onPause() {
        super.onPause()
        isAction = false
        // stop recording and release camera
        try {
            mMediaRecorder?.stop()    // stop the recording
        } catch (stopException: RuntimeException) {
        }
        releaseMediaRecorder()  // release the MediaRecorder object
        mCamera?.lock()          // take camera access back from MediaRecorder
        isRecording = false
        mainHandler.removeCallbacks(progressRunnable)
        stopButtonAnimation()
        mProgressView.reset()
        releaseCamera()

    }


    private fun switchCamera() {
        mCamera!!.stopPreview()
        releaseCamera()
        isCameraFront = !isCameraFront
        mPreview = null
        setVideoRatio()
        setupSurfaceIfNeeded()
        setupCamera()
        mCamera?.startPreview()
    }

    private fun setupProfile() {
        when {
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P) -> {
                val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_1080P)
                mMediaRecorder?.setProfile(profile)
                mMediaRecorder?.setVideoSize(640, 480)
                mMediaRecorder?.setVideoEncodingBitRate(profile.videoBitRate / 8)
            }
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P) -> {
                val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P)
                mMediaRecorder?.setProfile(profile)
                mMediaRecorder?.setVideoSize(640, 480)
                mMediaRecorder?.setVideoEncodingBitRate(profile.videoBitRate / 8)
            }
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P) -> {
                val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P)
                mMediaRecorder?.setProfile(profile)
                mMediaRecorder?.setVideoSize(640, 480)
                mMediaRecorder?.setVideoEncodingBitRate(profile.videoBitRate / 8)
            }
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA) -> {
                val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA)
                mMediaRecorder?.setProfile(profile)
                mMediaRecorder?.setVideoSize(640, 480)
                mMediaRecorder?.setVideoEncodingBitRate(profile.videoBitRate / 8)
            }
            CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF) -> {
                val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_CIF)
                mMediaRecorder?.setProfile(profile)
                mMediaRecorder?.setVideoSize(640, 480)
                mMediaRecorder?.setVideoEncodingBitRate(profile.videoBitRate / 8)
            }
            else -> {
                mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
                mMediaRecorder?.setVideoFrameRate(30)
                mMediaRecorder?.setVideoSize(640, 480)
                mMediaRecorder?.setVideoEncodingBitRate((1.5 * 1000 * 1000).toInt())
                mMediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)
                mMediaRecorder?.setAudioEncodingBitRate(96000)
                mMediaRecorder?.setAudioChannels(1)
                mMediaRecorder?.setAudioSamplingRate(48000)
                mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
            }
        }


    }


    private fun handleActionUpByState() {
        mainHandler.removeCallbacks(longPressRunnable) //移除长按逻辑的Runnable
        //根据当前状态处理
        if (isRecording) {
            stopMediaRecorder()
        }
    }

    private fun prepareVideoRecorder(): Boolean {
        mMediaRecorder = MediaRecorder()
        try {
            // Step 1: Unlock and set camera to MediaRecorder
            mCamera?.unlock()
            mMediaRecorder!!.setCamera(mCamera)
            val degrees = CameraUtils.getDisplayOrientation(this, cameraId, mCamera, true)
            mMediaRecorder!!.setOrientationHint(degrees)
            // Step 2: Set sources
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.CAMERA)
            // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
            setupProfile()
//        mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            // Step 4: Set output file
            val now = TimeUtil.getNow_millisecond()
            videoSavePath =
                CameraUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO, now.toString()).getAbsolutePath()
            mMediaRecorder!!.setOutputFile(videoSavePath)
            // Step 5: Set the preview output
            mMediaRecorder!!.setPreviewDisplay(mPreview?.holder?.surface)
            // Step 6: Prepare configured MediaRecorder
            mMediaRecorder!!.prepare()
        } catch (e: IllegalStateException) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        } catch (e: IOException) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        } catch (e: NullPointerException) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        } catch (e: RuntimeException) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.message)
            releaseMediaRecorder()
            return false
        }
        return true
    }

    private fun startMediaRecorder() {
        // Camera is available and unlocked, MediaRecorder is prepared,
        // now you can start recording
        mMediaRecorder?.start()
        isRecording = true

        startButtonAnimation()
        currentTime = 0
        mainHandler.postDelayed(progressRunnable, 0)
    }

    private fun stopMediaRecorder() {
        // stop recording and release camera
        try {
            mMediaRecorder?.stop()    // stop the recording
        } catch (stopException: RuntimeException) {
        }
        releaseMediaRecorder()  // release the MediaRecorder object
        mCamera?.lock()          // take camera access back from MediaRecorder
        isRecording = false
        mainHandler.removeCallbacks(progressRunnable)
        stopButtonAnimation()
        mProgressView.reset()
        if (currentTime <= RECORD_MIN_TIME) {
            CommonFunction.toast("录制时间不可小于5S")
        } else {
            startActivityForResult<VideoVerifyConfirmActivity>(
                VideoVerifyConfirmActivity.RESULT_CODE_CONFIRM_VIDEO,
                "ratio" to RATIO,
                "path" to videoSavePath,
                "duration" to currentTime * 1000L
            )
        }
    }

    private fun releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder!!.reset()    // clear recorder configuration
            mMediaRecorder!!.release()  // release the recorder object
            mMediaRecorder = null
            mCamera?.lock()            // lock camera for later use
        }
    }

    fun releaseCamera() {
        if (mCamera != null) {
            mCamera!!.release()         // release the camera for other applications
            mCamera = null
        }
    }

    inner class LongPressRunnable : Runnable {
        override fun run() {
            // initialize video camera
            if (prepareVideoRecorder()) {
                startMediaRecorder()
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder()
                // inform user
            }
        }
    }

    private val progressRunnable: Runnable by lazy {
        Runnable {
            currentTime++
            Log.i(TAG, "recordRunnable currentTime:" + currentTime)
            //如果超过最大录制时长则自动结束
            if (currentTime > RECORD_MAX_TIME) {
                isAction = false
                stopMediaRecorder()
            } else {
                mainHandler.postDelayed(progressRunnable, 1000)
            }
        }
    }

    private val animatorSet by lazy {
        //组合动画
        AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(view1, "scaleX", 1F, 2f, 1F)
            scaleX.repeatCount = -1
            val scaleY = ObjectAnimator.ofFloat(view1, "scaleY", 1F, 2f, 1F)
            scaleY.repeatCount = -1
            duration = 1000
            interpolator = LinearInterpolator()
            play(scaleX).with(scaleY) //两个动画同时开始
        }

    }

    //开始按下按钮动画
    private fun startButtonAnimation() {
        captureButton.setImageResource(R.drawable.icon_video_verify_pause)
        tvBalanceTime.base = SystemClock.elapsedRealtime()
        tvBalanceTime.start()
        animatorSet.start()
    }

    //停止按下按钮动画
    private fun stopButtonAnimation() {
        captureButton.setImageResource(R.drawable.icon_video_verify_start)
        animatorSet.cancel()
        tvBalanceTime.stop()
        tvBalanceTime.base = SystemClock.elapsedRealtime()

    }


    private fun requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            ConfirmationDialog().show(getFragmentManager(), PERMISSIONS_FRAGMENT_DIALOG)
        } else {
            ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS)
        }
    }

    private fun hasPermissionsGranted(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun shouldShowRequestPermissionRationale(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.size == VIDEO_PERMISSIONS.size) {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance(getString(R.string.permission_request)).show(
                            getFragmentManager(),
                            PERMISSIONS_FRAGMENT_DIALOG
                        )
                        break
                    }
                }
            } else {
                ErrorDialog.newInstance(getString(R.string.permission_request)).show(
                    getFragmentManager(),
                    PERMISSIONS_FRAGMENT_DIALOG
                )
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.switchRecordContentBtn -> {
                switchMvCopy()
            }
            R.id.btnBack -> {
                onBackPressed()
            }
            R.id.turnCameraBtn -> {
                switchCamera()
            }
            R.id.chooseVideoBtn -> {
                CommonFunction.onTakePhoto(
                    this,
                    1,
                    RESULT_CODE_CHOOSE_VIDEO,
                    PictureMimeType.ofVideo()
                )
            }
            R.id.captureButton -> {
                if (!isRecording) {
                    isAction = true
                    isRecording = false
                    mainHandler.post(longPressRunnable) //同时延长500启动长按后处理的逻辑Runnable
                } else {
                    if (isAction) {
                        isAction = false
                        handleActionUpByState()
                    }
                }
            }
        }
    }

    private fun switchMvCopy() {
        if (mvCopy.isNotEmpty()) {
            if (switchIndex < mvCopy.size - 1 && switchIndex >= 0) {
                switchIndex += 1
            } else {
                switchIndex = 0
            }
            recordTitle.text = mvCopy[switchIndex].title
            recordContent.text = mvCopy[switchIndex].content
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_CODE_CONFIRM_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                uploadProfile(videoSavePath)
            } else {
                File(videoSavePath).delete()
            }
        }
    }

    /**
     * 上传照片
     * imagePath 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     * face_source_type是否是消息过来的上传 1是 0否
     */
    private fun uploadProfile(filePath: String) {
        val fileKey =
            "${Constants.FILE_NAME_INDEX}${Constants.VIDEOFACE}${UserManager.getAccid()}/" +
                    "${System.currentTimeMillis()}/${RandomUtils.getRandomString(16)}"
        if (!NetWorkUtils.isNetWorkAvailable(this)) {
            return
        }
        mPresenter.loadingDialog.show()
        QNUploadManager.getInstance().put(
            filePath, fileKey, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                Log.d("OkHttp", "key=$key\ninfo=$info\nresponse=$response")
                if (info != null && info.isOK) {
                    //视频上传成功
                    mPresenter.uploadMv(
                        hashMapOf(
                            "mv_url" to key,
                            "normal_id" to if (switchIndex > -1 && mvCopy.size > switchIndex) {
                                mvCopy[switchIndex].id
                            } else {
                                0
                            }
                        )
                    )
                } else {
                    CommonFunction.toast("视频提交失败，请重新进入录制")
                }
            }, null
        )
    }

    override fun onUpdateFaceInfo(code: Int) {
        when (code) {
            200 -> {
                VerifyForceDialog(this, VerifyForceDialog.VIDEO_INTRODUCE_GOING).show()

                //聊天页面刷新认证数据数据
                EventBus.getDefault().post(UpdateApproveEvent())

                //更新录制视频介绍
                UserManager.my_mv_url = true
                EventBus.getDefault().post(FemaleVideoEvent(2))
            }
        }


    }


}
