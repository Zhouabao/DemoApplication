package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/6/2516:30
 *    desc   :显示新消息的event
 *    version: 1.0
 */
class NewMsgEvent

class GetNewMsgEvent

//重新认证事件通知
class ReVerifyEvent(val type: Int, var avator: String = UserManager.getAvator())


class UpdateAvatorEvent(val update: Boolean)


class EnableLabelEvent(val enable: Boolean)


//展示调研弹窗的event
class ShowSurveyDialogEvent(val slideCount: Int)


//修改主页的状态栏颜色
class ChangeStatusColorEvent