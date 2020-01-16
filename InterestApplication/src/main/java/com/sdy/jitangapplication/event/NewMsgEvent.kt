package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/6/2516:30
 *    desc   :显示新消息的event
 *    version: 1.0
 */

//首页更新红点消息
class GetNewMsgEvent

//重新认证事件通知
class ReVerifyEvent(val type: Int, var avator: String = UserManager.getAvator())


//账号异常认证事件通知
class AccountDangerEvent(val type: Int)

//更新首页滑动次数
class UpdateSlideCountEvent


//展示调研弹窗的event
class ShowSurveyDialogEvent(val slideCount: Int)

