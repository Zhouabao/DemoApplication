package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.UpdateAvatorEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.event.UserCenterLabelEvent
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.model.MyLabelsBean
import com.sdy.jitangapplication.model.TagBean
import com.sdy.jitangapplication.presenter.MyLabelPresenter
import com.sdy.jitangapplication.presenter.view.MyLabelView
import com.sdy.jitangapplication.ui.adapter.MyLabelAdapter
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_my_label.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.Serializable


/**
 * 管理我的标签
 */
class MyLabelActivity : BaseMvpActivity<MyLabelPresenter>(), MyLabelView, View.OnClickListener {
    companion object {
        val MIN_LABEL = 1
        val MAX_LABEL = 5
    }


    private var editMode = false //是否处于编辑模式
    private val adapter by lazy { MyLabelAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_label)
        initView()
        mPresenter.getMyTagsList()
    }

    private fun initView() {
        EventBus.getDefault().register(this)
        mPresenter = MyLabelPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        hotT1.text = "管理你的兴趣"
        btnBack.setOnClickListener(this)
        rightBtn.setOnClickListener(this)
        rightBtn.text = "删除"
        rightBtn.setTextColor(Color.parseColor("#FF191919"))


        addLabelBtn.setOnClickListener(this)
        stateMyLabel.retryBtn.onClick {
            stateMyLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMyTagsList()
        }

        mylabelRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mylabelRv.adapter = adapter
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.labelDelete -> {
                    //TODO删除标签
                    if (adapter.data.size <= MIN_LABEL) {
                        CommonFunction.toast("至少保留${MIN_LABEL}个兴趣")
                        return@setOnItemChildClickListener
                    }
                    showDeleteDialog(position)

                }
                R.id.labelEdit -> {
                    //TODO标签编辑
                    intent.putExtra("aimData", adapter.data[position])
                    intent.putExtra("from", AddLabelActivity.FROM_EDIT)
                    intent.putExtra("mode", LabelQualityActivity.MODE_EDIT)
                    intent.setClass(this, MyLabelQualityActivity::class.java)
                    startActivity(intent)
                }

            }
        }
    }

    private val deleteDialog by lazy { DeleteDialog(this) }
    private fun showDeleteDialog(position: Int) {
        deleteDialog.show()
        deleteDialog.title.text = "删除兴趣"
        deleteDialog.title.isVisible = true
        deleteDialog.tip.text = "您确定要删除兴趣「${adapter.data[position].title}」吗？"
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
            rightBtn.isVisible = true
            stateMyLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
            if (datas != null && datas.is_using.isNullOrEmpty()) {
                stateMyLabel.viewState = MultiStateView.VIEW_STATE_EMPTY
                rightBtn.isVisible = false
            }
            adapter.setNewData(datas?.is_using ?: mutableListOf())
            removedLabel.addAll(datas?.is_removed ?: mutableListOf())
        } else {
            stateMyLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun delTagResult(result: Boolean, position: Int, data: MutableList<TagBean>?) {
        if (result) {
            if (data != null) {
                UserManager.saveLabels(data)
                EventBus.getDefault().post(UpdateAvatorEvent(true))
                EventBus.getDefault().post(UserCenterLabelEvent())
            }

            removedLabel.add(adapter.data[position])
            adapter.remove(position)
        }
    }


    override fun onClick(p0: View) {
        when (p0) {
            addLabelBtn -> {//添加标签
                if (adapter.data.size >= MAX_LABEL) {
                    CommonFunction.toast("最多能拥有${MAX_LABEL}个兴趣")
                    return
                }

                intent.putExtra("from", AddLabelActivity.FROM_ADD_NEW)
                intent.putExtra("is_using", adapter.data as Serializable)
                intent.putExtra("is_removed", removedLabel as Serializable)
                intent.setClass(this, AddLabelActivity::class.java)
                startActivity(intent)
            }
            btnBack -> {
                finish()
            }
            rightBtn -> {
                editMode = !editMode
                rightBtn.text = if (editMode) {
                    "取消"
                } else {
                    "删除"
                }
                addLabelBtn.isVisible = !editMode
                for (data in adapter.data) {
                    data.editMode = editMode
                }
                adapter.notify = true
                adapter.notifyDataSetChanged()
            }
        }
    }

    private val loading by lazy { LoadingDialog(this) }
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

}
