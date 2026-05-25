package com.example.overloadtracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.overloadtracker.data.WorkoutDao
import com.example.overloadtracker.data.WorkoutSession
import com.example.overloadtracker.data.WorkoutSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        // 1. Son Antrenman Tarihi ve Geçmiş Listesini Güncelleme
        viewModelScope.launch {
            workoutDao.getAllSessionsWithSets().collect { sessionList ->
                val lastDateStr = if (sessionList.isNotEmpty()) {
                    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("tr"))
                    dateFormat.format(java.util.Date(sessionList.first().session.date))
                } else {
                    "Antrenman bulunamadı"
                }

                _uiState.update { currentState ->
                    currentState.copy(
                        sessions = sessionList,
                        lastWorkoutDate = lastDateStr
                    )
                }
            }
        }

        // 2. Haftalık Toplam Hacmi (Volume) Hesaplama
        viewModelScope.launch {
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)

            workoutDao.getWeeklyVolume(sevenDaysAgo).collect { volume ->
                _uiState.update { currentState ->
                    currentState.copy(totalVolumeThisWeek = volume ?: 0.0)
                }
            }
        }
    }

    // Antrenman bittiğinde verileri kaydeder
    fun saveActiveWorkout(activeSets: List<WorkoutSet>) {
        viewModelScope.launch {
            if (activeSets.isEmpty()) return@launch

            val newSession = WorkoutSession(
                date = System.currentTimeMillis(),
                bodyWeight = null,
                notes = null
            )
            val sessionId = workoutDao.insertSession(newSession).toInt()

            activeSets.forEach { uiSet ->
                if (uiSet.reps > 0) {
                    val finalSet = uiSet.copy(sessionId = sessionId)
                    workoutDao.insertSet(finalSet)
                }
            }
        }
    }

    // Antrenman anında silik renkle görünecek geçmiş rekoru getirir
    suspend fun getPreviousRecord(exerciseName: String): WorkoutSet? {
        return workoutDao.getLastRecord(exerciseName)
    }
}