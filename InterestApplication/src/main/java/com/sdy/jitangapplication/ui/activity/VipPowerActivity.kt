package com.sdy.jitangapplication.ui.activity

import android.content.Context
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
import org.jetbrains.anko.startActivity
import java.math.BigDecimal

class VipPowerActivity() :
    BaseMvpActivity<VipPowerPresenter>(), VipPowerView {

    companion object {
        //购买类型
        const val PURCHASE_PT_VIP = 100//VIP购买
        const val PURCHASE_CONTACT_CARD = 200//购买联系方式直连卡


        //高级会员
        const val SOURCE_FREE_CHAT = 1//免费聊天
        const val SOURCE_VIDEO_INTRODUCE = 2//免费视频介绍
        const val SOURCE_MORE_EXPODE = 3//提升曝光度
        const val SOURCE_LOCATION_ROAMING = 4//位置漫游
        const val SOURCE_SUPER_VIP_LOGO = 5//高级身份标识
        const val SOURCE_VISITED_ME = 6//查看看过我的
        const val SOURCE_COMMENT_PRIVACY = 7//广场评论权限
        const val SOURCE_FREE_ASSIST = 8//专属客服配套
        const val SOURCE_FREE_DATING = 9//发布约会
        const val SOURCE_BIG_CHARGE = 10//仅显示在线状态


        //直联卡
        const val SOURCE_LOCK_WECHAT = 11//微信直接查看
        const val SOURCE_SUPER_PT_LOGO = 11//尊贵身份
        const val SOURCE_FIRST_RECOMMEND = 12//优先推荐
        const val SOURCE_ONE_TO_ONE_ASSIST = 13//1对1客服

        fun start(context: Context, sourceType: Int, position: Int = 0) {
            context.startActivity<VipPowerActivity>(
                "source_type" to sourceType,
                "position" to position
            )
        }
    }

    private val source_type by lazy { intent.getIntExtra("source_type", -1) }
    private val position by lazy { intent.getIntExtra("position", -1) }
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
        hotT1.text = getString(R.string.vip_power)
        btnBack.onClick { finish() }

        initVp2()
    }

    private val powerPriceAdapter by lazy { AllVipPowerAdapter().apply {
        source_type = this@VipPowerActivity.source_type
    } }
    private val powerInfoAdapter by lazy { PowerInfoAdapter() }

    var lastPosition = false

    private fun initVp2() {
        powerInfoRv.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(powerInfoRv)
        powerInfoRv.adapter = powerInfoAdapter
        powerInfoRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var isPowerInfoRvSlideToLeft = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val last =
                    (powerInfoRv.layoutManager as CenterLayoutManager).findLastCompletelyVisibleItemPosition()
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (last == (powerInfoRv.layoutManager as CenterLayoutManager).itemCount - 1 && isPowerInfoRvSlideToLeft) {
                        powerPriceRv.smoothScrollToPosition(1)
//                        setPriceData(adapter.data[1])
                    } else {
//                        setPriceData(adapter.data[0])
                        powerPriceRv.smoothScrollToPosition(0)
                    }
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isPowerInfoRvSlideToLeft = dx > 0
            }
        })



        powerPriceRv.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(powerPriceRv)
        powerPriceRv.adapter = powerPriceAdapter
        powerPriceRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var ispowerPriceRvSlideToLeft = false
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val last =
                    (powerPriceRv.layoutManager as CenterLayoutManager).findLastCompletelyVisibleItemPosition()
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (last == (powerInfoRv.layoutManager as CenterLayoutManager).itemCount - 1 && ispowerPriceRvSlideToLeft) {
                        powerInfoRv.smoothScrollToPosition(1)
                    } else {
                        powerInfoRv.smoothScrollToPosition(0)
                    }
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                ispowerPriceRvSlideToLeft = dx > 0
//                powerInfoRv.scrollTo(dx,dy)
            }
        })

    }


    private var chargeWayBeans: ChargeWayBeans? = null

    override fun getChargeDataResult(data: ChargeWayBeans?) {
        chargeWayBeans = data
        if (data != null) {
            powerPriceAdapter.threshold_btn = data?.threshold_btn ?: false
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
            powerPriceAdapter.addData(
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
            powerPriceAdapter.addData(
                VipPowerBean(
                    data.direct_icon_list,
                    data.isdirect,
                    data.direct_vip_express,
                    data.direct_save_str,
                    VipPowerBean.TYPE_PT_VIP,
                    data.direct_list ?: mutableListOf(),
                    data.paylist ?: mutableListOf()
                )
            )
            powerInfoAdapter.setNewData(powerPriceAdapter.data)
            setPriceData(powerPriceAdapter.data[position])
            powerInfoRv.scrollToPosition(position)
            powerPriceRv.scrollToPosition(position)

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
        }} ${if (data.isplatinum) {
            getString(R.string.vip_renew)
        } else {
            getString(R.string.vip_buy)
        }}${if (data.type == VipPowerBean.TYPE_GOLD_VIP) {
            getString(R.string.vip_gold)
        } else {
            getString(R.string.vip_connection_card)
        }}"
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
            }} ${if (data.isplatinum) {
                getString(R.string.vip_renew)
            } else {
                getString(R.string.vip_buy)
            }}${if (data.type == VipPowerBean.TYPE_GOLD_VIP) {
                getString(R.string.vip_gold)
            } else {
                getString(R.string.vip_connection_card)
            }}"
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
                ConfirmPayCandyDialog(this, position, data.payway, source_type).show()
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
