package com.sdy.jitangapplication.event

/**
 *    author : ZFM
 *    date   : 2019/6/2516:30
 *    desc   :显示新消息的event
 *    version: 1.0
 */
class NewMsgEvent

class GetNewMsgEvent

//重新认证事件通知
class ReVerifyEvent(val type: Int)


class UpdateAvatorEvent(val update: Boolean)