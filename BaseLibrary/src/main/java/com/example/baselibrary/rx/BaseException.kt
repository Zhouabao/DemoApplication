package com.kotlin.base.rx

import java.io.IOException

/*
    定义通用异常
 */
class BaseException(val code: Int, val msg: String) : IOException()
