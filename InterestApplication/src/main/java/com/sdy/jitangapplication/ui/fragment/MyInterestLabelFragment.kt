package com.sdy.jitangapplication.ui.fragment


import android.content.Intent
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
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateEditModeEvent
import com.sdy.jitangapplication.event.UpdateEditShowEvent
import com.sdy.jitangapplication.event.UpdateMyInterestLabelEvent
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.MyInterestLabelPresenter
import com.sdy.jitangapplication.presenter.view.MyInterestLabelView
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.ui.adapter.MyInterestLabelAdapter
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_interest_label.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 我感兴趣的
 */
class MyInterestLabelFragment : BaseMvpLazyLoadFragment<MyInterestLabelPresenter>(), MyInterestLabelView {

    private val adapter: MyInterestLabelAdapter by lazy { MyInterestLabelAdapter() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_interest_label, container, false)
    }

    override fun loadData() {
        initView()
        mPresenter.getMyInterestTagsList()
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MyInterestLabelPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!

        addInterestLabelBtn.onClick {
            if (adapter.data.size >= UserManager.getMaxInterestLabelCount()) {
                CommonFunction.toast("最多能拥有${UserManager.getMaxInterestLabelCount()}个标签")
                return@onClick
            }

            val intent = Intent()
            intent.putExtra("from", AddLabelActivity.FROM_INTERSERT_LABEL)
            intent.setClass(activity!!, AddLabelActivity::class.java)
            startActivity(intent)

        }
        stateMyInterestLabel.retryBtn.onClick {
            stateMyInterestLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMyInterestTagsList()
        }

        myInterestLabelRv.layoutManager = GridLayoutManager(activity!!, 3, RecyclerView.VERTICAL, false)
        myInterestLabelRv.adapter = adapter
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.labelDelete -> {
                    if (adapter.data.size <= 1) {
                        CommonFunction.toast("至少要保留${MyLabelFragment.MIN_LABEL}个标签")
                        return@setOnItemChildClickListener
                    }

                    showDeleteDialog(position)

                }
            }
        }

    }

    private val deleteDialog by lazy { DeleteDialog(activity!!) }
    private fun showDeleteDialog(position: Int) {
        deleteDialog.show()
        deleteDialog.title.text = "删除标签"
        deleteDialog.title.isVisible = true
        deleteDialog.tip.text = "您确定要删除标签「${adapter.data[position].title}」吗？"
        deleteDialog.confirm.onClick {
            mPresenter.delMyInterest(adapter.data[position].id, position)
            deleteDialog.dismiss()
        }
        deleteDialog.cancel.onClick {
            deleteDialog.dismiss()
        }

    }

    override fun getMyTagsListResult(result: Boolean, data: MutableList<LabelQualityBean>?) {
        if (result) {
            stateMyInterestLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (data != null && data.isNotEmpty()) {
                adapter.setNewData(data)
                EventBus.getDefault().post(UpdateEditShowEvent(MyLabelActivity.MY_INTEREST_LABEL, true))
            } else {
                adapter.setEmptyView(R.layout.empty_layout, myInterestLabelRv)
                EventBus.getDefault().post(UpdateEditShowEvent(MyLabelActivity.MY_INTEREST_LABEL, true))
            }
        } else {
            EventBus.getDefault().post(UpdateEditShowEvent(MyLabelActivity.MY_INTEREST_LABEL, false))
            stateMyInterestLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }

    }


    override fun delTagResult(result: Boolean, position: Int) {
        if (result) {
            adapter.remove(position)
            EventBus.getDefault().post(RefreshEvent(true))
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
                data.isfuse = editMode
            }
            adapter.notifyDataSetChanged()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateMyInterestLabelEvent(event: UpdateMyInterestLabelEvent) {
        mPresenter.getMyInterestTagsList()
    }

}
