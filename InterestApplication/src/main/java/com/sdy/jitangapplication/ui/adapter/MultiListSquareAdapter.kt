package com.sdy.jitangapplication.ui.adapter

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.PagerSnapHelper
import com.airbnb.lottie.LottieAnimationView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.VibrateUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.flexbox.*
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.leochuan.ScaleLayoutManager
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.RefreshDeleteSquareEvent
import com.sdy.jitangapplication.event.UserCenterEvent
import com.sdy.jitangapplication.model.SquareBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.switchplay.SwitchUtil
import com.sdy.jitangapplication.ui.activity.*
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.ui.fragment.MyCollectionAndLikeFragment
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import kotlinx.android.synthetic.main.dialog_more_action_new.*
import kotlinx.android.synthetic.main.item_list_square_audio.view.*
import kotlinx.android.synthetic.main.item_list_square_official.view.*
import kotlinx.android.synthetic.main.item_list_square_pic.view.*
import kotlinx.android.synthetic.main.item_list_square_video.view.*
import kotlinx.android.synthetic.main.layout_square_list_bottom.view.*
import kotlinx.android.synthetic.main.layout_square_list_top.view.*
import kotlinx.android.synthetic.main.switch_video.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity


/**
 *    author : ZFM
 *    date   : 2019/6/2616:27
 *    desc   : 多状态的广场
 *    version: 1.0
 *     playState:Int = -1  //0停止  1播放中  2暂停
 *     playPosition播放的进度
 */
class MultiListSquareAdapter(
    data: MutableList<SquareBean>,
    var playState: Int = -1,
    var playPosition: Int = 0,
    var resetAudioListener: ResetAudioListener? = null,
    var type: Int = MyCollectionAndLikeFragment.TYPE_SQUARE
) :
    BaseMultiItemQuickAdapter<SquareBean, BaseViewHolder>(data) {
    companion object {
        val TAG = "RecyclerView2List"
    }

    var chat: Boolean = true

    init {
        addItemType(SquareBean.PIC, R.layout.item_list_square_pic)
        addItemType(SquareBean.VIDEO, R.layout.item_list_square_video)
        addItemType(SquareBean.AUDIO, R.layout.item_list_square_audio)
        addItemType(SquareBean.OFFICIAL_NOTICE, R.layout.item_list_square_official)
    }


    override fun convert(holder: BaseViewHolder, item: SquareBean) {
        holder.itemView.squareUserName1.text = item.nickname ?: ""
        GlideUtil.loadAvatorImg(mContext, item.avatar, holder.itemView.squareUserIv1)

        holder.itemView.squareOfficialTv.isVisible =
            holder.itemViewType == SquareBean.OFFICIAL_NOTICE
        holder.itemView.squareChatBtn1.isVisible = holder.itemViewType != SquareBean.OFFICIAL_NOTICE
        holder.itemView.squareTime.isVisible = holder.itemViewType != SquareBean.OFFICIAL_NOTICE
        if (holder.itemViewType == SquareBean.OFFICIAL_NOTICE) {
            holder.itemView.onClick {
                mContext.startActivity<ProtocolActivity>(
                    "type" to ProtocolActivity.TYPE_OTHER,
                    "url" to "${item.link_url ?: ""}"
                )
            }

            holder.itemView.squareOfficialContent.text = "${item.descr}"
            val layoutParams =
                holder.itemView.squareOfficialPic.layoutParams as LinearLayout.LayoutParams
            layoutParams.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(30F)
            layoutParams.height = layoutParams.width
            layoutParams.leftMargin = SizeUtils.dp2px(15F)
            layoutParams.rightMargin = SizeUtils.dp2px(15F)
            holder.itemView.squareOfficialPic.layoutParams = layoutParams
            GlideUtil.loadRoundImgCenterCrop(
                mContext,
                item.cover_url,
                holder.itemView.squareOfficialPic,
                SizeUtils.dp2px(5F)
            )
        } else {
//            holder.itemView.headSquareView.isVisible = type != MySquareFragment.TYPE_OTHER_DETAIL
            holder.itemView.view.isVisible = holder.layoutPosition - headerLayoutCount != 0


            //设置点赞状态
            setLikeStatus(
                item.isliked,
                item.like_cnt,
                holder.itemView.squareDianzanBtn1,
                holder.itemView.squareDianzanAni,
                false
            )
            //为自己，不能聊天（用户详情界面），未开启招呼，非好友   聊天按钮不可见
            if (item.isfriend)
                holder.itemView.squareChatBtn1.visibility = View.VISIBLE
            else
                holder.itemView.squareChatBtn1.visibility =
                    if (UserManager.getAccid() == item.accid || !item.greet_switch || !chat) {
                        View.INVISIBLE
                    } else {
                        View.VISIBLE
                    }

            if (item.descr.isNullOrEmpty()) {
                holder.itemView.squareContent1.visibility = View.GONE
            } else {
                holder.itemView.squareContent1.visibility = View.VISIBLE
                holder.itemView.squareContent1.setContent(item.descr)
            }
            if (item.approve_type != 0) {
                holder.itemView.squareTagLl.isVisible = false
                holder.itemView.squareLocationAndTime1Ll.isVisible = false
                holder.itemView.squareTagName.isVisible = false
                holder.itemView.squareUserSweetLogo.isVisible = true
                holder.itemView.squareSweetVerifyContentCl.isVisible = true
                val params =
                    holder.itemView.squareSweetVerifyContentCl.layoutParams as ConstraintLayout.LayoutParams
                params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
                params.height = (params.width * (177 / 1035F)).toInt()

                //// 0普通 1资产认证 2豪车认证 3 身材认证 4 职业认证  5充值认证
                if (item.approve_type == 1 || item.approve_type == 2 || item.approve_type == 5) {
                    holder.itemView.squareContent1.setTextColor(Color.parseColor("#FFFFCD52"))
                    if (CommonFunction.isEnglishLanguage()) {
                        holder.itemView.squareUserSweetLogo.imageAssetsFolder =
                            "images_sweet_logo_man_en"
                        holder.itemView.squareUserSweetLogo.setAnimation("data_sweet_logo_man_en.json")
                    } else {

                        holder.itemView.squareUserSweetLogo.imageAssetsFolder =
                            "images_sweet_logo_man"
                        holder.itemView.squareUserSweetLogo.setAnimation("data_sweet_logo_man.json")
                    }
                    holder.itemView.squareUserSweetLogo.playAnimation()
                } else {
                    if (CommonFunction.isEnglishLanguage()) {
                        holder.itemView.squareUserSweetLogo.imageAssetsFolder =
                            "images_sweet_logo_woman_en"
                        holder.itemView.squareUserSweetLogo.setAnimation("data_sweet_logo_woman_en.json")
                    } else {
                        holder.itemView.squareContent1.setTextColor(Color.parseColor("#FFFF7CA8"))
                        holder.itemView.squareUserSweetLogo.imageAssetsFolder =
                            "images_sweet_logo_woman"
                    }
                    holder.itemView.squareUserSweetLogo.setAnimation("data_sweet_logo_woman.json")
                    holder.itemView.squareUserSweetLogo.playAnimation()
                }

                holder.itemView.squareSweetVerifyName.text = item.assets_audit_descr
                holder.itemView.squareSweetVerifyContent.text = when (item.approve_type) {
                    1 -> {
                        mContext.getString(R.string.sweet_rich_user)
                    }
                    2 -> {
                        mContext.getString(R.string.sweet_luxury_car)
                    }
                    3 -> {
                        mContext.getString(R.string.sweet_good_shencai)
                    }
                    4 -> {
                        mContext.getString(R.string.sweet_job)
                    }
                    6 -> {
                        mContext.getString(R.string.sweet_education)
                    }
                    else -> {
                        ""
                    }
                }

                //// 0普通 1资产认证 2豪车认证 3 身材认证 4 职业认证  5高额充值
                holder.itemView.squareSweetVerifyContentCl.setBackgroundResource(
                    when (item.approve_type) {
                        1, 2, 5 -> {
                            R.drawable.icon_sweet_type_man
                        }
                        else -> {
                            R.drawable.icon_sweet_type_woman
                        }
                    }
                )
            } else {
                holder.itemView.squareUserSweetLogo.isVisible = false
                holder.itemView.squareSweetVerifyContentCl.isVisible = false
                holder.itemView.squareContent1.setTextColor(Color.parseColor("#FF191919"))

                if (item.puber_address.isNullOrEmpty()) {
                    holder.itemView.squareLocationAndTime1Ll.visibility = View.INVISIBLE
                } else {
                    holder.itemView.squareLocationAndTime1Ll.isVisible = true
                }

                if (!item.tags.isNullOrEmpty()) {
                    holder.itemView.squareTagName.text = item.tags
                    holder.itemView.squareTagLl.isVisible = true
                } else {
                    holder.itemView.squareTagLl.isVisible = false
                }

            }

            holder.itemView.squareCommentBtn1.text = "${item.comment_cnt}"
            holder.itemView.squareUserVipIv1.isVisible =
                (item.isplatinumvip || item.isdirectvip)


            if (item.isplatinumvip) {
                holder.itemView.squareUserVipIv1.setImageResource(R.drawable.icon_vip)
            } else if (item.isdirectvip) {
                holder.itemView.squareUserVipIv1.setImageResource(R.drawable.icon_direct_vip)
            }
            holder.itemView.squareLocation.text = "${item.puber_address}"

            holder.itemView.squareTime.text = item.out_time

            //点击跳转评论详情
            holder.itemView.squareCommentBtn1.onClick {
                if (resetAudioListener != null) {
                    resetAudioListener!!.resetAudioState()
                }
                if (UserManager.touristMode) {
                    TouristDialog(mContext).show()
                } else
                    SquareCommentDetailActivity.start(
                        mContext!!,
                        item,
                        position = holder.layoutPosition - headerLayoutCount, squareId = item.id,
                        type = if (item.approve_type != 0) {
                            SquareCommentDetailActivity.TYPE_SWEET
                        } else {
                            SquareCommentDetailActivity.TYPE_SQUARE
                        },
                        gender = item.gender
                    )
            }
            //更多弹窗
            holder.itemView.squareMoreBtn1.onClick {
                if (UserManager.touristMode) {
                    TouristDialog(mContext).show()
                } else
                    showMoreDialog(holder.layoutPosition - headerLayoutCount)
            }
            //点击转发
            holder.itemView.squareZhuanfaBtn1.clickWithTrigger {
                if (UserManager.touristMode) {
                    TouristDialog(mContext).show()
                } else
                    showTranspondDialog(item)
            }

            //进入聊天界面
            holder.itemView.squareChatBtn1.clickWithTrigger {
                if (resetAudioListener != null) {
                    resetAudioListener!!.resetAudioState()
                }
                if (UserManager.touristMode) {
                    TouristDialog(mContext).show()
                } else
                    CommonFunction.checkChat(mContext, item.accid)

            }

            //点赞
//            holder.itemView.squareDianzanBtn1.onClick {
//                clickZan(
//                    holder.itemView.squareDianzanAni,
//                    holder.itemView.squareDianzanImg,
//                    holder.itemView.squareDianzanBtn1,
//                    holder.layoutPosition - headerLayoutCount
//                )
//            }
            holder.itemView.squareDianzanAni.onClick {
                if (UserManager.touristMode) {
                    TouristDialog(mContext).show()
                } else
                    clickZan(
                        holder.itemView.squareDianzanAni,
                        holder.itemView.squareDianzanBtn1,
                        holder.layoutPosition - headerLayoutCount
                    )
            }


            //标题跳转
            holder.itemView.squareTitleRv.isVisible =
                !item.title_list.isNullOrEmpty() && item.approve_type == 0
            val manager = FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP)
            manager.alignItems = AlignItems.STRETCH
            manager.justifyContent = JustifyContent.FLEX_START
            holder.itemView.squareTitleRv.layoutManager = manager
            val adapter = SquareTitleAdapter()
            adapter.addData(item.title_list ?: mutableListOf())
            holder.itemView.squareTitleRv.adapter = adapter
            adapter.setOnItemClickListener { _, view, position ->
                if (UserManager.touristMode) {
                    TouristDialog(mContext).show()
                } else
                    mContext.startActivity<TagDetailCategoryActivity>(
                        "id" to adapter.data[position].id,
                        "type" to TagDetailCategoryActivity.TYPE_TOPIC
                    )
            }

            holder.itemView.squareTagName.onClick {
                if (UserManager.touristMode) {
                    TouristDialog(mContext).show()
                } else
                    mContext.startActivity<TagDetailCategoryActivity>(
                        "id" to item.tag_id,
                        "type" to TagDetailCategoryActivity.TYPE_TAG
                    )
            }

            holder.itemView.squareUserIv1.onClick {
                if (UserManager.touristMode) {
                    TouristDialog(mContext).show()
                } else
                    if (!(UserManager.getAccid() == item.accid || !chat)) {
                        MatchDetailActivity.start(mContext, item.accid)
                    }
            }

            when (holder.itemViewType) {
                SquareBean.PIC -> {
                    if (item.photo_json != null && item.photo_json!!.size > 0) {
                        holder.itemView.squareUserPics1.visibility = View.VISIBLE
                        holder.itemView.squareUserPics1.layoutManager =
                            ScaleLayoutManager(mContext, 0)
                        val adapter =
                            ListSquareImgsAdapter(mContext, item.photo_json ?: mutableListOf())
                        holder.itemView.squareUserPics1.adapter = adapter

                        //分页滑动效果
                        holder.itemView.squareUserPics1.onFlingListener = null
                        PagerSnapHelper().attachToRecyclerView(holder.itemView.squareUserPics1)
                        //滑动动画
//                        holder.itemView.squareUserPics1.addOnScrollListener(GalleryOnScrollListener())
                        adapter.setOnItemClickListener { adapter, view, position ->
                            if (UserManager.touristMode) {
                                TouristDialog(mContext).show()
                            } else {
                                if (data[holder.layoutPosition - headerLayoutCount].isliked != 1)
                                    clickZan(
                                        holder.itemView.squareDianzanAni,
                                        holder.itemView.squareDianzanBtn1,
                                        holder.layoutPosition - headerLayoutCount
                                    )
                                if (resetAudioListener != null) {
                                    resetAudioListener!!.resetAudioState()
                                }
                                mContext.startActivity<SquarePlayListDetailActivity>(
                                    "item" to data[holder.layoutPosition - headerLayoutCount],
                                    "picPosition" to position
                                )
                            }
                        }
                    } else {
                        holder.itemView.squareUserPics1.visibility = View.GONE
                    }

                }
                SquareBean.VIDEO -> {
                    //增加封面
                    val imageview = ImageView(mContext)
                    GlideUtil.loadRoundImgCenterCrop(
                        mContext,
                        item.cover_url ?: "",
                        imageview,
                        SizeUtils.dp2px(15F)
                    )
                    if (imageview.parent != null) {
                        val vg = imageview.parent as ViewGroup
                        vg.removeView(imageview)
                    }
                    holder.itemView.squareUserVideo.thumbImageView = imageview
                    holder.itemView.squareUserVideo.detail_btn.setOnClickListener {
                        if (UserManager.touristMode) {
                            TouristDialog(mContext).show()
                        } else
                            if (holder.itemView.squareUserVideo.isInPlayingState) {
                                SwitchUtil.savePlayState(holder.itemView.squareUserVideo)
                                holder.itemView.squareUserVideo.gsyVideoManager.setLastListener(
                                    holder.itemView.squareUserVideo
                                )
                                SquarePlayDetailActivity.startActivity(
                                    mContext as Activity,
                                    holder.itemView.squareUserVideo,
                                    item,
                                    holder.layoutPosition,
                                    type
                                )
                            }
                    }
                    holder.itemView.squareUserVideo.playTag = TAG
                    holder.itemView.squareUserVideo.playPosition = holder.layoutPosition
                    holder.itemView.squareUserVideo.setVideoAllCallBack(object :
                        GSYSampleCallBack() {
                        override fun onStartPrepared(url: String?, vararg objects: Any?) {
                            super.onStartPrepared(url, *objects)
                            if (resetAudioListener != null) {
                                resetAudioListener!!.resetAudioState()
                            }
                        }

                        override fun onPrepared(url: String?, vararg objects: Any?) {
                            if (!holder.itemView.squareUserVideo.isIfCurrentIsFullscreen && type != MyCollectionAndLikeFragment.TYPE_SQUARE_COMMENT) {
                                //静音
                                GSYVideoManager.instance().isNeedMute = true
                            }

                        }

                        override fun onQuitFullscreen(url: String?, vararg objects: Any?) {
                            super.onQuitFullscreen(url, *objects)
                            //退出全屏静音
                            if (type != MyCollectionAndLikeFragment.TYPE_SQUARE_COMMENT)
                                GSYVideoManager.instance().isNeedMute = true
                        }

                        override fun onEnterFullscreen(url: String?, vararg objects: Any?) {
                            super.onEnterFullscreen(url, *objects)
                            GSYVideoManager.instance().isNeedMute = false
                        }

                    })

                    SwitchUtil.optionPlayer(
                        holder.itemView.squareUserVideo,
                        item.video_json?.get(0)?.url ?: "",
                        true
                    )
                    holder.itemView.squareUserVideo.setUp(
                        item.video_json?.get(0)?.url ?: "",
                        false,
                        null,
                        null,
                        ""
                    )
                    if (type == MyCollectionAndLikeFragment.TYPE_SQUARE_COMMENT) {
                        holder.itemView.squareUserVideo.startPlayLogic()
                    }
                }
                SquareBean.AUDIO -> {
                    //点击播放
                    holder.addOnClickListener(R.id.audioPlayBtn)
                    val audioTimeView = holder.itemView.audioTime

                    if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PLAY) { //播放中
                        holder.itemView.voicePlayView.playAnimation()

                        audioTimeView.startTime(
                            (item.audio_json?.get(0)?.leftTime ?: 0).toLong(),
                            "3"
                        )
                        holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_pause_audio)
                    } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PAUSE) {//暂停中
                        holder.itemView.voicePlayView.pauseAnimation()
                        audioTimeView.stopTime()
                        item.audio_json?.get(0)?.leftTime =
                            UriUtils.stringToTimeInt(audioTimeView.text.toString())
                        holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                    } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_STOP || item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_ERROR) {//停止中
                        audioTimeView.stopTime()
                        item.audio_json?.get(0)?.leftTime = item.audio_json?.get(0)?.duration ?: 0
                        audioTimeView.text =
                            UriUtils.getShowTime(item.audio_json?.get(0)?.leftTime ?: 0)

                        holder.itemView.voicePlayView.pauseAnimation()
                        holder.itemView.voicePlayView.cancelAnimation()
                        holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                    } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PREPARE) {
                        audioTimeView.stopTime()
                        item.audio_json?.get(0)?.leftTime = item.audio_json?.get(0)?.duration ?: 0
                        audioTimeView.text =
                            UriUtils.getShowTime(item.audio_json?.get(0)?.leftTime ?: 0)

                        holder.itemView.voicePlayView.pauseAnimation()
                        holder.itemView.voicePlayView.cancelAnimation()

                        holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                    }
                    holder.itemView.audioRecordLl.onClick {
                        if (resetAudioListener != null) {
                            resetAudioListener!!.resetAudioState()
                        }
                        if (UserManager.touristMode) {
                            TouristDialog(mContext).show()
                        } else
                            mContext.startActivity<SquarePlayListDetailActivity>(
                                "item" to item,
                                "from" to "squareFragment"
                            )
                    }
                }
            }

        }
    }


    /**
     * 设置点赞状态
     */
    private fun setLikeStatus(
        isliked: Int,
        likeCount: Int,
        likeView: TextView,
        likeAni: LottieAnimationView,
        animated: Boolean = true
    ) {

        if (isliked == 1) {
            if (animated) {
                likeAni.playAnimation()
                VibrateUtils.vibrate(50L)
            } else {
                likeAni.progress = 1F
            }
        } else {
            likeAni.progress = 0F
        }

        likeView.text = "${if (likeCount < 0) {
            0
        } else {
            likeCount
        }}"
    }

    /**
     * 点赞按钮
     */
    private fun clickZan(
        likeAni: LottieAnimationView,
        likeBtn: TextView,
        position: Int
    ) {
        if (data[position].isliked == 1) {
            data[position].isliked = 0
            data[position].like_cnt = data[position].like_cnt!!.minus(1)
        } else {
            data[position].isliked = 1
            data[position].like_cnt = data[position].like_cnt!!.plus(1)
        }
        setLikeStatus(data[position].isliked, data[position].like_cnt, likeBtn, likeAni)

        likeBtn.postDelayed({
            if (data.isEmpty() || data.size - 1 < position)
                return@postDelayed
            if (data[position].originalLike == data[position].isliked) {
                return@postDelayed
            }
            val params = hashMapOf<String, Any>(
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "type" to if (data[position].isliked == 0) {
                    2
                } else {
                    1
                },
                "square_id" to data[position].id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            getSquareLike(params, position)
        }, 2000L)


    }


    /**
     * 点赞 取消点赞
     * 1 点赞 2取消点赞
     */
    fun getSquareLike(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareLike(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        onGetSquareLikeResult(position, true)
                    } else if (t.code == 403) {
                        UserManager.startToLogin(mContext as Activity)
                    } else {
                        onGetSquareLikeResult(position, false)
                    }

                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(mContext.getString(R.string.service_error))
                    }
                }
            })
    }

    /**
     * 点赞结果
     */
    private fun onGetSquareLikeResult(position: Int, success: Boolean) {
        if (success) {
            data[position].originalLike = data[position].isliked
        } else {
            data[position].isliked = data[position].originalLike
            data[position].like_cnt = data[position].originalLikeCount
//            refreshNotifyItemChanged(position)

        }
    }


    /**
     * 展示转发动态对话框
     */
    private fun showTranspondDialog(squareBean: SquareBean) {
        val transpondDialog = TranspondDialog(mContext, squareBean)
        transpondDialog.show()
    }


    lateinit var moreActionDialog: MoreActionNewDialog

    /**
     * 展示更多操作对话框
     */
    private fun showMoreDialog(position: Int) {
        moreActionDialog = MoreActionNewDialog(mContext, data[position])
        moreActionDialog.show()

        if (data[position]?.iscollected == 0) {
            moreActionDialog.collect.text = mContext.getString(R.string.collect)
            val top = mContext.resources.getDrawable(R.drawable.icon_collect1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        } else {
            moreActionDialog.collect.text = mContext.getString(R.string.cancel_collect)
            val top = mContext.resources.getDrawable(R.drawable.icon_collected1)
            moreActionDialog.collect.setCompoundDrawablesWithIntrinsicBounds(null, top, null, null)
        }
        if (data[position].accid == UserManager.getAccid()) {
            moreActionDialog.delete.visibility = View.VISIBLE
            moreActionDialog.report.visibility = View.GONE
            moreActionDialog.collect.visibility = View.GONE
        } else {
            moreActionDialog.delete.visibility = View.GONE
            moreActionDialog.report.visibility = View.VISIBLE
            moreActionDialog.collect.visibility = View.VISIBLE
        }
        moreActionDialog.delete.onClick {
            val params = hashMapOf<String, Any>(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "square_id" to data[position].id!!
            )
            removeMySquare(params, position)
            moreActionDialog.dismiss()

        }


        moreActionDialog.collect.onClick {

            //发起收藏请求
            val params = hashMapOf<String, Any>(
                "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                "type" to if (data[position].iscollected == 0) {
                    1
                } else {
                    2
                },
                "square_id" to data[position].id!!,
                "_timestamp" to System.currentTimeMillis()
            )
            getSquareCollect(params, position)
        }
        moreActionDialog.report.onClick {
            val dialog = DeleteDialog(mContext)
            dialog.show()
            dialog.title.text = mContext.getString(R.string.report_square_title)
            dialog.tip.text = mContext.getString(R.string.report_square)
            dialog.confirm.text = mContext.getString(R.string.report)
            dialog.cancel.onClick { dialog.dismiss() }
            dialog.confirm.onClick {
                dialog.dismiss()
                //发起举报请求
                val params = hashMapOf<String, Any>(
                    "accid" to SPUtils.getInstance(Constants.SPNAME).getString("accid"),
                    "token" to SPUtils.getInstance(Constants.SPNAME).getString("token"),
                    "type" to if (data[position].iscollected == 0) {
                        1
                    } else {
                        2
                    },
                    "square_id" to data[position].id!!,
                    "_timestamp" to System.currentTimeMillis()
                )
                getSquareReport(params, position)
            }
            moreActionDialog.dismiss()


        }
//        moreActionDialog.cancel.onClick {
//            moreActionDialog.dismiss()
//        }

    }


    /**
     * 广场删除
     */
    fun removeMySquare(params: HashMap<String, Any>, position: Int) {

        RetrofitFactory.instance.create(Api::class.java)
            .removeMySquare(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        onRemoveMySquareResult(true, position)
                    else if (t.code == 403) {
                        UserManager.startToLogin(mContext as Activity)
                    } else {
                        CommonFunction.toast(t.msg)
                        onRemoveMySquareResult(false, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(mContext.getString(R.string.service_error))
                        onRemoveMySquareResult(false, position)
                    }
                }
            })
    }


    /**
     * 收藏
     * 1 收藏 2取消收藏
     */
    fun getSquareCollect(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareCollect(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        onGetSquareCollectResult(position, t)
                    else if (t.code == 403) {
                        UserManager.startToLogin(mContext as Activity)

                    } else {
                        onGetSquareCollectResult(position, t)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(mContext.getString(R.string.service_error))
                        onGetSquareCollectResult(position, null)
                    }
                }
            })
    }


    /**
     * 广场举报
     */
    fun getSquareReport(params: HashMap<String, Any>, position: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .getSquareReport(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        onGetSquareReport(t, position)
                    else if (t.code == 403) {
                        UserManager.startToLogin(mContext as Activity)

                    } else {
                        onGetSquareReport(t, position)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(mContext).show()
                    } else {
                        CommonFunction.toast(mContext.getString(R.string.service_error))
                        onGetSquareReport(null, position)
                    }
                }
            })
    }

    fun onRemoveMySquareResult(result: Boolean, position: Int) {
        if (result) {
            if (data[position].type == SquareBean.AUDIO && resetAudioListener != null) {
                resetAudioListener!!.resetAudioState()
            } else if (data[position].type == SquareBean.VIDEO) {
                GSYVideoManager.releaseAllVideos()
            }
            EventBus.getDefault().post(RefreshDeleteSquareEvent(data[position].id ?: 0))
            data.removeAt(position)
            notifyItemRemoved(position + headerLayoutCount)
            EventBus.getDefault().postSticky(UserCenterEvent(true))
        }
    }


    fun onGetSquareCollectResult(position: Int, data1: BaseResp<Any?>?) {
        if (data1 != null) {
            CommonFunction.toast(data1.msg)
            if (data1.code == 200) {
                if (data[position].iscollected == 1) {
                    data[position].iscollected = 0
                } else {
                    data[position].iscollected = 1
                }
//                refreshNotifyItemChanged(position)
            }
        }
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int) {
        if (baseResp != null)
            CommonFunction.toast(baseResp.msg)
        if (moreActionDialog != null && moreActionDialog.isShowing) {
            moreActionDialog.dismiss()
        }
    }

    interface ResetAudioListener {
        fun resetAudioState()
    }


}
