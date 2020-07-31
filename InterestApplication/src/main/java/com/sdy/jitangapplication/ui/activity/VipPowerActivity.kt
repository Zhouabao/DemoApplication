package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.ChargeWayBean
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.presenter.VipPowerPresenter
import com.sdy.jitangapplication.presenter.view.VipPowerView
import com.sdy.jitangapplication.ui.adapter.AllVipPowerAdapter
import com.sdy.jitangapplication.ui.adapter.PowerInfoAdapter
import com.sdy.jitangapplication.ui.adapter.VipChargeAdapter
import com.sdy.jitangapplication.ui.adapter.VipPowerAdapter
import com.sdy.jitangapplication.ui.dialog.ConfirmPayCandyDialog
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import kotlinx.android.synthetic.main.activity_vip_power1.*
import kotlinx.android.synthetic.main.layout_actionbar.*
import java.math.BigDecimal

class VipPowerActivity() :
    BaseMvpActivity<VipPowerPresenter>(), VipPowerView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vip_power1)
        initView()
        mPresenter.getChargeData()
    }

    private fun initView() {
        mPresenter = VipPowerPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        StatusBarUtil.immersive(this)
        llTitle.setBackgroundColor(Color.TRANSPARENT)
//        llTitle.setBackgroundColor(Color.parseColor("#FF1D1F21"))
        btnBack.setImageResource(R.drawable.icon_back_white)
        divider.isVisible = false
        hotT1.setTextColor(resources.getColor(R.color.colorWhite))
        hotT1.text = "会员权益"
        btnBack.onClick { finish() }

        initVp2()
    }

    private val adapter by lazy { AllVipPowerAdapter() }
    private val powerInfoAdapter by lazy { PowerInfoAdapter() }
    var slideLeft = false
    var lastPosition = false

    private fun initVp2() {


        powerInfoRv.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(powerInfoRv)
        powerInfoRv.adapter = powerInfoAdapter
        powerPriceRv.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(powerPriceRv)
        powerPriceRv.adapter = adapter
        powerInfoRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val last =
                    (powerInfoRv.layoutManager as CenterLayoutManager).findLastCompletelyVisibleItemPosition()
                lastPosition =
                    last == (powerInfoRv.layoutManager as CenterLayoutManager).itemCount - 1
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (last == (powerInfoRv.layoutManager as CenterLayoutManager).itemCount - 1 && slideLeft) {
//                        if (chargeWayBeans?.isdirect == true)
//                            payBtn.text = "续费至尊直联卡"
//                        else
//                            payBtn.text = "获取至尊直联卡"
//                        payBtn.setTextColor(Color.parseColor("#FFFFD27A"))
//                        payBtn.setBackgroundResource(R.drawable.gradient_dark_black_0dp)
//                        powerPriceRv.scrollToPosition(1)
                        setPriceData(adapter.data[1])
                    } else {
                        setPriceData(adapter.data[0])
//                        powerPriceRv.scrollToPosition(0)
//                        if (chargeWayBeans?.isplatinum == true)
//                            payBtn.text = "续费高级会员"
//                        else
//                            payBtn.text = "成为高级会员"
//                        payBtn.setTextColor(Color.parseColor("#ff1d1f21"))
//                        payBtn.setBackgroundResource(R.drawable.gradient_light_orange_0dp)
                    }
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                slideLeft = dx > 0
            }
        })
    }


    private var chargeWayBeans: ChargeWayBeans? = null

    override fun getChargeDataResult(data: ChargeWayBeans?) {
        chargeWayBeans = data
        if (data != null) {
            adapter.threshold_btn = data?.threshold_btn ?: false
            var ispromote = false
            for (tdata in data.pt_list ?: mutableListOf()) {
                if (tdata.is_promote) {
                    ispromote = true
                    break
                }
            }
            if (!ispromote && !data.pt_list.isNullOrEmpty()) {
                data.pt_list[0]?.is_promote = true
            }
            adapter.addData(
                VipPowerBean(
                    data.pt_icon_list,
                    data.isplatinum,
                    data.platinum_vip_express,
                    data.platinum_save_str,
                    VipPowerBean.TYPE_GOLD_VIP,
                    data.pt_list ?: mutableListOf(),
                    data.paylist ?: mutableListOf()
                )
            )
            var ispromote1 = false
            for (tdata in data.direct_list ?: mutableListOf()) {
                if (tdata.is_promote) {
                    ispromote1 = true
                    break
                }
            }
            if (!ispromote1 && !data.direct_list.isNullOrEmpty()) {
                data.direct_list[0]?.is_promote = true
            }
            adapter.addData(
                VipPowerBean(
                    data.direct_icon_list,
                    data.isdirect,
                    data.direct_vip_express,
                    "每日免费解锁${data.direct_cnt}次联系方式",
                    VipPowerBean.TYPE_PT_VIP,
                    data.direct_list ?: mutableListOf(),
                    data.paylist ?: mutableListOf()
                )
            )
            powerInfoAdapter.setNewData(adapter.data)
            setPriceData(adapter.data[0])
        }
    }


    fun setPriceData(data: VipPowerBean) {
        var promotePos = -1
        for (tdata in data.list.withIndex()) {
            if (tdata.value.is_promote) {
                promotePos = tdata.index
            }
        }
        if (promotePos == -1) promotePos = 0
        openVipBtn.text = "¥${if (data.list[promotePos].type == 1) {
            BigDecimal(data.list[promotePos].original_price).setScale(
                0,
                BigDecimal.ROUND_HALF_UP
            )
        } else {
            BigDecimal(data.list[promotePos].discount_price).setScale(
                0,
                BigDecimal.ROUND_HALF_UP
            )
        }} 获取${if (data.type == VipPowerBean.TYPE_GOLD_VIP) {
            "黄金"
        } else {
            "钻石"
        }}会员"
        val chargePriceAdapter by lazy { VipChargeAdapter(data.type) }
        val chargeManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        vipPriceRv.layoutManager = chargeManager
        vipPriceRv.adapter = chargePriceAdapter
        chargePriceAdapter.setNewData(data.list)
        chargePriceAdapter.setOnItemClickListener { _, view, position ->
            for (data in chargePriceAdapter.data) {
                data.is_promote = data == chargePriceAdapter.data[position]
            }
            openVipBtn.text = "¥${if (chargePriceAdapter.data[position].type == 1) {
                BigDecimal(chargePriceAdapter.data[position].original_price).setScale(
                    0,
                    BigDecimal.ROUND_HALF_UP
                )
            } else {
                BigDecimal(chargePriceAdapter.data[position].discount_price).setScale(
                    0,
                    BigDecimal.ROUND_HALF_UP
                )
            }} 获取${if (data.type == VipPowerBean.TYPE_GOLD_VIP) {
                "黄金"
            } else {
                "钻石"
            }}会员"
            chargePriceAdapter.notifyDataSetChanged()
        }


        openVipBtn.clickWithTrigger {
            var position: ChargeWayBean? = null
            for (data in chargePriceAdapter.data) {
                if (data.is_promote) {
                    position = data
                    break
                }
            }
            if (position != null)
                ConfirmPayCandyDialog(this, position, data.payway).show()
        }


        val vipPowerAdapter = VipPowerAdapter(data.type)
        val manager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        vipPowerRv.layoutManager = manager
        vipPowerRv.adapter = vipPowerAdapter
        vipPowerAdapter.setNewData(data.icon_list)

        when (data.type) {
            VipPowerBean.TYPE_PT_VIP -> {
                openVipBtn.setBackgroundResource(R.drawable.gradient_pt_vip_24dp)
                vipLogo.setImageResource(R.drawable.icon_logo_pt_vip)
            }
            VipPowerBean.TYPE_GOLD_VIP -> {
                vipLogo.setImageResource(R.drawable.icon_logo_gold_vip)
                openVipBtn.setBackgroundResource(R.drawable.gradient_light_orange_24dp)
            }
        }

    }

}
