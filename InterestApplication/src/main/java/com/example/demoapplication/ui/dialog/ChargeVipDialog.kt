package com.example.demoapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.bigkoo.convenientbanner.ConvenientBanner
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator
import com.example.demoapplication.R
import com.example.demoapplication.model.BannerBean
import com.example.demoapplication.ui.adapter.BannerHolderView
import com.kotlin.base.ext.onClick
import kotlinx.android.synthetic.main.dialog_charge_vip.*

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 充值会员底部对话框
 *    version: 1.0
 */
class ChargeVipDialog(context: Context) : Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_charge_vip)
        initWindow()
        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
    }


    private fun initData(): MutableList<BannerBean> {
        return mutableListOf(
            BannerBean("无限滑动次数", R.drawable.img_avatar_01, "滑动次数不设限，散播更多你更多的爱"),
            BannerBean("会员标识", R.drawable.img_avatar_02, "用户名后追加会员标识，更容易获取配对"),
            BannerBean("独享筛选", R.drawable.img_avatar_03, "筛选你身边的用户找到最对的人"),
            BannerBean("看过我的", R.drawable.img_avatar_04, "看到所有看过你的人"),
            BannerBean("招呼次数x2", R.drawable.img_avatar_05, "24小时打招呼次数翻倍，畅所欲言吧"),
            BannerBean("对我感兴趣的", R.drawable.img_avatar_06, "查看对你感兴趣的人，直接反选配对")
        )
    }


    private var position: Int = -1
    private fun initView() {
        (bannerVip as ConvenientBanner<BannerBean>).setPages(object : CBViewHolderCreator {
            override fun createHolder(itemView: View): BannerHolderView {
                return BannerHolderView(itemView, context)
            }

            override fun getLayoutId(): Int {
                return R.layout.item_vip_banner
            }

        }, initData())
            .setPageIndicator(intArrayOf(R.drawable.shape_oval_gray, R.drawable.shape_oval_white))
            .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
            .startTurning(2000)



        vipOneMonth.onClick {
            if (position != 0) {
                vipOneMonth.setBackgroundResource(R.drawable.shape_rectangle_orange)
                vipThreeMonth.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
                vipOneYear.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
                position = 0
            }
        }

        vipThreeMonth.onClick {
            if (position != 1) {
                vipThreeMonth.setBackgroundResource(R.drawable.shape_rectangle_orange)
                vipOneMonth.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
                vipOneYear.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
                position = 1
            }
        }

        vipOneYear.onClick {
            if (position != 2) {
                vipOneYear.setBackgroundResource(R.drawable.shape_rectangle_orange)
                vipOneMonth.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
                vipThreeMonth.setBackgroundResource(R.drawable.shape_rectangle_gray_vip)
                position = 2
            }
        }


    }

}