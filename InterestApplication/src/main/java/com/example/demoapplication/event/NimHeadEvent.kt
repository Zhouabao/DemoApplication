package com.example.demoapplication.event

import com.example.demoapplication.model.NimBean


/**
 *    author : ZFM
 *    date   : 2019/8/1710:55
 *    desc   : 聊天界面的頭佈局數據
 *    version: 1.0
 */
class NimHeadEvent(val nimBean: NimBean)


/**
 * 倒计时数据
 */
class NimCountDownEvent(var totalTime: Int = 0, var leftTime: Int = 0)