package com.sdy.jitangapplication.ui.activity

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.core.view.isVisible
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.DatingOptionsBean
import com.sdy.jitangapplication.player.MediaPlayerHelper
import com.sdy.jitangapplication.player.MediaRecorderHelper
import com.sdy.jitangapplication.player.UpdateVoiceTimeThread
import com.sdy.jitangapplication.presenter.CompleteDatingInfoPresenter
import com.sdy.jitangapplication.presenter.view.CompleteDatingInfoView
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.utils.UriUtils
import kotlinx.android.synthetic.main.activity_complete_dating_info.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity


/**
 * 完善约会信息
 */
class CompleteDatingInfoActivity : BaseMvpActivity<CompleteDatingInfoPresenter>(),
    CompleteDatingInfoView, OnLazyClickListener {
    private val dating_type by lazy { intent.getIntExtra("dating_type", -1) }
    private var typeText = true //文本限制

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_dating_info)

        initView()

        mPresenter.datingOptions()
    }

    private fun initView() {
        mPresenter = CompleteDatingInfoPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = "完善约会信息"
        btnBack.clickWithTrigger {
            finish()
        }
        rightBtn1.text = "发布"
        rightBtn1.isVisible = true

        switchContentTypeBtn.setOnClickListener(this)
        chooseDatingPlaceBtn.setOnClickListener(this)
        chooseDatingPlanBtn.setOnClickListener(this)
        chooseDatingObjectBtn.setOnClickListener(this)
        chooseDatingPayBtn.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)

        initAudioCl()

    }

    /**************** * 初始化录音控件 录音时间在5S~1M之间********************/
    private var mIsRecorder = false
    private var mIsPreview = false

    //是否显示顶部预览
    private var isTopPreview = false
    private var countTimeThread: CountDownTimer? = null
    private var mPreviewTimeThread: UpdateVoiceTimeThread? = null
    private lateinit var mMediaRecorderHelper: MediaRecorderHelper
    private var totalSecond = 0
    private var currentActionState = MediaRecorderHelper.ACTION_NORMAL

    //判断是否是第一次点击上部分预览界面的播放按钮
    private var click = false
    private fun initAudioCl() {
        revertRecord.setOnClickListener(this)
        startRecordBtn.setOnClickListener(this)
        finishRecord.setOnClickListener(this)
        restartRecordBtn.setOnClickListener(this)
        mMediaRecorderHelper = MediaRecorderHelper(this)
        myDatingAudioView.setUi()

        //开启录音计时线程
        countTimeThread = object : CountDownTimer(1 * 60 * 1000, 1000) {
            override fun onFinish() {
                switchActionState()
            }

            override fun onTick(millisUntilFinished: Long) {
                totalSecond++
                if (!mIsRecorder) {
                    countTimeThread?.cancel()
                }

                recordTime.text = UriUtils.getShowTime(totalSecond)
                recordTime.setTextColor(resources.getColor(R.color.colorOrange))

            }
        }

    }

    /**
     * 切换录音ACTION状态
     */
    private fun switchActionState() {
        mIsRecorder = false
        if (currentActionState == MediaRecorderHelper.ACTION_NORMAL) {
            currentActionState = MediaRecorderHelper.ACTION_RECORDING
            playSmallAnimation()
            //开始录音
            mMediaRecorderHelper.startRecord()
            mIsRecorder = true
            countTimeThread?.start()

        } else if (currentActionState == MediaRecorderHelper.ACTION_RECORDING) {//录制中
            currentActionState = MediaRecorderHelper.ACTION_COMMPLETE
            startRecordBtn.setImageResource(R.drawable.icon_dating_record_play)
            cancelAnimation()
            //停止录音
            recordTv.text = "录制完成"
            audioPlayTip.text = "点击试听"
            mMediaRecorderHelper.stopAndRelease()

            restartTip.isVisible = true
            revertRecord.isVisible = true
            audioFinishTip.isVisible = true
            finishRecord.isVisible = true
            audioPlayTip.isVisible = true
            myDatingAudioView.prepareAudio(mMediaRecorderHelper.currentFilePath, totalSecond)
        } else if (currentActionState == MediaRecorderHelper.ACTION_COMMPLETE) {//录制完成
            currentActionState = MediaRecorderHelper.ACTION_PLAYING

            //预览播放录音
            recordTv.text = "播放中.."
            mPreviewTimeThread = UpdateVoiceTimeThread.getInstance(UriUtils.getShowTime(totalSecond), recordTime)
            startRecordBtn.setImageResource(R.drawable.icon_dating_record_pause)
            mPreviewTimeThread?.start()
            MediaPlayerHelper.playSound(mMediaRecorderHelper.currentFilePath) {
                //当播放完了之后切换到录制完成的状态
                mPreviewTimeThread?.stop()
//                recordTime.setTextColor(resources.getColor(R.color.colorBlack22))

                currentActionState = MediaRecorderHelper.ACTION_COMMPLETE
                recordTime.text = UriUtils.getShowTime(totalSecond)
                recordTv.text = "录制完成"
                startRecordBtn.setImageResource(R.drawable.icon_dating_record_play)
            }

        } else if (currentActionState == MediaRecorderHelper.ACTION_PLAYING) {//播放中
            currentActionState = MediaRecorderHelper.ACTION_PAUSE
            mPreviewTimeThread?.pause()

            recordTv.text = "暂停"
            startRecordBtn.setImageResource(R.drawable.icon_dating_record_play)
            //暂停播放
            MediaPlayerHelper.pause()
        } else if (currentActionState == MediaRecorderHelper.ACTION_PAUSE) {//暂停
            currentActionState = MediaRecorderHelper.ACTION_PLAYING
            //开启预览计时线程
            mPreviewTimeThread?.start()

            recordTv.text = "播放中.."
            startRecordBtn.setImageResource(R.drawable.icon_dating_record_pause)
            //继续播放
            MediaPlayerHelper.resume()
        } else if (currentActionState == MediaRecorderHelper.ACTION_DONE) {
            mMediaRecorderHelper.cancel()
            changeToNormalState()
        }
    }


    //恢复成未录制状态
    //todo 恢复未录制状态 bug
    private fun changeToNormalState() {
        click = false
        datingAudioPreviewCl.isVisible = false
        datingAudioCl.isVisible = true

        restartTip.isVisible = false
        revertRecord.isVisible = false
        audioFinishTip.isVisible = false
        finishRecord.isVisible = false
        audioPlayTip.isVisible = false


        mIsPreview = false
        isTopPreview = false
        mIsRecorder = false
        MediaPlayerHelper.realese()
        currentActionState = MediaRecorderHelper.ACTION_NORMAL
        startRecordBtn.setImageResource(R.drawable.icon_record_normal)
        totalSecond = 0
        mPreviewTimeThread?.stop()
        recordTv.text = "点击录音"
        recordTime.text = "00:00"
        recordTime.setTextColor(Color.parseColor("#FF888D92"))

        myDatingAudioView.releaseAudio()
    }


    private fun playBigAnimation() {
        val animation = AnimationSet(true)
        //缩放动画，以中心从原始放大到1.4倍
        val scaleAnimation = ScaleAnimation(
            1.5f, 2.16f, 1.5f, 2.16f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f
        )
        //渐变动画
        val alphaAnimation = AlphaAnimation(1.0f, 0.5f)
        scaleAnimation.duration = 1000
        scaleAnimation.repeatCount = Animation.INFINITE
        alphaAnimation.repeatCount = Animation.INFINITE
        animation.duration = 1000
        animation.addAnimation(scaleAnimation)
        animation.addAnimation(alphaAnimation)
        recordAnimaBig.startAnimation(animation)

    }

    private fun playSmallAnimation() {
        val animation = AnimationSet(true)
        //缩放动画，以中心从1.4倍放大到1.8倍
        val scaleAnimation =
            ScaleAnimation(
                1.0f, 1.5f, 1.0f, 1.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f
            )
        //渐变动画
        val alphaAnimation = AlphaAnimation(0.5f, 0.1f)
        scaleAnimation.duration = 1000
        scaleAnimation.repeatCount = Animation.INFINITE
        alphaAnimation.repeatCount = Animation.INFINITE
        animation.duration = 1000
        animation.addAnimation(scaleAnimation)
        animation.addAnimation(alphaAnimation)
        recordAnimaSmall.startAnimation(animation)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
            }

            override fun onAnimationStart(animation: Animation?) {
                recordAnimaSmall.postDelayed({ playBigAnimation() }, 200L)

            }

        })
    }

    fun cancelAnimation() {
        recordAnimaSmall.isVisible = false
        recordAnimaBig.isVisible = false
        recordAnimaSmall.clearAnimation()
        recordAnimaBig.clearAnimation()
    }


    /**
     * 展示条件选择器
     */
    private val params by lazy { hashMapOf<String, Any>("dating_type" to dating_type) }

    private fun showConditionPicker(
        textview: TextView,
        params1: String,
        title: String,
        optionsItems1: MutableList<String>,
        optionsItems2: MutableList<MutableList<String>> = mutableListOf(),
        params2: String = ""
    ) {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                if (optionsItems2.isEmpty()) {
                    textview.text = optionsItems1[options1]
                    params[params1] = optionsItems1[options1]
                } else {
                    textview.text =
                        "${optionsItems1[options1]}·${optionsItems2[options1][options2]}"
                    params[params1] = optionsItems1[options1]
                    params[params2] = optionsItems2[options1][options2]
                }
            })
            .setSubmitText("确定")
            .setTitleText(title)
            .setTitleColor(resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView((window.decorView.findViewById(android.R.id.content)) as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<String>()

        if (optionsItems2.isNotEmpty()) {
            pvOptions.setPicker(optionsItems1, optionsItems2)
        } else {
            pvOptions.setPicker(optionsItems1)
        }
        pvOptions.show()
    }

    private val datingCostMoneyCondition = mutableListOf<MutableList<String>>()
    private val datingCostTypeCondition = mutableListOf<String>()
    private val datingTargetCondition = mutableListOf<String>()
    private val datingPlanCondition = mutableListOf<String>()
    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.chooseDatingPlaceBtn -> { //约会地点
                startActivity<LocationActivity>()
            }
            R.id.chooseDatingObjectBtn -> {//约会对象
                showConditionPicker(
                    chooseDatingObjectBtn,
                    "dating_target",
                    "选择约会对象",
                    datingTargetCondition
                )
            }
            R.id.chooseDatingPayBtn -> {//费用开支
                showConditionPicker(
                    chooseDatingPayBtn,
                    "cost_type",
                    "费用开支预估",
                    datingCostTypeCondition,
                    datingCostMoneyCondition,
                    "cost_money"
                )
            }
            R.id.chooseDatingPlanBtn -> {//后续活动
                showConditionPicker(
                    chooseDatingPlanBtn,
                    "follow_up",
                    "后续活动",
                    datingPlanCondition
                )

            }
            R.id.switchContentTypeBtn -> {//切换文字描述
                typeText = !typeText
                datingTextCl.isVisible = typeText
                datingAudioCl.isVisible = !typeText
                if (typeText) {
                    myDatingAudioView.releaseAudio()
                    switchContentTypeBtn.text = "切换语音描述"
                } else {
                    datingDescrEt.setText("")
                    switchContentTypeBtn.text = "切换文字描述"
                }

            }


            R.id.startRecordBtn -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.isGranted(
                        Manifest.permission.RECORD_AUDIO
                    )
                ) {
                    PermissionUtils.permission(PermissionConstants.MICROPHONE)
                        .callback(object : PermissionUtils.SimpleCallback {
                            override fun onGranted() {
                                if (currentActionState == MediaRecorderHelper.ACTION_RECORDING && totalSecond < 5) {
                                    CommonFunction.toast("再录制长一点吧")
                                    return
                                }
                                switchActionState()
                            }

                            override fun onDenied() {
                                CommonFunction.toast("录音权限被拒,请开启后再录音.")
                            }
                        }).request()
                } else {
                    if (currentActionState == MediaRecorderHelper.ACTION_RECORDING && totalSecond < 5) {
                        CommonFunction.toast("再录制长一点吧")
                        return
                    }
                    switchActionState()
                }

            }

            R.id.revertRecord, R.id.restartRecordBtn -> {
                val dialog = DeleteDialog(this)
                dialog.show()
                dialog.title.text = "重新录制"
                dialog.tip.text = "确定重新录制？"
                dialog.confirm.onClick {
//                    mMediaRecorderHelper.cancel()
//                    changeToNormalState()
                    dialog.dismiss()
                }
                dialog.cancel.onClick {
                    dialog.dismiss()
                }
            }
            R.id.finishRecord -> {
//                mMediaRecorderHelper.cancel()
//                changeToNormalState()
                datingAudioCl.isVisible = false
                datingAudioPreviewCl.isVisible = true
            }
            R.id.rightBtn1 -> {
                //dating_type [int]	是	约会类型
                //title [string]	是	约会标题
                //place [string]		约会地点（）
                //dating_target [string]	是	约会要求
                //cost_type [int]	是	费用类型AA....
                //content_type [int]	是	1 文本 2语音
                //content [string]	是	文本 或 语音地址
                //cost_money [string]	是	费用标准
                //follow_up [int]	是	后续约会类型
                //province_name [string]	是	省
                //city_name [string]	是	市
                //lat [string]	是
                //lng [string]	是

                mPresenter.releaseDate()
            }
        }


    }

    override fun onDatingOptionsResult(data: DatingOptionsBean?) {
        if (data != null) {
            datingTargetCondition.addAll(data.dating_target)
            datingPlanCondition.addAll(data.follow_up)
            datingCostTypeCondition.addAll(data.cost_type)
            for (index in 0 until datingCostTypeCondition.size) {
                datingCostMoneyCondition.add(data.cost_money)
            }
        }
    }


}