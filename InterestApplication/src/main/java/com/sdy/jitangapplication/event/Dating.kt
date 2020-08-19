package com.sdy.jitangapplication.event

import android.content.Context
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.model.LabelQuality

/**
 *    author : ZFM
 *    date   : 2020/8/1214:11
 *    desc   :
 *    version: 1.0
 */
class UpdateMyDatingEvent(var tags: MutableList<DatingBean>? = null)



class DatingStopPlayEvent()


class DatingOnePlayEvent(val positionId:Int, val context: Context)
