package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.google.gson.Gson
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.tools.SdkVersionUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.MediaParamBean
import com.sdy.jitangapplication.presenter.SweetHeartVerifyUploadPresenter
import com.sdy.jitangapplication.presenter.view.SweetHeartVerifyUploadView
import com.sdy.jitangapplication.ui.adapter.SweetVerifyPicAdapter
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_sweet_heart_verify_upload.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 甜心圈认证上传
 * 男性的
 * 豪车认证 需要上传——手持身份证—身份证正面—车辆行驶证
 * 资产认证  需要上传——手持身份证—身份证正面—房产证
 * 女性的
 * 身材/职业  都是上传——手持身份证—身份证正面
 */
class SweetHeartVerifyUploadActivity : BaseMvpActivity<SweetHeartVerifyUploadPresenter>(),
    SweetHeartVerifyUploadView {
    //    // public_type [int]	是	1非公开 2公开
    //    //type [int]	是	1资产证明 2豪车 3身材 4职业
    //    //photo [string]	是	json串
    companion object {
        const val REQUEST_VERIFY = 1001
        const val MAX_COUNT_MAN = 3
        const val MAX_COUNT_WOMAN = 2
        const val TYPE_WEALTH = 1
        const val TYPE_CAR = 2
        const val TYPE_FIGURE = 3
        const val TYPE_PROFESSION = 4
    }

    private val type by lazy { intent.getIntExtra("type", 0) }

    private val sweetVerifyPicAdapter by lazy { SweetVerifyPicAdapter() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sweet_heart_verify_upload)
        initView()
    }

    private val maxCount by lazy {
        if (UserManager.getGender() == 1) {
            MAX_COUNT_MAN
        } else {
            MAX_COUNT_WOMAN
        }
    }

    private fun initView() {
        mPresenter = SweetHeartVerifyUploadPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        BarUtils.setStatusBarColor(this, Color.WHITE)
        btnBack.clickWithTrigger {
            finish()
        }

        if (type == TYPE_FIGURE || type == TYPE_PROFESSION) {
            hotT1.text = if (type == TYPE_FIGURE) {
                "身材认证"
            } else {
                "职业认证"
            }
            t1.text = "首先上传手持身份证及身份证正面\n请确保与本人头像一致，此流程不对外公开"
            sweetVerifyNormalIv.setImageResource(R.drawable.icon_sweet_upload_normal_woman)
        } else if (type == TYPE_CAR) {
            hotT1.text = "豪车认证"
            t1.text = "首先上传手持身份证及驾驶证\n请确保与本人头像一致，照片不会对外公开"
            sweetVerifyNormalIv.setImageResource(R.drawable.icon_sweet_upload_normal_man_car)

        } else {
            hotT1.text = "资产认证"
            t1.text = "首先上传手持身份证及证明资产所有权\n如房产证、公司注册、存款明细等\n照片不会对外公开"
            sweetVerifyNormalIv.setImageResource(R.drawable.icon_sweet_upload_normal_man_wealth)
        }



        sweetPicRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        sweetPicRv.adapter = sweetVerifyPicAdapter
        sweetVerifyPicAdapter.addData(MediaParamBean(""))
        sweetVerifyPicAdapter.setOnItemClickListener { _, view, position ->
            if (sweetVerifyPicAdapter.data[position].url.isEmpty()) {
                CommonFunction.onTakePhoto(
                    this,maxCount- (sweetVerifyPicAdapter.data.size - 1),
                    REQUEST_VERIFY
                )
            }
        }
        sweetVerifyPicAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.sweetPicDelete -> {
                    sweetVerifyPicAdapter.remove(position)
                    if (sweetVerifyPicAdapter.data.size < maxCount + 1
                        && !sweetVerifyPicAdapter.data.contains(MediaParamBean(""))
                    ) {
                        sweetVerifyPicAdapter.addData(MediaParamBean(""))
                    }
                    checkEnable()
                }
            }
        }

        uploadVerifyBtn.clickWithTrigger {
            mPresenter.uploadPhoto(sweetVerifyPicAdapter.data[index].url, index)
        }
    }

    private fun checkEnable() {
        if (sweetVerifyPicAdapter.data.size - 1 == maxCount) {
            uploadVerifyBtn.isEnabled = true
            sweetVerifyPicAdapter.remove(sweetVerifyPicAdapter.data.size - 1)
        } else {
            uploadVerifyBtn.isEnabled = false
        }
    }

    private var index = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_VERIFY) {
            if (data != null) {
                if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                    for (tdata in PictureSelector.obtainMultipleResult(data)) {
                        if (SdkVersionUtils.checkedAndroid_Q() && !tdata.androidQToPath.isNullOrEmpty()) {
                            sweetVerifyPicAdapter.addData(
                                sweetVerifyPicAdapter.data.size - 1,
                                MediaParamBean(tdata.androidQToPath, 0, tdata.width, tdata.height)
                            )
                        } else {
                            sweetVerifyPicAdapter.addData(
                                sweetVerifyPicAdapter.data.size - 1,
                                MediaParamBean(tdata.path, 0, tdata.width, tdata.height)
                            )
                        }

                    }
                    checkEnable()
                }
            }
        }
    }

    private val keys = arrayListOf<MediaParamBean>()
    override fun uploadImgResult(success: Boolean, key: String, index1: Int) {
        if (success) {
            keys.add(
                MediaParamBean(
                    key,
                    0,
                    sweetVerifyPicAdapter.data[index1].width,
                    sweetVerifyPicAdapter.data[index1].height
                )
            )
            if (index == maxCount - 1) {
                mPresenter.uploadData(1, type, Gson().toJson(keys))
            } else {
                index++
                mPresenter.uploadPhoto(sweetVerifyPicAdapter.data[index].url)
            }
        } else {
            index = 0
            keys.clear()
            CommonFunction.toast("图片上传失败，请重新尝试。")
        }
    }

    override fun uploadDataResult(success: Boolean) {
        if (success) {
            index = 0
            keys.clear()
            startActivity<SweetHeartSquareUploadActivity>("type" to type)
        }
    }

    override fun getPicTplResult(datas: ArrayList<String>) {


    }

}