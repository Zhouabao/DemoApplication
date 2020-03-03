package com.sdy.jitangapplication.event

import android.content.Context

class ShakeEvent(val left: Boolean)

class UpdateHiCountEvent

class GreetEvent(val context: Context, val success: Boolean)


class TurnToLastLabelEvent(val from: Int)


class CloseDialogEvent()

//更新首页标签列表数据
class UpdateFindByTagEvent()

//更新某个标签下的数据
class UpdateFindByTagListEvent(var position: Int = -1)



