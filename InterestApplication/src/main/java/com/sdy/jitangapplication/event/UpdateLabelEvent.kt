package com.sdy.jitangapplication.event

import android.content.Context
import com.sdy.jitangapplication.model.LabelBean

/**
 *    author : ZFM
 *    date   : 2019/6/2516:30
 *    desc   : 广场的事件
 *    version: 1.0
 */
//更新标签去请求
class UpdateLabelEvent(var label: LabelBean)

//更新播放器
class NotifyEvent(var position: Int)

//刷新事件
class RefreshEvent(val refresh: Boolean)


//刷新事件  local 是否是本地
class RefreshSquareEvent(val refresh: Boolean, var from: String = "")

//根据条件筛选请求的params
class FilterEvent(val params: HashMap<String, Any>)


//上传进度事件 from 1广场 2用户中心
class UploadEvent(
    var totalFileCount: Int = 0,
    var currentFileIndex: Int = 0,
    var progress: Double = 0.0,
    var qnSuccess: Boolean = true,
    var from: Int = 1
)


//上传成功或者失败事件
/**
 * @param serverSuccess 成功或者失败
 * @param  code失败的code码 判断是否是审核不通过
 */
class AnnounceEvent(var serverSuccess: Boolean = false, var code: Int = 0)


//重新上传内容的通知成功或者失败事件
class RePublishEvent(var republish: Boolean, val context: Context)

