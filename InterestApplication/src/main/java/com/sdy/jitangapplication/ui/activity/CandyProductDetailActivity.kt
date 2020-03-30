package com.sdy.jitangapplication.ui.activity

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
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ProductDetailBean
import com.sdy.jitangapplication.presenter.CandyProductDetailPresenter
import com.sdy.jitangapplication.presenter.view.CandyProductDetailView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.AddAndMessageDialog
import com.sdy.jitangapplication.ui.dialog.AddExchangeAddressDialog
import com.sdy.jitangapplication.ui.dialog.AlertCandyEnoughDialog
import com.sdy.jitangapplication.ui.fragment.CommentFragment
import com.sdy.jitangapplication.ui.fragment.MessageFragment
import com.sdy.jitangapplication.ui.fragment.WantProductListFragment
import com.sdy.jitangapplication.ui.holder.ProductDetailImgHolderView
import com.zhpan.bannerview.BannerViewPager
import kotlinx.android.synthetic.main.activity_candy_product_detail.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import java.util.*

/**
 *商品详情
 */
class CandyProductDetailActivity : BaseMvpActivity<CandyProductDetailPresenter>(),
    CandyProductDetailView, View.OnClickListener {
    private val goods_id by lazy { intent.getIntExtra("id", -1) }
    private val stack by lazy { Stack<Fragment>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candy_product_detail)
        initView()
        mPresenter.goodsInfo(goods_id)
    }

    private fun initView() {
        mPresenter = CandyProductDetailPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick {
            finish()
        }

        stateProductDetail.retryBtn.onClick {
            stateProductDetail.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.goodsInfo(goods_id)
        }

        divider.isVisible = false
        hotT1.text = "商品详情"
        btnBack1.setOnClickListener(this)
        exchangeCandy.setOnClickListener(this)
        productCollect.setOnClickListener(this)
        productCandyPrice.typeface = Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")

        productDetailAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, verticalOffset ->
            Log.d("verticalOffset", "$verticalOffset,${SizeUtils.dp2px(507F)}")
            statusBar.isVisible = Math.abs(verticalOffset) < SizeUtils.dp2px(507F)
            statusBarView.isVisible = !statusBar.isVisible
        })


        (bannerProduct as BannerViewPager<String, ProductDetailImgHolderView>)
            .setHolderCreator { ProductDetailImgHolderView() }
            .setIndicatorSliderRadius(SizeUtils.dp2px(3F))
            .setIndicatorSliderWidth(SizeUtils.dp2px(6f), SizeUtils.dp2px(18F))
            .setIndicatorHeight(SizeUtils.dp2px(6f))
            .setIndicatorSliderGap(SizeUtils.dp2px(5F))
            .create(mutableListOf())

        initVp()

//        AlertCandyEnoughDialog
    }

    private fun initVp() {
        stack.add(WantProductListFragment(goods_id))
        stack.add(MessageFragment(goods_id))
        stack.add(CommentFragment(goods_id))
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
            R.id.btnBack1 -> {
                finish()
            }
            R.id.productCollect -> {
                if (!productBean.is_wished)
                    AddAndMessageDialog(this, productBean.id).show()
            }
            R.id.exchangeCandy -> {
                if (productBean.my_candy_amount >= productBean.amount) {
                    AddExchangeAddressDialog(this, goods_id).show()
                } else {
                    AlertCandyEnoughDialog(this).show()
                }
            }
        }

    }


    private lateinit var productBean: ProductDetailBean
    override fun onGoodsInfoResult(data: ProductDetailBean?) {
        if (data != null) {
            productBean = data
            (bannerProduct as BannerViewPager<String, ProductDetailImgHolderView>).create(data.cover_list)
            productCandyPrice.text = "${data!!.amount}"
            productRmbPrice.text = "价值¥${data!!.price}"
            productCollect.text = if (data.is_wished) {
                "已加入"
            } else {
                "加入心愿"
            }
            productCollect.setCompoundDrawablesWithIntrinsicBounds(
                null, if (data.is_wished) {
                    resources.getDrawable(R.drawable.icon_collected)
                } else {
                    resources.getDrawable(R.drawable.icon_collect)
                }, null, null
            )
            productDesc.text = data.descr
            rbMessage.text = "留言(${data.msg_cnt})"
            rbComment.text = "评价(${data.comments_cnt})"
            if (data.my_candy_amount >= data.amount) {
                exchangeProgress.progress = 100
                exchangeCandy.text = SpanUtils.with(exchangeCandy)
                    .appendImage(R.drawable.icon_candy_small)
                    .append("\t\t${data.amount}\t\t")
                    .setTypeface(Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf"))
                    .append("兑换")
                    .create()
            } else {
                exchangeProgress.progress = (data.my_candy_amount * 1F / data.amount * 100).toInt()
                exchangeCandy.text = SpanUtils.with(exchangeCandy)
                    .append("还需要\t\t")
                    .appendImage(R.drawable.icon_candy_small)
                    .append("${data.amount - data.my_candy_amount}")
                    .setTypeface(Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf"))
                    .create()
            }

            stateProductDetail.viewState = MultiStateView.VIEW_STATE_CONTENT
        } else {
            stateProductDetail.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }


    override fun onGoodsAddWishResult(success: Boolean) {
        if (success) {
            productBean.is_wished = true
            productCollect.text = if (productBean.is_wished) {
                "已加入"
            } else {
                "加入心愿"
            }
            productCollect.setCompoundDrawablesWithIntrinsicBounds(
                null, if (productBean.is_wished) {
                    resources.getDrawable(R.drawable.icon_collected)
                } else {
                    resources.getDrawable(R.drawable.icon_collect)
                }, null, null
            )
            mPresenter.goodsAddWish(productBean.id)
        }

    }

}
