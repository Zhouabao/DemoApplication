package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.BarUtils
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.presenter.MyCandyPresenter
import com.sdy.jitangapplication.presenter.view.MyCandyView
import com.sdy.jitangapplication.ui.adapter.CandyProductAdapter
import com.sdy.jitangapplication.ui.dialog.RechargeCandyDialog
import com.sdy.jitangapplication.ui.dialog.WithdrawCandyDialog
import kotlinx.android.synthetic.main.activity_my_candy.*
import org.jetbrains.anko.startActivity


/**
 * 我的糖果中心
 */
class MyCandyActivity : BaseMvpActivity<MyCandyPresenter>(), MyCandyView, View.OnClickListener {

    private val candyProductAdapter by lazy { CandyProductAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_candy)
        BarUtils.setStatusBarVisibility(this, false)
        initView()
    }

    private fun initView() {
        btnBack.setOnClickListener(this)
        withdrawCandy.setOnClickListener(this)
        rechargeCandy.setOnClickListener(this)
        candyRecordBtn.setOnClickListener(this)
        allProductBtn.setOnClickListener(this)


        productRv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        productRv.adapter = candyProductAdapter

        for (i in 0 until 10) {
            candyProductAdapter.addData("$i")
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.allProductBtn -> {
                startActivity<CandyMallActivity>()
            }
            R.id.btnBack -> {
                finish()
            }
            R.id.candyRecordBtn -> { //交易记录
                startActivity<CandyRecordActivity>()
            }
            R.id.rechargeCandy -> {//充值
                RechargeCandyDialog(this).show()
            }
            R.id.withdrawCandy -> {//提现
                WithdrawCandyDialog(this).show()
            }
        }

    }
}
