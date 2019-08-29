package com.example.demoapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.presenter.MyJobPresenter
import com.example.demoapplication.presenter.view.MyJobView
import com.example.demoapplication.ui.adapter.LabelAdapter
import com.example.demoapplication.utils.UserManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator
import kotlinx.android.synthetic.main.activity_labels.btnBack
import kotlinx.android.synthetic.main.activity_my_job.*
import kotlinx.android.synthetic.main.activity_my_job.stateview
import kotlinx.android.synthetic.main.error_layout.view.*

/**
 * 职业
 */
class MyJobActivity : BaseMvpActivity<MyJobPresenter>(), MyJobView, View.OnClickListener {

    private var checkJob: LabelBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_job)

        initView()
    }

    private lateinit var adapter: LabelAdapter
    private val params by lazy { hashMapOf("token" to UserManager.getToken(), "accid" to UserManager.getAccid()) }
    private fun initView() {
        mPresenter = MyJobPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }
        saveJobBtn.setOnClickListener(this)
        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getJobList(params)
        }

        initRv()

        mPresenter.getJobList(params)


    }

    private fun initRv() {
        val manager = FlexboxLayoutManager(this)
        //item的排列方向
        manager.flexDirection = FlexDirection.ROW
        //是否换行
        manager.flexWrap = FlexWrap.WRAP
        manager.alignItems = AlignItems.STRETCH
        userJobRv.layoutManager = manager
        adapter = LabelAdapter()
        userJobRv.adapter = adapter
        //设置添加和移除动画
        userJobRv.itemAnimator = ScaleInLeftAnimator()

        adapter.setOnItemClickListener { _, view, position ->
            for (data in adapter.data.withIndex()) {
                if (data.index == position) {
                    data.value.checked = !data.value.checked
                } else {
                    data.value.checked = false
                }
            }
            if (adapter.data[position].checked) {
                checkJob = adapter.data[position]
                saveJobBtn.isEnabled = true
            } else {
                checkJob = null
                saveJobBtn.isEnabled = false
            }
            adapter.notifyDataSetChanged()

        }
    }

    override fun onGetJobListResult(mutableList: MutableList<LabelBean>?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT
        adapter.setNewData(mutableList)
    }

    override fun onError(text: String) {
        stateview.viewState = MultiStateView.VIEW_STATE_ERROR
        stateview.errorMsg.text = if (mPresenter.checkNetWork()) {
            getString(R.string.retry_load_error)
        } else {
            getString(R.string.retry_net_error)
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.saveJobBtn -> {
                if (checkJob != null) {
                    setResult(Activity.RESULT_OK, intent.putExtra("job", checkJob))

//                    val params = hashMapOf(
//                        "token" to UserManager.getToken(),
//                        "accid" to UserManager.getAccid(),
//                        "job" to (checkJob?.id ?: 0)
//                    )
//                    mPresenter.savePersonal(params)
                }
                finish()
            }
        }
    }

    override fun onSavePersonal(b: Boolean) {
        if (b) {
            ToastUtils.showShort("职业设置成功")
            setResult(Activity.RESULT_OK, intent.putExtra("job", checkJob?.title))
            finish()
        } else {
            ToastUtils.showShort("职业设置失败，请重试")
        }
    }

}
