package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.ClickUtils
import com.google.gson.Gson
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.tools.SdkVersionUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.MediaParamBean
import com.sdy.jitangapplication.model.SweetUploadBean
import com.sdy.jitangapplication.presenter.SweetHeartVerifyUploadPresenter
import com.sdy.jitangapplication.presenter.view.SweetHeartVerifyUploadView
import com.sdy.jitangapplication.ui.adapter.SweetNormalPicAdapter
import com.sdy.jitangapplication.ui.adapter.SweetVerifyPicAdapter
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import com.sdy.jitangapplication.widgets.CustomPagerSnapHelper
import kotlinx.android.synthetic.main.activity_sweet_heart_square_upload.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 甜心圈对外展示动态
 */
class SweetHeartSquareUploadActivity : BaseMvpActivity<SweetHeartVerifyUploadPresenter>(),
    View.OnClickListener,
    SweetHeartVerifyUploadView {
    companion object {
        const val MAX_COUNT = 6
        const val REQUEST_SQUARE_PIC = 1002
    }

    private val type by lazy { intent.getIntExtra("type", 0) }
    private val keys = arrayListOf<MediaParamBean>()
    private var index = 0
    private val adappter by lazy { SweetVerifyPicAdapter() }
    private val normalPicAdapter by lazy { SweetNormalPicAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sweet_heart_square_upload)

        initView()
        initData()
        mPresenter.getPicTpl(type)
    }


    //type [int]	是	1资产证明 2豪车 3身材 4职业
    private fun initData() {
        when (type) {
            SweetHeartVerifyUploadActivity.TYPE_WEALTH -> {
                hotT1.text = getString(R.string.sweet_big_house_title)
                uploadTitle.text = getString(R.string.sweet_rich_to_be_friend)
                uploadType.text = getString(R.string.sweet_fullscreen_house)
                tip.text = getString(R.string.sweet_upload_one_or_more_pic)
            }
            SweetHeartVerifyUploadActivity.TYPE_CAR -> {
                hotT1.text = getString(R.string.sweet_luxury_car_title)
                uploadTitle.text = getString(R.string.sweet_car_to_be_friend)
                uploadType.text = getString(R.string.sweet_car_and_man)
                tip.text = getString(R.string.sweet_upload_one_or_more_pic)

            }
            SweetHeartVerifyUploadActivity.TYPE_EDUCATION -> {
                hotT1.text = getString(R.string.sweet_figure_title)
                uploadTitle.text = getString(R.string.sweet_education_rich)
                uploadType.text = getString(R.string.sweet_schoolsuit)
                tip.text = getString(R.string.sweet_school_pic_education)
            }
            SweetHeartVerifyUploadActivity.TYPE_PROFESSION -> {
                hotT1.text = getString(R.string.sweet_job_title)
                uploadTitle.text = getString(R.string.sweet_funny_job)
                uploadType.text = getString(R.string.sweet_appropriate_suit)
                tip.text = getString(R.string.one_or_more_job_pic)
            }

        }
    }


    private fun initView() {
        mPresenter = SweetHeartVerifyUploadPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        BarUtils.setStatusBarColor(this, Color.WHITE)
        rightBtn.isVisible = true
        rightBtn.text = getString(R.string.commit)
        rightBtn.isEnabled = false

        ClickUtils.applySingleDebouncing(
            arrayOf<View>(seeUploadNormalBtn, btnBack, normalCloseBtn, rightBtn),
            this
        )

        sweetSquareRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        sweetSquareRv.adapter = adappter
        adappter.addData(SweetUploadBean())
        adappter.setOnItemClickListener { _, view, position ->
            if (adappter.data[position].url.isEmpty()) {
                CommonFunction.onTakePhoto(
                    this,
                    MAX_COUNT - (adappter.data.size - 1),
                    REQUEST_SQUARE_PIC,
                    compress = true
                )
            }
        }

        adappter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.sweetPicDelete -> {
                    adappter.remove(position)
                    if (adappter.data.size < MAX_COUNT + 1
                        && !adappter.data.contains(SweetUploadBean())
                    ) {
                        adappter.addData(SweetUploadBean())
                    }
                    rightBtn.isEnabled = adappter.data.size > 1
                }
            }
        }

        normalIconRv.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        normalIconRv.adapter = normalPicAdapter
        CustomPagerSnapHelper().attachToRecyclerView(normalIconRv)

    }

    override fun onClick(view: View) {
        when (view) {
            btnBack -> {
                finish()
            }
            rightBtn -> {
                mPresenter.uploadPhoto(adappter.data[index].url, index)
            }
            normalCloseBtn -> {
                BarUtils.setStatusBarColor(this, Color.WHITE)
                normalIconLl.isVisible = false
            }
            seeUploadNormalBtn -> {
                BarUtils.setStatusBarColor(this, Color.parseColor("#B3000000"))
                normalIconLl.isVisible = true
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_SQUARE_PIC) {
            if (data != null) {
                if (!PictureSelector.obtainMultipleResult(data).isNullOrEmpty()) {
                    for (tdata in PictureSelector.obtainMultipleResult(data)) {
                        if (SdkVersionUtils.checkedAndroid_Q() && !tdata.androidQToPath.isNullOrEmpty()) {
                            adappter.addData(
                                adappter.data.size - 1,
                                SweetUploadBean(
                                    0,
                                    0,
                                    tdata.androidQToPath,
                                    tdata.width,
                                    tdata.height
                                )
                            )
                        } else {
                            adappter.addData(
                                adappter.data.size - 1,
                                SweetUploadBean(
                                    0, 0,
                                    if (tdata.compressPath.isNotEmpty()) {
                                        tdata.compressPath
                                    } else {
                                        tdata.path
                                    }, tdata.width, tdata.height
                                )

                            )
                        }
                    }
                    rightBtn.isEnabled = adappter.data.size > 1
                    if (adappter.data.size - 1 == MAX_COUNT) {
                        adappter.remove(adappter.data.size - 1)
                    }
                }
            }
        }
    }


    override fun uploadImgResult(success: Boolean, key: String, index1: Int) {
        if (success) {
            keys.add(
                MediaParamBean(
                    key,
                    0,
                    adappter.data[index1].width,
                    adappter.data[index1].height
                )
            )
            var size = 0
            for (data in adappter.data) {
                if (data.url.isNotEmpty()) {
                    size++
                }
            }
            if (index == size - 1) {
                mPresenter.uploadData(2, type, Gson().toJson(keys))
            } else {
                index++
                mPresenter.uploadPhoto(adappter.data[index].url, index)
            }
        } else {
            index = 0
            keys.clear()
        }
    }

    override fun uploadDataResult(success: Boolean) {
        if (success) {
            startActivity<SweetHeartVerifyingActivity>()
        }
    }

    override fun getPicTplResult(datas: ArrayList<String>) {
        normalPicAdapter.setNewData(datas)
        seeUploadNormalBtn.isInvisible = datas.isEmpty()
    }
}