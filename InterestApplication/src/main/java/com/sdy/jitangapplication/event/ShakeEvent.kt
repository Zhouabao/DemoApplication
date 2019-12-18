package com.sdy.jitangapplication.event

import android.content.Context

class ShakeEvent(val left: Boolean)

class UpdateHiCountEvent()

class GreetEvent(val context: Context, val success: Boolean)


class ShowCompleteLabelEvent(val show: Boolean)
