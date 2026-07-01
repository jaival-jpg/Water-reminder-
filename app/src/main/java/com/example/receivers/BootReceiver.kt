package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.SettingsRepository
import com.example.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = SettingsRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = repository.remindersEnabledFlow.first()
                val interval = repository.reminderIntervalFlow.first()
                if (enabled) {
                    AlarmScheduler.scheduleNextAlarm(context, interval)
                }
            }
        }
    }
}
