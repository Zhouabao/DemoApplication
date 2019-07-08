package com.example.demoapplication.model

/**
 *    author : ZFM
 *    date   : 2019/6/219:19
 *    desc   :
 *    version: 1.0
 */

//可以设计一个字段来包含父级和子级之间的关系 比如A A1 A11当取消选中A时 就删除所有包含A的标签（当然一级标签不能删除 最好是以名字来连接）
data class Label(
    var name: String,
    var level: Int = 1,
    var checked: Boolean,
    var parId: Int = -1,
    var subId: Int = -1,
    var subSubId: Int = -1
)

data class LabelBean(
    var descr: String,
    var icon: String,
    var id: Int,
    var level: Int,
    var parentId: Int,
    var path: String,
    var son: List<LabelBean>,
    var title: String,
    var videoPath: String,
    var checked: Boolean = false
)

/*{"msg":"获取数据成功！","code":200,"data":{"version":9,"data":
[{"id":1,
"parent_id":0,
"title":"测试1",
"path":"1",
"icon":"","video_path":"","descr":"","level":0,
"son":[{"id":4,"parent_id":1,"title":"测试3","path":"1-4","icon":"","video_path":"","descr":"","level":1,"son":[]}]
},
{"id":3,"parent_id":0,"title":"测试2","path":"3","icon":"","video_path":"","descr":"","level":0,
"son":[{"id":5,"parent_id":3,"title":"测试4","path":"3-5","icon":"","video_path":"","descr":"","level":1,"son":[]},
{"id":6,"parent_id":3,"title":"测试5","path":"3-6","icon":"","video_path":"","descr":"","level":1,
"son":[{"id":7,"parent_id":6,"title":"测试3级","path":"3-6-7","icon":"","video_path":"","descr":"","level":2,"son":[]}]}]}]},"time":1562551530}
* */


