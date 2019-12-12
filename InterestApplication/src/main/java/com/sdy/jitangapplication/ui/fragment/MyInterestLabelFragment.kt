package com.sdy.jitangapplication.ui.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.UpdateEditModeEvent
import com.sdy.jitangapplication.model.MyLabelsBean
import com.sdy.jitangapplication.model.TagBean
import com.sdy.jitangapplication.presenter.MyLabelPresenter
import com.sdy.jitangapplication.presenter.view.MyLabelView
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.ui.adapter.MyInterestLabelAdapter
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_interest_label.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.Serializable

/**
 * 我感兴趣的
 */
class MyInterestLabelFragment : BaseMvpLazyLoadFragment<MyLabelPresenter>(), MyLabelView {
    private val adapter: MyInterestLabelAdapter by lazy { MyInterestLabelAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_interest_label, container, false)
    }

    override fun loadData() {
        initView()
        mPresenter.getMyTagsList()
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MyLabelPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        addInterestLabelBtn.onClick {
            if (adapter.data.size >= MyLabelFragment.MAX_LABEL) {
                CommonFunction.toast("最多能拥有${MyLabelFragment.MAX_LABEL}个兴趣")
                return@onClick
            }

            activity!!.intent.putExtra("from", AddLabelActivity.FROM_ADD_NEW)
            activity!!.intent.putExtra("is_using", adapter.data as Serializable)
            activity!!.intent.setClass(activity!!, AddLabelActivity::class.java)
            startActivity(activity!!.intent)

        }
        stateMyInterestLabel.retryBtn.onClick {
            stateMyInterestLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMyTagsList()
        }

        myInterestLabelRv.layoutManager = GridLayoutManager(activity!!, 3, RecyclerView.VERTICAL, false)
        myInterestLabelRv.adapter = adapter
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.labelDelete->{
                    adapter.remove(position)
//                    mPresenter.delMyInterestTags(adapter.data[position].tag_id,position)
                }
            }
        }

    }

    override fun getMyTagsListResult(result: Boolean, datas: MyLabelsBean?) {
        if (result) {
            stateMyInterestLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (datas != null && datas.is_using.isNullOrEmpty()) {
                stateMyInterestLabel.viewState = MultiStateView.VIEW_STATE_EMPTY
            }
            adapter.addData(datas?.is_using ?: mutableListOf())
            adapter.addData(datas?.is_using ?: mutableListOf())
            adapter.addData(datas?.is_using ?: mutableListOf())
            adapter.addData(datas?.is_using ?: mutableListOf())
        } else {
            stateMyInterestLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun delTagResult(result: Boolean, position: Int, data: MutableList<TagBean>?) {
        if (result) {
            adapter.remove(position)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private var editMode = false
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEditModeEvent(event: UpdateEditModeEvent) {
        if (event.position == MyLabelActivity.MY_INTEREST_LABEL) {
            editMode = !editMode
            addInterestLabelBtn.isVisible = !editMode
            for (data in adapter.data) {
                data.editMode = editMode
            }
            adapter.notifyDataSetChanged()
        }
    }

}
