package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.core.view.get
import androidx.core.view.size
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.ui.adapter.SquareDetailImgsAdaper
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_big_image.*

/**
 * 点击图片实现查看大图
 */
class BigImageActivity : BaseActivity() {
    companion object {
        val IMG_KEY = "squareBean"
        val IMG_POSITION = "imagepostion"
        val IMG_CURRENT_POSITION = "image_current_postion"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_big_image)
        //延迟共享动画的执行
//        postponeEnterTransition()
        initView()
    }

    private val squareBean: SquareBean by lazy { intent.getSerializableExtra(IMG_KEY) as SquareBean }
    private val currIndex by lazy { intent.getIntExtra(IMG_POSITION, 0) }
    private fun initView() {
        ScreenUtils.setFullScreen(this)

        bigImageVP.adapter = SquareDetailImgsAdaper(this, squareBean.photo_json ?: mutableListOf())
        //图片加载完后 继续执行过渡动画
        if (squareBean.photo_json != null && squareBean.photo_json!!.size > 1) {
            for (i in 0 until squareBean.photo_json!!.size) {
                val indicator = RadioButton(this)
                indicator.width = SizeUtils.dp2px(10F)
                indicator.height = SizeUtils.dp2px(10F)
                indicator.buttonDrawable = null
                indicator.background = this.resources.getDrawable(R.drawable.selector_circle_indicator)

                indicator.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                val layoutParams: LinearLayout.LayoutParams =
                    indicator.layoutParams as LinearLayout.LayoutParams
                layoutParams.setMargins(0, 0, SizeUtils.dp2px(6f), 0)
                indicator.layoutParams = layoutParams

                indicator.isChecked = i == 0
                bigImageIndicator.addView(indicator)
            }
        }
        //自定义你的Holder，实现更多复杂的界面，不一定是图片翻页，其他任何控件翻页亦可。
        bigImageVP.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until bigImageIndicator.size) {
                    (bigImageIndicator[i] as RadioButton).isChecked = i == position
                }
            }
        })
        bigImageVP.setCurrentItem(currIndex, false)

        bigImageVP.onClick {
            finish()
        }
    }

}
