package com.sdy.jitangapplication.ui.activity

import android.content.Intent
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
import com.sdy.jitangapplication.event.RefreshEvent
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.MyLabelsBean
import com.sdy.jitangapplication.model.TagBean
import com.sdy.jitangapplication.presenter.MyLabelPresenter
import com.sdy.jitangapplication.presenter.view.MyLabelView
import com.sdy.jitangapplication.ui.adapter.MyLabelAdapter
import com.sdy.jitangapplication.ui.dialog.ChargeLabelDialog
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import com.sdy.jitangapplication.ui.dialog.LoadingDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_my_label.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.empty_label_layout.view.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * 管理我的标签
 */
class MyLabelActivity : BaseMvpActivity<MyLabelPresenter>(), MyLabelView, View.OnClickListener {
    companion object {
        val MIN_LABEL = 1
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
        btnBack.setOnClickListener(this)
        rightBtn.setOnClickListener(this)
        addLabelBtn.setOnClickListener(this)
        hotT1.text = "标签管理"
        rightBtn.isVisible = true
        rightBtn.text = "删除"
        divider.isVisible = false
        rightBtn.setTextColor(Color.parseColor("#FF191919"))


        EventBus.getDefault().register(this)
        mPresenter = MyLabelPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        stateMyLabel.retryBtn.onClick {
            stateMyLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMyTagsList()
        }

        mylabelRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        mylabelRv.adapter = adapter
        adapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.labelPurchase -> {
                    ChargeLabelDialog(this, adapter.data[position].tag_id).show()
                }
                R.id.labelDelete -> {
                    // 删除标签
                    if (adapter.data.size <= MIN_LABEL) {
                        CommonFunction.toast("至少保留${MIN_LABEL}个标签")
                        return@setOnItemChildClickListener
                    }
                    showDeleteDialog(position)
                }
                R.id.labelEdit -> {
                    val intent = Intent()
                    intent.putExtra("aimData", adapter.data[position])
                    intent.putExtra(
                        "mode", if (adapter.data[position].label_quality.isNullOrEmpty()) {
                            LabelQualityActivity.MODE_NEW
                        } else {
                            LabelQualityActivity.MODE_EDIT
                        }
                    )
                    intent.setClass(this, LabelQualityActivity::class.java)
                    startActivity(intent)
                }
                R.id.labelQualityAddBtn -> {
                    if (adapter.data[position].is_expire) {
                        return@setOnItemChildClickListener
                    }
                    val intent = Intent()
                    intent.putExtra("aimData", adapter.data[position])
                    intent.putExtra(
                        "mode", if (adapter.data[position].label_quality.isNullOrEmpty()) {
                            LabelQualityActivity.MODE_NEW
                        } else {
                            LabelQualityActivity.MODE_EDIT
                        }
                    )
                    intent.setClass(this, LabelQualityActivity::class.java)
                    startActivity(intent)
                }

            }
        }
    }



    override fun getMyTagsListResult(result: Boolean, datas: MyLabelsBean?) {
        if (result) {
            stateMyLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
            //保存标签的最大个数
            UserManager.saveMaxMyLabelCount(datas?.limit_count ?: 0)
            if (datas != null && datas.is_using.isNullOrEmpty()) {
                addLabelBtn.isVisible = false
                adapter.setEmptyView(R.layout.empty_label_layout, mylabelRv)
                adapter.emptyView.addLabelBtn.onClick {
                    intent.putExtra("from", AddLabelActivity.FROM_ADD_NEW)
                    intent.setClass(this, AddLabelActivity::class.java)
                    startActivity(intent)
                }
                adapter.emptyView.emptyLabelTip.isVisible = true
                adapter.emptyView.emptyTip.isVisible = true
                adapter.emptyView.addLabelBtn.text = "添加标签"
            } else {
                addLabelBtn.isVisible = true
                adapter.setNewData(datas?.is_using ?: mutableListOf())
            }
        } else {
            stateMyLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun delTagResult(result: Boolean, position: Int, data: MutableList<TagBean>?) {
        if (result) {
            UserManager.saveLabels(data ?: mutableListOf())
            adapter.remove(position)
            EventBus.getDefault().post(RefreshEvent(true))
            EventBus.getDefault().post(UserCenterEvent(true))
        }
    }






    private val deleteDialog by lazy { DeleteDialog(this) }
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

    override fun onClick(p0: View) {
        when (p0) {
            btnBack -> {
                finish()
            }
            rightBtn -> {
                onUpdateEditMode()
            }
            addLabelBtn -> {//添加标签
                if (adapter.data.size >= UserManager.getMaxMyLabelCount()) {
                    CommonFunction.toast("最多能拥有${UserManager.getMaxMyLabelCount()}个标签")
                    return
                }
                intent.putExtra("from", AddLabelActivity.FROM_ADD_NEW)
                intent.setClass(this, AddLabelActivity::class.java)
                startActivity(intent)
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

    fun onUpdateEditMode() {
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
