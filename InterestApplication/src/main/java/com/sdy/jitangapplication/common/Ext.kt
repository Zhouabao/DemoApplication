package com.sdy.jitangapplication.common

import android.view.View
import com.sdy.jitangapplication.R

/**
 *    author : ZFM
 *    date   : 2020/4/2210:41
 *    desc   : 点击事件
 *    version: 1.0
 */


/**
 * get set
 * 给view添加一个上次触发时间的属性（用来屏蔽连击操作）
 */
private var <T : View>T.triggerLastTime: Long
    get() = if (getTag(R.id.triggerLastTimeKey) != null) getTag(R.id.triggerLastTimeKey) as Long else 0
    set(value) {
        setTag(R.id.triggerLastTimeKey, value)
    }

/**
 * get set
 * 给view添加一个延迟的属性（用来屏蔽连击操作）
 */
private var <T : View> T.triggerDelay: Long
    get() = if (getTag(R.id.triggerDelayKey) != null) getTag(R.id.triggerDelayKey) as Long else -1
    set(value) {
        setTag(R.id.triggerDelayKey, value)
    }

/**
 * 判断时间是否满足再次点击的要求（控制点击）
 */
private fun <T : View> T.clickEnable(): Boolean {
    var clickable = false
    val currentClickTime = System.currentTimeMillis()
    if (currentClickTime - triggerLastTime >= triggerDelay) {
        clickable = true
    }
    triggerLastTime = currentClickTime
    return clickable
}

/***
 * 带延迟过滤点击事件的 View 扩展
 * @param delay Long 延迟时间，默认500毫秒
 * @param block: (T) -> Unit 函数
 * @return Unit
 */
fun <T : View> T.clickWithTrigger(delay: Long = 500, block: (T) -> Unit) {
    triggerDelay = delay
    setOnClickListener {
        if (clickEnable()) {
            block(this)
        }
    }
}


/***
 * 带延迟过滤的点击事件监听，见[View.OnClickListener]
 * 延迟时间根据triggerDelay获取：600毫秒，不能动态设置
 */
interface OnLazyClickListener : View.OnClickListener {

    override fun onClick(v: View?) {
        if (v?.clickEnable() == true) {
            onLazyClick(v)
        }
    }

    fun onLazyClick(v: View)
}

//3月21日~4月19日	白羊座	Aries	Ari
//4月20日~5月20日	金牛座	Taurus	Tau
//5月21日~6月20日	双子座	Genimi	Gem
//6月21日~7月21日	巨蟹座	Cancer	Cnc
//7月22日~8月22日	狮子座	Leonis	Leo
//8月23日~9月22日	处女座	Virgo	Vir
//9月23日~10月22日	天秤座	Libra	Lib
//10月23日~11月21日	天蝎座	Scorpius	Sco
//11月22日~12月21日	射手座	Sagittarius	Sgr
//12月22日~1月19日	摩羯座	Capricornus	Cap
//1月20日~2月18日	水瓶座	Aguarius	Agr
//2月19日~3月20日	双鱼座	Pisces	Psc
fun getZodiacEn(zodaic: String): String {
    val ZODIAC = arrayOf(
        "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座",
        "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"
    )
    val ZODIAC_EN = arrayOf(
        "Agr", "Psc", "Ari", "Tau", "Gem", "Cnc",
        "Leo", "Vir", "Lib", "Sco", "Sgr", "Cap"
    )
    return ZODIAC_EN[ZODIAC.indexOf(zodaic)]
}