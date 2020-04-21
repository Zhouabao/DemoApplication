package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
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
import com.sdy.jitangapplication.event.GetAddressvent
import com.sdy.jitangapplication.event.RefreshAddressvent
import com.sdy.jitangapplication.model.AddressBean
import com.sdy.jitangapplication.model.MyAddressBean
import com.sdy.jitangapplication.presenter.AddressManagerPresenter
import com.sdy.jitangapplication.presenter.view.AddressManagerView
import com.sdy.jitangapplication.ui.adapter.AddressAdapter
import com.sdy.jitangapplication.ui.dialog.DeleteDialog
import kotlinx.android.synthetic.main.activity_address_manager.*
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.error_layout.view.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * 地址管理界面
 */
class AddressManagerActivity : BaseMvpActivity<AddressManagerPresenter>(), AddressManagerView,
    View.OnClickListener {
    private val addressAdapter by lazy { AddressAdapter() }
    private val address by lazy { intent.getSerializableExtra("address") as AddressBean? }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_manager)
        initView()
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        mPresenter = AddressManagerPresenter()
        mPresenter.mView = this
        mPresenter.context = this

        rightBtn.isVisible = true
        rightBtn.text = "添加新地址"
        rightBtn.setTextColor(Color.parseColor("#FF191919"))
        hotT1.text = "收货地址"
        rightBtn.setOnClickListener(this)
        btnBack.setOnClickListener(this)

        stateAddress.retryBtn.onClick {
            stateAddress.viewState = MultiStateView.VIEW_STATE_LOADING
            mPresenter.getAddress()
        }

        rvAddress.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvAddress.adapter = addressAdapter
        addressAdapter.bindToRecyclerView(rvAddress)
        addressAdapter.setEmptyView(R.layout.empty_layout, rvAddress)
        addressAdapter.isUseEmpty(false)


        addressAdapter.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.addressEdit -> {//地址编辑
                    startActivity<AddAddressActivity>("address" to addressAdapter.data[position])
                }
                R.id.menuDelete -> { //删除地址
                    val deleteDialog = DeleteDialog(this)
                    deleteDialog.show()
                    deleteDialog.title.text = "提示"
                    deleteDialog.tip.text = "确认删除该地址？"
                    deleteDialog.confirm.onClick {
                        mPresenter.delAddress(addressAdapter.data[position].id, position)
                        deleteDialog.dismiss()
                    }
                    deleteDialog.cancel.onClick {
                        deleteDialog.dismiss()
                    }
                }
                R.id.menuDefault -> { //设为默认
                    mPresenter.editAddress(addressAdapter.data[position].id, position)
                }
                R.id.content -> { //选中地址
                    finish()
                    EventBus.getDefault().post(GetAddressvent(addressAdapter.data[position]))
                }
            }
        }
        mPresenter.getAddress()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnBack -> {
                finish()
            }
            R.id.rightBtn -> {
                if (addressAdapter.data.size < max_count) {
                    startActivity<AddAddressActivity>()
                } else {
                    CommonFunction.toast("最多可以拥有$max_count 地址")
                }
            }
        }
    }


    private var max_count = 0
    override fun getAddressResult(data: MyAddressBean?) {
        stateAddress.viewState = MultiStateView.VIEW_STATE_CONTENT
        max_count = data?.max_cnt ?: 0
        if (!data?.list.isNullOrEmpty() && address != null) {
            for (data in data?.list ?: mutableListOf()) {
                data.checked = data.id == address!!.id
            }
        }
        if (data?.list.isNullOrEmpty()) {
            addressAdapter.isUseEmpty(true)
        } else {
            addressAdapter.addData(data?.list ?: mutableListOf<AddressBean>())
        }
    }

    override fun delAddressResult(success: Boolean, position: Int) {
        if (success) {
            addressAdapter.remove(position)
        }
    }

    override fun defaultAddressResult(success: Boolean, position: Int) {
        if (success) {
            for (data in addressAdapter.data) {
                data.is_default = data.id == addressAdapter.data[position].id
            }
            addressAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshAddressvent(event: RefreshAddressvent) {
        addressAdapter.data.clear()
        addressAdapter.notifyDataSetChanged()
        mPresenter.getAddress()
    }


}
