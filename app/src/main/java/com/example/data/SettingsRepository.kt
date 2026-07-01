package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val NAME = stringPreferencesKey("name")
        val AGE = stringPreferencesKey("age")
        val WEIGHT = stringPreferencesKey("weight")
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val CUP_SIZE_ML = intPreferencesKey("cup_size_ml")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_INTERVAL_HOURS = intPreferencesKey("reminder_interval_hours")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val NEXT_ALARM_TIME = androidx.datastore.preferences.core.longPreferencesKey("next_alarm_time")
        val COUNTRY = stringPreferencesKey("country")
        val USE_COUNTRY_SLEEP = booleanPreferencesKey("use_country_sleep")
        val USE_MANUAL_SLEEP = booleanPreferencesKey("use_manual_sleep")
        val MANUAL_SLEEP_START_HOUR = intPreferencesKey("manual_sleep_start_hour")
        val MANUAL_SLEEP_START_MINUTE = intPreferencesKey("manual_sleep_start_minute")
        val MANUAL_SLEEP_END_HOUR = intPreferencesKey("manual_sleep_end_hour")
        val MANUAL_SLEEP_END_MINUTE = intPreferencesKey("manual_sleep_end_minute")
        val DISPLAY_OVERLAY_SCREEN = booleanPreferencesKey("display_overlay_screen")
    }

    val userIdFlow: Flow<String> = dataStore.data.map { it[USER_ID] ?: "" }
    val nameFlow: Flow<String> = dataStore.data.map { it[NAME] ?: "" }
    val ageFlow: Flow<String> = dataStore.data.map { it[AGE] ?: "" }
    val weightFlow: Flow<String> = dataStore.data.map { it[WEIGHT] ?: "" }
    val dailyGoalFlow: Flow<Int> = dataStore.data.map { it[DAILY_GOAL_ML] ?: 3000 }
    val cupSizeFlow: Flow<Int> = dataStore.data.map { it[CUP_SIZE_ML] ?: 200 }
    val remindersEnabledFlow: Flow<Boolean> = dataStore.data.map { it[REMINDERS_ENABLED] ?: true }
    val reminderIntervalFlow: Flow<Int> = dataStore.data.map { 
        val stored = it[REMINDER_INTERVAL_HOURS] ?: 60
        if (stored <= 5) stored * 60 else stored
    }
    val hasCompletedOnboardingFlow: Flow<Boolean> = dataStore.data.map { it[HAS_COMPLETED_ONBOARDING] ?: false }
    val nextAlarmTimeFlow: Flow<Long> = dataStore.data.map { it[NEXT_ALARM_TIME] ?: 0L }
    val countryFlow: Flow<String> = dataStore.data.map { it[COUNTRY] ?: "India" }
    val useCountrySleepFlow: Flow<Boolean> = dataStore.data.map { it[USE_COUNTRY_SLEEP] ?: false }
    val useManualSleepFlow: Flow<Boolean> = dataStore.data.map { it[USE_MANUAL_SLEEP] ?: false }
    val manualSleepStartHourFlow: Flow<Int> = dataStore.data.map { it[MANUAL_SLEEP_START_HOUR] ?: 22 }
    val manualSleepStartMinuteFlow: Flow<Int> = dataStore.data.map { it[MANUAL_SLEEP_START_MINUTE] ?: 0 }
    val manualSleepEndHourFlow: Flow<Int> = dataStore.data.map { it[MANUAL_SLEEP_END_HOUR] ?: 6 }
    val manualSleepEndMinuteFlow: Flow<Int> = dataStore.data.map { it[MANUAL_SLEEP_END_MINUTE] ?: 0 }
    val displayOverlayScreenFlow: Flow<Boolean> = dataStore.data.map { it[DISPLAY_OVERLAY_SCREEN] ?: true }

    suspend fun saveUserId(userId: String) = dataStore.edit { it[USER_ID] = userId }
    suspend fun saveName(name: String) = dataStore.edit { it[NAME] = name }
    suspend fun saveAge(age: String) = dataStore.edit { it[AGE] = age }
    suspend fun saveWeight(weight: String) = dataStore.edit { it[WEIGHT] = weight }
    suspend fun saveDailyGoal(goal: Int) = dataStore.edit { it[DAILY_GOAL_ML] = goal }
    suspend fun saveCupSize(size: Int) = dataStore.edit { it[CUP_SIZE_ML] = size }
    suspend fun saveRemindersEnabled(enabled: Boolean) = dataStore.edit { it[REMINDERS_ENABLED] = enabled }
    suspend fun saveReminderInterval(hours: Int) = dataStore.edit { it[REMINDER_INTERVAL_HOURS] = hours }
    suspend fun setOnboardingCompleted() = dataStore.edit { it[HAS_COMPLETED_ONBOARDING] = true }
    suspend fun saveNextAlarmTime(timeMs: Long) = dataStore.edit { it[NEXT_ALARM_TIME] = timeMs }
    suspend fun saveCountry(country: String) = dataStore.edit { it[COUNTRY] = country }
    suspend fun saveUseCountrySleep(enabled: Boolean) = dataStore.edit { it[USE_COUNTRY_SLEEP] = enabled }
    suspend fun saveUseManualSleep(enabled: Boolean) = dataStore.edit { it[USE_MANUAL_SLEEP] = enabled }
    suspend fun saveManualSleepStartHour(hour: Int) = dataStore.edit { it[MANUAL_SLEEP_START_HOUR] = hour }
    suspend fun saveManualSleepStartMinute(minute: Int) = dataStore.edit { it[MANUAL_SLEEP_START_MINUTE] = minute }
    suspend fun saveManualSleepEndHour(hour: Int) = dataStore.edit { it[MANUAL_SLEEP_END_HOUR] = hour }
    suspend fun saveManualSleepEndMinute(minute: Int) = dataStore.edit { it[MANUAL_SLEEP_END_MINUTE] = minute }
    suspend fun saveDisplayOverlayScreen(enabled: Boolean) = dataStore.edit { it[DISPLAY_OVERLAY_SCREEN] = enabled }

    suspend fun saveOnboardingData(
        name: String,
        age: String,
        weight: String,
        dailyGoal: Int,
        reminderInterval: Int
    ) {
        dataStore.edit { preferences ->
            preferences[NAME] = name
            preferences[AGE] = age
            preferences[WEIGHT] = weight
            preferences[DAILY_GOAL_ML] = dailyGoal
            preferences[REMINDER_INTERVAL_HOURS] = reminderInterval
            preferences[HAS_COMPLETED_ONBOARDING] = true
        }
    }

    suspend fun clearSettings() = dataStore.edit { it.clear() }
}
