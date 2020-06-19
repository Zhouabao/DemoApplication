package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2020/6/1910:09
 *    desc   :
 *    version: 1.0
 */

data class TicketBean(
    var mv_url: Boolean = false,
    var isplatinum: Boolean = false,
    var gender: Int = 0,
    var my_ticket_sum: Int = 0,
    var ticket: Ticket = Ticket()
)

data class Ticket(
    var amount: Int = 0,
    var descr: String = "",
    var title: String = ""
)


