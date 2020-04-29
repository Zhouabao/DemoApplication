package com.sdy.jitangapplication.ui.fragment


import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.ShowNearCountEvent
import com.sdy.jitangapplication.event.UpdateIndexCandyEvent
import com.sdy.jitangapplication.presenter.IndexPresenter
import com.sdy.jitangapplication.presenter.view.IndexView
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.FilterUserDialog
import com.sdy.jitangapplication.ui.dialog.RechargeCandyDialog
import com.sdy.jitangapplication.widgets.CustomScaleTransitionPagerTitleView
import kotlinx.android.synthetic.main.fragment_index.*
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 首页fragment
 */
class IndexFragment : BaseMvpLazyLoadFragment<IndexPresenter>(), IndexView {

    private val fragments by lazy { Stack<Fragment>() }
    private val titles by lazy { arrayOf("匹配", "附近") }
    private val matchFragment by lazy { MatchFragment() }
    //    private val findByTagFragment by lazy { FindByTagFragment() }
    private val findByTagFragment by lazy { PeopleNearbyFragment() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_index, container, false)
    }

//    val titleAdapter by lazy { IndexSwitchAdapter() }

    override fun loadData() {
        EventBus.getDefault().register(this)


        initFragments()

        filterBtn.clickWithTrigger {
            FilterUserDialog(activity!!).show()
        }

        myCandyAmount.clickWithTrigger {
            RechargeCandyDialog(activity!!).show()
        }
    }

    private fun initFragments() {


        fragments.add(matchFragment)
        fragments.add(findByTagFragment)

        vpIndex.setScrollable(true)
        vpIndex.offscreenPageLimit = 2
        vpIndex.adapter = MainPagerAdapter(activity!!.supportFragmentManager, fragments)

        initIndicator()

        vpIndex.currentItem = 0
    }

    private fun initIndicator() {
        val commonNavigator = CommonNavigator(activity!!)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return fragments.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val simplePagerTitleView = CustomScaleTransitionPagerTitleView(context)
                simplePagerTitleView.text = titles[index]
                simplePagerTitleView.minScale = 0.66F
                simplePagerTitleView.textSize = 24F
                simplePagerTitleView.normalColor = Color.parseColor("#191919")
                simplePagerTitleView.selectedColor = Color.parseColor("#FF6318")
                simplePagerTitleView.setPadding(SizeUtils.dp2px(5F), 0, 0, 0)
                simplePagerTitleView.onClick {
                    if (index == 1) {
                        EventBus.getDefault().post(ShowNearCountEvent())
                    }
                    vpIndex.currentItem = index
                }
                return simplePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 4.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 16.0).toFloat()
                indicator.setPadding(SizeUtils.dp2px(15F), 0, 0, 0)
                indicator.roundRadius = UIUtil.dip2px(context, 2.0).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(1.0f)
                indicator.setColors(resources.getColor(R.color.colorOrange))
                return indicator
            }
        }
        titleIndex.navigator = commonNavigator
        ViewPagerHelper.bind(titleIndex, vpIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateIndexCandyEvent(event: UpdateIndexCandyEvent) {
        myCandyAmount.isVisible = true
        myCandyAmount.text = SpanUtils.with(myCandyAmount)
            .append("${event.candyCount}")
            .setTypeface(Typeface.createFromAsset(activity!!.assets, "DIN_Alternate_Bold.ttf"))
            .setVerticalAlign(SpanUtils.ALIGN_CENTER)
            .setFontSize(16, true)
            .setBold()
            .append("\t充值")
            .setFontSize(12, true)
            .setVerticalAlign(SpanUtils.ALIGN_CENTER)
            .create()
    }
}
