package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.model.MyLabelBean

/**
 *    author : ZFM
 *    date   : 2019/10/159:38
 *    desc   :
 *    version: 1.0
 */


class UpdateMyLabelEvent(var tags: MutableList<MyLabelBean>? = null)

//兴趣支付结果
class PayLabelResultEvent(val success: Boolean)

