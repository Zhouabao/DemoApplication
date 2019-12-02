package com.sdy.jitangapplication.ui.activity

import android.animation.Animator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.PermissionUtils
import com.kotlin.base.ui.activity.BaseActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.NewLabel
import kotlinx.android.synthetic.main.activity_add_label_success.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.startActivity

/**
 * 添加标签成功
 */
class AddLabelSuccessActivity : BaseActivity(), View.OnClickListener {
    private val labelBean by lazy { intent.getSerializableExtra("data") as NewLabel }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_label_success)

        initView()

        successLabelName.text = "${labelBean.title}"
        startJitangBtn.onClick {
            ActivityUtils.finishAllActivities()
            startActivity<MainActivity>()
        }

    }

    private fun initView() {
        lottieView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                lottieView.isVisible = false
                contentCl.isVisible = true
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationStart(p0: Animator?) {
            }

        })
        publishImage.setOnClickListener(this)
        changeLabel.setOnClickListener(this)
        startJitangBtn.setOnClickListener(this)
        publish.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view) {
            //上传照片
            publishImage -> {
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
                                                    this@AddLabelSuccessActivity,
                                                    1,
                                                    PictureConfig.CHOOSE_REQUEST,
                                                    PictureMimeType.ofImage()
                                                )
                                            }

                                            override fun onDenied() {
                                                CommonFunction.toast("文件存储权限被拒,请允许权限后再上传照片.")
                                            }

                                        })
                                        .request()
                            }

                            override fun onDenied() {
                                CommonFunction.toast("相机权限被拒,请允许权限后再上传照片.")
                            }
                        })
                        .request()
                } else {
                    CommonFunction.onTakePhoto(
                        this@AddLabelSuccessActivity,
                        1,
                        PictureConfig.CHOOSE_REQUEST,
                        PictureMimeType.ofImage()
                    )
                }
            }
            //稍后再说
            startJitangBtn -> {
            }
            //换一个标题
            changeLabel -> {
            }
            //发布
            publish -> {
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PictureConfig.CHOOSE_REQUEST) {
            if (data != null) {
                if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                    for (tdata in PictureSelector.obtainMultipleResult(data)) {
                    }
                }
            }
        }
    }

}
