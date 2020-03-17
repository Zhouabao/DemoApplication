package com.sdy.jitangapplication.event

import android.content.Context

class ShakeEvent(val left: Boolean)

class UpdateHiCountEvent

class GreetEvent(val context: Context, val success: Boolean)

class GreetTopEvent(val context: Context, val success: Boolean)

class GreetDetailSuccessEvent(val success: Boolean)


class CloseDialogEvent()

//更新首页兴趣列表数据
class UpdateFindByTagEvent()

//更新某个兴趣下的数据
class UpdateFindByTagListEvent(var position: Int = -1, var accid: String = "")



