package com.example.demoapplication

import com.blankj.utilcode.util.TimeUtils
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        System.out.println("${TimeUtils.date2Millis(
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).parse("1995-02-27")
        )}")
        System.out.println(TimeUtils.string2Millis("19950227"))
    }
}
