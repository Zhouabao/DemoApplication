package com.sdy.jitangapplication.model

data class ApproveBean(
    var approve_time: Long,
    var isapprove: Int,//0 不验证  1去认证 2去开通会员  3去认证+去会员  4去会员+去认证
    var issend: Boolean = true//判断是否可以发送消息 0就可以发，不是就不能发
)