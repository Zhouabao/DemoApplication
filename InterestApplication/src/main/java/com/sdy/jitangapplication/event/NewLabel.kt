package com.sdy.jitangapplication.event

import com.sdy.jitangapplication.model.NewLabel

/**
 *    author : ZFM
 *    date   : 2019/10/159:38
 *    desc   :
 *    version: 1.0
 */
class UpdateAllNewLabelEvent(var labels: MutableList<NewLabel> = mutableListOf())


class UpdateChooseLabelEvent(var label: NewLabel)

class UpdateChooseAllLabelEvent(var label: NewLabel)