package com.sdy.jitangapplication.event

import android.content.Context
import com.sdy.jitangapplication.model.NewLabel
import com.sdy.jitangapplication.model.WechatNameBean
import com.sdy.jitangapplication.ui.fragment.MySquareFragment.Companion.TYPE_SQUARE

/**
 *    author : ZFM
 *    date   : 2019/6/2516:30
 *    desc   : 广场的事件
 *    version: 1.0
 */
//更新标签去请求
class UpdateLabelEvent(var label: NewLabel)

//更新播放器
class NotifyEvent(var position: Int, var type: Int = TYPE_SQUARE)

//刷新事件
class RefreshEvent(val refresh: Boolean)


//刷新点赞等事件
class RefreshLikeEvent(val isLike: Int, val position: Int)

//刷新评论数量
class RefreshCommentEvent(val commentNum: Int, val position: Int)


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


//更新用户中心信息
class UserCenterEvent(var refresh: Boolean)

//更新用户中心的标签信息
class UserCenterLabelEvent()


//更新用户详情状态视图
class UserDetailViewStateEvent(var success: Boolean)


//更新账号信息
class UpdateAccountEvent(val account: WechatNameBean)

