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
import com.blankj.utilcode.util.SizeUtils
import com.google.gson.Gson
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.tools.SdkVersionUtils
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.MediaParamBean
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.presenter.AddLabelSuccessPresenter
import com.sdy.jitangapplication.presenter.view.AddLabelSuccessView
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_add_label_success.*
import kotlinx.android.synthetic.main.error_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity

/**
 * 添加兴趣成功
 */
class AddLabelSuccessActivity : BaseMvpActivity<AddLabelSuccessPresenter>(), AddLabelSuccessView, View.OnClickListener {

    private val labelBean by lazy { intent.getSerializableExtra("data") as MyLabelBean }
    private var currTitleIndex = -1
    private val labelTitles = mutableListOf<LabelQualityBean>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_label_success)
        initView()
        mPresenter.getTagTraitInfo(hashMapOf("type" to LabelQualityActivity.TYPE_TITLE, "tag_id" to labelBean.tag_id))

    }

    private fun initView() {
        mPresenter = AddLabelSuccessPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        successLabelName.text = "${labelBean.title}"
        startJitangBtn.setOnClickListener(this)
        changeLabel.setOnClickListener(this)
        publishImage.setOnClickListener(this)
        publish.setOnClickListener(this)

        stateAddLabelSuccess.retryBtn.onClick {
            stateAddLabelSuccess.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getTagTraitInfo(hashMapOf("type" to LabelQualityActivity.TYPE_TITLE, "tag_id" to labelBean.tag_id))
        }


        lottieView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                lottieView.isVisible = false
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

    override fun getTagTraitInfoResult(b: Boolean, mutableList: MutableList<LabelQualityBean>?) {

        if (b) {
            stateAddLabelSuccess.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (!mutableList.isNullOrEmpty()) {
                labelTitles.clear()
                labelTitles.addAll(mutableList)
                currTitleIndex = 0
                setTitleBean()
            } else {
                successLabelGuide.isVisible = false
                changeLabel.isVisible = false
            }
        } else {
            stateAddLabelSuccess.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }


    private fun setTitleBean() {
        successLabelGuide.text = labelTitles[currTitleIndex].content
    }

    override fun onUploadImgResult(b: Boolean, qnPath: String) {
        if (b) {
            val param = hashMapOf(
                "token" to UserManager.getToken(),
                "accid" to UserManager.getAccid(),
                "descr" to "",
                "tag_id" to labelBean.tag_id,
                "title" to "${successLabelGuide.text}",
                "lat" to UserManager.getlatitude(),
                "lng" to UserManager.getlongtitude(),
                "province_name" to UserManager.getProvince(),
                "city_name" to UserManager.getCity(),
                "city_code" to UserManager.getCityCode(),
                "puber_address" to "",
                //发布消息的类型0,纯文本的 1，照片 2，视频 3，声音
                "type" to 1,
                "comment" to Gson().toJson(
                    mutableListOf(
                        Gson().toJson(
                            MediaParamBean(
                                qnPath,
                                0,
                                mediaBean!!.width,
                                mediaBean!!.height
                            )
                        )
                    )
                )
            )
            mPresenter.publishContent(param)
        }
        publish.isEnabled = true
    }


    override fun onSquareAnnounceResult(b: Boolean, code: Int) {
        if (b) {
            ActivityUtils.finishAllActivities()
            startActivity<MainActivity>()
        }
        publish.isEnabled = true
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
                                                    PictureMimeType.ofImage(), true
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
                        PictureMimeType.ofImage(), true
                    )
                }
            }
            //稍后再说
            startJitangBtn -> {
                EventBus.getDefault().post(UpdateMyLabelEvent())
                EventBus.getDefault().post(RefreshEvent(true))
                finish()
            }
            //换一个标题
            changeLabel -> {
                if (currTitleIndex != -1 && labelTitles.size - 1 > currTitleIndex) {
                    currTitleIndex += 1
                } else {
                    currTitleIndex = 0
                }
                setTitleBean()
            }
            //发布
            publish -> {
                publish.isEnabled = false
                if (mediaBean == null) {
                    CommonFunction.toast("请先选择要上传的图片")
                    return
                }
                val qnPath =
                    "${Constants.FILE_NAME_INDEX}${Constants.PUBLISH}${UserManager.getAccid()}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                        16
                    )}"
                mPresenter.uploadFile(
                    if (SdkVersionUtils.checkedAndroid_Q()) {
                        if (mediaBean!!.androidQToPath.isNullOrEmpty()) {
                            mediaBean!!.path
                        } else {
                            mediaBean!!.androidQToPath
                        }
                    } else {
                        mediaBean!!.compressPath
                    }, qnPath
                )


            }
        }

    }


    private var mediaBean: LocalMedia? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PictureConfig.CHOOSE_REQUEST) {
            if (data != null) {
                if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                    mediaBean = PictureSelector.obtainMultipleResult(data)[0]

                    GlideUtil.loadRoundImgCenterCrop(
                        this, if (SdkVersionUtils.checkedAndroid_Q()) {
                            if (mediaBean!!.androidQToPath.isNullOrEmpty()) {
                                mediaBean!!.path
                            } else {
                                mediaBean!!.androidQToPath
                            }
                        } else {
                            mediaBean!!.compressPath
                        }, publishImage, SizeUtils.dp2px(12F)
                    )
                    publish.isEnabled = true
                }
            }
        }
    }


    override fun onError(text: String) {
        CommonFunction.toast(text)
    }

    private val loading by lazy { LoadingDialog(this) }
    override fun showLoading() {
        if (!loading.isShowing)
            loading.show()
    }

    override fun hideLoading() {
        if (loading.isShowing)
            loading.dismiss()
    }
}
