package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.NearPersonBean
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.ui.adapter.PeopleRecommendTopAdapter
import com.sdy.jitangapplication.ui.dialog.ChooseChoicenessDialog
import com.sdy.jitangapplication.ui.dialog.PurchaseIndexChoicenessDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.activity_index_choiceness.*
import org.jetbrains.anko.startActivity

/**
 * 成为首页精选
 */
class IndexChoicenessActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index_choiceness)
        initView()
    }

    private fun initView() {
        StatusBarUtil.immersive(this)

        backbtn.clickWithTrigger {
            finish()
        }

        //榜首
        initHeadRecommendUser()

        setAutoChoicenessData()


        //todo 判断男性用户是否是钻石会员，女性用户是否上传视频介绍
        //立即获取
        if (UserManager.getGender() == 1) {
            //男性置顶充值钻石会员
            tobeChoicenessBtn.text = "立即获取"
            tobeChoicenessIv.setImageResource(R.drawable.icon_choicess_power_man_bg)
            tobeChoicenessBtn.clickWithTrigger {
                startActivity<VipPowerActivity>("type" to VipPowerBean.TYPE_PT_VIP)
            }
        } else {
            //女性置顶上传视频介绍
            tobeChoicenessBtn.text = "上传视频介绍"
            tobeChoicenessIv.setImageResource(R.drawable.icon_choicenss_woman_bg)
            tobeChoicenessBtn.clickWithTrigger {
                VideoVerifyActivity.start(this)
            }
        }

        //选择置顶日期
        choicenessDate.clickWithTrigger {
            ChooseChoicenessDialog(this).show()
        }

        //立即使用,todo 如果有置顶券就立即使用,没有就弹购买
        useChoicenessTicketBtn.clickWithTrigger {
            ChooseChoicenessDialog(this).show()
        }

        //立即购买 糖果置换置顶券
        purchaseBtn.clickWithTrigger {
            //todo 如果糖果余额不足就弹充值
            PurchaseIndexChoicenessDialog(this).show()
        }
    }

    /**
     * 设置前三名数据
     */
    private fun setAutoChoicenessData() {
        GlideUtil.loadImg(this, "", top1Iv)
        top1Tv.text = ""
        top1Candy.text = ""
        GlideUtil.loadImg(this, "", top2Iv)
        top2Tv.text = ""
        top2Candy.text = ""
        GlideUtil.loadImg(this, "", top3Iv)
        top3Tv.text = ""
        top3Candy.text = ""

        choicenessCandy.text = ""
    }


    private val peopleRecommendTopAdapter by lazy { PeopleRecommendTopAdapter() }
    //初始化榜首数据
    private fun initHeadRecommendUser() {
        indexChoicenessRv.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        indexChoicenessRv.adapter = peopleRecommendTopAdapter
        peopleRecommendTopAdapter.addData(
            NearPersonBean(
                UserManager.getAvator(),
                10,
                distance = "附近",
                gender = 2
            )
        )
        peopleRecommendTopAdapter.addData(
            NearPersonBean(
                UserManager.getAvator(),
                10,
                distance = "附近",
                gender = 2
            )
        )
        peopleRecommendTopAdapter.addData(
            NearPersonBean(
                UserManager.getAvator(),
                10,
                distance = "附近",
                gender = 2
            )
        )
    }

}
