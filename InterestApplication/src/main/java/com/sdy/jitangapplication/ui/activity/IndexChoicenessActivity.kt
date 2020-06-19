package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.FemaleVideoEvent
import com.sdy.jitangapplication.event.UpdateMyTicketEvent
import com.sdy.jitangapplication.event.UpdateTicketDataEvent
import com.sdy.jitangapplication.model.IndexListBean
import com.sdy.jitangapplication.model.TicketBean
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.ui.adapter.PeopleRecommendTopAdapter
import com.sdy.jitangapplication.ui.dialog.ChooseChoicenessDialog
import com.sdy.jitangapplication.ui.dialog.PurchaseIndexChoicenessDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_index_choiceness.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 * 成为首页精选
 */
class IndexChoicenessActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index_choiceness)
        initView()
        getList()
    }

    private fun initView() {
        EventBus.getDefault().register(this)

        StatusBarUtil.immersive(this)

        backbtn.clickWithTrigger {
            finish()
        }

        //榜首
        initHeadRecommendUser()

        setAutoChoicenessData()


        //选择置顶日期
        choicenessDate.clickWithTrigger {
            if (myTicket != null)
                ChooseChoicenessDialog(this).show()
        }

        //立即使用,todo 如果有置顶券就立即使用,没有就弹购买
        useChoicenessTicketBtn.clickWithTrigger {
            if (myTicket?.my_ticket_sum == 0) {
                PurchaseIndexChoicenessDialog(this, myTicket?.ticket).show()
            } else {
                ChooseChoicenessDialog(this).show()
            }
        }

        //立即购买 糖果置换置顶券
        purchaseBtn.clickWithTrigger {
            //todo 如果糖果余额不足就弹充值
            PurchaseIndexChoicenessDialog(this, myTicket?.ticket).show()
        }
    }

    /**
     * 设置前三名数据
     */
    private fun setAutoChoicenessData() {
        if (peopleRecommendTopAdapter.data.size >= 1) {
            GlideUtil.loadImg(this, peopleRecommendTopAdapter.data[0].avatar, top1Iv)
            top1Name.text = peopleRecommendTopAdapter.data[0].nickname
            top1Candy.text = "${peopleRecommendTopAdapter.data[0].amount}"
        }
        if (peopleRecommendTopAdapter.data.size >= 2) {
            GlideUtil.loadImg(this, peopleRecommendTopAdapter.data[1].avatar, top2Iv)
            top2Name.text = peopleRecommendTopAdapter.data[1].nickname
            top2Candy.text = "${peopleRecommendTopAdapter.data[1].amount}"
        }
        if (peopleRecommendTopAdapter.data.size >= 3) {
            GlideUtil.loadImg(this, peopleRecommendTopAdapter.data[2].avatar, top3Iv)
            top3Name.text = peopleRecommendTopAdapter.data[2].nickname
            top3Candy.text = "${peopleRecommendTopAdapter.data[2].amount}"
        }

        choicenessCandy.text = ""
    }


    private val peopleRecommendTopAdapter by lazy { PeopleRecommendTopAdapter() }
    //初始化榜首数据
    private fun initHeadRecommendUser() {
        indexChoicenessRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        indexChoicenessRv.adapter = peopleRecommendTopAdapter
    }


    private var myTicket: TicketBean? = null
    fun getList() {
        RetrofitFactory.instance.create(Api::class.java)
            .getList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<TicketBean?>>(null) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<TicketBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        myTicket = t.data
                        peopleRecommendTopAdapter.setNewData(myTicket?.list)

                        choicenessTitle.text = t.data?.ticket?.title ?: ""
                        choicenessDate.text = t.data?.ticket?.descr ?: ""
                        choicenessCandy.text = "${t.data?.ticket?.amount ?: 0}"

                        choicenessTicketCount.text = "持有${t.data?.my_ticket_sum}张"
                        choicenessTicketTitle.text = t.data?.ticket?.title ?: ""
                        if (t.data?.my_ticket_sum == 0) {
                            useChoicenessTicketBtn.text = "立即购买"
                        } else
                            useChoicenessTicketBtn.text = "立即使用"


                        //todo 判断男性用户是否是钻石会员，女性用户是否上传视频介绍
                        //立即获取
                        if (t?.data?.gender == 1) {
                            if (t?.data?.isplatinum == true) {
                                tobeChoicenessBtn.text = "已获取"
                                tobeChoicenessIv.setImageResource(R.drawable.icon_choicess_power_man_bg)
                                tobeChoicenessBtn.isEnabled = false
                                tobeChoicenessBtn.setBackgroundResource(R.drawable.icon_bg_choiceness_btn_unable)
                            } else {
                                //男性置顶充值钻石会员
                                tobeChoicenessBtn.text = "立即获取"
                                tobeChoicenessIv.setImageResource(R.drawable.icon_choicess_power_man_bg)
                                tobeChoicenessBtn.isEnabled = true
                                tobeChoicenessBtn.setBackgroundResource(R.drawable.icon_bg_choiceness_btn)
                                tobeChoicenessBtn.clickWithTrigger {
                                    startActivity<VipPowerActivity>("type" to VipPowerBean.TYPE_PT_VIP)
                                }
                            }
                        } else {
                            tobeChoicenessIv.setImageResource(R.drawable.icon_choicenss_woman_bg)
                            if (t?.data?.isfaced == 1) {
                                if (t?.data?.my_mv_url == 1) {
                                    tobeChoicenessBtn.text = "已获取"
                                    tobeChoicenessBtn.isEnabled = false
                                    tobeChoicenessBtn.setBackgroundResource(R.drawable.icon_bg_choiceness_btn_unable)
                                } else if (t?.data?.my_mv_url == 2) {
                                    tobeChoicenessBtn.text = "视频审核中"
                                    tobeChoicenessBtn.isEnabled = false
                                    tobeChoicenessBtn.setBackgroundResource(R.drawable.icon_bg_choiceness_btn_unable)
                                } else {
                                    //女性置顶上传视频介绍
                                    tobeChoicenessBtn.text = "上传视频介绍"
                                    tobeChoicenessBtn.isEnabled = true
                                    tobeChoicenessBtn.setBackgroundResource(R.drawable.icon_bg_choiceness_btn)
                                    tobeChoicenessBtn.clickWithTrigger {
                                        VideoVerifyActivity.start(this@IndexChoicenessActivity)
                                    }
                                }
                            } else if (t?.data?.isfaced == 2) {
                                tobeChoicenessBtn.clickWithTrigger {
                                    CommonFunction.toast("请耐心等待认证结果")
                                }
                            } else {
                                tobeChoicenessBtn.clickWithTrigger {
                                    CommonFunction.startToFace(this@IndexChoicenessActivity)
                                }
                            }
                        }

                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateMyTicketEvent(eventBus: UpdateMyTicketEvent) {
        myTicket?.my_ticket_sum = (myTicket?.my_ticket_sum ?: 0) + eventBus.ticketCount
        choicenessTicketCount.text = "持有${(myTicket?.my_ticket_sum ?: 0)}张"

        if (myTicket?.my_ticket_sum == 0) {
            useChoicenessTicketBtn.text = "立即购买"
        } else
            useChoicenessTicketBtn.text = "立即使用"
    }

    /**
     * 充值成功刷新页面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUpdateTicketDataEvent(eventBus: UpdateTicketDataEvent) {
        getList()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onFemaleVideoEvent(eventBus: FemaleVideoEvent) {
        myTicket?.my_mv_url = eventBus.videoState
        if (myTicket?.my_mv_url == 1) {
            tobeChoicenessBtn.text = "已获取"
            tobeChoicenessBtn.isEnabled = false
            tobeChoicenessBtn.setBackgroundResource(R.drawable.icon_bg_choiceness_btn_unable)
        } else if (myTicket?.my_mv_url == 2) {
            tobeChoicenessBtn.text = "视频审核中"
            tobeChoicenessBtn.isEnabled = false
            tobeChoicenessBtn.setBackgroundResource(R.drawable.icon_bg_choiceness_btn_unable)
        } else {
            //女性置顶上传视频介绍
            tobeChoicenessBtn.text = "上传视频介绍"
            tobeChoicenessBtn.isEnabled = true
            tobeChoicenessBtn.setBackgroundResource(R.drawable.icon_bg_choiceness_btn)
            tobeChoicenessBtn.clickWithTrigger {
                VideoVerifyActivity.start(this@IndexChoicenessActivity)
            }
        }
    }

}
