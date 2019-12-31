package com.sdy.jitangapplication.ui.adapter

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SendMsgBean
import com.sdy.jitangapplication.player.IjkMediaPlayerUtil
import com.sdy.jitangapplication.utils.UriUtils
import kotlinx.android.synthetic.main.item_greet_user_chat_content.view.*
import kotlinx.android.synthetic.main.item_list_square_audio.view.*

class ChatContentAdapter : BaseMultiItemQuickAdapter<SendMsgBean, BaseViewHolder>(mutableListOf()) {

    init {
        addItemType(MsgTypeEnum.audio.value, R.layout.item_greet_user_chat_audio)
        addItemType(MsgTypeEnum.text.value, R.layout.item_greet_user_chat_content)
    }

    override fun convert(holder: BaseViewHolder, item: SendMsgBean) {
        holder.itemView.chatContentIndex.text = "${holder.layoutPosition + 1}/${mData.size}"


        when (holder.itemViewType) {
            MsgTypeEnum.audio.value -> {
                //0未播放  1 播放中 2暂停  3 停止
                //点击播放
                holder.addOnClickListener(R.id.audioPlayBtn)
                val audioTimeView = holder.itemView.audioTime

                if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PLAY) { //播放中
                    holder.itemView.voicePlayView.playAnimation()
                    audioTimeView.startTime(item.leftDuration.toLong(), "3")
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_pause_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PAUSE) {//暂停中
                    holder.itemView.voicePlayView.pauseAnimation()
                    audioTimeView.stopTime()
                    item.leftDuration = UriUtils.stringToTimeInt(audioTimeView.text.toString())
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_STOP || item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_ERROR) {//停止中
                    audioTimeView.stopTime()
                    item.leftDuration = item.duration
                    audioTimeView.text = UriUtils.getShowTime(item.leftDuration)

                    holder.itemView.voicePlayView.pauseAnimation()
                    holder.itemView.voicePlayView.cancelAnimation()
                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                } else if (item.isPlayAudio == IjkMediaPlayerUtil.MEDIA_PREPARE) {
                    audioTimeView.stopTime()
                    item.leftDuration = item.duration
                    audioTimeView.text = UriUtils.getShowTime(item.leftDuration)

                    holder.itemView.voicePlayView.pauseAnimation()
                    holder.itemView.voicePlayView.cancelAnimation()

                    holder.itemView.audioPlayBtn.setImageResource(R.drawable.icon_play_audio)
                }
            }
            else -> {
                holder.itemView.chatContentMsg.text = item.content
            }
        }
    }

}
