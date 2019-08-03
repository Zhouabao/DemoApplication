package com.example.demoapplication.event

/**
 *    author : ZFM
 *    date   : 2019/6/2516:30
 *    desc   :显示新消息的event
 *    version: 1.0
 */
class NewMsgEvent(var likeCount: Int,var  squareCount: Int,var HiCount: Int, var chatCount: Int)


class UpdateAvatorEvent(val update: Boolean)