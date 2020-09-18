package com.sdy.jitangapplication.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.PagerSnapHelper
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.leochuan.ScaleLayoutManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.presenter.SweetHeartInfoPresenter
import com.sdy.jitangapplication.presenter.view.SweetHeartInfoView
import com.sdy.jitangapplication.ui.adapter.ListSquareImgsAdapter
import com.sdy.jitangapplication.widgets.snaphelper.CardScaleHelper
import kotlinx.android.synthetic.main.activity_sweet_heart_info.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.jetbrains.anko.startActivity

/**
 * 甜心圈 认证信息
 */
class SweetHeartInfoActivity : BaseMvpActivity<SweetHeartInfoPresenter>(), SweetHeartInfoView,
    View.OnClickListener {
    private val square_id by lazy { intent.getIntExtra("square_id", 0) }
    private val gender by lazy { intent.getIntExtra("gender", 1) }

    companion object {
        fun startToSweetInfo(context: Context, id: Int, gender: Int) {
            context.startActivity<SweetHeartInfoActivity>("square_id" to id, "gender" to gender)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sweet_heart_info)

        initView()
        mPresenter.getSquareInfo(square_id)
    }

    val adapter by lazy { ListSquareImgsAdapter(this, mutableListOf()) }
    private val cardHelper by lazy { CardScaleHelper() }
    private fun initView() {
        btnBack.clickWithTrigger { finish() }
        hotT1.text = "${if (gender == 1) {
            "他"
        } else {
            "她"
        }}的认证资料"

        ClickUtils.applySingleDebouncing(
            arrayOf<View>(
                squareDianzanAni,
                squareDianzanBtn1,
                squareCommentBtn1,
                squareMoreBtn1
            ), this
        )

        val params = sweetHeartIv.layoutParams as ConstraintLayout.LayoutParams
        params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
        params.height = ((177 / 1035f) * params.width).toInt()

        val manager = ScaleLayoutManager(this, 0)
//        manager.maxVisibleItemCount = 9
        sweetHeartInfoRv.layoutManager = manager
        sweetHeartInfoRv.adapter = adapter

//        CenterSnapHelper().attachToRecyclerView(sweetHeartInfoRv)
        //分页滑动效果
//        sweetHeartInfoRv.onFlingListener = null
        PagerSnapHelper().attachToRecyclerView(sweetHeartInfoRv)
        //滑动动画
//        sweetHeartInfoRv.addOnScrollListener(GalleryOnScrollListener())

    }

    override fun onClick(view: View) {
        when (view) {
            squareDianzanAni, squareDianzanBtn1 -> {
                if (squareDianzanAni.progress == 0F) {
                    squareDianzanAni.playAnimation()
                } else {
                    squareDianzanAni.progress = 0F
//                    squareDianzanAni.cancelAnimation()
                }
            }
            squareCommentBtn1 -> {
            }
            squareMoreBtn1 -> {
            }
        }

    }

    override fun onGetSquareInfoResults(data: SquareBean?) {
        if (data != null) {
            adapter.addData(data.photo_json ?: arrayListOf())
        }
    }
}