package com.example.overloadtracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // 1. Yeni Antrenman Oturumu Ekleme
    @Insert
    suspend fun insertSession(session: WorkoutSession): Long

    // 2. Antrenmana Ait Seti Ekleme
    @Insert
    suspend fun insertSet(workoutSet: WorkoutSet)

    // 3. Geçmiş Sekmesi İçin (Oturumlar ve içindeki setler tek seferde gelir)
    @Transaction
    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    fun getAllSessionsWithSets(): Flow<List<SessionWithSets>>

    // 4. Ana Ekran İçin (Son 7 Günlük Hacim)
    @Query("""
        SELECT SUM(weight * reps) 
        FROM workout_sets 
        INNER JOIN workout_sessions ON workout_sets.sessionId = workout_sessions.id 
        WHERE date >= :oneWeekAgo
    """)
    fun getWeeklyVolume(oneWeekAgo: Long): Flow<Double?>

    // 5. Antrenman Ekranı İçin (Geçmiş Rekor / Contextual History)
    @Query("SELECT * FROM workout_sets WHERE exerciseName = :exerciseName ORDER BY id DESC LIMIT 1")
    suspend fun getLastRecord(exerciseName: String): WorkoutSet?

    // Belirli bir ID'ye sahip SessionWithSets verisini çekmek için (Karta tıklandığında detayı açmak için)
    @Transaction
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    fun getSessionWithSetsById(sessionId: Int): Flow<SessionWithSets?>

    // Antrenmanı bitirdiğimizde isCompleted durumunu true yapmak için
    @Query("UPDATE workout_sessions SET isCompleted = 1 WHERE id = :sessionId")
    suspend fun markSessionAsCompleted(sessionId: Int)

    @Query("DELETE FROM workout_sets WHERE sessionId = :sessionId")
    suspend fun deleteSetsBySessionId(sessionId: Int)

    @Query("DELETE FROM workout_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Int)
}