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
import com.sdy.jitangapplication.model.SweetUploadBean
import com.sdy.jitangapplication.presenter.SweetHeartVerifyUploadPresenter
import com.sdy.jitangapplication.presenter.view.SweetHeartVerifyUploadView
import com.sdy.jitangapplication.ui.adapter.SweetVerifyPicAdapter
import com.sdy.jitangapplication.ui.dialog.SweetUploadNormalDialog
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
        const val TYPE_IDHAND = 5
        const val TYPE_IDFACE = 6
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

    private val datas by lazy {
        arrayListOf<SweetUploadBean>(
            SweetUploadBean(R.drawable.icon_sweet_upload_idcard_hand, TYPE_IDHAND),
            SweetUploadBean(R.drawable.icon_sweet_upload_idcard_face, TYPE_IDFACE),
            when (type) {
                TYPE_WEALTH -> {//房产证
                    SweetUploadBean(R.drawable.icon_sweet_upload_housecard, TYPE_WEALTH)
                }

                TYPE_CAR -> {//行驶证
                    SweetUploadBean(R.drawable.icon_sweet_upload_drivingcard, TYPE_CAR)
                }

                TYPE_FIGURE -> {//胸围证明
                    SweetUploadBean(R.drawable.icon_sweet_upload_chest, TYPE_FIGURE)
                }
                else -> {//工作证明
                    SweetUploadBean(R.drawable.icon_sweet_upload_workcard, TYPE_PROFESSION)
                }

            }
        )
    }

    private fun initView() {
        mPresenter = SweetHeartVerifyUploadPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        BarUtils.setStatusBarColor(this, Color.WHITE)
        btnBack.clickWithTrigger {
            finish()
        }

        if (type == TYPE_FIGURE ) {
            hotT1.text = "身材认证"
            t1.text = "上传手持身份证、身份证正面及胸围测量\n请确保与本人头像一致，此流程不对外公开"
        }else if (type == TYPE_PROFESSION) {
            hotT1.text  = "职业认证"
            t1.text = "上传手持身份证、身份证正面及工作牌等\n请确保与本人头像一致，此流程不对外公开"
        } else if (type == TYPE_CAR) {
            hotT1.text = "豪车认证"
            t1.text = "上传手持身份证、身份证正面及驾驶证\n请确保与本人头像一致，照片不会对外公开"
        } else {
            hotT1.text = "豪宅认证"
            t1.text = "上传手持身份证、身份证正面及所有权的房产证\n请确保与本人头像一致，照片不会对外公开"
        }



        sweetPicRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        sweetPicRv.adapter = sweetVerifyPicAdapter
        sweetVerifyPicAdapter.addData(datas)

        sweetVerifyPicAdapter.setOnItemClickListener { _, view, position ->
            if (sweetVerifyPicAdapter.data[position].url.isNullOrEmpty()) {
                SweetUploadNormalDialog(
                    this,
                    position,
                    sweetVerifyPicAdapter.data[position].type
                ).show()
            } else {
                CommonFunction.onTakePhoto(this, 1, position)

            }

//            if (sweetVerifyPicAdapter.data[position].url.isEmpty()) {
//                CommonFunction.onTakePhoto(this, 1, position)
//            }
        }
        sweetVerifyPicAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.sweetPicDelete -> {
                    sweetVerifyPicAdapter.data[position].url = ""
                    sweetVerifyPicAdapter.notifyItemChanged(position)
                    checkEnable()
                }
            }
        }

        uploadVerifyBtn.clickWithTrigger {
            mPresenter.uploadPhoto(sweetVerifyPicAdapter.data[index].url, index)
        }
    }

    private fun checkEnable() {
        var enable = true
        for (tdata in sweetVerifyPicAdapter.data) {
            if (tdata.url == "") {
                enable = false
                break
            }
        }
        uploadVerifyBtn.isEnabled = enable
    }

    private var index = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                    val tdata = PictureSelector.obtainMultipleResult(data)[0]
                    sweetVerifyPicAdapter.data[requestCode].url =
                        if (SdkVersionUtils.checkedAndroid_Q() && !tdata.androidQToPath.isNullOrEmpty()) {
                            tdata.androidQToPath
                        } else {
                            tdata.path
                        }
                    sweetVerifyPicAdapter.data[requestCode].width = tdata.width
                    sweetVerifyPicAdapter.data[requestCode].height = tdata.height
                    sweetVerifyPicAdapter.notifyItemChanged(requestCode)
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
            if (index == sweetVerifyPicAdapter.data.size - 1) {
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