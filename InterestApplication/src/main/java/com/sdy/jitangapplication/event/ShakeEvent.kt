package com.sdy.jitangapplication.event

import android.content.Context

class ShakeEvent(val left: Boolean)

class UpdateHiCountEvent


class GreetTopEvent(val context: Context, val success: Boolean, val targetAccid: String)

class GreetDetailSuccessEvent(val success: Boolean)

class MatchByWishHelpEvent(val isFirend: Boolean,val target_accid: String = "")

class UpdateMyCandyAmountEvent(val reduceAmout: Int)


class CloseDialogEvent()

//更新首页兴趣列表数据
class UpdateFindByTagEvent()

//更新某个兴趣下的数据
class UpdateFindByTagListEvent(var position: Int = -1, var accid: String = "")



