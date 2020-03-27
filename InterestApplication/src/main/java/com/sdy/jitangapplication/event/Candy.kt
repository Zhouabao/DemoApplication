package com.sdy.jitangapplication.event

/**
 *    author : ZFM
 *    date   : 2020/3/2417:36
 *    desc   :
 *    version: 1.0
 */

/**
 * 获取支付宝账号事件总线
 */
class GetAlipayAccountEvent(val account: String)

/**
 * 获取评论图片事件总线
 */
class CommentPicEvent(val imgs: MutableList<String>)


/**
 * 获取收货地址事件总线
 */
class GetAddressvent(val address: String)