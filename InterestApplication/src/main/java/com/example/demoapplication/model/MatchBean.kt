package com.example.demoapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/6/2418:17
 *    desc   :  sex：1男2女
 *    version: 1.0
 */
data class MatchBean(
    var name: String,
    var age: Int,
    var sex: Int,
    var imgs: MutableList<Int>,
    var type: Int = 1
) :
    Serializable, MultiItemEntity {
    override fun getItemType(): Int {
        return type
    }

    companion object {
        const val PIC = 1
        const val VIDEO = 2
        const val AUDIO = 3
    }


}