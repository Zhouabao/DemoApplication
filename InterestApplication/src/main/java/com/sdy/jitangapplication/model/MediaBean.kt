package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2019/7/2314:52
 *    desc   :
 *    version: 1.0
 */
data class MediaBean(
    val id :Int = 0,
    val fileType: TYPE = TYPE.IMAGE,
    var filePath: String = "",
    var fileName: String = "",
    var thumbnail: String = "",
    var duration: Int = 0,
    var size: Long = 0L,
    var ischecked: Boolean = false,
    var width: Int = 0,
    var height: Int = 0
) {
    enum class TYPE { IMAGE, VIDEO }

}

/**
 * 媒体文件的参数
 */
data class MediaParamBean(
    var url: String = "",
    var duration: Int = 0,
    var width: Int = 0,
    var height: Int = 0
)