package com.sdy.jitangapplication.receiver

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.util.Log

/**
 *    author : ZFM
 *    date   : 2019/9/2714:33
 *    desc   :
 *    version: 1.0
 */
class SMSContentObserver(val context: Context, val handler: Handler) : ContentObserver(handler) {
    companion object {

        public val TAG = SMSContentObserver::class.java.simpleName
        public val MSG_OUTBOXCONTENT = 2
    }

    override fun onChange(selfChange: Boolean) {

        val uri = Uri.parse("content://sms")
        val c = context.contentResolver.query(uri, null, null, null, "date desc")
        if (c != null) {
            Log.d(TAG, "${c.count}")

            val sb = StringBuffer()
            while (c.moveToNext()) {
//                sb.append(c.getString(c.getColumnIndex("body")))
                Log.d(
                    TAG,
                    "${c.getString(c.getColumnIndex("address"))}," +
                            "${c.getString(c.getColumnIndex("person"))}," +
                            "${c.getString(
                                c.getColumnIndex("body")
                            )}"
                )
            }
            c.close()
            handler.obtainMessage(MSG_OUTBOXCONTENT, sb.toString()).sendToTarget()
        }

    }
}