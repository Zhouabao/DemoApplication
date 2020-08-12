package com.sdy.jitangapplication.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.UpdateMyDatingEvent
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.presenter.MyTagPresenter
import com.sdy.jitangapplication.presenter.view.MyTagView
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.ui.adapter.DatingSquareAdapter
import kotlinx.android.synthetic.main.empty_my_square_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_tag.*
import kotlinx.android.synthetic.main.headerview_user_center_square.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity

/**
 * 我的约会记录
 */
class MyDatingFragment : BaseMvpFragment<MyTagPresenter>(), MyTagView {
    private val datingSquareAdapter by lazy { DatingSquareAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_tag, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
    }

    fun loadData() {
        initView()
    }

    private fun initView() {
        EventBus.getDefault().register(this)


        //用户标签
        val tagManager = GridLayoutManager(activity!!, 2)
        rvMyTag.layoutManager = tagManager
        rvMyTag.adapter = datingSquareAdapter
        //android 瀑布流
        datingSquareAdapter.setHeaderAndEmpty(false)
        datingSquareAdapter.setEmptyView(R.layout.empty_my_square_layout, rvMyTag)
        datingSquareAdapter.emptyView.emptyPublishBtn.text = "发布约会"
        datingSquareAdapter.emptyView.emptyImg.setImageResource(R.drawable.icon_empty_my_square)
        datingSquareAdapter.emptyView.emptyMySquareTip.text = "您还没有发布过约会\n快发布你的第一次约会吧"
        datingSquareAdapter.emptyView.emptyPublishBtn.onClick {
            //todo 发布约会
            CommonFunction.checkPublishDating(activity!!)
        }


        datingSquareAdapter.setOnItemClickListener { _, view, position ->
            startActivity<MyLabelActivity>()
//            val intent = Intent()
//            intent.putExtra("aimData", tagAdapter.data[position])
//            intent.putExtra("mode", LabelQualityActivity.MODE_NEW)
//            intent.setClass(activity!!, LabelQualityActivity::class.java)
//            startActivity(intent)
        }
    }

    fun setTagData(datas: MutableList<DatingBean>) {
        datingSquareAdapter.setNewData(datas)
        if (datingSquareAdapter.data.size == 0) {
            datingSquareAdapter.isUseEmpty(true)
        } else {
            datingSquareAdapter.removeAllHeaderView()
            datingSquareAdapter.addHeaderView(initHeadDating())
        }

    }


    /**
     *头部banner
     */
    private fun initHeadDating(): View {
        val headDating = LayoutInflater.from(activity!!)
            .inflate(R.layout.headerview_user_center_square, rvMyTag, false)
        headDating.publishImg.setImageResource(R.drawable.icon_update_dating)
        headDating.publishBtn.text = "更新约会"
        headDating.publishCl.onClick {
            //todo 传递约会信息
            CommonFunction.checkPublishDating(activity!!)
        }
        return headDating
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateMyDatingEvent(event: UpdateMyDatingEvent) {
//        if (!event.tags.isNullOrEmpty())
        setTagData(event.tags ?: mutableListOf<DatingBean>())
    }
}
