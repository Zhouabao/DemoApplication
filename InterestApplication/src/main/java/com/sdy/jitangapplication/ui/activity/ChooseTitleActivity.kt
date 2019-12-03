package com.sdy.jitangapplication.ui.activity

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.presenter.ChooseTitlePresenter
import com.sdy.jitangapplication.presenter.view.ChooseTitleView
import com.sdy.jitangapplication.ui.adapter.ChooseTitleAdapter
import com.sdy.jitangapplication.widgets.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_choose_title.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 发布选择标题
 */
class ChooseTitleActivity : BaseMvpActivity<ChooseTitlePresenter>(), ChooseTitleView, View.OnClickListener {

    private val adapter by lazy { ChooseTitleAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_title)
        initView()
    }


    private var checkPos = -1
    private fun initView() {
        mPresenter = ChooseTitlePresenter()
        mPresenter.mView = this
        mPresenter.context = this

        btnBack.setOnClickListener(this)
        hotT1.text = "选择标题"
        rightBtn1.setOnClickListener(this)
        rightBtn1.text = "保存"
        rightBtn1.isVisible = true

//        stateTitle
        titleRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        titleRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.HORIZONTAL_LIST,
                SizeUtils.dp2px(1f),
                resources.getColor(R.color.colorDivider)
            )
        )
        titleRv.adapter = adapter
        adapter.setOnItemClickListener { _, view, position ->
            checkPos = position
            titleEt.setText(adapter.data[position].content)
            for (data in adapter.data) {
                data.checked = data == adapter.data[position]
            }
            adapter.notifyDataSetChanged()
        }

        titleEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s != null && checkPos != -1 && s.toString() != adapter.data[checkPos].content) {
                    adapter.data[checkPos].checked = false
                    adapter.notifyItemChanged(checkPos)
                }
                checkConfirmEnable()
                titleEt.setSelection(titleEt.text.length)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })


        adapter.addData(LabelQualityBean(content = "标题提是点击开始看"))
        adapter.addData(LabelQualityBean(content = "标题提是点击开始看1"))
        adapter.addData(LabelQualityBean(content = "标题提是点击开始看2"))
        adapter.addData(LabelQualityBean(content = "标题提是点击开始看3"))
        adapter.addData(LabelQualityBean(content = "标题提是点击开始看4"))
        adapter.addData(LabelQualityBean(content = "标题提是点击开始看5"))
    }


    fun checkConfirmEnable() {
        rightBtn1.isEnabled = titleEt.text.trim().toString().isNotEmpty()
    }


    override fun onClick(view: View) {
        when (view) {
            btnBack -> {
                finish()
            }
            rightBtn1 -> {
                intent.putExtra("title", titleEt.text.trim().toString())
                setResult(Activity.RESULT_OK, intent)
            }
        }
    }
}
