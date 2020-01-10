package com.sdy.jitangapplication.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.BarUtils
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseFragment
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.ShowDeleteMyLabelEvent
import com.sdy.jitangapplication.event.UpdateEditModeEvent
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.ui.activity.MyIntentionActivity
import com.sdy.jitangapplication.ui.adapter.MainPagerAdapter
import com.sdy.jitangapplication.ui.dialog.FilterUserDialog
import kotlinx.android.synthetic.main.fragment_index.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivityForResult
import java.util.*


/**
 * 首页
 */
class IndexFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_index, container, false)
    }


    private val matchFragment by lazy { MatchFragment1() }
    private val tagFragment by lazy { MyLabelFragment() }
    private val stack by lazy { Stack<BaseFragment>() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initVp()
        initView()
    }

    private var editModes = false
    private fun initView() {
        EventBus.getDefault().register(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val param = customStatusBar.layoutParams as ConstraintLayout.LayoutParams
            param.height = BarUtils.getStatusBarHeight()
        } else {
            customStatusBar.isVisible = false
        }

        findToTalkIv.onClick {
            startActivityForResult<MyIntentionActivity>(100, "id" to -1, "from" to MyIntentionActivity.FROM_USERCENTER)
        }


        rightOperationBtn.onClick {
            when (vpIndex.currentItem) {
                TAB_MATCH -> {//筛选
                    FilterUserDialog(activity!!).show()
                }
                TAB_TAG -> {//删除标签
                    editModes = !editModes
                    rightOperationBtn.text = if (editModes) {
                        "取消"
                    } else {
                        "删除"
                    }
                    EventBus.getDefault().post(UpdateEditModeEvent(0))
                }
            }
        }
    }

    companion object {
        const val TAB_MATCH = 0
        const val TAB_TAG = 1
    }

    private fun initVp() {
        stack.add(matchFragment)
        stack.add(tagFragment)
        vpIndex.setScrollable(false)
        vpIndex.adapter = MainPagerAdapter(fragmentManager!!, stack)
        vpIndex.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    TAB_MATCH -> {
                        rightOperationBtn.isVisible = true
                        rightOperationBtn.setBackgroundResource(R.drawable.icon_filter)
                        rightOperationBtn.text = ""
                        rgIndexTab.check(R.id.tabMatch)
                        findToTalkIv.isVisible = false
                    }
                    else -> {
                        rightOperationBtn.isVisible = false
                        rightOperationBtn.background = null
                        rightOperationBtn.text = if (editModes) {
                            "取消"
                        } else {
                            "删除"
                        }
                        rgIndexTab.check(R.id.tabTag)
                        findToTalkIv.isVisible = false
                    }
                }

            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })


        rgIndexTab.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.tabMatch -> {
                    vpIndex.currentItem = TAB_MATCH
                }
                R.id.tabTag -> {
                    vpIndex.currentItem = TAB_TAG
                }
            }
        }
        rgIndexTab.check(R.id.tabMatch)
    }

    private var myIntention: LabelQualityBean? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                if (data != null && data.getSerializableExtra("intention") != null) {
                    myIntention = data.getSerializableExtra("intention") as LabelQualityBean
                    GlideUtil.loadImg(activity!!, myIntention!!.icon, findToTalkIv)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowDeleteMyLabelEvent(event: ShowDeleteMyLabelEvent) {
        rightOperationBtn.isVisible = event.show
    }
}
