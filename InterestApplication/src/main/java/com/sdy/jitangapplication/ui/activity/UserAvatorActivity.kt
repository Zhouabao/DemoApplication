package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.SPUtils
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
import com.sdy.jitangapplication.presenter.UserNickNamePresenter
import com.sdy.jitangapplication.presenter.view.UserNickNameView
import com.sdy.jitangapplication.ui.adapter.UploadAvatorAdapter
import com.sdy.jitangapplication.ui.dialog.UploadAvatorDialog
import com.sdy.jitangapplication.utils.QNUploadManager
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_user_avator.*
import kotlinx.android.synthetic.main.dialog_upload_avator.*
import org.jetbrains.anko.startActivity
import top.zibin.luban.OnCompressListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 用户头像
 */
class UserAvatorActivity : BaseMvpActivity<UserNickNamePresenter>(), UserNickNameView, View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_avator)
        initView()
    }

    private fun initView() {
        mPresenter = UserNickNamePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        userAvatorTake.setOnClickListener(this)
        btnNextStep.setOnClickListener(this)
        help.setOnClickListener(this)

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
                                            choosePhoto()
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
                choosePhoto()
                uploadAvatorDialog.cancel()
            }
        }
        uploadAvatorDialog.takePhoto.onClick {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (!PermissionUtils.isGranted(PermissionConstants.CAMERA) ||
                        !PermissionUtils.isGranted(PermissionConstants.STORAGE))
            ) {
                PermissionUtils.permission(PermissionConstants.CAMERA)
                    .callback(object : PermissionUtils.SimpleCallback {
                        override fun onGranted() {
                            if (!PermissionUtils.isGranted(PermissionConstants.STORAGE)) {
                                PermissionUtils.permission(PermissionConstants.STORAGE)
                                    .callback(object : PermissionUtils.SimpleCallback {
                                        override fun onGranted() {
                                            takePhoto()
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
                takePhoto()
                uploadAvatorDialog.cancel()
            }
        }
        uploadAvatorDialog.cancel.onClick {
            uploadAvatorDialog.cancel()
        }

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
                startActivityForResult(intent, SetInfoActivity.REQUEST_CODE_TAKE_PHOTO) //调起系统相机
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    if (data != null) {
                        startAnimation(false)
                        var path = PictureSelector.obtainMultipleResult(data)[0].path
                        UriUtils.getLubanBuilder(this)
                            .load(path)
                            .setCompressListener(object : OnCompressListener {
                                override fun onSuccess(file: File?) {
                                    if (file != null) {
                                        path = file.absolutePath
                                    }
                                    GlideUtil.loadCircleImg(this@UserAvatorActivity, path, userAvatorTake)
                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
                                            "accid"
                                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                            16
                                        )}"
                                    uploadProfile(path, key)
                                }

                                override fun onError(e: Throwable?) {
                                    Log.d("SetInfoActivity", "$path===========================")
                                    GlideUtil.loadCircleImg(this@UserAvatorActivity, path, userAvatorTake)
                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
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

                SetInfoActivity.REQUEST_CODE_TAKE_PHOTO -> {
                    if (imageFile != null) {
                        startAnimation(false)
                        var path = imageFile!!.absolutePath
                        UriUtils.getLubanBuilder(this)
                            .load(path)
                            .setCompressListener(object : OnCompressListener {
                                override fun onSuccess(file: File?) {
                                    if (file != null) {
                                        path = file.absolutePath
                                    }
                                    GlideUtil.loadCircleImg(this@UserAvatorActivity, path, userAvatorTake)
                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
                                            "accid"
                                        )}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                            16
                                        )}"
                                    uploadProfile(path, key.toString())
                                }

                                override fun onError(e: Throwable?) {
                                    GlideUtil.loadCircleImg(this@UserAvatorActivity, path, userAvatorTake)
                                    val key =
                                        "${Constants.FILE_NAME_INDEX}${Constants.AVATOR}${SPUtils.getInstance(Constants.SPNAME).getString(
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
                Log.d("OkHttp", "token = ${SPUtils.getInstance(Constants.SPNAME).getString("qntoken")}")
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
        params["token"] = UserManager.getToken()
        params["accid"] = UserManager.getAccid()


        RetrofitFactory.instance.create(Api::class.java)
            .checkAvatar(params)
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
                    onCheckAvatorResult(false, CommonFunction.getErrorMsg(this@UserAvatorActivity))
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

    }


    override fun onUploadUserInfoResult(uploadResult: Boolean, msg: String?) {
        mPresenter.loadingDialg.dismiss()
        if (uploadResult) {
            startActivity<LabelsActivity>()
            finish()
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.userAvatorTake -> {
                showAvatorDialog()
            }
            R.id.help -> {
                startActivity<LoginHelpActivity>()
            }
            R.id.btnNextStep -> {
                mPresenter.loadingDialg.show()
                mPresenter.uploadUserInfo(4, hashMapOf("avatar" to userProfile.toString()))
            }
        }
    }

    override fun onBackPressed() {

    }

}
