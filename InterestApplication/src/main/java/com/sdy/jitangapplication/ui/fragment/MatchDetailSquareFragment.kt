package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.flexbox.*
import com.kotlin.base.ui.fragment.BaseFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.BlockDataEvent
import com.sdy.jitangapplication.event.ListDataEvent
import com.sdy.jitangapplication.event.UpdateSquareEvent
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.ui.adapter.MatchDetailLabelAdapter
import kotlinx.android.synthetic.main.match_detail_same_interest.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 用户详情之动态和标签
 *
 */
class MatchDetailSquareFragment(val matchBean: MatchBean, val targetAccid: String) : BaseFragment() {
    private val labelsAdapter by lazy { MatchDetailLabelAdapter(activity!!) }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.match_detail_same_interest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    private fun initData() {
        //用户标签
        detailLabelRv.adapter = labelsAdapter
        labelsAdapter.setData(matchBean!!.tags ?: mutableListOf())
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        //用户标签
        val manager = FlexboxLayoutManager(activity!!, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.CENTER
        detailLabelRv.layoutManager = manager

        initFragment()

        detailSquareSwitchRg.setOnCheckedChangeListener { radioGroup, checkedId ->
            if (checkedId == R.id.rbList) {
                changeFragment(1)
                if (!loadList) {
                    EventBus.getDefault().postSticky(ListDataEvent(targetAccid, true))
                }
                loadList = true
                currIndex = 1
            } else if (checkedId == R.id.rbBlock) {
                changeFragment(0)
                if (!loadBlock) {
                    EventBus.getDefault().postSticky(BlockDataEvent(targetAccid, true))
                }
                loadBlock = true
                currIndex = 0
            }
        }
        detailSquareSwitchRg.check(R.id.rbList)
    }


    //fragment栈管理
    private val mStack = Stack<Fragment>()
    //九宫格
    private val blockFragment by lazy { BlockSquareFragment() }
    //列表
    private val listFragment by lazy { ListSquareFragment() }
    //标识 来确认是否已经加载过数据
    private var loadBlock = false
    private var loadList = false
    private var currIndex = 1

    /**
     * 初始化fragments
     */
    private fun initFragment() {
        val manager = activity!!.supportFragmentManager.beginTransaction()
        manager.add(R.id.detail_content_fragment, blockFragment) //九宫格照片模式
        manager.add(R.id.detail_content_fragment, listFragment) //列表
        manager.commit()
        mStack.add(blockFragment)
        mStack.add(listFragment)
    }

    /**
     * 点击切换fragment
     */
    private fun changeFragment(position: Int) {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        for (fragment in mStack) {
            transaction.hide(fragment)
        }
        transaction.show(mStack[position])
        transaction.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateSquareEvent(event: UpdateSquareEvent) {
        if (currIndex == 0) {
            EventBus.getDefault().postSticky(BlockDataEvent(targetAccid, true))
        } else {
            //请求成功了请求列表广场
            EventBus.getDefault().postSticky(ListDataEvent(targetAccid, true))
        }
    }

}
