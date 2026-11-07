package de.bauhaupt.urlaubsschein.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.bauhaupt.urlaubsschein.data.db.entity.IcsQuelle
import kotlinx.coroutines.flow.Flow

@Dao
interface IcsQuelleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(quelle: IcsQuelle)

    @Query("SELECT * FROM ics_quelle WHERE aktiv = 1")
    fun beobachteAktive(): Flow<List<IcsQuelle>>
}