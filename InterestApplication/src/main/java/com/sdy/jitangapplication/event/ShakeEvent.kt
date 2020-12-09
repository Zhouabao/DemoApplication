package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.model.CustomerMsgBean


class MatchByWishHelpEvent(val isFirend: Boolean, val target_accid: String = "")

class UpdateMyCandyAmountEvent(val reduceAmout: Int)


class CloseDialogEvent()

class CloseRegVipEvent(val paySuccess: Boolean)

//更新首页兴趣列表数据
class UpdateFindByTagEvent()

//更新某个兴趣下的数据
class UpdateFindByTagListEvent(var position: Int = -1, var accid: String = "")

//更新附近的人筛选参数
class UpdateNearPeopleParamsEvent()


//更新附近的人充值会员付费
class UpdateSameCityVipEvent(val isVip: Boolean)

//更新头部横幅
class UpdateShowTopAlert()

//更新新的搭讪消息以及系统发送的假消息
class UpdateNewMsgEvent(val customerMsgBean: CustomerMsgBean)

//允许列表滑动
class EnableRvScrollEvent(val enable: Boolean)


//置顶卡片
class TopCardEvent(val showTop: Boolean)

//女性是否录制视频
class FemaleVideoEvent(val videoState: Int)

//认证状态修改
class FemaleVerifyEvent(val verifyState: Int)


//点击跳转到约会界面
class JumpToDatingEvent()


//点击切换列表模式
class ChangeListStyleEvent()

//刷新甜心圈认证状态
class RefreshSweetEvent()

//加入甜心圈
class JoinSweetEvent()

//刷新加入甜心圈显示
class RefreshSweetAddEvent(val isHoney: Boolean = false)


class SweetAddClickEvent()




