package com.example.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.SettingsRepository
import com.example.utils.AlarmScheduler
import com.example.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.example.ACTION_WATER_ALARM") {
            // Auto reschedule the next duration
            val repository = SettingsRepository(context)
            CoroutineScope(Dispatchers.IO).launch {
                val enabled = repository.remindersEnabledFlow.first()
                val interval = repository.reminderIntervalFlow.first()
                
                val country = repository.countryFlow.first()
                val useCountrySleep = repository.useCountrySleepFlow.first()
                val useManualSleep = repository.useManualSleepFlow.first()
                val manualStartHour = repository.manualSleepStartHourFlow.first()
                val manualStartMin = repository.manualSleepStartMinuteFlow.first()
                val manualEndHour = repository.manualSleepEndHourFlow.first()
                val manualEndMin = repository.manualSleepEndMinuteFlow.first()

                val isFirst = intent.getBooleanExtra("is_first", false)

                val sleeping = if (isFirst) {
                    false
                } else {
                    isCurrentlySleeping(
                        country = country,
                        useCountrySleep = useCountrySleep,
                        useManualSleep = useManualSleep,
                        manualStartHour = manualStartHour,
                        manualStartMin = manualStartMin,
                        manualEndHour = manualEndHour,
                        manualEndMin = manualEndMin
                    )
                }

                if (!sleeping) {
                    // Display high priority custom sound notification
                    NotificationHelper.showReminderNotification(context)
                    
                    // Trigger the Truecaller-like floating overlay reminder
                    val showOverlay = repository.displayOverlayScreenFlow.first()
                    val hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        android.provider.Settings.canDrawOverlays(context)
                    } else {
                        true
                    }

                    if (showOverlay && hasOverlayPermission) {
                        val overlayIntent = Intent(context, com.example.services.OverlayReminderService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(overlayIntent)
                        } else {
                            context.startService(overlayIntent)
                        }
                    } else {
                        // If overlay is not shown/allowed, explicitly play sound to guarantee audible ringing
                        NotificationHelper.playNotificationSound(context)
                    }
                }

                if (enabled) {
                    AlarmScheduler.scheduleNextAlarm(context, interval)
                }
            }
        }
    }

    private fun getSleepHoursForCountry(country: String): Pair<Int, Int> {
        return when (country) {
            "Japan" -> Pair(23, 7) // 11 PM to 7 AM
            "France", "Germany", "United Kingdom" -> Pair(23, 7) // 11 PM to 7 AM
            else -> Pair(22, 6) // 10 PM to 6 AM (India, US, Canada, Australia, etc.)
        }
    }

    private fun isCurrentlySleeping(
        country: String,
        useCountrySleep: Boolean,
        useManualSleep: Boolean,
        manualStartHour: Int,
        manualStartMin: Int,
        manualEndHour: Int,
        manualEndMin: Int
    ): Boolean {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val range = when {
            useManualSleep -> {
                SleepTimeRange(manualStartHour, manualEndHour, manualStartMin, manualEndMin)
            }
            useCountrySleep -> {
                val (sh, eh) = getSleepHoursForCountry(country)
                SleepTimeRange(sh, eh, 0, 0)
            }
            else -> return false
        }

        val currentTotalMinutes = hour * 60 + minute
        val startTotalMinutes = range.startHour * 60 + range.startMin
        val endTotalMinutes = range.endHour * 60 + range.endMin

        return if (startTotalMinutes <= endTotalMinutes) {
            currentTotalMinutes in startTotalMinutes..endTotalMinutes
        } else {
            currentTotalMinutes >= startTotalMinutes || currentTotalMinutes <= endTotalMinutes
        }
    }

    private data class SleepTimeRange(
        val startHour: Int,
        val endHour: Int,
        val startMin: Int,
        val endMin: Int
    )
}
