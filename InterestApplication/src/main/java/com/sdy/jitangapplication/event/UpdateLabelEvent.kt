package com.sdy.jitangapplication.event

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
    val totalFileCount: Int,
    val currentFileIndex: Int,
    val progress: Double,
    var success: Boolean = true,
    var from: Int = 1
)

