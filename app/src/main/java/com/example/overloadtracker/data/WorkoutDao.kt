package com.example.overloadtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    @Insert
    suspend fun insertSet(workoutSet: WorkoutSet)

    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessionsWithSets(): Flow<List<SessionWithSets>>

    @Query("""
        SELECT SUM(weight * reps) 
        FROM workout_sets 
        INNER JOIN workout_sessions ON workout_sets.sessionId = workout_sessions.id 
        WHERE date >= :oneWeekAgo
    """)
    fun getWeeklyVolume(oneWeekAgo: Long): Flow<Double?>

    @Query("SELECT * FROM workout_sets WHERE exerciseName = :exerciseName ORDER BY id DESC LIMIT 1")
    suspend fun getLastRecord(exerciseName: String): WorkoutSet?

    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionWithSetsById(sessionId: Int): Flow<SessionWithSets?>

    @Query("UPDATE workout_sessions SET isCompleted = 1 WHERE id = :sessionId")
    suspend fun markSessionAsCompleted(sessionId: Int)

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsBySessionId(sessionId: Int)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Int)

    // --- YENİ EKLENEN RUTİN (PROGRAM) SORGULARI ---
    @Insert
    suspend fun insertRoutine(routine: Routine): Long

    @Insert
    suspend fun insertRoutineExercise(exercise: RoutineExercise)

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Int)

    @Query("DELETE FROM routine_exercises WHERE routineId = :routineId")
    suspend fun deleteExercisesByRoutineId(routineId: Int)

    @Query("DELETE FROM routine_exercises WHERE id = :exerciseId")
    suspend fun deleteRoutineExerciseById(exerciseId: Int)

    @androidx.room.Update
    suspend fun updateRoutineExercise(exercise: RoutineExercise)

    @androidx.room.Update
    suspend fun updateRoutineExercises(exercises: List<RoutineExercise>)

    @Transaction
    @Query("SELECT * FROM routines")
    fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>>
}