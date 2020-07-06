package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.bigkoo.pickerview.listener.OnTimeSelectListener
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.KeyboardUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.TimeUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.presenter.RegisterInfoPresenter
import com.sdy.jitangapplication.presenter.view.RegisterInfoView
import com.sdy.jitangapplication.ui.adapter.UploadAvatorAdapter
import com.sdy.jitangapplication.ui.dialog.UploadAvatorDialog
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_register_info.*
import kotlinx.android.synthetic.main.dialog_upload_avator.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import top.zibin.luban.OnCompressListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * 介绍一下自己吧
 */
class RegisterInfoActivity : BaseMvpActivity<RegisterInfoPresenter>(), RegisterInfoView,
    OnLazyClickListener {


    companion object {
        const val REQUEST_CODE_TAKE_PHOTO = 1
        const val REQUEST_PIC_CODE = 111

    }

    private val genders by lazy { mutableListOf("男", "女") }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_info)

        initView()
    }

    private fun initView() {
        mPresenter = RegisterInfoPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        setSwipeBackEnable(false)


        userGender.setOnClickListener(this)
        userBirth.setOnClickListener(this)
        nextBtn.setOnClickListener(this)
        chooseContactBtn.setOnClickListener(this)
        quickSign.setOnClickListener(this)
        userQuickSignDel.setOnClickListener(this)
        userAvatorTake.setOnClickListener(this)
        userAvator.setOnClickListener(this)

        userNickName.setFilters(arrayOf<InputFilter>(InputFilter { source, start, end, dest, dstart, dend ->
            if (source.equals(" ") || source.toString().contentEquals("\n")) {
                ""
            } else {
                null
            }
        }))
        userNickName.addTextChangedListener {
            checkConfirmEnable()
        }

        userSign.addTextChangedListener {
            checkConfirmEnable()
        }

        contactWayEt.addTextChangedListener {
            checkConfirmEnable()
        }

        CommonFunction.startAnimation(moreInfoTitle)
        t2.postDelayed({
            CommonFunction.startAnimation(t2)
        }, 50L)
        clName.postDelayed({
            CommonFunction.startAnimation(clName)
        }, 100L)

        clGender.postDelayed({
            CommonFunction.startAnimation(clGender)
        }, 150L)

        clBirth.postDelayed({
            CommonFunction.startAnimation(clBirth)
        }, 200L)

        t6.postDelayed({
            CommonFunction.startAnimation(t6)
        }, 250L)

        userSign.postDelayed({
            CommonFunction.startAnimation(userSign)
        }, 300L)
    }


    private val uploadAvatorDialog by lazy { UploadAvatorDialog(this) }
    private fun showAvatorDialog(showAlert: Boolean = true, requestCode: Int) {
        uploadAvatorDialog!!.show()
        uploadAvatorDialog.rvPersons.isVisible = showAlert
        uploadAvatorDialog.tv11.isVisible = showAlert
        uploadAvatorDialog.tv2.isVisible = showAlert
        val imgs = mutableListOf<Int>(
            R.drawable.icon_pass_person,
            R.drawable.icon_huangse_person,
            R.drawable.icon_network_person,
            R.drawable.icon_famous_person
        )
        val adapter = UploadAvatorAdapter()
        adapter.setNewData(imgs)
        uploadAvatorDialog.rvPersons.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        uploadAvatorDialog.rvPersons.adapter = adapter

        uploadAvatorDialog.choosePhoto.onClick {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (!PermissionUtils.isGranted(PermissionConstants.CAMERA) ||
                        !PermissionUtils.isGranted(PermissionConstants.STORAGE))
            ) {
                PermissionUtils.permission(PermissionConstants.CAMERA)
                    .callback(object : PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            if (!PermissionUtils.isGranted(PermissionConstants.STORAGE))
                                PermissionUtils.permission(PermissionConstants.STORAGE)
                                    .callback(object : PermissionUtils.SimpleCallback {
                                        override fun onGranted() {
                                            CommonFunction.onTakePhoto(
                                                this@RegisterInfoActivity,
                                                1,
                                                requestCode,
                                                PictureMimeType.ofImage(),
                                                rotateEnable = true,
                                                cropEnable = true
                                            )
                                            uploadAvatorDialog.cancel()
                                        }

                                        override fun onDenied() {
                                            CommonFunction.toast("文件存储权限被拒,请允许权限后再上传头像.")
                                            uploadAvatorDialog.cancel()
                                        }

                                    })
                                    .request()
                        }

                        override fun onDenied() {
                            CommonFunction.toast("相机权限被拒,请允许权限后再上传头像.")
                            uploadAvatorDialog.cancel()
                        }
                    })
                    .request()
            } else {
                CommonFunction.onTakePhoto(
                    this@RegisterInfoActivity,
                    1,
                    requestCode,
                    PictureMimeType.ofImage(),
                    rotateEnable = true,
                    cropEnable = true
                )
                uploadAvatorDialog.cancel()
            }
        }
        uploadAvatorDialog.takePhoto.onClick {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (!PermissionUtils.isGranted(
                    PermissionConstants.CAMERA
                ) ||
                        !PermissionUtils.isGranted(PermissionConstants.STORAGE))
            ) {
                PermissionUtils.permission(PermissionConstants.CAMERA)
                    .callback(object : PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            if (!PermissionUtils.isGranted(PermissionConstants.STORAGE)) {
                                PermissionUtils.permission(PermissionConstants.STORAGE)
                                    .callback(object : PermissionUtils.SimpleCallback {
                                        override fun onGranted() {
                                            CommonFunction.openCamera(
                                                this@RegisterInfoActivity,
                                                requestCode,
                                                1,
                                                compress = false,
                                                rotateEnable = true,
                                                cropEnable = true
                                            )
//                                            takePhoto()
                                            uploadAvatorDialog.cancel()
                                        }

                                        override fun onDenied() {
                                            CommonFunction.toast("文件存储权限被拒,请允许权限后再上传头像.")
                                            uploadAvatorDialog.cancel()
                                        }
                                    })
                                    .request()
                            }

                        }

                        override fun onDenied() {
                            CommonFunction.toast("相机权限被拒,请允许权限后再上传头像.")
//                            PermissionUtils.launchAppDetailsSettings()
                            uploadAvatorDialog.cancel()
                        }
                    })
                    .request()
            } else {
//                takePhoto()
                CommonFunction.openCamera(
                    this@RegisterInfoActivity,
                    requestCode,
                    1,
                    compress = false,
                    rotateEnable = true,
                    cropEnable = true
                )
                uploadAvatorDialog.cancel()
            }
        }
        uploadAvatorDialog.cancel.onClick {
            uploadAvatorDialog.cancel()
        }

    }


    private fun startAnimation(show: Boolean) {
        //使用AnimationUtils类的静态方法loadAnimation()来加载XML中的动画XML文件
        val animation = AnimationUtils.loadAnimation(
            this, if (show) {
                R.anim.dialog_center_in
            } else {
                R.anim.dialog_center_exit
            }
        )
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                avatorNotifyLl.isVisible = show
            }

            override fun onAnimationStart(p0: Animation?) {
            }

        })
        avatorNotifyLl.startAnimation(animation)
    }


    private fun checkConfirmEnable() {
        nextBtn.isEnabled =
//            userGender.text.isNotEmpty() &&
            userBirth.text.isNotEmpty()
                    && userNickName.text.isNotEmpty()
//                    && (userSign.text.isNotEmpty() || userQuickSign.text.isNotEmpty())
                    && contactWay != -1
                    && contactWayEt.text.isNotEmpty()
                    && (params["avatar"] != null && params["avatar"].toString().isNotEmpty())
    }

    override fun onLazyClick(v: View) {
        when (v.id) {
            R.id.userAvatorTake,
            R.id.userAvator -> {
                showAvatorDialog(true, PictureConfig.CHOOSE_REQUEST)
            }
            R.id.userGender -> {//性别选择
                KeyboardUtils.hideSoftInput(this)
                showGenderPicker()
            }
            R.id.userBirth -> {//选择生日
                KeyboardUtils.hideSoftInput(this)
                showBirthdayPicker()
            }
            R.id.chooseContactBtn -> {//选择联系方式
                KeyboardUtils.hideSoftInput(this)
                showContactPicker()
            }
            R.id.quickSign -> {//快速签名
                if (params["gender"] == null) {
                    CommonFunction.toast("请先选择性别")
                    return
                }
                startActivityForResult<QuickSignActivity>(
                    100,
                    "gender" to if (params["gender"] != null) {
                        params["gender"]
                    } else {
                        3
                    }
                )
            }
            R.id.userQuickSignDel -> {//快速签名
                userQuickSign.text = ""
                userQuickSign.isVisible = false
                userQuickSignDel.isVisible = false
                checkConfirmEnable()
            }
            R.id.nextBtn -> {//下一步
//                if (!alertGender) {
//                    CommonAlertDialog.Builder(this)
//                        .setTitle("提示")
//                        .setContent("性别确定了就不能更改了奥")
//                        .setConfirmText("我知道了")
//                        .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
//                            override fun onClick(dialog: Dialog) {
//                                params["nickname"] = userNickName.text.trim().toString()
//                                if (chooseSign != null) {
//                                    params["sign_id"] = chooseSign!!.id
//                                    params["sign"] = chooseSign!!.content
//                                } else {
//                                    params["sign"] = userSign.text.trim().toString()
//                                }
//                                params["contact_way"] = contactWay
//                                params["contact_way_content"] = contactWayEt.text.trim().toString()
//                                mPresenter.setProfileCandy(1, params)
//                                dialog.dismiss()
//                            }
//                        })
//                        .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
//                            override fun onClick(dialog: Dialog) {
//                                dialog.dismiss()
//                                alertGender = true
//                            }
//                        })
//                        .setCancelAble(true)
//                        .setCancelText("取消")
//                        .create()
//                        .show()
//                } else {
                params["nickname"] = userNickName.text.trim().toString()
//                params["sign"] = userSign.text.trim().toString()
//                if (chooseSign != null) {
//                    params["sign_id"] = chooseSign!!.id
//                    params["sign"] = chooseSign!!.content
//                } else {
//                    params["sign"] = userSign.text.trim().toString()
//                }
                params["contact_way"] = contactWay
                params["contact_way_content"] = contactWayEt.text.trim().toString()
                mPresenter.setProfileCandy(1, params)
//                }

            }
        }

    }


    private val params by lazy { hashMapOf<String, Any>() }

    /**
     * 展示日历
     */
    //错误使用案例： startDate.set(2013,1,1);  endDate.set(2020,12,1);
    //正确使用案例： startDate.set(2013,0,1);  endDate.set(2020,11,1);
    private fun showBirthdayPicker() {
        val startDate = Calendar.getInstance()
        val endDate = Calendar.getInstance()
        startDate.set(endDate.get(Calendar.YEAR) - 50, 0, 1)
        endDate.set(
            endDate.get(Calendar.YEAR) - 18, endDate.get(Calendar.MONTH), endDate.get(
                Calendar.DATE
            )
        )
        val clOptions = TimePickerBuilder(this, OnTimeSelectListener { date, v ->
            //            getZodiac
            userBirth.text =
                "${TimeUtils.date2String(
                    date,
                    SimpleDateFormat("yyyy-MM-dd")
                )}/${TimeUtils.getZodiac(date)}"
            params["birth"] = TimeUtils.date2Millis(date) / 1000L
            checkConfirmEnable()
        })
            .setRangDate(startDate, endDate)
            .setDate(endDate)
            .setTitleText("生日")
            .setTitleColor(Color.BLACK)//标题文字颜色
            .build()
        clOptions.show()
    }

    private var alertGender = false

    /**
     * 展示条件选择器
     */
    private fun showGenderPicker() {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                //选过性别并且和之前的不一样，就删除
                if (params["gender"] != null && params["gender"] != options1 + 1) {
                    chooseSign = null
//                        userSign.setText(data?.getStringExtra("quickSign"))
//                        userSign.setSelection(userSign.text.length)
                    userSign.isEnabled = true
                    userQuickSign.isVisible = false
                    userQuickSignDel.isVisible = false
                    userQuickSign.text = ""
                }
                userGender.text = genders[options1]
                params["gender"] = options1 + 1
                checkConfirmEnable()
            })
            .setSubmitText("确定")
            .setTitleText("性别")
            .setTitleColor(resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView(window.decorView.findViewById(android.R.id.content) as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<String>()

        pvOptions.setPicker(genders)
        pvOptions.show()
    }

    /**
     * 展示联系方式
     */
    private var contactWay = 2 //0手机号   1微信  2QQ
    private val contactWays by lazy { mutableListOf("手机号", "微信", "QQ") }
    private val contactWaysIcon by lazy {
        mutableListOf(
            R.drawable.icon_phone_reg,
            R.drawable.icon_wechat_reg,
            R.drawable.icon_qq_reg
        )
    }

    private fun showContactPicker() {
        //条件选择器
        val pvOptions = OptionsPickerBuilder(this,
            OnOptionsSelectListener { options1, options2, options3, v ->
                chooseContactBtn.setImageResource(contactWaysIcon[options1])
                contactWay = options1 + 1
                contactWayEt.hint = "${when (options1) {
                    0 -> {
                        "请输入正确的手机号，可设置隐藏"
                    }
                    1 -> {
                        "请输入正确的微信号，可设置隐藏"
                    }
                    else -> {
                        "请输入正确的QQ号，可设置隐藏"
                    }
                }}"
                checkConfirmEnable()
            })
            .setSubmitText("确定")
            .setTitleText("联系方式")
            .setTitleColor(resources.getColor(R.color.colorBlack))
            .setTitleSize(16)
            .setDividerColor(resources.getColor(R.color.colorDivider))
            .setContentTextSize(20)
            .setDecorView(window.decorView.findViewById(android.R.id.content) as ViewGroup)
            .setSubmitColor(resources.getColor(R.color.colorBlueSky1))
            .build<String>()

        pvOptions.setPicker(contactWays)
        pvOptions.setSelectOptions(1)
        pvOptions.show()
    }


    override fun onBackPressed() {

    }


    private var chooseSign: LabelQualityBean? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                //相册拍摄
                PictureConfig.CHOOSE_REQUEST,
                    //选择照片
                REQUEST_CODE_TAKE_PHOTO -> {
                    if (data != null) {
                        params.remove("avatar")
//                        startAnimation(false)
                        var path =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !PictureSelector.obtainMultipleResult(
                                    data
                                )[0].androidQToPath.isNullOrEmpty()
                            ) {
                                PictureSelector.obtainMultipleResult(data)[0].androidQToPath
                            } else {
                                PictureSelector.obtainMultipleResult(data)[0].path
                            }
                        UriUtils.getLubanBuilder(this)
                            .load(path)
                            .setCompressListener(object : OnCompressListener {
                                override fun onSuccess(file: File?) {
                                    if (file != null) {
                                        path = file.absolutePath
                                    }
                                    GlideUtil.loadCircleImg(
                                        this@RegisterInfoActivity, path, userAvator
                                    )
                                    userAvator.isVisible = true
                                    userAvatorTip.setImageResource(R.drawable.icon_bg_transparent_avator_change_bg)

                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(
                                            Constants.SPNAME
                                        ).getString(
                                            "accid"
                                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                            16
                                        )}"
                                    mPresenter.uploadProfile(path, key)
                                }

                                override fun onError(e: Throwable?) {
                                    Log.d("SetInfoActivity", "$path===========================")
                                    GlideUtil.loadCircleImg(
                                        this@RegisterInfoActivity, path,
                                        userAvator
                                    )
                                    userAvator.isVisible = true
                                    userAvatorTip.setImageResource(R.drawable.icon_bg_transparent_avator_change_bg)

                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(
                                            Constants.SPNAME
                                        ).getString(
                                            "accid"
                                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                            16
                                        )}"
                                    mPresenter.uploadProfile(path, key.toString())


                                }

                                override fun onStart() {

                                }

                            })
                            .launch()
                    }
                }
                //快速签名
                100 -> {
//                    LabelQualityBean
                    if (data?.getSerializableExtra("quickSign") != null) {
                        chooseSign = data?.getSerializableExtra("quickSign") as LabelQualityBean
//                        userSign.setText(data?.getStringExtra("quickSign"))
//                        userSign.setSelection(userSign.text.length)
                        userSign.isEnabled = false
                        userQuickSign.isVisible = true
                        userQuickSignDel.isVisible = true
                        userQuickSign.text = chooseSign?.content
                        checkConfirmEnable()
                    }
                }
            }
        }
    }


    override fun onUploadUserInfoResult(
        uploadResult: Boolean,
        msg: String?,
        moreMatchBean: MoreMatchBean?
    ) {
        if (uploadResult) {
            SPUtils.getInstance(Constants.SPNAME).put("avatar", moreMatchBean?.avatar)
//            startActivity<RegisterInfoActivity>()

            if (moreMatchBean?.living_btn == true) {//  true  需要活体   false  不需要活体
                startActivity<WomanLivingActivity>("morematchbean" to moreMatchBean)
            } else
                UserManager.startToFlow(this, moreMatchBean)
        } else {
            CommonFunction.toast(msg ?: "")
        }

    }

    //用户头像
    private var userProfile: String? = null

    override fun uploadImgResult(ok: Boolean, key: String) {
        if (ok) {
            params["avatar"] = userProfile ?: ""
        } else {
            CommonFunction.longToast("头像上传失败")
        }
        checkConfirmEnable()
    }

}
