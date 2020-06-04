package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.model.CheckBean


class GreetDetailSuccessEvent(val success: Boolean)

class MatchByWishHelpEvent(val isFirend: Boolean, val target_accid: String = "")

class UpdateMyCandyAmountEvent(val reduceAmout: Int)


class CloseDialogEvent()

//更新首页兴趣列表数据
class UpdateFindByTagEvent()

//更新某个兴趣下的数据
class UpdateFindByTagListEvent(var position: Int = -1, var accid: String = "")

//更新附近的人筛选参数
class UpdateNearPeopleParamsEvent()


//更新附近的人筛选参数
class UpdateSameCityVipEvent()

//更新头部横幅
class UpdateShowTopAlert()

//更新今日意向
class UpdateTodayWantEvent(val todayWantBean: CheckBean?)


//置顶卡片
class TopCardEvent(val showTop: Boolean)




