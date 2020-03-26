package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.activity.AddAddressActivity
import com.sdy.jitangapplication.ui.adapter.AddressAdapter
import kotlinx.android.synthetic.main.dialog_add_exchange_address.*
import kotlinx.android.synthetic.main.headerview_address.view.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 管理添加选择收货地址
 *    version: 1.0
 */
class AddExchangeAddressDialog(var context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContentView(R.layout.dialog_add_exchange_address)
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

    private val addressAdapter = AddressAdapter()

    fun initview() {
        rvAddress.layoutManager = LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)
        rvAddress.adapter = addressAdapter
        addressAdapter.addHeaderView(initHeadView())

        addressAdapter.setOnItemClickListener { _, view, position ->
            context1.startActivity<AddAddressActivity>()
        }

        addressAdapter.headerLayout.addAddressBtn.onClick {
            context1.startActivity<AddAddressActivity>()
        }


        addressAdapter.addData("")
        addressAdapter.addData("")
        addressAdapter.addData("")
        addressAdapter.addData("")
        addressAdapter.addData("")


//        addAddressBtn.onClick {
//            //todo 跳转到添加地址
//
//        }

        exchangeBtn.onClick {
            dismiss()
            ExchangeSuccessDialog(context1).show()
        }
    }

    private fun initHeadView(): View {
        val imageview =
            LayoutInflater.from(context1).inflate(R.layout.headerview_address, rvAddress, false)

        return imageview
    }

}