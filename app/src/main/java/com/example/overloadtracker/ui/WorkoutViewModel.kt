package com.example.overloadtracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.overloadtracker.data.SessionWithSets
import com.example.overloadtracker.data.WorkoutDao
import com.example.overloadtracker.data.WorkoutSession
import com.example.overloadtracker.data.WorkoutSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            workoutDao.getAllSessionsWithSets().collect { sessionList ->
                // Ana ekranda sadece bitmiş antrenmanların tarihini gösterelim
                val completedSessions = sessionList.filter { it.session.isCompleted }
                val lastDateStr = if (completedSessions.isNotEmpty()) {
                    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("tr"))
                    dateFormat.format(java.util.Date(completedSessions.first().session.date))
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

        viewModelScope.launch {
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            workoutDao.getWeeklyVolume(sevenDaysAgo).collect { volume ->
                _uiState.update { currentState ->
                    currentState.copy(totalVolumeThisWeek = volume ?: 0.0)
                }
            }
        }
    }

    // 1. Yeni ve boş bir aktif antrenman başlatıp ID'sini döner
    suspend fun createNewSession(): Int {
        val newSession = WorkoutSession(
            date = System.currentTimeMillis(),
            bodyWeight = null,
            notes = null,
            isCompleted = false
        )
        return workoutDao.insertSession(newSession).toInt()
    }

    // 2. Yarım kalan antrenmanı ekrana yüklemek için bir kereye mahsus veriyi çeker
    suspend fun getSessionWithSetsOnce(sessionId: Int): SessionWithSets? {
        return workoutDao.getSessionWithSetsById(sessionId).firstOrNull()
    }

    // 3. İlerlemeyi veya Biten Antrenmanı kaydeder
    suspend fun saveProgress(sessionId: Int, activeSets: List<WorkoutSet>, isFinished: Boolean) {
        // Önce bu oturuma ait eski set kayıtlarını siliyoruz ki aynı setler çiftlenmesin
        workoutDao.deleteSetsBySessionId(sessionId)

        // Güncel setleri veritabanına yazıyoruz
        activeSets.forEach { uiSet ->
            if (uiSet.reps > 0) {
                workoutDao.insertSet(uiSet)
            }
        }

        // Eğer kullanıcı "Bitir" butonuna bastıysa oturumu tamamla
        if (isFinished) {
            workoutDao.markSessionAsCompleted(sessionId)
        }
    }

    suspend fun getPreviousRecord(exerciseName: String): WorkoutSet? {
        return workoutDao.getLastRecord(exerciseName)
    }
}