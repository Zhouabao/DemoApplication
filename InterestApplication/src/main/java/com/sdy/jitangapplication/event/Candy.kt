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
 * 更新我的糖果之类的数据
 */
class RefreshMyCandyEvent(val candyCount: Int)


/**
 * 更新我的糖果之类的数据
 */
class RefreshTodayFateEvent()


/**
 * 更新糖果商城详情页
 */
class RefreshCandyMessageEvent(val orderId: Int, val state: Int)


/**
 * 设置我的糖果
 */
class SetMyCandyEvent(val candyCount: Int)
