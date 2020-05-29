package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SpanUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.gson.Gson
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.OnLazyClickListener
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.MyPhotoBean
import com.sdy.jitangapplication.presenter.UserNickNamePresenter
import com.sdy.jitangapplication.presenter.view.UserNickNameView
import com.sdy.jitangapplication.ui.adapter.UploadAvatorAdapter
import com.sdy.jitangapplication.ui.adapter.UserUploadPicAdapter
import com.sdy.jitangapplication.ui.dialog.UploadAvatorDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_user_avator.*
import kotlinx.android.synthetic.main.dialog_upload_avator.*
import org.jetbrains.anko.startActivity
import top.zibin.luban.OnCompressListener
import java.io.File
import java.util.*

/**
 * 用户头像
 */
class UserAvatorActivity : BaseMvpActivity<UserNickNamePresenter>(), UserNickNameView,
    OnLazyClickListener {

    companion object {
        const val REQUEST_CODE_TAKE_PHOTO = 1
        const val REQUEST_PIC_CODE = 111

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_avator)
        initView()
    }

    private val adapter by lazy {
        UserUploadPicAdapter()
            .apply {
                addData(
                    mutableListOf(
                        MyPhotoBean(id = -1),
                        MyPhotoBean(id = -1),
                        MyPhotoBean(id = -1),
                        MyPhotoBean(id = -1),
                        MyPhotoBean(id = -1),
                        MyPhotoBean(id = -1),
                        MyPhotoBean(id = -1),
                        MyPhotoBean(id = -1)
                    )
                )
            }
    }


    private var clickPos = 0
    private fun initView() {
        setSwipeBackEnable(false)
        mPresenter = UserNickNamePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        userAvatorTake.setOnClickListener(this)
        userAvator.setOnClickListener(this)
        btnNextStep.setOnClickListener(this)


        userAvatorTitle.text = SpanUtils.with(userAvatorTitle)
            .append("上传")
            .append("本人高清/美颜头像")
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .append("，优先获得")
            .append("推荐")
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .create()
        userPhotosRv.layoutManager = GridLayoutManager(this, 4, RecyclerView.VERTICAL, false)
        userPhotosRv.adapter = adapter

        adapter.setOnItemClickListener { _, view, position ->
            clickPos = position
            showAvatorDialog(false, REQUEST_PIC_CODE)
        }


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
                                                this@UserAvatorActivity,
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
                    this@UserAvatorActivity,
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
                                                this@UserAvatorActivity,
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
                    this@UserAvatorActivity,
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                //相册拍摄
                PictureConfig.CHOOSE_REQUEST,
                    //选择照片
                REQUEST_CODE_TAKE_PHOTO -> {
                    if (data != null) {
                        startAnimation(false)
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
                                        this@UserAvatorActivity, path, userAvator
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
                                    uploadProfile(path, key)
                                }

                                override fun onError(e: Throwable?) {
                                    Log.d("SetInfoActivity", "$path===========================")
                                    GlideUtil.loadCircleImg(
                                        this@UserAvatorActivity, path,
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
                                    uploadProfile(path, key.toString())


                                }

                                override fun onStart() {

                                }

                            })
                            .launch()
                    }
                }


                //上传相册
                REQUEST_PIC_CODE -> {
                    if (data != null) {
                        var path =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !PictureSelector.obtainMultipleResult(
                                    data
                                )[0].androidQToPath.isNullOrEmpty()
                            ) {
                                PictureSelector.obtainMultipleResult(data)[0].androidQToPath
                            } else {
                                PictureSelector.obtainMultipleResult(data)[0].path
                            }

                        val qnPath =
                            "${Constants.FILE_NAME_INDEX}${Constants.USERCENTER}${UserManager.getAccid()}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                16
                            )}"
                        mPresenter.loadingDialg.show()
                        mPresenter.uploadProfile(path, qnPath)


                    }
                }

            }
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


    private fun showAplhaAnimation(isShow: Boolean) {

        YoYo.with(
            if (isShow) {
                Techniques.FadeInUp
            } else {
                Techniques.FadeOutDown
            }
        )
            .duration(1000L)
            .repeat(0)
            .playOn(userPhotosRv)

//        val objctAnimator = ObjectAnimator.ofFloat(userPhotosRv, "alpha", from, to)
//        objctAnimator.duration = 800L
//        objctAnimator.start()
    }


    /**
     * 上传照片
     * imagePath 文件名格式： ppns/文件类型名/用户ID/当前时间戳/16位随机字符串
     */
    fun uploadProfile(filePath: String, imagePath: String) {
        if (!mPresenter.checkNetWork()) {
            return
        }
        mPresenter.loadingDialg.show()
        QNUploadManager.getInstance().put(
            filePath, imagePath, SPUtils.getInstance(Constants.SPNAME).getString("qntoken"),
            { key, info, response ->
                Log.d(
                    "OkHttp",
                    "token = ${SPUtils.getInstance(Constants.SPNAME).getString("qntoken")}"
                )
                Log.d("OkHttp", "key=$key\ninfo=$info\nresponse=$response")
                if (info != null) {
                    if (!info.isOK) {
                        CommonFunction.toast("头像上传失败！")
                    } else {
                        onUploadUserAvatorResult(key)
                    }
                }
            }, null
        )
    }


    // 检查头像是否合规
    private fun checkAvatar(params: HashMap<String, Any>) {
        if (!mPresenter.checkNetWork()) {
            return
        }

        RetrofitFactory.instance.create(Api::class.java)
            .checkAvatar(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        onCheckAvatorResult(true, t.msg)
                    } else {
                        onCheckAvatorResult(false, t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
//                    onCheckAvatorResult(false, CommonFunction.getErrorMsg(this@UserAvatorActivity))
                    onCheckAvatorResult(false, "网络异常，请重新选择上传")
                }
            })
    }


    //用户头像
    private var userProfile: String? = null

    private fun onUploadUserAvatorResult(key: String) {
        userProfile = key
        checkAvatar(hashMapOf("avatar" to userProfile.toString()))
    }


    /**
     * 头像审核结果
     */
    fun onCheckAvatorResult(checkResult: Boolean, msg: String?) {
        mPresenter.loadingDialg.dismiss()
        btnNextStep.isEnabled = checkResult
        avatorNotify.text = "$msg"
        startAnimation(true)
        if (checkResult)
            showAplhaAnimation(true)
    }


    override fun onUploadUserInfoResult(
        uploadResult: Boolean,
        msg: String?,
        moreMatchBean: MoreMatchBean?
    ) {

    }

    override fun uploadImgResult(b: Boolean, key: String) {
        if (b) {
            mPresenter.addPhotoWall(key)
        }
    }

    override fun onAddPhotoWallResult(data: MyPhotoBean) {
        if ((clickPos > 0 && adapter.data[clickPos - 1].id != -1) || clickPos == 0) {
            adapter.setData(clickPos, data)
        } else {
            for (tData in adapter.data.withIndex()) {
                if (tData.value.id == -1) {
                    adapter.setData(tData.index, data)
                    break
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun onRegisterAddPhoto(data: MoreMatchBean?) {
        if (data != null && data!!.gender_str.isNotEmpty()) {
            SPUtils.getInstance(Constants.SPNAME).put("avatar", data?.avatar)
            startActivity<GetMoreMatchActivity>("moreMatch" to data)
            finish()
        } else {
            btnNextStep.isEnabled = true
        }
    }


    override fun onLazyClick(view: View) {
        when (view.id) {
            R.id.userAvatorTake,
            R.id.userAvator -> {
                showAvatorDialog(true, PictureConfig.CHOOSE_REQUEST)
            }
            R.id.help -> {
                startActivity<LoginHelpActivity>()
            }
            R.id.btnNextStep -> {
                val params = hashMapOf<String, Any>("avatar" to userProfile.toString())
                val registerPhotos = mutableListOf<Int>()
                for (data in adapter.data) {
                    if (data.id != -1) {
                        registerPhotos.add(data.id)
                    }
                }
                params["photos"] = Gson().toJson(registerPhotos)
                mPresenter.registerAddPhoto(params)
            }
        }
    }

    override fun onBackPressed() {

    }



}
