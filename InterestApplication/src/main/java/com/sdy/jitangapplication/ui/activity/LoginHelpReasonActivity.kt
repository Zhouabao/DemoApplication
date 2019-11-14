package com.sdy.jitangapplication.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.RegexUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.common.AppManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureConfig.CHOOSE_REQUEST
import com.luck.picture.lib.config.PictureMimeType
import com.sdy.baselibrary.utils.RandomUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.ReportBean
import com.sdy.jitangapplication.presenter.LoginHelpResonPresenter
import com.sdy.jitangapplication.presenter.view.LoginHelpResonView
import com.sdy.jitangapplication.ui.adapter.ReportPicAdapter
import com.sdy.jitangapplication.ui.adapter.ReportResonAdapter
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_login_help_reason.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 问题反馈填写界面
 */
class LoginHelpReasonActivity : BaseMvpActivity<LoginHelpResonPresenter>(), LoginHelpResonView, View.OnClickListener {
    private val loading by lazy { LoadingDialog(this) }

    private val reportResonAdapter by lazy { ReportResonAdapter() }
    private val reportParams: HashMap<String, Any> = hashMapOf()

    //举报图片
    private val reportPicAdapter by lazy { ReportPicAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_help_reason)
        initView()

    }


    private fun initView() {
        mPresenter = LoginHelpResonPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick {
            finish()
        }
        hotT1.text = "问题反馈"
        helpConfirm.setOnClickListener(this)


        helpPicRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        helpPicRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL_LIST,
                SizeUtils.dp2px(10F),
                resources.getColor(R.color.colorWhite)
            )
        )
        helpPicRv.adapter = reportPicAdapter
        reportPicAdapter.addData("")
        reportPicAdapter.setOnItemClickListener { _, view, position ->
            if (reportPicAdapter.data[position] == "") {
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
                                                onTakePhoto()
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
                    onTakePhoto()
                }
            } else {
                reportPicAdapter.remove(position)
                if (!reportPicAdapter.data.contains("")) {
                    reportPicAdapter.addData(reportPicAdapter.data.size, "")
                }
            }
        }

        helpContentLength.text = SpanUtils.with(helpContentLength)
            .append(helpContent.length().toString())
            .setFontSize(14, true)
            .setForegroundColor(resources.getColor(R.color.colorOrange))
            .setBold()
            .append("/500")
            .setFontSize(10, true)
            .create()

        //问题反馈内容
        helpContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable) {
                checkConfirm()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                helpContentLength.text = SpanUtils.with(helpContentLength)
                    .append(helpContent.length().toString())
                    .setFontSize(14, true)
                    .setForegroundColor(resources.getColor(R.color.colorOrange))
                    .setBold()
                    .append("/500")
                    .setFontSize(10, true)
                    .create()
            }

        })

        //手机号码
        helpPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                checkConfirm()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

        })
    }


    fun checkConfirm() {
        helpConfirm.isEnabled =
            !helpContent.text.trim().toString().isNullOrEmpty() && RegexUtils.isMobileSimple(helpPhone.text)
    }


    /**
     * 拍照或者选取照片
     */
    private fun onTakePhoto() {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .maxSelectNum(3 - (reportPicAdapter.data.size - 1))
            .minSelectNum(0)
            .imageSpanCount(4)
            .selectionMode(PictureConfig.MULTIPLE)
            .previewImage(true)
            .isCamera(true)
            .enableCrop(false)
            .compressSavePath(UriUtils.getCacheDir(this))
            .compress(false)
            .scaleEnabled(true)
            .showCropFrame(true)
            .rotateEnabled(false)
            .withAspectRatio(9, 16)
            .compressSavePath(UriUtils.getCacheDir(this))
            .openClickSound(false)
            .forResult(CHOOSE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_REQUEST) {
            if (data != null) {
                if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                    for (tdata in PictureSelector.obtainMultipleResult(data)) {
                        reportPicAdapter.addData(0, tdata.path)
                    }
                    if (reportPicAdapter.data.size == 4) {
                        reportPicAdapter.remove(reportPicAdapter.data.size - 1)
                    }
                }
            }
        }
    }

    override fun onGetUserActionResult(b: Boolean, msg: String?) {
        CommonFunction.toast("$msg")
        if (b) {
            AppManager.instance.finishAllActivity()
            startActivity<LoginActivity>()
        }
    }

    override fun onGetReportMsgResult(b: Boolean, reasons: MutableList<String>?) {
        if (b && !reasons.isNullOrEmpty()) {
            for (reason in reasons) {
                reportResonAdapter.addData(ReportBean(reason, false))
            }
        }
    }

    override fun uploadImgResult(success: Boolean, imageName: String, uploadedNum: Int) {
        if (success) {
            photosNameArray.add(imageName)
            if ((uploadedNum + 1) == (reportPicAdapter.data.size - if (reportPicAdapter.data.contains("")) {
                    1
                } else {
                    0
                })
            ) {
                loading.dismiss()
                mPresenter.feedback(helpContent.text.toString(), helpPhone.text.toString(), photosNameArray)
            } else {
                photosNum++
                if (reportPicAdapter.data[photosNum] != "") {
                    val imageName =
                        "${Constants.FILE_NAME_INDEX}${Constants.REPORTUSER}${UserManager.getAccid()}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                            16
                        )}"
                    mPresenter.uploadProfile(reportPicAdapter.data[photosNum], imageName, photosNum)
                }
            }
        }


    }


    override fun onError(text: String) {
        CommonFunction.toast(CommonFunction.getErrorMsg(this))
    }

    private var photosNum = 0
    private var photosNameArray = mutableListOf<String>()
    override fun onClick(view: View) {
        when (view.id) {
            R.id.helpConfirm -> {

//                ActivityUtils.finishToActivity(LoginActivity::class.java, false)
                loading.show()
                if (!helpContent.text.isNullOrEmpty()) {
                    reportParams["supplement_comment"] = helpContent.text.toString()
                }

                if (reportPicAdapter.data.isNotEmpty() && reportPicAdapter.data.size > 1) {
                    if (reportPicAdapter.data[photosNum] != "") {
                        val imageName =
                            "${Constants.FILE_NAME_INDEX}${Constants.REPORTUSER}${UserManager.getAccid()}/${System.currentTimeMillis()}/${RandomUtils.getRandomString(
                                16
                            )}"
                        mPresenter.uploadProfile(reportPicAdapter.data[photosNum], imageName, photosNum)
                    }
                } else {
                    mPresenter.feedback(helpContent.text.toString(), helpPhone.text.toString(), photosNameArray)
                }
            }
        }

    }

}
