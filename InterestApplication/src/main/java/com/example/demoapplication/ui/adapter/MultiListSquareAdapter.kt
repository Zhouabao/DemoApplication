package com.example.demoapplication.ui.adapter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.demoapplication.R
import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.ui.activity.SquarePlayDetailActivity
import com.example.demoapplication.ui.dialog.TranspondDialog
import com.kotlin.base.ext.onClick
import kotlinx.android.synthetic.main.item_list_square_audio.view.*
import kotlinx.android.synthetic.main.item_list_square_pic.view.*
import kotlinx.android.synthetic.main.item_list_square_pic.view.squareDianzanBtn
import kotlinx.android.synthetic.main.item_list_square_pic.view.squareZhuanfaBtn
import kotlinx.android.synthetic.main.item_list_square_video.view.*
import org.jetbrains.anko.startActivity


/**
 *    author : ZFM
 *    date   : 2019/6/2616:27
 *    desc   : 多状态的广场
 *    version: 1.0
 */
class MultiListSquareAdapter(var context: Context, data: MutableList<MatchBean>) :
    BaseMultiItemQuickAdapter<MatchBean, BaseViewHolder>(data) {

    init {
        addItemType(MatchBean.PIC, R.layout.item_list_square_pic)
        addItemType(MatchBean.VIDEO, R.layout.item_list_square_video)
        addItemType(MatchBean.AUDIO, R.layout.item_list_square_audio)
    }

    override fun convert(holder: BaseViewHolder, item: MatchBean) {
        val drawable1 =
            context.resources.getDrawable(if (item.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
        drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
        holder.itemView.squareDianzanBtn.setCompoundDrawables(drawable1, null, null, null)

        holder.itemView.squareDianzanBtn.onClick {
            item.zan = !item.zan
            val drawable1 =
                context.resources.getDrawable(if (item.zan) R.drawable.icon_dianzan_red else R.drawable.icon_dianzan)
            drawable1!!.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)    //需要设置图片的大小才能显示
            holder.itemView.squareDianzanBtn.setCompoundDrawables(drawable1, null, null, null)
        }
        holder.itemView.squareZhuanfaBtn.onClick {
            showTranspondDialog()
        }


        when (holder.itemViewType) {
            MatchBean.PIC -> {
                holder.itemView.squareUserPics.layoutManager =
                    LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                val adapter = ListSquareImgsAdapter(context, item.imgs)
                holder.itemView.squareUserPics.adapter = adapter
                adapter.setOnItemClickListener(object : ListSquareImgsAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        context.startActivity<SquarePlayDetailActivity>()
                    }

                })
//                holder.itemView.squareUserPics.onClick {
//                    context.startActivity<SquarePlayDetailActivity>()
//                }

            }
            MatchBean.VIDEO -> {
                holder.itemView.squareUserVideo.onClick {
                    context.startActivity<SquarePlayDetailActivity>()
                }

//                holder.itemView.squareUserVideo.backButton.visibility = View.GONE
//                holder.itemView.squareUserVideo.fullscreenButton.onClick {
//                    holder.itemView.squareUserVideo.startWindowFullscreen(context, false, true)
//                }
//                holder.itemView.squareUserVideo.isReleaseWhenLossAudio = false
//                holder.itemView.squareUserVideo.setIsTouchWiget(false)
//                holder.itemView.squareUserVideo.isShowFullAnimation = true
            }
            MatchBean.AUDIO -> {
                holder.itemView.squareUserAudio.onClick {
                    context.startActivity<SquarePlayDetailActivity>()

                }
            }
        }

    }

    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog() {
        TranspondDialog(context).show()
    }
}
