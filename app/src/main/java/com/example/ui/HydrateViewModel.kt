package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SettingsRepository
import com.example.data.WaterLog
import com.example.utils.AlarmScheduler
import com.example.utils.FirebaseHelper
import com.example.utils.NotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class HydrateViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {

    private val waterLogDao = AppDatabase.getDatabase(application).waterLogDao()

    val name: StateFlow<String> = settingsRepository.nameFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val age: StateFlow<String> = settingsRepository.ageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val weight: StateFlow<String> = settingsRepository.weightFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val dailyGoal: StateFlow<Int> = settingsRepository.dailyGoalFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3000)
    val cupSize: StateFlow<Int> = settingsRepository.cupSizeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 200)
    val remindersEnabled: StateFlow<Boolean> = settingsRepository.remindersEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val displayOverlayScreen: StateFlow<Boolean> = settingsRepository.displayOverlayScreenFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val reminderInterval: StateFlow<Int> = settingsRepository.reminderIntervalFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)
    val hasCompletedOnboarding: StateFlow<Boolean?> = settingsRepository.hasCompletedOnboardingFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val userId: StateFlow<String> = settingsRepository.userIdFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val country: StateFlow<String> = settingsRepository.countryFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "India")
    val useCountrySleep: StateFlow<Boolean> = settingsRepository.useCountrySleepFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val useManualSleep: StateFlow<Boolean> = settingsRepository.useManualSleepFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val manualSleepStartHour: StateFlow<Int> = settingsRepository.manualSleepStartHourFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 22)
    val manualSleepStartMinute: StateFlow<Int> = settingsRepository.manualSleepStartMinuteFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val manualSleepEndHour: StateFlow<Int> = settingsRepository.manualSleepEndHourFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6)
    val manualSleepEndMinute: StateFlow<Int> = settingsRepository.manualSleepEndMinuteFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            var currentId = settingsRepository.userIdFlow.first()
            if (currentId.isEmpty()) {
                currentId = UUID.randomUUID().toString()
                settingsRepository.saveUserId(currentId)
            }
            
            // Check if onboarded, then sync settings from Firebase to local as well
            val onboarded = settingsRepository.hasCompletedOnboardingFlow.first()
            if (onboarded && currentId.isNotEmpty()) {
                FirebaseHelper.fetchUserDetails(
                    userId = currentId,
                    onSuccess = { fName, fDailyGoal, fCupSize, fWeight, fAge ->
                        viewModelScope.launch {
                            if (fName.isNotEmpty()) settingsRepository.saveName(fName)
                            if (fDailyGoal > 0) settingsRepository.saveDailyGoal(fDailyGoal)
                            if (fCupSize > 0) settingsRepository.saveCupSize(fCupSize)
                            if (fWeight.isNotEmpty()) settingsRepository.saveWeight(fWeight)
                            if (fAge.isNotEmpty()) settingsRepository.saveAge(fAge)
                        }
                    },
                    onFailure = { e ->
                        android.util.Log.e("HydrateViewModel", "Firestore fetch settings failed: ${e.message}")
                    }
                )
            }
        }
        // Safely check and initialize alarms on startup without resetting a scheduled future alarm
        viewModelScope.launch {
            val enabled = settingsRepository.remindersEnabledFlow.first()
            val interval = settingsRepository.reminderIntervalFlow.first()
            val nextAlarmTime = settingsRepository.nextAlarmTimeFlow.first()
            if (enabled) {
                val currentTime = System.currentTimeMillis()
                if (nextAlarmTime <= currentTime) {
                    AlarmScheduler.scheduleNextAlarm(getApplication(), interval, isFirst = false)
                }
            } else {
                AlarmScheduler.cancelAlarm(getApplication())
            }
        }
    }

    // Calculate start and end of today
    private val startOfToday: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private val endOfToday: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        
    val todayLogs: StateFlow<List<WaterLog>> = waterLogDao.getLogsBetween(startOfToday, endOfToday)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<WaterLog>> = waterLogDao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayIntake: StateFlow<Int> = combine(todayLogs) { logsArray ->
        val logs = logsArray.first()
        logs.sumOf { it.amountMl }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun syncToFirebase(
        overrideName: String? = null,
        overrideAge: String? = null,
        overrideWeight: String? = null,
        overrideDailyGoal: Int? = null,
        overrideCupSize: Int? = null
    ) {
        viewModelScope.launch {
            val uid = userId.value.ifEmpty { settingsRepository.userIdFlow.first() }
            val currentName = overrideName ?: name.value
            val currentDailyGoal = overrideDailyGoal ?: dailyGoal.value
            val currentCupSize = overrideCupSize ?: cupSize.value
            val currentWeight = overrideWeight ?: weight.value
            val currentAge = overrideAge ?: age.value
            
            FirebaseHelper.syncUserDetails(
                userId = uid,
                name = currentName,
                dailyGoal = currentDailyGoal,
                cupSize = currentCupSize,
                weight = currentWeight,
                age = currentAge
            )
        }
    }

    fun saveOnboarding(
        name: String,
        age: String,
        weight: String,
        dailyGoal: Int,
        reminderInterval: Int
    ) {
        viewModelScope.launch {
            settingsRepository.saveOnboardingData(
                name = name,
                age = age,
                weight = weight,
                dailyGoal = dailyGoal,
                reminderInterval = reminderInterval
            )
            // Immediately sync latest values to Firebase
            syncToFirebase(
                overrideName = name,
                overrideAge = age,
                overrideWeight = weight,
                overrideDailyGoal = dailyGoal
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted()
            syncToFirebase()
        }
    }

    fun updateName(newName: String) {
        viewModelScope.launch {
            settingsRepository.saveName(newName)
            syncToFirebase(overrideName = newName)
        }
    }

    fun updateAge(newAge: String) {
        viewModelScope.launch {
            settingsRepository.saveAge(newAge)
            syncToFirebase(overrideAge = newAge)
        }
    }

    fun updateWeight(newWeight: String) {
        viewModelScope.launch {
            settingsRepository.saveWeight(newWeight)
            syncToFirebase(overrideWeight = newWeight)
        }
    }

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            settingsRepository.saveDailyGoal(goal)
            syncToFirebase(overrideDailyGoal = goal)
        }
    }

    fun updateCupSize(size: Int) {
        viewModelScope.launch {
            settingsRepository.saveCupSize(size)
            syncToFirebase(overrideCupSize = size)
        }
    }

    fun toggleReminders(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveRemindersEnabled(enabled)
            if (enabled) {
                val interval = settingsRepository.reminderIntervalFlow.first()
                AlarmScheduler.scheduleNextAlarm(getApplication(), interval, isFirst = true)
            } else {
                AlarmScheduler.cancelAlarm(getApplication())
            }
        }
    }

    fun updateReminderInterval(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.saveReminderInterval(minutes)
            val enabled = settingsRepository.remindersEnabledFlow.first()
            if (enabled) {
                AlarmScheduler.scheduleNextAlarm(getApplication(), minutes, isFirst = true)
            }
        }
    }

    fun updateCountry(newCountry: String) {
        viewModelScope.launch { settingsRepository.saveCountry(newCountry) }
    }

    fun updateUseCountrySleep(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.saveUseCountrySleep(enabled) }
    }

    fun updateUseManualSleep(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.saveUseManualSleep(enabled) }
    }

    fun updateDisplayOverlayScreen(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.saveDisplayOverlayScreen(enabled) }
    }

    fun updateManualSleepTimes(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch {
            settingsRepository.saveManualSleepStartHour(startHour)
            settingsRepository.saveManualSleepStartMinute(startMinute)
            settingsRepository.saveManualSleepEndHour(endHour)
            settingsRepository.saveManualSleepEndMinute(endMinute)
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            waterLogDao.insertLog(WaterLog(amountMl = amountMl, timeInMillis = System.currentTimeMillis()))
        }
    }

    fun resetData() {
        viewModelScope.launch {
            waterLogDao.deleteAllLogs()
            settingsRepository.clearSettings()
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HydrateViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HydrateViewModel(application, SettingsRepository(application)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
