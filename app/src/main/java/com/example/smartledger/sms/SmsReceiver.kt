package com.example.smartledger.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.example.smartledger.SmartLedgerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION != intent.action) return
        val bundle = intent.extras ?: return
        val pdus = bundle.get("pdus") as? Array<*>
        val format = bundle.getString("format")
        val messages = pdus?.mapNotNull { pdu ->
            SmsMessage.createFromPdu(pdu as ByteArray, format)
        }.orEmpty()
        val fullText = messages.joinToString(separator = "") { it.messageBody }
        val app = context.applicationContext as SmartLedgerApp
        val processor = app.container.messageProcessor
        scope.launch {
            processor.processIncoming(
                text = fullText,
                channel = "SMS",
                methodId = null,
                cardId = null
            )
        }
    }
}


