package com.example.demoapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.cazaea.sweetalert.SweetAlertDialog
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.presenter.SetInfoPresenter
import com.example.demoapplication.presenter.view.SetInfoView
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.android.synthetic.main.activity_set_info.*
import org.jetbrains.anko.startActivity


/**
 * 填写个人信息页面
 */
class SetInfoActivity : BaseMvpActivity<SetInfoPresenter>(), SetInfoView, View.OnClickListener {

    private val handler by lazy { Handler() }
    private val delayRun by lazy { Runnable { mPresenter.checkNickName(userNickNameEt.text.toString()) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initState()

        setContentView(R.layout.activity_set_info)
        mPresenter = SetInfoPresenter()
        mPresenter.mView = this
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
            }

        })


    }


    /**
     * 拍照或者选取照片
     */
    override fun onTakePhoto() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofAll())
            .maxSelectNum(1)
            .minSelectNum(0)
            .imageSpanCount(4)
            .selectionMode(PictureConfig.SINGLE)
            .previewImage(true)
            .previewVideo(true)
            .isCamera(true)
            .enableCrop(true)
            .withAspectRatio(4, 3)
            .compress(true)
            .openClickSound(true)
            .forResult(PictureConfig.CHOOSE_REQUEST)
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
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("")
                .setContentText("昵称违规请修改")
                .setConfirmText("OK")
                .show()
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.userProfileBtn -> {
//                onTakePhoto()
                startActivity<VideoRecordActivity>()
            }
            //点击跳转到标签选择页
            R.id.confirmBtn -> {
                SweetAlertDialog(this)
                    .setTitleText("温馨提示")
                    .setContentText("性别是不可更改项仅可选择一次")
                    .setConfirmText("确定")
                    .setConfirmClickListener {
                        startActivity<LabelsActivity>()
                        it.cancel()
                    }
                    .show()
            }
            R.id.userBirthTv -> {
                startActivity<UserBirthActivity>()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    val selectList: List<LocalMedia> = PictureSelector.obtainMultipleResult(data)
                    GlideUtil.loadCircleImg(applicationContext, selectList[0].compressPath, userProfileBtn)
                }
            }
        }
    }


}
