package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2020/9/2214:20
 *    desc   :
 *    version: 1.0
 */

data class ProviceBean(
    var city: ArrayList<City> = arrayListOf(),
    var name: String = ""
)

data class City(
    var area: ArrayList<String> = arrayListOf(),
    var name: String = ""
)

data class CityBean(
    val name: String = "",
    val provinceName: String = "",
    val index: String = ""
)

data class SweetUploadBean(
    val defacultIcon: Int = 0,
    val type: Int = 0,
    var url: String = "",
    var width: Int = 0,
    var height: Int = 0
)