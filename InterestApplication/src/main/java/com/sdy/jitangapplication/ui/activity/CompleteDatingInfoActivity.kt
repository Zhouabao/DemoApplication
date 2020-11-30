package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.core.view.isVisible
import com.amap.api.services.core.PoiItem
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.UpdateMyDatingEvent
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.DatingOptionsBean
import com.sdy.jitangapplication.player.MediaPlayerHelper
import com.sdy.jitangapplication.player.MediaRecorderHelper
import com.sdy.jitangapplication.player.UpdateVoiceTimeThread
import com.sdy.jitangapplication.presenter.CompleteDatingInfoPresenter
import com.sdy.jitangapplication.presenter.view.CompleteDatingInfoView
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_complete_dating_info.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivityForResult


/**
 * 完善约会信息
 */
class CompleteDatingInfoActivity : BaseMvpActivity<CompleteDatingInfoPresenter>(),
    CompleteDatingInfoView, OnLazyClickListener {
    private val dating_type by lazy { intent.getSerializableExtra("dating_type") as CheckBean? }
    private var typeText = true //文本限制

    val MAX_DESCR_LENGTH by lazy {
        if (UserManager.overseas) {
            150 * 2
        } else {
            150
        }
    }

    companion object {
        const val REQUEST_CODE_DATING_CONTENT = 1
        const val REQUEST_CODE_DATING_PLACE = 2
    }

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

        hotT1.text = getString(R.string.dating_info_complete)
        btnBack.clickWithTrigger {
            finish()
        }
        rightBtn1.text = getString(R.string.publish)
        rightBtn1.isVisible = true
        rightBtn1.isEnabled = true

        datingProjectContentEt.setOnClickListener(this)
        switchContentTypeBtn.setOnClickListener(this)
        chooseDatingPlaceBtn.setOnClickListener(this)
        chooseDatingPlanBtn.setOnClickListener(this)
        chooseDatingObjectBtn.setOnClickListener(this)
        chooseDatingPayBtn.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)

        datingDescrEt.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(MAX_DESCR_LENGTH))
        SpanUtils.with(datingDescrLength)
            .append(datingDescrEt.length().toString())
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .setBold()
            .append("/${MAX_DESCR_LENGTH}")
            .create()


        datingDescrEt.addTextChangedListener(object : TextWatcher {
            /************编辑内容监听**************/
            override fun afterTextChanged(p0: Editable) {
//                if (p0.length > MAX_DESCR_LENGTH) {
//                    datingDescrEt.setText(publishContent.text.subSequence(0, MAX_DESCR_LENGTH))
//                    datingDescrEt.setSelection(publishContent.text.length)
//                    CommonFunction.toast("超出字数限制")
//                    KeyboardUtils.hideSoftInput(datingDescrEt)
//                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                SpanUtils.with(datingDescrLength)
                    .append(datingDescrEt.length().toString())
                    .setForegroundColor(resources.getColor(R.color.colorOrange))
                    .setBold()
                    .append("/${MAX_DESCR_LENGTH}")
                    .create()
            }

        })

        initAudioCl()

    }

    /**************** * 初始化录音控件 录音时间在5S~1M之间********************/
    private var mIsRecorder = false
    private var countTimeThread: CountDownTimer? = null
    private var mPreviewTimeThread: UpdateVoiceTimeThread? = null
    private lateinit var mMediaRecorderHelper: MediaRecorderHelper
    private var totalSecond = 0
    private var currentActionState = MediaRecorderHelper.ACTION_NORMAL

    //判断是否是第一次点击上部分预览界面的播放按钮
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
            recordTv.text = getString(R.string.record_complete)
            audioPlayTip.text = getString(R.string.try_listen)
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
            recordTv.text = getString(R.string.playing)
            mPreviewTimeThread =
                UpdateVoiceTimeThread.getInstance(UriUtils.getShowTime(totalSecond), recordTime)
            startRecordBtn.setImageResource(R.drawable.icon_dating_record_pause)
            mPreviewTimeThread?.start()
            MediaPlayerHelper.playSound(mMediaRecorderHelper.currentFilePath) {
                //当播放完了之后切换到录制完成的状态
                mPreviewTimeThread?.stop()
//                recordTime.setTextColor(resources.getColor(R.color.colorBlack22))

                currentActionState = MediaRecorderHelper.ACTION_COMMPLETE
                recordTime.text = UriUtils.getShowTime(totalSecond)
                recordTv.text = getString(R.string.record_complete)
                startRecordBtn.setImageResource(R.drawable.icon_dating_record_play)
            }

        } else if (currentActionState == MediaRecorderHelper.ACTION_PLAYING) {//播放中
            currentActionState = MediaRecorderHelper.ACTION_PAUSE
            mPreviewTimeThread?.pause()

            recordTv.text = getString(R.string.pause)
            startRecordBtn.setImageResource(R.drawable.icon_dating_record_play)
            //暂停播放
            MediaPlayerHelper.pause()
        } else if (currentActionState == MediaRecorderHelper.ACTION_PAUSE) {//暂停
            currentActionState = MediaRecorderHelper.ACTION_PLAYING
            //开启预览计时线程
            mPreviewTimeThread?.start()

            recordTv.text = getString(R.string.playing)
            startRecordBtn.setImageResource(R.drawable.icon_dating_record_pause)
            //继续播放
            MediaPlayerHelper.resume()
        } else if (currentActionState == MediaRecorderHelper.ACTION_DONE) {
//            mMediaRecorderHelper.cancel()
            changeToNormalState()
            myDatingAudioView.releaseAudio()
        }
    }


    //恢复成未录制状态
    private fun changeToNormalState() {
        datingAudioPreviewCl.isVisible = false
        datingAudioCl.isVisible = true

        restartTip.isVisible = false
        revertRecord.isVisible = false
        audioFinishTip.isVisible = false
        finishRecord.isVisible = false
        audioPlayTip.isVisible = false


        mIsRecorder = false
        MediaPlayerHelper.realese()
        currentActionState = MediaRecorderHelper.ACTION_NORMAL
        startRecordBtn.setImageResource(R.drawable.icon_record_normal)
        totalSecond = 0
        mPreviewTimeThread?.stop()
        countTimeThread?.cancel()
        recordTv.text = getString(R.string.click_record)
        recordTime.text = "00:00"
        recordTime.setTextColor(Color.parseColor("#FF888D92"))

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
    private val params by lazy { hashMapOf<String, Any>("dating_type" to (dating_type?.id ?: 0)) }

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
            .setSubmitText(getString(R.string.ok))
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
            R.id.datingProjectContentEt -> { //活动内容
                if (KeyboardUtils.isSoftInputVisible(this)) {
                    KeyboardUtils.hideSoftInput(this)
                }
                startActivityForResult<WriteDatingContentActivity>(
                    REQUEST_CODE_DATING_CONTENT,
                    "dating_type" to (dating_type?.title ?: "")
                )
            }
            R.id.chooseDatingPlaceBtn -> { //约会地点
                if (KeyboardUtils.isSoftInputVisible(this)) {
                    KeyboardUtils.hideSoftInput(this)
                }
                startActivityForResult<LocationActivity>(REQUEST_CODE_DATING_PLACE)
            }
            R.id.chooseDatingObjectBtn -> {//约会对象
                if (KeyboardUtils.isSoftInputVisible(this)) {
                    KeyboardUtils.hideSoftInput(this)
                }
                showConditionPicker(
                    chooseDatingObjectBtn,
                    "dating_target",
                    getString(R.string.dating_person_choose),
                    datingTargetCondition
                )
            }
            R.id.chooseDatingPayBtn -> {//费用开支
                if (KeyboardUtils.isSoftInputVisible(this)) {
                    KeyboardUtils.hideSoftInput(this)
                }
                showConditionPicker(
                    chooseDatingPayBtn,
                    "cost_type",
                    getString(R.string.dating_pay_estimated),
                    datingCostTypeCondition,
                    datingCostMoneyCondition,
                    "cost_money"
                )
            }
            R.id.chooseDatingPlanBtn -> {//后续活动
                if (KeyboardUtils.isSoftInputVisible(this)) {
                    KeyboardUtils.hideSoftInput(this)
                }
                showConditionPicker(
                    chooseDatingPlanBtn,
                    "follow_up",
                    getString(R.string.dating_later_plan),
                    datingPlanCondition
                )

            }
            R.id.switchContentTypeBtn -> {//切换文字描述
                typeText = !typeText

                if (typeText) {
                    mMediaRecorderHelper.cancel()
                    cancelAnimation()
                    changeToNormalState()
                    switchContentTypeBtn.text = getString(R.string.switch_audio_descr)
                } else {
                    if (KeyboardUtils.isSoftInputVisible(this)) {
                        KeyboardUtils.hideSoftInput(this)
                    }
                    datingDescrEt.setText("")
                    switchContentTypeBtn.text = getString(R.string.switch_text_descr)
                }
                datingTextCl.isVisible = typeText
                datingAudioCl.isVisible = !typeText
            }


            R.id.startRecordBtn -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionUtils.isGranted(
                        *PermissionConstants.getPermissions(PermissionConstants.MICROPHONE)
                    )
                ) {
                    PermissionUtils.permission(PermissionConstants.MICROPHONE)
                        .callback(object : PermissionUtils.SimpleCallback {
                            override fun onGranted() {
                                if (currentActionState == MediaRecorderHelper.ACTION_RECORDING && totalSecond < 5) {
                                    CommonFunction.toast(getString(R.string.record_longer))
                                    return
                                }
                                switchActionState()
                            }

                            override fun onDenied() {
                                CommonFunction.toast(getString(R.string.permission_audio))
                            }
                        }).request()
                } else {
                    if (currentActionState == MediaRecorderHelper.ACTION_RECORDING && totalSecond < 5) {
                        CommonFunction.toast(getString(R.string.record_longer))
                        return
                    }
                    switchActionState()
                }

            }

            R.id.revertRecord, R.id.restartRecordBtn -> {
                val dialog = DeleteDialog(this)
                dialog.show()
                dialog.title.text = getString(R.string.re_record)
                dialog.tip.text = getString(R.string.confirm_re_record)
                dialog.confirm.onClick {
                    mMediaRecorderHelper.cancel()
                    changeToNormalState()
                    myDatingAudioView.releaseAudio()
                    dialog.dismiss()
                }
                dialog.cancel.onClick {
                    dialog.dismiss()
                }
            }
            R.id.finishRecord -> {
//                mMediaRecorderHelper.cancel()
                //如果下面在预览播放，那么就先释放资源，停止播放
                changeToNormalState()
                currentActionState = MediaRecorderHelper.ACTION_DONE
                datingAudioCl.isVisible = false
                datingAudioPreviewCl.isVisible = true
            }
            R.id.rightBtn1 -> {
                if (dating_type == null) {
                    CommonFunction.toast(getString(R.string.dating_back_to_dating_type))
                    return
                }

                if (chooseDatingPlaceBtn.text.isEmpty()) {
                    CommonFunction.toast(getString(R.string.dating_choose_place))
                    return
                }
                if (chooseDatingObjectBtn.text.isEmpty()) {
                    CommonFunction.toast(getString(R.string.dating_choose_person))
                    return
                }
                if (chooseDatingPayBtn.text.isEmpty()) {
                    CommonFunction.toast(getString(R.string.dating_cost_money))
                    return
                }
                if (chooseDatingPlanBtn.text.isEmpty()) {
                    CommonFunction.toast(getString(R.string.dating_choose_later_plan))
                    return
                }


                if (!typeText && ((!mMediaRecorderHelper.currentFilePath.isNullOrEmpty() && currentActionState != MediaRecorderHelper.ACTION_DONE) || mMediaRecorderHelper.currentFilePath.isNullOrEmpty())) {
                    CommonFunction.toast(getString(R.string.confirm_record_and_publish))
                    return
                }

                if (typeText && datingDescrEt.text.isNullOrEmpty()) {
                    CommonFunction.toast(getString(R.string.write_mind))
                    return
                }

                if (mMediaRecorderHelper.currentFilePath.isNullOrEmpty() && datingDescrEt.text.isNullOrEmpty()) {
                    CommonFunction.toast(getString(R.string.text_or_audio_must))
                    return
                }
                if (myDatingAudioView.isPlaying())
                    myDatingAudioView.releaseAudio()


                if (datingDescrEt.text.trim().isNotEmpty()) {
                    publishDating(datingDescrEt.text.trim().toString())
                } else {
                    //上传音频
                    val imagePath =
                        "${Constants.FILE_NAME_INDEX}${Constants.DATING}${UserManager.getAccid()}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                            16
                        )}"
                    params["duration"] = myDatingAudioView.duration
                    mPresenter.uploadFile(mMediaRecorderHelper.currentFilePath, imagePath)
                }
            }
        }


    }

    private fun publishDating(sourceFile: String) {
        params["dating_type"] = dating_type?.id ?: 0
        params["title"] = datingProjectContentEt.text.trim()
        params["dating_target"] = chooseDatingObjectBtn.text
        params["cost_type"] = chooseDatingPayBtn.text.split("·")[0]
        params["content_type"] = if (datingDescrEt.text.isNullOrBlank()) {
            2
        } else {
            1
        }
        params["content"] = RegexUtils.getReplaceAll(sourceFile, "\\t|\\r|\\n|\\\\s*", "")
        params["cost_money"] = chooseDatingPayBtn.text.split("·")[1]
        params["follow_up"] = chooseDatingPlanBtn.text



        mPresenter.releaseDate(params)

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

        //                currentActionState = MediaRecorderHelper.ACTION_DONE
        //                switchActionState()
    }

    private var positionItem: PoiItem? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_DATING_CONTENT -> {
                    datingProjectContentEt.text = data?.getStringExtra("datingContent")
                }
                REQUEST_CODE_DATING_PLACE -> {
                    if (data?.getParcelableExtra<PoiItem>("poiItem") != null) {
                        positionItem = data!!.getParcelableExtra("poiItem") as PoiItem
                        chooseDatingPlaceBtn.text =
                            (positionItem!!.cityName
                                ?: "") + if (!positionItem!!.cityName.isNullOrEmpty()) {
                                "·"
                            } else {
                                ""
                            } + positionItem!!.title
                        params["place"] = positionItem!!.title
                        params["province_name"] = positionItem?.provinceName ?: ""
                        params["city_name"] = positionItem?.cityName ?: ""
                        params["lat"] = positionItem?.latLonPoint?.latitude ?: 0F
                        params["lng"] = positionItem?.latLonPoint?.longitude ?: 0F
                        chooseDatingPlaceBtn.ellipsize = TextUtils.TruncateAt.MARQUEE
                        chooseDatingPlaceBtn.maxLines = 1
                        chooseDatingPlaceBtn.isSelected = true
                        chooseDatingPlaceBtn.isFocusable = true
                        chooseDatingPlaceBtn.isFocusableInTouchMode = true
                    }

                }
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

    override fun onDatingReleaseResult(success: Boolean, code: Int) {
        if (success) {
            CommonFunction.toast(getString(R.string.dating_publish_success))
            EventBus.getDefault().post(UpdateMyDatingEvent())
            if (ActivityUtils.isActivityExistsInStack(ChooseDatingTypeActivity::class.java)) {
                ActivityUtils.finishActivity(ChooseDatingTypeActivity::class.java)
            }
            finish()
        }

    }

    override fun onQnUploadResult(success: Boolean, key: String) {
        if (success) {
            publishDating(key)
        }

    }


    override fun finish() {
        super.finish()
        mMediaRecorderHelper.cancel()
        changeToNormalState()
        myDatingAudioView.releaseAudio()
        if (KeyboardUtils.isSoftInputVisible(this)) {
            KeyboardUtils.hideSoftInput(this)
        }
    }
}