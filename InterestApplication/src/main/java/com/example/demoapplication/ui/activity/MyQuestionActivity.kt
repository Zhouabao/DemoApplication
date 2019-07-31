package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.view.View
import com.example.demoapplication.R
import com.example.demoapplication.presenter.MyQuestionPresenter
import com.example.demoapplication.presenter.view.MyQuestionView
import com.example.demoapplication.ui.adapter.AllQuestionAdapter
import com.example.demoapplication.ui.adapter.MyQuestionAdapter
import com.kotlin.base.ui.activity.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_my_question.*
import org.jetbrains.anko.toast

/**
 * 我的问题
 */
class MyQuestionActivity : BaseMvpActivity<MyQuestionPresenter>(), MyQuestionView, View.OnClickListener {
    private val myQuestionAdapter by lazy { MyQuestionAdapter() }
    private val allQuestionAdapter by lazy { AllQuestionAdapter() }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_question)
        initView()
    }

    private fun initView() {
        newQuestionBtn.setOnClickListener(this)
        btnBack.setOnClickListener(this)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.newQuestionBtn -> {
                toast("换一批")
            }
            R.id.btnBack -> {
                finish()
            }
        }

    }
}
