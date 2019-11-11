package com.sdy.jitangapplication.ui.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.kennyc.view.MultiStateView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.ChooseSchoolPresenter
import com.sdy.jitangapplication.presenter.view.ChooseSchoolView
import com.sdy.jitangapplication.ui.adapter.SchoolAdapter
import kotlinx.android.synthetic.main.activity_choose_school.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 选择学校界面
 */
class ChooseSchoolActivity : BaseMvpActivity<ChooseSchoolPresenter>(), ChooseSchoolView {

    companion object {
        public const val CHOOSE_SCHOOL = 1
        public const val CHOOSE_JOB = 2
    }

    private val adapter by lazy { SchoolAdapter() }
    private var schools: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_school)
        initView()


        if (intent.getIntExtra("type", CHOOSE_SCHOOL) == CHOOSE_SCHOOL) {
            mPresenter.getSchoolList()
        } else {
            mPresenter.getOccupationList()
        }
    }

    private fun initView() {
        mPresenter = ChooseSchoolPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        if (intent.getIntExtra("type", CHOOSE_SCHOOL) == CHOOSE_SCHOOL) {
            hotT1.text = "添加学校"
            searchSchool.queryHint = "学校名称"
        } else {
            hotT1.text = "添加职业"
            searchSchool.queryHint = "职业名称"
        }
        btnBack.isVisible = false
        rightBtn.isVisible = true
        rightBtn.text = "取消"
        rightBtn.onClick {
            setResult(Activity.RESULT_OK)
            onBackPressed()
        }

        stateSchool.retryBtn.onClick {
            stateSchool.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getSchoolList()
        }

        schoolRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        schoolRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            intent.putExtra("schoolBean", adapter.data[position])
            setResult(Activity.RESULT_OK, intent)
            onBackPressed()
        }



        searchSchool.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    adapter.data.clear()
                    for (school in schools) {
                        if (school.contains(query)) {
                            adapter.data.add(school)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    adapter.data.clear()
                    adapter.data.addAll(schools)
                    adapter.notifyDataSetChanged()
                }

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })
        val searchCloseButtonId =
            searchSchool.context.resources.getIdentifier("android:id/search_close_btn", null, null)
        val closeButton = searchSchool.findViewById(searchCloseButtonId) as ImageView?
        if (closeButton != null)
            closeButton.onClick {
                searchSchool.setQuery("", false)
                adapter.data.clear()
                adapter.data.addAll(schools)
                adapter.notifyDataSetChanged()
            }
    }


    override fun onGetSchoolListResult(success: Boolean, schoolList: MutableList<String>?) {
        if (success)
            stateSchool.viewState = MultiStateView.VIEW_STATE_CONTENT
        else
            stateSchool.viewState = MultiStateView.VIEW_STATE_ERROR
        adapter.addData(schoolList ?: mutableListOf())
        schools.addAll(schoolList ?: mutableListOf<String>())
    }

    override fun onGetJobListResult(jobList: MutableList<String>?) {
        if (jobList != null)
            stateSchool.viewState = MultiStateView.VIEW_STATE_CONTENT
        else
            stateSchool.viewState = MultiStateView.VIEW_STATE_ERROR
        adapter.addData(jobList ?: mutableListOf())
        schools.addAll(jobList ?: mutableListOf<String>())
    }


    override fun onBackPressed() {
        rootView.animate()                           //contentRoot退出activity的根视图
            .translationY(ScreenUtils.getScreenHeight().toFloat())  //根视图整体向下平移整个屏幕高度效
            .setDuration(1500)                           //动画持续时间为300毫秒
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    overridePendingTransition(0, 0)
                }
            }).start()

        super.onBackPressed()

    }
}
