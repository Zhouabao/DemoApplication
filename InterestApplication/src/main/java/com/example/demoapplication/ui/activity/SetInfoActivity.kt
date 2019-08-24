package com.example.demoapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.example.baselibrary.glide.GlideUtil
import com.example.baselibrary.utils.RandomUtils
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.presenter.SetInfoPresenter
import com.example.demoapplication.presenter.view.SetInfoView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.android.synthetic.main.activity_set_info.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


/**
 * 填写个人信息页面
 */
class SetInfoActivity : BaseMvpActivity<SetInfoPresenter>(), SetInfoView, View.OnClickListener {
    companion object {
        const val USER_BIRTH_REQUEST_CODE = 1000
        const val USER_PROFILE_REQUEST_CODE = 1001
    }

    //请求参数
    private val params by lazy { HashMap<String, Any>() }
    //用户头像
    private var userProfile: String? = null
    //昵称是否合法
    private var nickNameValidate = false
    //是否提示过性别
    private var sexTip = false
    //handler
    private val handler by lazy { Handler() }
    //延迟校验输入昵称的runnable
    private val delayRun by lazy { Runnable { mPresenter.mView.onCheckNickNameResult() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        initState()
        setContentView(R.layout.activity_set_info)

        initView()

        if (SPUtils.getInstance(Constants.SPNAME).getString("sensitive").isNullOrEmpty())
            mPresenter.checkNickName()

    }

    private fun initView() {
        mPresenter = SetInfoPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }
        userProfileBtn.setOnClickListener(this)
        confirmBtn.setOnClickListener(this)
        userBirthTv.setOnClickListener(this)

        sexGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (!sexTip) {
                ToastUtils.setGravity(Gravity.CENTER, 0, 0)
                ToastUtils.showShort("性别是不可更改项仅可选择一次")
                sexTip = true
            }
        }
        //判断et输入完成并检验输入昵称的合法性
        userNickNameEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (delayRun != null) {
                    //如果每次et有变化的时候，就移除上次发出的延迟线程
                    handler.removeCallbacks(delayRun)
                }
                //延迟800ms，如果没有输入，就执行输入检查
                handler.postDelayed(delayRun, 800)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                userNickNameEt.setBackgroundResource(R.drawable.login_rectangle)
                userNickNameEt.setCompoundDrawables(null, null, null, null)
            }

        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        userNickNameEt.postDelayed({
            KeyboardUtils.showSoftInput(userNickNameEt, 0)
        }, 200)
    }


    /**
     * 上传信息成功
     */
    override fun onUploadUserInfoResult(uploadResult: Boolean) {
        if (uploadResult) {
            startActivity<LabelsActivity>()
        }
    }

    /**
     * 验证昵称是否正确
     */
    override fun onCheckNickNameResult() {
        val userNickName = userNickNameEt.text.toString()
        val sensitiveWords = SPUtils.getInstance(Constants.SPNAME).getString("sensitive")
        for (tempChar in userNickName) {
            if (sensitiveWords.contains(tempChar)) {
                //包含敏感词汇
                userNickNameEt.setBackgroundResource(R.drawable.shape_rectangle_et_error)
                val drawableRight = resources.getDrawable(R.drawable.icon_error_tip1)
                drawableRight.setBounds(0, 0, drawableRight.intrinsicWidth, drawableRight.intrinsicHeight)
                userNickNameEt.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null)
                toast("昵称不合法")
                nickNameValidate = false
                break
            } else {
                nickNameValidate = true
            }
        }
        checkConfirmBtnEnable()

    }

    /**
     * 拍照或者选取照片
     */
    private fun onTakePhoto() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .maxSelectNum(1)
            .minSelectNum(0)
            .imageSpanCount(4)
            .selectionMode(PictureConfig.SINGLE)
            .previewImage(true)
            .isCamera(true)
            .enableCrop(true)
            .withAspectRatio(9, 16)
            .compress(true)
            .openClickSound(true)
            .forResult(PictureConfig.CHOOSE_REQUEST)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.userProfileBtn -> {
                onTakePhoto()
                userProfile = null
//                startActivityForResult<TCCameraActivity>(USER_PROFILE_REQUEST_CODE)
            }
            //点击跳转到标签选择页
            R.id.confirmBtn -> {
                params["accid"] = SPUtils.getInstance(Constants.SPNAME).getString("accid")
                params["token"] = SPUtils.getInstance(Constants.SPNAME).getString("token")
                params["avatar"] = userProfile.toString()
                params["nickname"] = userNickNameEt.text.toString()
                params["gender"] = "${if (sexGroup.checkedRadioButtonId == R.id.userSexMan) 1 else 2}"
                params["birth"] = "${TimeUtils.date2Millis(
                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).parse(userBirthTv.text.toString())
                ) / 1000L}"

                params["_timestamp"] = "${TimeUtils.getNowMills()}"
                params["sign"] = ""
                params["tags"] = ""
                mPresenter.uploadUserInfo(params)
//                        startActivity<LabelsActivity>("params" to params)

            }
            R.id.userBirthTv -> {
                startActivityForResult<UserBirthActivity>(USER_BIRTH_REQUEST_CODE)
                userBirthTv.text = ""
            }
        }
    }


    private var userBirth = ""
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    if (data != null) {
                        val selectList: List<LocalMedia> = PictureSelector.obtainMultipleResult(data)
                        GlideUtil.loadCircleImg(applicationContext, selectList[0].compressPath, userProfileBtn)
                        userProfile =
                            "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
                                "accid"
                            )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                16
                            )}.jpg"
                        mPresenter.uploadProfile(selectList[0].compressPath, userProfile.toString())
                        checkConfirmBtnEnable()
                    }
                }
                USER_BIRTH_REQUEST_CODE -> {
                    if (data != null) {
                        userBirth = "${data.getStringExtra("year")}${data.getStringExtra("month")}"
                        val year = data.getStringExtra("year")
                        val monthDay = data.getStringExtra("month").substring(0, 2).plus("-")
                            .plus(data.getStringExtra("month").substring(2, 4))
                        userBirthTv.text = year.plus("-").plus(monthDay)
                        checkConfirmBtnEnable()
                    }

                }
            }
        }
    }


    private fun checkConfirmBtnEnable() {
        confirmBtn.isEnabled =
            !userProfile.isNullOrEmpty() && userBirthTv.text.toString().isNotEmpty() && userNickNameEt.text.toString().isNotEmpty() && nickNameValidate
    }
}
