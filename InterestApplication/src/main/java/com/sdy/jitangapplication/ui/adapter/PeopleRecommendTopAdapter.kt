package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.IndexTopBean
import com.sdy.jitangapplication.ui.activity.MyVisitActivity
import com.sdy.jitangapplication.ui.dialog.ChargePtVipDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_people_recommend_top_content.view.*
import kotlinx.android.synthetic.main.item_people_recommend_top_title.view.*
import org.jetbrains.anko.startActivity


/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   : 首页顶部推荐人员
 *    version: 1.0
 */
class PeopleRecommendTopAdapter :
    BaseMultiItemQuickAdapter<IndexTopBean, BaseViewHolder>(mutableListOf()) {
    init {
        addItemType(0, R.layout.item_people_recommend_top_title)
        addItemType(1, R.layout.item_people_recommend_top_content)
    }

    var isplatinum: Boolean = false
    var free_show: Boolean = false
    var mv_url: Boolean = false
    var todayvisit: Int = 0
    var todayExplosure: Int = 0
    var total_exposure_cnt: Int = 0
    var allvisit: Int = 0


    override fun convert(helper: BaseViewHolder, item: IndexTopBean) {

        when (helper.itemViewType) {
            0 -> {
                val itemView = helper.itemView
                if ((UserManager.getGender() == 1 && isplatinum) || (UserManager.getGender() == 2 && mv_url)) {
                    itemView.choicenessCount.text = "${total_exposure_cnt}人通过精选\n用户看到了你"
                    itemView.openVipBtn.text = "谁看过我"
                    itemView.openVipBtn.setBackgroundResource(R.drawable.shape_rectangle_orange_24dp)
                    itemView.openVipBtn.clickWithTrigger {
                        mContext.startActivity<MyVisitActivity>(
                            "today" to todayvisit,
                            "todayExplosure" to todayExplosure,
                            "all" to allvisit,
                            "freeShow" to free_show,
                            "from" to MyVisitActivity.FROM_TOP_RECOMMEND
                        )
                    }
                } else {
                    itemView.openVipBtn.setBackgroundResource(R.drawable.gradient_orange_10dp)
                    if (UserManager.getGender() == 1) {
                        itemView.choicenessCount.text = "成为“精选”用户\n获取更多聊天"
                        itemView.openVipBtn.text = "升级会员"
                        itemView.openVipBtn.clickWithTrigger {
                            ChargePtVipDialog(
                                ChargePtVipDialog.UPDATE_VISIT,
                                mContext,
                                ChargePtVipDialog.PURCHASE_PT_VIP
                            ).show()
                        }
                    } else {
                        itemView.choicenessCount.text = "成为“精选”用户\n获取更多聊天"
                        itemView.openVipBtn.text = "上传视频"
                        itemView.openVipBtn.clickWithTrigger {
                            CommonFunction.startToVideoIntroduce(mContext)
                        }
                    }
                }
            }

            1 -> {
                val itemView = helper.itemView
                val params = itemView.layoutParams as RecyclerView.LayoutParams
                if (helper.layoutPosition == mData.size - 1) {
                    params.rightMargin = SizeUtils.dp2px(15F)
                }

                GlideUtil.loadRoundImgCenterCrop(
                    mContext,
                    item.avatar,
                    itemView.userAvator,
                    SizeUtils.dp2px(5f)
                )

                itemView.userMvBtn.isVisible = item.source_type == 1
                itemView.userVipLogo.isVisible = item.isplatinum
            }
        }
    }


}
