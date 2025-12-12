package com.example.smartledger.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartledger.SmartLedgerApp
import com.example.smartledger.data.Method
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as SmartLedgerApp
        val repo = app.container.repository
        val settings = app.container.settingsRepository.settings.first()
        val leadDays = settings.repayLeadDays

        val methods = repo.methods().first().filter { it.isCredit }
        val today = LocalDate.now().dayOfMonth
        methods.forEach { method ->
            val cards = repo.cards(method.id).first()
            if (cards.isEmpty()) {
                if (shouldRemind(method, today, method.repayLeadDays ?: leadDays)) {
                    push(method.name, method.dueDay)
                }
            } else {
                cards.forEach { card ->
                    if (shouldRemindCard(card.dueDay ?: method.dueDay, today, card.repayLeadDays ?: method.repayLeadDays
                            ?: leadDays)) {
                        push("${method.name}-${card.label}", card.dueDay ?: method.dueDay)
                    }
                }
            }
        }
        return Result.success()
    }

    private fun push(name: String, dueDay: Int?) {
        if (dueDay == null) return
        val notification = NotificationHelper.buildNotification(
            applicationContext,
            title = "信用卡还款提醒",
            body = "$name 将在 $dueDay 日到期，请关注还款"
        )
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(name.hashCode(), notification)
    }

    private fun shouldRemind(method: Method, today: Int, leadDays: Int): Boolean {
        val due = method.dueDay ?: return false
        return today == due || today == (due - leadDays)
    }

    private fun shouldRemindCard(dueDay: Int?, today: Int, leadDays: Int): Boolean {
        val due = dueDay ?: return false
        return today == due || today == (due - leadDays)
    }
}
                val notification = NotificationHelper.buildNotification(
                    applicationContext,
                    title = "信用卡还款提醒",
                    body = "${method.name} 将在 ${method.dueDay} 日到期，请关注还款"
                )
                val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                manager.notify(method.id.toInt(), notification)
            }
        }
        return Result.success()
    }

    private fun shouldRemind(method: Method, today: Int, leadDays: Int): Boolean {
        val due = method.dueDay ?: return false
        return today == due || today == (due - leadDays)
    }
}

