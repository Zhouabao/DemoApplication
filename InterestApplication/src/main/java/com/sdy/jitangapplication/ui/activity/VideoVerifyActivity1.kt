package com.sdy.jitangapplication.ui.activity

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.kotlin.base.utils.NetWorkUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nim.uikit.common.media.imagepicker.camera.ConfirmationDialog
import com.netease.nim.uikit.common.media.imagepicker.camera.ErrorDialog
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.camera_filter.ConstantFilters
import com.sdy.jitangapplication.camera_filter.callback.LoadAssetsImageCallback
import com.sdy.jitangapplication.camera_filter.dialog.DialogFilter
import com.sdy.jitangapplication.camera_filter.listener.EndRecordingFilterCallback
import com.sdy.jitangapplication.camera_filter.listener.StartRecordingFilterCallback
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.event.FemaleVideoEvent
import com.sdy.jitangapplication.event.UpdateApproveEvent
import com.sdy.jitangapplication.event.VideoTrimmerEvent
import com.sdy.jitangapplication.model.CopyMvBean
import com.sdy.jitangapplication.model.VideoVerifyBannerBean
import com.sdy.jitangapplication.presenter.VideoVerifyPresenter
import com.sdy.jitangapplication.presenter.view.VideoVerifyView
import com.sdy.jitangapplication.ui.dialog.VerifyForceDialog
import com.sdy.jitangapplication.ui.dialog.VideoIntroduceBeforeDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_video_verify1.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.textColor
import org.wysaid.myUtils.ImageUtil
import org.wysaid.nativePort.CGENativeLibrary
import java.io.File


/**
 * 视频认证界面
 */
class VideoVerifyActivity1 : BaseMvpActivity<VideoVerifyPresenter>(), VideoVerifyView,
    OnLazyClickListener {
    companion object {
        val RATIO = 500 / 375F //拍摄比例
        val TAG = VideoVerifyActivity1::class.java.simpleName
        val VIDEO_PERMISSIONS =
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        const val RESULT_CODE_CHOOSE_VIDEO = 1007
        const val RESULT_CODE_CONFIRM_VIDEO = 1008


        const val RECORD_MAX_TIME = 15 //录制的总时长秒数，单位秒，默认30秒
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

    private val filterDialog by lazy { DialogFilter(this) }
    private val mStartRecordingFilterCallback by lazy {
        object : StartRecordingFilterCallback(this) {
            override fun startRecordingOver(success: Boolean) {
//            super.startRecordingOver(success)
                if (success) {
                    CommonFunction.toast("开始录制视频")
                    runOnUiThread {
                        chooseVideoBtn.isEnabled = false
                    }
                } else {
                    CommonFunction.toast("录制视频失败")
                }
            }
        }
    }
    private val mEndRecordingFilterCallback by lazy {
        object : EndRecordingFilterCallback(this) {
            override fun endRecordingOK() {
                super.endRecordingOK()
                runOnUiThread {
                    chooseVideoBtn.isEnabled = true
                }
                startActivityForResult<VideoVerifyConfirmActivity>(
                    VideoVerifyConfirmActivity.RESULT_CODE_CONFIRM_VIDEO,
                    "ratio" to RATIO,
                    "path" to videoSavePath,
                    "duration" to currentTime * 1000L
                )
            }
        }
    }
    private lateinit var mainHandler: Handler
    private var longPressRunnable: LongPressRunnable? = null
    private var isAction = false
    private var isRecording = false
    private var currentTime = 0
    private var videoSavePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_verify1)
        initView()
        initListener()
        switchMvCopy()

    }

    private fun initListener() {
        btnBack.setOnClickListener(this)
        rightBtn2.setOnClickListener(this)
        switchRecordContentBtn.setOnClickListener(this)
        captureButton.setOnClickListener(this)
        chooseVideoBtn.setOnClickListener(this)
        turnCameraBtn.setOnClickListener(this)

        //滤镜对话框选择滤镜的监听
        filterDialog.setOnFilterChangedListener {
            camera_preview.setFilterWithConfig(ConstantFilters.FILTERS[it])
        }

        filterDialog.setOnShowListener {
//            recordCl.animate().alpha(0F).setDuration(1000L).start()
        }


        filterDialog.setOnDismissListener {
//            recordCl.animate().alpha(1F).setDuration(1000L).start()
        }
    }

    private var switchIndex = -1
    private val mvCopy: MutableList<VideoVerifyBannerBean> by lazy {
        (intent.getSerializableExtra("copyMv") as CopyMvBean?)?.list ?: mutableListOf()
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = VideoVerifyPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        StatusBarUtil.immersive(this)
        mainHandler = Handler()

        llTitle.setBackgroundResource(R.color.colorTransparent)
        hotT1.text = "视频介绍"
        hotT1.textColor = Color.WHITE
        rightBtn2.isVisible = true
        rightBtn2.setImageResource(R.drawable.icon_filter_gallery)
        btnBack.setImageResource(R.drawable.icon_back_white)

        CGENativeLibrary.setLoadImageCallback(LoadAssetsImageCallback(this), null)

        //设置摄像头方向
        camera_preview.presetCameraForward(false)
        //录制视频大小
        camera_preview.presetRecordingSize(480, 640)
        val params = camera_preview.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth()
        params.height = (VideoVerifyActivity.RATIO * ScreenUtils.getScreenWidth()).toInt()
        camera_preview.layoutParams = params
        //拍照大小
        camera_preview.setPictureSize(2048, 2048, true)
        //充满view
        camera_preview.setFitFullView(true)
//        setupTouchListener()

        longPressRunnable = LongPressRunnable()


    }


    private fun handleActionUpByState() {
        mainHandler.removeCallbacks(longPressRunnable) //移除长按逻辑的Runnable
        //根据当前状态处理
        if (isRecording) {
            stopMediaRecorder()
        }
    }


    private fun startMediaRecorder() {
        isRecording = true
        startButtonAnimation()
        currentTime = 0
        mainHandler.postDelayed(progressRunnable, 0)

        videoSavePath = ImageUtil.getPath() + "/" + System.currentTimeMillis() + ".mp4"
//        videoSavePath = CameraUtils.getOutputMediaFile(
//            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
//            System.currentTimeMillis().toString()
//        ).getAbsolutePath()
        mEndRecordingFilterCallback.setVideoFilePath(videoSavePath)
        camera_preview.startRecording(videoSavePath, mStartRecordingFilterCallback)
    }

    private fun stopMediaRecorder() {
        if (currentTime <= RECORD_MIN_TIME) {
            CommonFunction.toast("录制时间不可小于5S")
            isAction = true
            return
        }
        isRecording = false
        mainHandler.removeCallbacks(progressRunnable)
        stopButtonAnimation()
        mProgressView.reset()
//        mEndRecordingFilterCallback.endRecordingOK()
        camera_preview.endRecording(mEndRecordingFilterCallback)

    }

    inner class LongPressRunnable : Runnable {
        override fun run() {
            startMediaRecorder()
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
                camera_preview.switchCamera()
//                switchCamera()
            }
            R.id.rightBtn2 -> {
                filterDialog.show()
            }
            R.id.chooseVideoBtn -> {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    CommonFunction.onTakePhoto(
                        this,
                        1,
                        RESULT_CODE_CHOOSE_VIDEO,
                        PictureMimeType.ofVideo(),
                        minSeconds = RECORD_MIN_TIME,
                        maxSeconds = RECORD_MAX_TIME
                    )
                } else
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
        if (requestCode == RESULT_CODE_CONFIRM_VIDEO) { //视频拍摄预览回调
            if (resultCode == Activity.RESULT_OK) {
                uploadProfile(videoSavePath)
            } else {
                File(videoSavePath).delete()
            }
        } else if (requestCode == RESULT_CODE_CHOOSE_VIDEO) {//视频选择成功
            if (resultCode == Activity.RESULT_OK) {
                videoSavePath =
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P && !PictureSelector.obtainMultipleResult(
                            data
                        )[0].androidQToPath.isNullOrEmpty()
                    ) {
                        PictureSelector.obtainMultipleResult(data)[0].androidQToPath
                    } else {
                        PictureSelector.obtainMultipleResult(data)[0].path
                    }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                    startActivityForResult<VideoVerifyConfirmActivity>(
                        VideoVerifyConfirmActivity.RESULT_CODE_CONFIRM_VIDEO,
                        "ratio" to RATIO,
                        "path" to videoSavePath,
                        "duration" to PictureSelector.obtainMultipleResult(data)[0].duration
                    )
                else
                    VideoTrimmerActivity.start(this, videoSavePath)

//

            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions()
            return
        } else {
            camera_preview.resumePreview()
        }
    }


    override fun onPause() {
        super.onPause()
        isAction = false
        isRecording = false
        camera_preview.stopPreview()
        stopButtonAnimation()
        mProgressView.reset()

    }

    override fun onDestroy() {
        super.onDestroy()
        camera_preview.release(null)
        EventBus.getDefault().unregister(this)
    }


    /**
     * 上传照片
     * imagePath 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     * face_source_type是否是消息过来的上传 1是 0否
     */
    private fun uploadProfile(filePath: String, fromCrop: Boolean = false) {
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
                    if (fromCrop)
                        File(filePath).delete()
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onVideoTrimmerEvent(event: VideoTrimmerEvent) {
        if (!event.filePath.isNullOrEmpty()) {
            uploadProfile(event.filePath, true)
        }
    }


}
