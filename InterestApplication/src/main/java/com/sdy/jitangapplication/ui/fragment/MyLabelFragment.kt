package com.sdy.jitangapplication.ui.fragment


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.fragment.BaseMvpLazyLoadFragment
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateEditModeEvent
import com.sdy.jitangapplication.event.UpdateEditShowEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.model.MyLabelsBean
import com.sdy.jitangapplication.model.TagBean
import com.sdy.jitangapplication.presenter.MyLabelPresenter
import com.sdy.jitangapplication.presenter.view.MyLabelView
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.ui.activity.LabelQualityActivity
import com.sdy.jitangapplication.ui.activity.MyLabelActivity
import com.sdy.jitangapplication.ui.adapter.MyLabelAdapter
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.empty_label_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.fragment_my_label.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.support.v4.startActivity
import java.io.Serializable


class MyLabelFragment : BaseMvpLazyLoadFragment<MyLabelPresenter>(), MyLabelView, View.OnClickListener {
    companion object {
        val MIN_LABEL = 1
        val MAX_LABEL = 5
    }


    private var editMode = false //是否处于编辑模式
    private val adapter by lazy { MyLabelAdapter() }

    override fun loadData() {
        initView()
        mPresenter.getMyTagsList()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_label, container, false)
    }

    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = MyLabelPresenter()
        mPresenter.mView = this
        mPresenter.context = activity!!
        addLabelBtn.setOnClickListener(this)
        stateMyLabel.retryBtn.onClick {
            stateMyLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMyTagsList()
        }

        mylabelRv.layoutManager = LinearLayoutManager(activity!!, RecyclerView.VERTICAL, false)
        mylabelRv.adapter = adapter
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.labelDelete -> {
                    // 删除标签
                    if (adapter.data.size <= MIN_LABEL) {
                        CommonFunction.toast("至少保留${MIN_LABEL}个标签")
                        return@setOnItemChildClickListener
                    }
                    showDeleteDialog(position)

                }
                R.id.labelEdit -> {
                    //TODO标签编辑
                    val intent = Intent()
                    intent.putExtra("aimData", adapter.data[position])
                    intent.putExtra("from", AddLabelActivity.FROM_EDIT)
                    intent.putExtra("mode", LabelQualityActivity.MODE_EDIT)
                    intent.setClass(activity!!, LabelQualityActivity::class.java)
                    startActivity(intent)
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
            mPresenter.delMyTags(adapter.data[position].id, position)
            deleteDialog.dismiss()
        }
        deleteDialog.cancel.onClick {
            deleteDialog.dismiss()
        }

    }


    private val removedLabel = mutableListOf<MyLabelBean>()
    override fun getMyTagsListResult(result: Boolean, datas: MyLabelsBean?) {
        if (result) {
            stateMyLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (datas != null && datas.is_using.isNullOrEmpty()) {
                addLabelBtn.isVisible = false
                adapter.setEmptyView(R.layout.empty_label_layout, mylabelRv)
                adapter.emptyView.addLabelBtn.onClick {
                    startActivity<AddLabelActivity>("from" to AddLabelActivity.FROM_ADD_NEW)
                }
                adapter.emptyView.emptyLabelTip.isVisible = false
                adapter.emptyView.emptyTip.isVisible = false
                adapter.emptyView.addLabelBtn.text = "添加标签"
                EventBus.getDefault().post(UpdateEditShowEvent(MyLabelActivity.MY_LABEL, false))
            } else {
                EventBus.getDefault().post(UpdateEditShowEvent(MyLabelActivity.MY_LABEL, true))
                adapter.setNewData(datas?.is_using ?: mutableListOf())
            }
            removedLabel.addAll(datas?.is_removed ?: mutableListOf())
        } else {
            EventBus.getDefault().post(UpdateEditShowEvent(MyLabelActivity.MY_LABEL, false))
            stateMyLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun delTagResult(result: Boolean, position: Int, data: MutableList<TagBean>?) {
        if (result) {
            removedLabel.add(adapter.data[position])
            adapter.remove(position)
            EventBus.getDefault().post(RefreshEvent(true))
        }
    }


    override fun onClick(p0: View) {
        when (p0) {
            addLabelBtn -> {//添加标签
                if (adapter.data.size >= MAX_LABEL) {
                    CommonFunction.toast("最多能拥有${MAX_LABEL}个标签")
                    return
                }

                activity!!.intent.putExtra("from", AddLabelActivity.FROM_ADD_NEW)
                activity!!.intent.putExtra("is_using", adapter.data as Serializable)
                activity!!.intent.putExtra("is_removed", removedLabel as Serializable)
                activity!!.intent.setClass(activity!!, AddLabelActivity::class.java)
                startActivity(activity!!.intent)
            }

        }
    }

    private val loading by lazy { LoadingDialog(activity!!) }
    override fun showLoading() {
        if (!loading.isShowing)
            loading.show()
    }

    override fun hideLoading() {
        if (loading.isShowing)
            loading.hide()
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateMyLabelEvent(event: UpdateMyLabelEvent) {
        mPresenter.getMyTagsList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateEditModeEvent(event: UpdateEditModeEvent) {
        if (event.position == MyLabelActivity.MY_LABEL) {
            editMode = !editMode
            addLabelBtn.isVisible = !editMode
            for (data in adapter.data) {
                data.editMode = editMode
            }
            adapter.notify = true
            adapter.notifyDataSetChanged()
        }
    }

}
