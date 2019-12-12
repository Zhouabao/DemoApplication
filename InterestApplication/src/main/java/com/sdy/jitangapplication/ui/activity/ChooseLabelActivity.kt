package com.sdy.jitangapplication.ui.activity

import android.app.Activity
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
import com.sdy.jitangapplication.event.UpdateMyLabelEvent
import com.sdy.jitangapplication.model.MyLabelBean
import com.sdy.jitangapplication.model.MyLabelsBean
import com.sdy.jitangapplication.model.TagBean
import com.sdy.jitangapplication.presenter.MyLabelPresenter
import com.sdy.jitangapplication.presenter.view.MyLabelView
import com.sdy.jitangapplication.ui.adapter.ChooseLabelAdapter
import com.sdy.jitangapplication.ui.fragment.MyLabelFragment
import kotlinx.android.synthetic.main.activity_choose_label.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.Serializable

/**
 * 发布选择标签
 */
class ChooseLabelActivity : BaseMvpActivity<MyLabelPresenter>(), MyLabelView, View.OnClickListener {
    private val removedLabel = mutableListOf<MyLabelBean>()
    private val adapter by lazy { ChooseLabelAdapter() }
    private var mylabelBean: MyLabelBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_label)

        initView()
        mPresenter.getMyTagsList()
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = MyLabelPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setOnClickListener(this)
        rightBtn1.setOnClickListener(this)
        addMoreLabelBtn.setOnClickListener(this)
        hotT1.text = "选择兴趣"
        rightBtn1.isVisible = true
        rightBtn1.text = "完成"
        rightBtn1.isEnabled = true

        stateChooseLabel.retryBtn.onClick {
            stateChooseLabel.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getMyTagsList()
        }

        rvMyLabels.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvMyLabels.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            for (label in adapter.data) {
                label.checked = label == adapter.data[position]
            }
            mylabelBean = adapter.data[position]
            adapter.notifyDataSetChanged()
        }
    }

    override fun getMyTagsListResult(result: Boolean, datas: MyLabelsBean?) {
        if (result) {
            stateChooseLabel.viewState = MultiStateView.VIEW_STATE_CONTENT
            adapter.setNewData(datas?.is_using ?: mutableListOf())

            for (data in adapter.data.withIndex()) {
                if (data.value.tag_id == intent.getIntExtra("tag_id", -1)) {
                    data.value.checked = true
                    mylabelBean = data.value
                    adapter.notifyItemChanged(data.index)
                    break
                }
            }
            removedLabel.addAll(datas?.is_removed ?: mutableListOf())
        } else {
            stateChooseLabel.viewState = MultiStateView.VIEW_STATE_ERROR
        }
    }

    override fun delTagResult(result: Boolean, position: Int, data: MutableList<TagBean>?) {

    }


    override fun onClick(v: View) {
        when (v) {
            btnBack -> {
                finish()
            }

            rightBtn1 -> {
                if (mylabelBean == null) {
                    CommonFunction.toast("兴趣为必选项")
                    return
                }

                intent.putExtra("label", mylabelBean)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            addMoreLabelBtn -> {
                if (adapter.data.size == 5) {
                    CommonFunction.toast("至多能拥有${MyLabelFragment.MAX_LABEL}个标签")
                    return
                }

                intent.putExtra("from", AddLabelActivity.FROM_PUBLISH)
                intent.putExtra("is_using", adapter.data as Serializable)
                intent.putExtra("is_removed", removedLabel as Serializable)
                intent.setClass(this, AddLabelActivity::class.java)
                startActivity(intent)
            }
        }
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
