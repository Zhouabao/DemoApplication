package com.example.demoapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.TimeUtils
import com.cazaea.sweetalert.SweetAlertDialog
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.common.Constants
import com.example.demoapplication.presenter.SetInfoPresenter
import com.example.demoapplication.presenter.view.SetInfoView
import com.example.demoapplication.videorecord.TCCameraActivity
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_set_info.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast


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
    private var userProfile: String? = null
    private var userBirth: String? = null
    private val handler by lazy { Handler() }

    private val delayRun by lazy { Runnable { mPresenter.checkNickName(userNickNameEt.text.toString()) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initState()

        setContentView(R.layout.activity_set_info)
        mPresenter = SetInfoPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }
        userProfileBtn.setOnClickListener(this)
        confirmBtn.setOnClickListener(this)
        userBirthTv.setOnClickListener(this)


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
     * 改变性别
     */
    override fun onChangeSex(id: Int) {}

    /**
     * 填写生日
     */
    override fun onChangeBirth() {

    }

    /**
     * 上传信息结果
     */
    override fun onUploadUserInfoResult() {

    }

    override fun onCheckNickNameResult(result: Boolean) {
        if (!result) {
            userNickNameEt.setBackgroundResource(R.drawable.shape_rectangle_et_error)
            val drawableRight = resources.getDrawable(R.drawable.icon_error_tip1)
            drawableRight.setBounds(0, 0, drawableRight.intrinsicWidth, drawableRight.intrinsicHeight)
            userNickNameEt.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null)


            toast("昵称不合法")

//            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
//                .setTitleText("")
//                .setContentText("昵称违规请修改")
//                .setConfirmText("OK")
//                .show()
        }
        checkConfirmBtnEnable()

    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.userProfileBtn -> {
                startActivityForResult<TCCameraActivity>(USER_PROFILE_REQUEST_CODE)
            }
            //点击跳转到标签选择页
            R.id.confirmBtn -> {
                SweetAlertDialog(this)
                    .setTitleText("温馨提示")
                    .setContentText("性别是不可更改项仅可选择一次")
                    .setConfirmText("确定")
                    .setConfirmClickListener {
                        params["accid"] = SPUtils.getInstance(Constants.SPNAME).getString("accid")
                        params["token"] = SPUtils.getInstance(Constants.SPNAME).getString("token")
                        params["avatar"] = userProfile.toString()
                        params["nickname"] = userNickNameEt.text.toString()
                        params["gender"] = if (sexGroup.checkedRadioButtonId == R.id.userSexMan) 1 else 2
                        params["birth"] = TimeUtils.string2Millis(userBirth.toString())
                        params["timestamp"] = TimeUtils.getNowMills()
//                        params["sign"] = TimeUtils.getNowMills()
//                        params["tags"] = TimeUtils.getNowMills()
                        startActivity<LabelsActivity>("params" to params)
                        it.cancel()
                    }
                    .show()
            }
            R.id.userBirthTv -> {
                startActivityForResult<UserBirthActivity>(USER_BIRTH_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
//                PictureConfig.CHOOSE_REQUEST -> {
//                    val selectList: List<LocalMedia> = PictureSelector.obtainMultipleResult(data)
//                    GlideUtil.loadCircleImg(applicationContext, selectList[0].compressPath, userProfileBtn)
//                }

                USER_BIRTH_REQUEST_CODE -> {
                    if (data != null) {
                        userBirthTv.text = data.getStringExtra("birthday")
                        checkConfirmBtnEnable()
                    }

                }
                USER_PROFILE_REQUEST_CODE -> {
                    if (data != null) {
                        userProfile = data.getStringExtra("filePath")
                        if (userProfile != null) {
                            Log.i("SetInfoActivity", userProfile.toString())
                            GlideUtil.loadCircleImg(applicationContext, userProfile, userProfileBtn)
//                            userProfileBtn.setImageBitmap(data.getParcelableExtra("file"))
                            mPresenter.uploadProfile(userProfile!!)
                        }
                        checkConfirmBtnEnable()
                    }
                }
            }
        }
    }


    private fun checkConfirmBtnEnable() {
        confirmBtn.isEnabled =
            !userProfile.isNullOrEmpty() && !userBirth.isNullOrEmpty() && userNickNameEt.text.toString().isNotEmpty()
    }
}
