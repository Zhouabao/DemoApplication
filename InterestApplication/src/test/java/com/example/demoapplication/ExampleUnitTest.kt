package com.example.demoapplication

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
//        assertEquals(4, 2 + 2)
        val data = arrayOf("1", "2", "哈", "敏感")
        var sensitive = ""
        for (char in data) {
            System.out.println(char)

            sensitive = sensitive.plus(char)
        }
        System.out.println(sensitive)
    }
}
