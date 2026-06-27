package com.example.overloadtracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.overloadtracker.data.Routine
import com.example.overloadtracker.data.RoutineExercise
import com.example.overloadtracker.data.RoutineWithExercises
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

    private val _routines = MutableStateFlow<List<RoutineWithExercises>>(emptyList())
    val routines: StateFlow<List<RoutineWithExercises>> = _routines.asStateFlow()

    init {
        loadDashboardData()
        loadRoutines()
    }

    private fun loadRoutines() {
        viewModelScope.launch {
            workoutDao.getAllRoutinesWithExercises().collect { dbRoutines ->
                // Verileri orderIndex'e göre sıralayıp UI'a gönderiyoruz
                val sortedRoutines = dbRoutines.map { routineData ->
                    routineData.copy(exercises = routineData.exercises.sortedBy { it.orderIndex })
                }
                if (sortedRoutines.isEmpty()) {
                    seedDatabaseWithOldSplits()
                } else {
                    _routines.value = sortedRoutines
                }
            }
        }
    }

    private suspend fun seedDatabaseWithOldSplits() {
        val oldSplits = mapOf(
            "PUSH" to listOf(
                Triple("Bench Press", "2+2 x 10", "2.5 - 3 Dakika"),
                Triple("Incline Press", "3 x 10", "2 - 3 Dakika"),
                Triple("Pec Deck Fly", "3 x Failure", "60 - 90 Saniye"),
                Triple("Shoulder Press", "1+3 x Failure", "2.5 - 3 Dakika"),
                Triple("Lateral Raise", "3 x 12", "60 - 90 Saniye"),
                Triple("Skull Crusher", "3 x 10", "60 - 90 Saniye"),
                Triple("Triceps Pushdown", "2 x Drop", "90 Saniye")
            ),
            "PULL" to listOf(
                Triple("Lat Pulldown", "2+2 x 10", "2 - 3 Dakika"),
                Triple("Rope Pullover", "3 x 10", "60 - 90 Saniye"),
                Triple("Seated Cable Row", "3 x 10", "2 - 3 Dakika"),
                Triple("T-Bar Row", "1+2 x Failure", "2.5 - 3 Dakika"),
                Triple("Reverse Fly", "3 x 10", "60 - 90 Saniye"),
                Triple("Dumbbell / Cable Curl", "3 x Failure", "60 - 90 Saniye"),
                Triple("Hammer Curl", "3 x 10", "60 - 90 Saniye")
            ),
            "LOWER" to listOf(
                Triple("Leg Press", "2+2 x 10", "2.5 - 3 Dakika"),
                Triple("Hack Squat", "2 x 12", "2.5 - 3 Dakika"),
                Triple("Leg Extension", "2 x Failure", "90 Saniye"),
                Triple("Leg Curl", "2 x Failure", "90 Saniye"),
                Triple("Calf Raise", "2 x Failure", "60 - 90 Saniye"),
                Triple("Karın (Leg Raise / Crunch)", "3 x Failure", "60 Saniye")
            ),
            "UPPER" to listOf(
                Triple("Incline Dumbbell Press", "2+2 x Failure", "2.5 - 3 Dakika"),
                Triple("Pec Deck Fly", "3 x Failure", "60 - 90 Saniye"),
                Triple("Dumbbell Press (Omuz)", "2 x Failure", "2.5 - 3 Dakika"),
                Triple("Lat Pulldown", "1+2 x 10", "2 - 3 Dakika"),
                Triple("Row Alternatifi", "1+2 x Failure", "2.5 - 3 Dakika"),
                Triple("Lateral Raise", "3 x 12", "60 - 90 Saniye"),
                Triple("Triceps Pushdown", "3 x 10", "60 - 90 Saniye"),
                Triple("Dumbbell Curl", "3 x 10", "60 - 90 Saniye")
            )
        )

        oldSplits.forEach { (splitName, exercises) ->
            val routineId = workoutDao.insertRoutine(Routine(name = splitName)).toInt()
            exercises.forEach { ex ->
                workoutDao.insertRoutineExercise(
                    RoutineExercise(routineId = routineId, name = ex.first, setsAndReps = ex.second, restTime = ex.third)
                )
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            workoutDao.getAllSessionsWithSets().collect { sessionList ->
                val completedSessions = sessionList.filter { it.session.isCompleted }
                val lastDateStr = if (completedSessions.isNotEmpty()) {
                    val dateFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("tr"))
                    dateFormat.format(java.util.Date(completedSessions.first().session.date))
                } else {
                    "Antrenman bulunamadı"
                }
                _uiState.update { it.copy(sessions = sessionList, lastWorkoutDate = lastDateStr) }
            }
        }

        viewModelScope.launch {
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            workoutDao.getWeeklyVolume(sevenDaysAgo).collect { volume ->
                _uiState.update { it.copy(totalVolumeThisWeek = volume ?: 0.0) }
            }
        }
    }

    suspend fun createNewSession(): Int {
        val newSession = WorkoutSession(date = System.currentTimeMillis(), bodyWeight = null, notes = null, isCompleted = false)
        return workoutDao.insertSession(newSession).toInt()
    }

    suspend fun getSessionWithSetsOnce(sessionId: Int): SessionWithSets? = workoutDao.getSessionWithSetsById(sessionId).firstOrNull()

    suspend fun saveProgress(sessionId: Int, activeSets: List<WorkoutSet>, isFinished: Boolean) {
        workoutDao.deleteSetsBySessionId(sessionId)
        activeSets.forEach { if (it.reps > 0) workoutDao.insertSet(it) }
        if (isFinished) workoutDao.markSessionAsCompleted(sessionId)
    }

    fun deleteSession(sessionId: Int) {
        viewModelScope.launch {
            workoutDao.deleteSetsBySessionId(sessionId)
            workoutDao.deleteSessionById(sessionId)
        }
    }

    suspend fun getPreviousRecord(exerciseName: String): WorkoutSet? = workoutDao.getLastRecord(exerciseName)

    // --- PROGRAM VE HAREKET YÖNETİMİ ---
    fun addNewRoutine(name: String) {
        viewModelScope.launch { workoutDao.insertRoutine(Routine(name = name)) }
    }

    fun deleteRoutineAndExercises(routineId: Int) {
        viewModelScope.launch {
            workoutDao.deleteExercisesByRoutineId(routineId)
            workoutDao.deleteRoutine(routineId)
        }
    }

    fun addExerciseToRoutine(routineId: Int, name: String, setsAndReps: String, restTime: String) {
        viewModelScope.launch {
            // Listenin en sonuna eklemek için mevcut hareket sayısını buluyoruz
            val currentRoutine = _routines.value.find { it.routine.id == routineId }
            val nextIndex = currentRoutine?.exercises?.size ?: 0

            workoutDao.insertRoutineExercise(
                RoutineExercise(routineId = routineId, name = name, setsAndReps = setsAndReps, restTime = restTime, orderIndex = nextIndex)
            )
        }
    }

    fun removeExerciseFromRoutine(exerciseId: Int) {
        viewModelScope.launch { workoutDao.deleteRoutineExerciseById(exerciseId) }
    }

    // 1. Hareketi Düzenle
    fun updateExercise(exercise: RoutineExercise, newName: String, newSets: String, newRest: String) {
        viewModelScope.launch {
            workoutDao.updateRoutineExercise(
                exercise.copy(name = newName, setsAndReps = newSets, restTime = newRest)
            )
        }
    }

    // Sürükle-bırak işlemi bittiğinde yeni sırayı veritabanına kaydeder
    fun updateRoutineExercisesOrder(exercises: List<com.example.overloadtracker.data.RoutineExercise>) {
        viewModelScope.launch {
            // Her hareketin orderIndex'ini güncel sırasına göre ayarlıyoruz
            val updatedList = exercises.mapIndexed { index, ex ->
                ex.copy(orderIndex = index)
            }
            workoutDao.updateRoutineExercises(updatedList)
        }
    }
}