package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.GridLayoutManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.adapter.CandyPriceAdapter
import kotlinx.android.synthetic.main.dialog_recharge_candy_discount.*


/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 糖果充值价格获取
 *    version: 1.0
 */
class RechargeCandyDialog(val myContext: Context) : Dialog(myContext, R.style.MyDialog) {
    private val candyPriceAdapter by lazy { CandyPriceAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_recharge_candy_discount)
        initWindow()
        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
    }


    private fun initView() {
        rvRechargeCandy.layoutManager = GridLayoutManager(myContext, 2)
        rvRechargeCandy.adapter = candyPriceAdapter
        for (i in 0 until 5) {
            candyPriceAdapter.addData((i + 1) * 10)
        }

        candyPriceAdapter.setOnItemClickListener { _, view, position ->
            ConfirmPayCandyDialog(myContext).show()
        }
    }

}