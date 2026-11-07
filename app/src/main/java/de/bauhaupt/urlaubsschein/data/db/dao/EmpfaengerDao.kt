package de.bauhaupt.urlaubsschein.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.bauhaupt.urlaubsschein.data.db.entity.Empfaenger
import kotlinx.coroutines.flow.Flow

@Dao
interface EmpfaengerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(empfaenger: Empfaenger)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAlle(empfaenger: List<Empfaenger>)

    @Query("SELECT * FROM empfaenger WHERE aktiv = 1 ORDER BY rolle, sort_index")
    fun beobachteAktive(): Flow<List<Empfaenger>>

    @Query("SELECT * FROM empfaenger WHERE aktiv = 1 AND rolle = :rolle ORDER BY sort_index")
    suspend fun ladeAktiveNachRolle(rolle: String): List<Empfaenger>
}