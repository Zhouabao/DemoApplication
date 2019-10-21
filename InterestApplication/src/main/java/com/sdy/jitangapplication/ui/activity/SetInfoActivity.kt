package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.KeyboardUtils
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
import com.sdy.jitangapplication.presenter.SetInfoPresenter
import com.sdy.jitangapplication.presenter.view.SetInfoView
import com.sdy.jitangapplication.ui.adapter.UploadAvatorAdapter
import com.sdy.jitangapplication.ui.dialog.UploadAvatorDialog
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import kotlinx.android.synthetic.main.activity_set_info.*
import kotlinx.android.synthetic.main.dialog_upload_avator.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import top.zibin.luban.OnCompressListener
import java.io.File
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
        const val REQUEST_CODE_TAKE_PHOTO = 1
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


    }

    private fun initView() {
        setSwipeBackEnable(false)
        mPresenter = SetInfoPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }
        userProfileBtn.setOnClickListener(this)
        confirmBtn.setOnClickListener(this)
        userBirthTv.setOnClickListener(this)

        sexGroup.setOnCheckedChangeListener { radioGroup, i ->
            if (!sexTip) {
                CommonFunction.toast("性别是不可更改项仅可选择一次")
                sexTip = true
            }
            checkConfirmBtnEnable()
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
     * 上传头像结果
     */
    override fun onUploadUserAvatorResult(key: String) {
        userProfile = key
        checkConfirmBtnEnable()
    }


    /**
     * 上传信息成功
     */
    override fun onUploadUserInfoResult(uploadResult: Boolean) {
        if (uploadResult) {
            startActivity<NewLabelsActivity1>()
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
                CommonFunction.toast("昵称不合法")
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
    private fun choosePhoto() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .maxSelectNum(1)
            .minSelectNum(0)
            .imageSpanCount(4)
            .selectionMode(PictureConfig.SINGLE)
            .previewImage(true)
            .isCamera(true)
            .enableCrop(false)//是否裁剪
            .isDragFrame(true)
            .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false   true or false
            .showCropGrid(false)// 是否显示裁剪矩形网格 圆形裁剪时建议设为false    true or fa
            .scaleEnabled(true)
            .rotateEnabled(false)
            .withAspectRatio(1, 1)
            .compress(false)//是否压缩
            .openClickSound(false)
            .forResult(PictureConfig.CHOOSE_REQUEST)
    }

    private val uploadAvatorDialog by lazy { UploadAvatorDialog(this) }
    private fun showAvatorDialog() {
        uploadAvatorDialog!!.show()
        val imgs = mutableListOf<Int>(
            R.drawable.icon_pass_person,
            R.drawable.icon_huangse_person,
            R.drawable.icon_network_person,
            R.drawable.icon_famous_person
        )
        val adapter = UploadAvatorAdapter()
        adapter.setNewData(imgs)
        uploadAvatorDialog.rvPersons.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        uploadAvatorDialog.rvPersons.adapter = adapter

        uploadAvatorDialog.choosePhoto.onClick {
            choosePhoto()
            uploadAvatorDialog.cancel()

        }
        uploadAvatorDialog.takePhoto.onClick {
            takePhoto()
            uploadAvatorDialog.cancel()
        }
        uploadAvatorDialog.cancel.onClick {
            uploadAvatorDialog.cancel()
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.userProfileBtn -> {
                showAvatorDialog()
//                startActivityForResult<TCCameraActivity>(USER_PROFILE_REQUEST_CODE)
            }
            //点击跳转到标签选择页
            R.id.confirmBtn -> {
                if (userBirthTv.text.isNullOrEmpty() || userBirthTv.text == "点击填写生日") {
                    CommonFunction.toast("请填写生日!")
                    confirmBtn.isEnabled = false
                    return
                }
                if (sexGroup.checkedRadioButtonId != R.id.userSexMan && sexGroup.checkedRadioButtonId != R.id.userSexWoman) {
                    CommonFunction.toast("请选择性别!")
                    confirmBtn.isEnabled = false
                    return
                }
                if (userNickNameEt.text.isNullOrEmpty()) {
                    CommonFunction.toast("请填写昵称!")
                    confirmBtn.isEnabled = false
                    return
                }

                params["city_code"] = UserManager.getCityCode()
                params["lat"] = UserManager.getlatitude()
                params["lng"] = UserManager.getlongtitude()

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
                        var path = PictureSelector.obtainMultipleResult(data)[0].path
                        UriUtils.getLubanBuilder(this)
                            .load(path)
                            .setCompressListener(object : OnCompressListener {
                                override fun onSuccess(file: File?) {
                                    if (file != null) {
                                        path = file.absolutePath
                                    }
                                    Log.d("SetInfoActivity", "$path===========================")
                                    GlideUtil.loadCircleImg(this@SetInfoActivity, path, userProfileBtn)
                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
                                            "accid"
                                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                            16
                                        )}"
                                    mPresenter.uploadProfile(path, key)

                                }

                                override fun onError(e: Throwable?) {
                                    Log.d("SetInfoActivity", "$path===========================")
                                    GlideUtil.loadCircleImg(this@SetInfoActivity, path, userProfileBtn)
                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
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

                REQUEST_CODE_TAKE_PHOTO -> {
                    if (imageFile != null) {
                        var path = imageFile!!.absolutePath
                        UriUtils.getLubanBuilder(this)
                            .load(path)
                            .setCompressListener(object : OnCompressListener {
                                override fun onSuccess(file: File?) {
                                    if (file != null) {
                                        path = file.absolutePath
                                    }
                                    Log.d("SetInfoActivity", "$path===========================")
                                    GlideUtil.loadCircleImg(this@SetInfoActivity, path, userProfileBtn)
                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
                                            "accid"
                                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                            16
                                        )}"
                                    mPresenter.uploadProfile(path, key.toString())
                                }

                                override fun onError(e: Throwable?) {
                                    Log.d("SetInfoActivity", "$path===========================")
                                    GlideUtil.loadCircleImg(this@SetInfoActivity, path, userProfileBtn)
                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
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
            !userProfile.isNullOrEmpty() && userBirthTv.text.toString().isNotEmpty() && userBirthTv.text != "点击填写生日"
                    && userNickNameEt.text.toString().isNotEmpty() && nickNameValidate
                    && (sexGroup.checkedRadioButtonId == R.id.userSexWoman || sexGroup.checkedRadioButtonId == R.id.userSexMan)
    }


    private var imageFile: File? = null     //拍照后保存的照片
    private var imgUri: Uri? = null         //拍照后保存的照片的uri
    private fun takePhoto() {
        imageFile = createImageFile()
        imageFile?.let {
            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //如果是7.0以上，使用FileProvider，否则会报错
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                imgUri = FileProvider.getUriForFile(this, PublishActivity.AUTHORITY, it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri) //设置拍照后图片保存的位置
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(it)) //设置拍照后图片保存的位置
            }
            intent.resolveActivity(packageManager)?.let {
                startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO) //调起系统相机
            }
        }
    }

    /**
     * 创建文件夹来保存照片
     */
    private fun createImageFile(): File? {
        return try {
            var rootFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "jitangapplicaiton/camera"
            )
            if (!rootFile.exists())
                rootFile.mkdirs()
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val fileName = "IMG_$timeStamp.jpg"
            File(rootFile.absolutePath + File.separator + fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    override fun onBackPressed() {

    }


    /**
     * 0-相机
     * 1-相册
     */
    fun requestForCamera(typeCode: Int) {
        //TODO 请求接口看是否已经屏蔽过通讯录
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //申请权限
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS), typeCode)
        } else {
            startToCamera(typeCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 1 || requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startToCamera(requestCode)
            } else {
                CommonAlertDialog.Builder(this)
                    .setTitle("权限开启")
                    .setContent("您已拒绝相机权限的开启，请到设置界面打开权限后再操作")
                    .setConfirmText("确定")
                    .setCancelIconIsVisibility(false)
                    .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                        override fun onClick(dialog: Dialog) {
                            val packageURI = Uri.parse("package:${AppUtils.getAppPackageName()}")
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
                            startActivity(intent)
                            dialog.dismiss()
                        }
                    })


            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * 0-相机
     * 1-相册
     */
    private fun startToCamera(typeCode: Int) {
        when (typeCode) {
            0 -> {
                choosePhoto()
            }
            1 -> {
                takePhoto()
            }
        }
    }
}
