package com.sdy.jitangapplication.ui.activity

import android.annotation.SuppressLint
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
import com.sdy.jitangapplication.presenter.SweetHeartVerifyUploadPresenter
import com.sdy.jitangapplication.presenter.view.SweetHeartVerifyUploadView
import com.sdy.jitangapplication.ui.adapter.SweetNormalPicAdapter
import com.sdy.jitangapplication.ui.adapter.SweetVerifyPicAdapter
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import com.sdy.jitangapplication.widgets.CustomPagerSnapHelper
import kotlinx.android.synthetic.main.activity_sweet_heart_square_upload.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textColorResource

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
            1 -> {
                hotT1.text = "资产认证"
                uploadTitle.text="听说你很有钱，她们想和你做朋友"
                uploadType.text = "照片中资产大于200万"
                tip.text="上传一张或多张图片，照片将对外展示"
            }
            2 -> {
                hotT1.text = "豪车认证"
                uploadTitle.text="听说你的车很漂亮，她们想和你做朋友"
                uploadType.text = "豪车照片或人车合照"
                tip.text="上传一张或多张图片，照片将对外展示"

            }
            3 -> {
                hotT1.text = "身材认证"
                uploadTitle.text="我咬了一口嘴巴，也太辣了吧"
                uploadType.text = "游泳装或者私房写真照"
                tip.text="照片将对外展示\n注意照片尺度过大或导致无法通过认证"
            }
            4 -> {
                hotT1.text = "职业认证"
                uploadTitle.text="听说你的职业很有趣，我和她们不一样"
                uploadType.text = "职业为规定职业中的一种"
                tip.text="上传一张或多张能证明你职业的图片，照片将对外展示"
            }

        }
    }


    private fun initView() {
        mPresenter = SweetHeartVerifyUploadPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        BarUtils.setStatusBarColor(this, Color.WHITE)
        rightBtn.isVisible = true
        rightBtn.text = "提交"
        rightBtn.isEnabled = false

        ClickUtils.applySingleDebouncing(
            arrayOf<View>(seeUploadNormalBtn, btnBack, normalCloseBtn, rightBtn),
            this
        )

        sweetSquareRv.layoutManager = GridLayoutManager(this, 3, RecyclerView.VERTICAL, false)
        sweetSquareRv.adapter = adappter
        adappter.addData(MediaParamBean(""))
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
                        && !adappter.data.contains(MediaParamBean(""))
                    ) {
                        adappter.addData(MediaParamBean(""))
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
                                0,
                                MediaParamBean(tdata.androidQToPath, 0, tdata.width, tdata.height)
                            )
                        } else {
                            adappter.addData(
                                0,
                                MediaParamBean(
                                    if (tdata.compressPath.isNotEmpty()) {
                                        tdata.compressPath
                                    } else {
                                        tdata.path
                                    }, 0, tdata.width, tdata.height
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