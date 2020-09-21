package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.model.CheckBean


class MatchByWishHelpEvent(val isFirend: Boolean, val target_accid: String = "")

class UpdateMyCandyAmountEvent(val reduceAmout: Int)


class CloseDialogEvent()

class CloseRegVipEvent()

//更新首页兴趣列表数据
class UpdateFindByTagEvent()

//更新某个兴趣下的数据
class UpdateFindByTagListEvent(var position: Int = -1, var accid: String = "")

//更新附近的人筛选参数
class UpdateNearPeopleParamsEvent()


//更新附近的人充值会员付费
class UpdateSameCityVipEvent(val isVip:Boolean)

//更新头部横幅
class UpdateShowTopAlert()

//更新今日意向
class UpdateTodayWantEvent(val todayWantBean: CheckBean?)

//允许列表滑动
class EnableRvScrollEvent(val enable: Boolean)


//置顶卡片
class TopCardEvent(val showTop: Boolean)

//女性是否录制视频
class FemaleVideoEvent(val videoState: Int)


//点击跳转到约会界面
class JumpToDatingEvent()




