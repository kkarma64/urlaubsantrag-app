package de.bauhaupt.urlaubsschein.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.bauhaupt.urlaubsschein.data.db.entity.StatusProtokoll

@Dao
interface StatusProtokollDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(protokoll: StatusProtokoll): Long

    @Query("SELECT * FROM status_protokoll WHERE antrag_id = :antragId ORDER BY zeitpunkt ASC")
    suspend fun ladeFuerAntrag(antragId: Long): List<StatusProtokoll>
}