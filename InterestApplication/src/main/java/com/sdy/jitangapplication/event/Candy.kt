package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.model.AddressBean
import com.sdy.jitangapplication.model.Alipay

/**
 *    author : ZFM
 *    date   : 2020/3/2417:36
 *    desc   :
 *    version: 1.0
 */

/**
 * 获取支付宝账号事件总线
 */
class GetAlipayAccountEvent(val account: Alipay)

/**
 * 获取评论图片事件总线
 */
class CommentPicEvent(val imgs: MutableList<String>)


/**
 * 获取收货地址事件总线
 */
class GetAddressvent(val address: AddressBean)

/**
 * 更新商品状态事件总线
 */
class RefreshOrderStateEvent(val position: Int, val state: Int)

/**
 * 更新商品状态事件总线
 */
class RefreshGoodsMessageEvent()

/**
 * 更新我的糖果之类的数据
 */
class RefreshMyCandyEvent(val candyCount: Int)

/**
 * 更新糖果商城
 */
class RefreshCandyMallEvent()

/**
 * 更新糖果商城详情页
 */
class RefreshCandyMallDetailEvent()