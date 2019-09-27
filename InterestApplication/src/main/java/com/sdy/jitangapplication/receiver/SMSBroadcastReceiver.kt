package com.sdy.jitangapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import java.util.regex.Pattern

/**
 *    author : ZFM
 *    date   : 2019/9/2713:46
 *    desc   :
 *    version: 1.0
 */
class SMSBroadcastReceiver(var mOnReceiveSMSListener: OnReceivedSMSListener? = null) : BroadcastReceiver() {

    companion object {
        const val TAG = "SMSBroadcastReceiver"
        const val SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED"
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action == SMS_RECEIVED_ACTION) {
            val pdus = intent.extras?.get("pdus") as Array<*>
            for (pdu in pdus) {
                val smsMessage = SmsMessage.createFromPdu(pdu as ByteArray)
                //短信号码
                val sender = smsMessage.displayOriginatingAddress
                Log.d(TAG, sender)
                //短信内容
                val content = smsMessage.displayMessageBody
                Log.d(TAG, content)
                //筛选
                if (sender.endsWith("120162",true) && mOnReceiveSMSListener != null) {
                    val pattern = Pattern.compile("\\d+")
                    val matcher = pattern.matcher(content)
                    if (matcher.find()) {
                        mOnReceiveSMSListener!!.onReceived(matcher.group())
                    }
                    abortBroadcast()
                }
            }
        }
    }

    interface OnReceivedSMSListener {
        fun onReceived(message: String)
    }
}