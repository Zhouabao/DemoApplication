package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.google.android.material.appbar.AppBarLayout
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.GetAddressvent
import com.sdy.jitangapplication.presenter.CandyProductDetailPresenter
import com.sdy.jitangapplication.presenter.view.CandyProductDetailView
import com.sdy.jitangapplication.ui.adapter.CandyProductAdapter
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.AddAndMessageDialog
import com.sdy.jitangapplication.ui.dialog.AddExchangeAddressDialog
import com.sdy.jitangapplication.ui.fragment.CommentFragment
import com.sdy.jitangapplication.ui.fragment.MessageFragment
import com.sdy.jitangapplication.ui.fragment.WantProductListFragment
import kotlinx.android.synthetic.main.activity_candy_product_detail.*
import kotlinx.android.synthetic.main.activity_candy_product_detail.btnBack
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 *商品详情
 */
class CandyProductDetailActivity : BaseMvpActivity<CandyProductDetailPresenter>(),
    CandyProductDetailView, View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candy_product_detail)
        initView()
    }

    private val candyProductAdapter = CandyProductAdapter()
    private val stack by lazy { Stack<Fragment>() }

    private fun initView() {
        divider.isVisible = false
        hotT1.text = "商品详情"

        btnBack.setOnClickListener(this)
        exchangeCandy.setOnClickListener(this)
        productCollect.setOnClickListener(this)

        productCandyPrice.typeface = Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")
//        rvCategoryProduct.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
//        rvCategoryProduct.adapter = candyProductAdapter
        for (i in 0 until 10) {
            candyProductAdapter.addData("")
        }

        productDetailAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, verticalOffset ->
            Log.d("verticalOffset", "$verticalOffset,${SizeUtils.dp2px(507F)}")
            statusBarView.isVisible = Math.abs(verticalOffset) >= 1228
            statusBar.isVisible = !statusBarView.isVisible
        })


        exchangeCandy.text = SpanUtils.with(exchangeCandy)
            .append("还需要\t\t")
            .appendImage(R.drawable.icon_candy_me)
            .append("399")
            .setTypeface(Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf"))
            .create()

        initVp()


//        AlertCandyEnoughDialog
    }

    private fun initVp() {
        stack.add(WantProductListFragment())
        stack.add(MessageFragment())
        stack.add(CommentFragment())
        vpProductOptions.adapter = MainPagerAdapter(supportFragmentManager, stack)
        vpProductOptions.offscreenPageLimit = 3


        rgCandyCategory.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWant -> {
                    vpProductOptions.currentItem = 0
                    rbWant.paint.isFakeBoldText = true
                    rbMessage.paint.isFakeBoldText = false
                    rbComment.paint.isFakeBoldText = false
                }
                R.id.rbMessage -> {
                    vpProductOptions.currentItem = 1
                    rbMessage.paint.isFakeBoldText = true
                    rbWant.paint.isFakeBoldText = false
                    rbComment.paint.isFakeBoldText = false
                }
                R.id.rbComment -> {
                    vpProductOptions.currentItem = 2
                    rbComment.paint.isFakeBoldText = true
                    rbWant.paint.isFakeBoldText = false
                    rbMessage.paint.isFakeBoldText = false
                }
            }
        }

        vpProductOptions.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {


            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        rgCandyCategory.check(R.id.rbWant)
                    }
                    1 -> {
                        rgCandyCategory.check(R.id.rbMessage)
                    }
                    2 -> {
                        rgCandyCategory.check(R.id.rbComment)
                    }
                }
            }


        })

        rgCandyCategory.check(R.id.rbWant)
        vpProductOptions.currentItem = 0
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnBack -> {
                finish()
            }
            R.id.productCollect -> {
                AddAndMessageDialog(this).show()
            }
            R.id.exchangeCandy -> {

//                WithdrawCandyDialog(this).show()
                AddExchangeAddressDialog(this).show()
//                AlertCandyEnoughDialog(this).show()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                if (!data?.getStringExtra("address").isNullOrEmpty()) {
                    EventBus.getDefault().post(GetAddressvent(data?.getStringExtra("address")!!))
                }
            }
        }
    }
}
