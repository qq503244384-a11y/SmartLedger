package com.example.smartledger.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.smartledger.SmartLedgerApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LedgerNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        // Placeholder: parse notification content for ledger rules.
        val app = applicationContext as SmartLedgerApp
        val processor = app.container.messageProcessor
        scope.launch {
            val pkg = sbn.packageName ?: ""
            val text = sbn.notification.extras.getCharSequence("android.text")?.toString().orEmpty()
            val title = sbn.notification.extras.getCharSequence("android.title")?.toString().orEmpty()
            val body = "$title $text".trim()
            if (body.isNotBlank()) {
                processor.processIncoming(
                    text = body,
                    channel = pkg,
                    methodId = null,
                    cardId = null
                )
            }
        }
    }
}

