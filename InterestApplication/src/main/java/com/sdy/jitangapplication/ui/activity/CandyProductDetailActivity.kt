package com.sdy.jitangapplication.ui.activity

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.google.android.material.appbar.AppBarLayout
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.RefreshCandyMallDetailEvent
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.model.ProductDetailBean
import com.sdy.jitangapplication.presenter.CandyProductDetailPresenter
import com.sdy.jitangapplication.presenter.view.CandyProductDetailView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.adapter.ProductDetailMediaAdapter
import com.sdy.jitangapplication.ui.dialog.AddAndMessageDialog
import com.sdy.jitangapplication.ui.dialog.AddExchangeAddressDialog
import com.sdy.jitangapplication.ui.dialog.AlertCandyEnoughDialog
import com.sdy.jitangapplication.ui.fragment.CommentFragment
import com.sdy.jitangapplication.ui.fragment.MessageFragment
import com.sdy.jitangapplication.ui.fragment.WantProductListFragment
import com.sdy.jitangapplication.widgets.CustomPagerSnapHelper
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.activity_candy_product_detail.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.math.abs


/**
 *商品详情
 */
class CandyProductDetailActivity : BaseMvpActivity<CandyProductDetailPresenter>(),
    CandyProductDetailView, View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {
    private val goods_id by lazy { intent.getIntExtra("id", -1) }
    private val stack by lazy { Stack<Fragment>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // 设置一个exit transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            window.enterTransition = Explode()
            window.exitTransition = Explode()
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_candy_product_detail)
        initView()
        mPresenter.goodsInfo(goods_id)
    }

    private var height = 0
    val layoutmanager by lazy {
        object : LinearLayoutManager(this, RecyclerView.HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean {
                return true
            }
        }
    }

    private val bannerAdapter by lazy { ProductDetailMediaAdapter(this) }

    private fun initView() {
        EventBus.getDefault().register(this)

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

        productDetailAppbar.viewTreeObserver.addOnGlobalLayoutListener(this)
        productDetailAppbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, verticalOffset ->
            Log.d("verticalOffset", "productDetailAppbarH = ${height}")
            Log.d("verticalOffset", "${verticalOffset},${height - SizeUtils.dp2px(52F)}")

            if (height > 0) {
                statusBar.isVisible = abs(verticalOffset) < height - SizeUtils.dp2px(52F)
                statusBarView.isVisible = abs(verticalOffset) >= height - SizeUtils.dp2px(52F)
                if (statusBarView.isVisible) {
                    statusBarViewDivider.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                }
            }

        })
        bannerProduct.layoutManager = layoutmanager
        bannerProduct.adapter = bannerAdapter
        CustomPagerSnapHelper().attachToRecyclerView(bannerProduct)
        bannerProductIndicator.typeface =
            Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf")
        bannerProduct.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                bannerProductIndicator.text = SpanUtils.with(bannerProductIndicator)
                    .append("${layoutmanager.findLastVisibleItemPosition() + 1}")
                    .setFontSize(14, true)
                    .setBold()
                    .append(" / ${bannerAdapter.data.size}")
                    .setFontSize(10, true)
                    .create()
            }
        })

//        (bannerProduct as BannerViewPager<String, ProductDetailImgHolderView>)
//            .setHolderCreator { ProductDetailImgHolderView() }
//            .setIndicatorSliderRadius(SizeUtils.dp2px(3F))
//            .setIndicatorSliderWidth(SizeUtils.dp2px(6f), SizeUtils.dp2px(18F))
//            .setIndicatorHeight(SizeUtils.dp2px(6f))
//            .setIndicatorSliderGap(SizeUtils.dp2px(5F))
//            .create(mutableListOf())

        initVp()

    }

    val wantProductListFragment by lazy { WantProductListFragment(goods_id) }
    private fun initVp() {
        stack.add(wantProductListFragment)
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
                if (!productBean.is_wished) {
                    mPresenter.goodsAddWish(productBean.id)
                } else {
                    mPresenter.goodsDelWish(productBean.id)
                }

            }
            R.id.exchangeCandy -> {
                if (productBean.mycandy_amount >= productBean.amount) {
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
            wantProductListFragment.myCandyAmount = productBean.mycandy_amount
            wantProductListFragment.giftBean = GiftBean(
                productBean.amount,
                icon = productBean.icon,
                id = productBean.id,
                min_amount = productBean.min_amount,
                title = productBean.title
            )
            bannerAdapter.addData(data?.cover_list)
            if (!data?.cover_list.isNullOrEmpty()) {
                bannerProductIndicator.isVisible = true
                bannerProductIndicator.text = SpanUtils.with(bannerProductIndicator)
                    .append("1")
                    .setFontSize(14, true)
                    .setBold()
                    .append(" / ${bannerAdapter.data.size}")
                    .setFontSize(10, true)
                    .create()
            } else {
                bannerProductIndicator.isVisible = false
            }

            productCandyPrice.text = CommonFunction.num2thousand("${data!!.amount}")
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
            productDesc.text = data.title
            rbMessage.text = "留言(${data.msg_cnt})"
            rbComment.text = "评价(${data.comments_cnt})"
            if (data.mycandy_amount >= data.amount) {

                exchangeProgress.setProgress(100F)
                exchangeCandy.text = SpanUtils.with(exchangeCandy)
                    .appendImage(R.drawable.icon_candy_detail)
                    .append("\t\t${CommonFunction.num2thousand("${data!!.amount}")}\t\t")
                    .setTypeface(Typeface.createFromAsset(assets, "DIN_Alternate_Bold.ttf"))
                    .append("兑换")
                    .create()
            } else {
                exchangeProgress.setProgress((data.mycandy_amount * 1F / data.amount * 100))

                exchangeCandy.text = SpanUtils.with(exchangeCandy)
                    .append("还需要\t\t")
                    .appendImage(R.drawable.icon_candy_detail)
                    .append("${CommonFunction.num2thousand("${data.amount - data.mycandy_amount}")}")
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
            productBean.is_wished = !productBean.is_wished
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
            if (productBean.is_wished)
                AddAndMessageDialog(this, productBean.id).show()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        GSYVideoManager.releaseAllVideos()
    }

    override fun onPause() {
        super.onPause()
        GSYVideoManager.onPause()
    }

    override fun onResume() {
        super.onResume()
        GSYVideoManager.onResume(false)
    }

    override fun onBackPressed() {
        if (GSYVideoManager.backFromWindowFull(this)) {
            return
        }
        super.onBackPressed()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshCandyMallDetailEvent(event: RefreshCandyMallDetailEvent) {
        mPresenter.goodsInfo(goods_id)
    }

    override fun onGlobalLayout() {
        if (height <= 0)
            height = productDetailAppbar.height
        else
            productDetailAppbar.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }


    private var currentIndex = 0
    private fun moveToPosition(
        manager: LinearLayoutManager,
        mRecyclerView: RecyclerView,
        currentIndex: Int
    ) {
        val firstItem = manager.findFirstVisibleItemPosition()
        val lastItem = manager.findLastVisibleItemPosition()
        if (currentIndex <= firstItem) {
            mRecyclerView.scrollToPosition(currentIndex)
        } else if (currentIndex <= lastItem) {
            val top = mRecyclerView.getChildAt(currentIndex - firstItem).top
            mRecyclerView.scrollBy(0, top)
        } else {
            mRecyclerView.scrollToPosition(currentIndex)
        }
    }
}
