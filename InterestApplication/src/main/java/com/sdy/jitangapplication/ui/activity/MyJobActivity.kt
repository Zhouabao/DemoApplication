package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.google.android.flexbox.*
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.presenter.MyJobPresenter
import com.sdy.jitangapplication.presenter.view.MyJobView
import com.sdy.jitangapplication.ui.adapter.LabelAdapter
import com.sdy.jitangapplication.utils.UserManager
import jp.wasabeef.recyclerview.animators.ScaleInLeftAnimator
import kotlinx.android.synthetic.main.activity_my_job.*
import kotlinx.android.synthetic.main.error_layout.view.*

/**
 * 职业
 */
class MyJobActivity   : BaseMvpActivity<MyJobPresenter>(), MyJobView, View.OnClickListener {

    private var checkJob: NewLabel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_job)

        initView()
    }

    private lateinit var adapter: LabelAdapter
    private var checkedJob: String = ""
    private val params by lazy { hashMapOf("token" to UserManager.getToken(), "accid" to UserManager.getAccid()) }
    private fun initView() {
        //已经选中的职业
        checkedJob = intent.getStringExtra("job") ?: ""

        mPresenter = MyJobPresenter()
        mPresenter.mView = this
        mPresenter.context = this
        btnBack.onClick { finish() }
        saveJobBtn.setOnClickListener(this)
        stateview.retryBtn.onClick {
            stateview.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getOccupationList()
        }


        initRv()

        mPresenter.getOccupationList()


    }

    private fun initRv() {
        val manager = FlexboxLayoutManager(this)
        //item的排列方向
        manager.flexDirection = FlexDirection.ROW
        //是否换行
        manager.flexWrap = FlexWrap.WRAP
        manager.alignItems = AlignItems.STRETCH
        manager.justifyContent = JustifyContent.CENTER
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

    override fun onGetJobListResult(mutableList: MutableList<String>?) {
        stateview.viewState = MultiStateView.VIEW_STATE_CONTENT

        for (job in mutableList?: mutableListOf()) {
            if (job == checkedJob) {
//                job.isfuse = true
                break
            }
        }
//        adapter.setNewData(mutableList)
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
            CommonFunction.toast(getString(R.string.job_success))
            setResult(Activity.RESULT_OK, intent.putExtra("job", checkJob?.title))
            finish()
        } else {
            CommonFunction.toast(getString(R.string.job_setting_fail))
        }
    }

}
