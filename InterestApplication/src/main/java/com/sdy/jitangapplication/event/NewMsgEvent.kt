package com.sdy.jitangapplication.event

import com.netease.nimlib.sdk.msg.model.IMMessage
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/6/2516:30
 *    desc   :显示新消息的event
 *    version: 1.0
 */

//首页更新红点消息
class GetNewMsgEvent

class ShowPublishAndDatingEvent(val show: Boolean)


//重新认证事件通知
class ReVerifyEvent(val type: Int, var avator: String = UserManager.getAvator())


//账号异常认证事件通知
class AccountDangerEvent(val type: Int)

class ForceFaceEvent(val type: Int)

//更新首页滑动次数
class UpdateSlideCountEvent(var showCardTimes: Boolean = true)


/**
 * 展示附近的数量
 */
class ShowNearCountEvent()

class UpdateApproveEvent()

class UpdateAccostListEvent()


class HideContactLlEvent()


class UpdateStarEvent(val star: Boolean)

/**
 * 更新发送礼物的事件
 */
class UpdateSendGiftEvent(val message: IMMessage)
