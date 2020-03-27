package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.GetAddressvent
import com.sdy.jitangapplication.ui.activity.AddAddressActivity
import com.sdy.jitangapplication.ui.adapter.AddressAdapter
import kotlinx.android.synthetic.main.dialog_add_exchange_address.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivityForResult

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 管理添加选择收货地址
 *    version: 1.0
 */
class AddExchangeAddressDialog(var context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_exchange_address)
        initWindow()
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

        addressAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.addAddressIv -> {
                    (context1 as Activity).startActivityForResult<AddAddressActivity>(100)
                }
                R.id.addressCl -> {
                    (context1 as Activity).startActivityForResult<AddAddressActivity>(100)
                }
            }
        }

        addressAdapter.addData("")

        exchangeBtn.onClick {
            dismiss()
            ExchangeSuccessDialog(context1).show()
        }
    }

    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetAddressvent(event: GetAddressvent) {
        addressAdapter.addData(event.address)
        addressAdapter.notifyDataSetChanged()
    }
}