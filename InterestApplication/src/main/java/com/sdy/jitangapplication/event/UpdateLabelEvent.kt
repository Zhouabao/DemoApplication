package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.model.WechatNameBean
import com.sdy.jitangapplication.ui.fragment.MyCollectionAndLikeFragment.Companion.TYPE_SQUARE

/**
 *    author : ZFM
 *    date   : 2019/6/2516:30
 *    desc   : 广场的事件
 *    version: 1.0
 */

//更新播放器
class NotifyEvent(var position: Int, var type: Int = TYPE_SQUARE)

//刷新事件
class RefreshEvent(val refresh: Boolean)


//刷新点赞等事件
class RefreshLikeEvent(
    val squareId: Int,
    val isLike: Int,
    val position: Int,
    var likeCount: Int = -1
)


//刷新删除动态事件
class RefreshDeleteSquareEvent(val squareId: Int)


//刷新评论数量
class RefreshCommentEvent(val commentNum: Int, val position: Int)


//刷新事件  local 是否是本地
class RefreshSquareEvent(val refresh: Boolean, var from: String = "")

//根据性别筛选更新数据
class RefreshSquareByGenderEvent()


//上传进度事件 from 1广场 2用户中心
class UploadEvent(
    var totalFileCount: Int = 0,
    var currentFileIndex: Int = 0,
    var progress: Double = 0.0,
    var qnSuccess: Boolean = true,
    var from: Int = FROM_SQUARE
) {
    companion object {
        val FROM_SQUARE = 1
        val FROM_USERCENTER = 2
    }
}


//上传成功或者失败事件
/**
 * @param serverSuccess 成功或者失败
 * @param  code失败的code码 判断是否是审核不通过
 */
class AnnounceEvent(var serverSuccess: Boolean = false, var code: Int = 0)


//重新上传内容的通知成功或者失败事件
class RePublishEvent(var republish: Boolean, val context: String)


//更新用户中心信息
class UserCenterEvent(var refresh: Boolean)


//更新用户详情状态视图
class UserDetailViewStateEvent(var success: Boolean)




//更新账号信息
class UpdateAccountEvent(val account: WechatNameBean)

