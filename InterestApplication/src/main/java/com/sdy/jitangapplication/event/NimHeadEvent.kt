package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.model.NimBean


/**
 *    author : ZFM
 *    date   : 2019/8/1710:55
 *    desc   : 聊天界面的頭佈局數據
 *    version: 1.0
 */
class NimHeadEvent(val nimBean: NimBean)


/**
 * 是否能启用图片和定位
 * @param enable true启用 false关闭
 */
class EnablePicEvent(val enable: Boolean)


/**
 * 星标好友事件
 */
class StarEvent(var stared: Boolean = false,var isfriend: Boolean = false)