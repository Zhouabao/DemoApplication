package com.sdy.jitangapplication.event

import java.io.File

/**
 *    author : ZFM
 *    date   : 2020/7/216:03
 *    desc   :
 *    version: 1.0
 */

class VideoTrimmerEvent(val filePath: String)


open class SaveImgSuccessEvent(val filePath: File)