package de.bauhaupt.urlaubsschein.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.bauhaupt.urlaubsschein.data.db.entity.Vertretung

@Dao
interface VertretungDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(vertretung: Vertretung): Long

    @Query("SELECT * FROM vertretung WHERE antrag_id = :antragId ORDER BY sort_index")
    suspend fun ladeFuerAntrag(antragId: Long): List<Vertretung>

    @Query("DELETE FROM vertretung WHERE antrag_id = :antragId")
    suspend fun loescheFuerAntrag(antragId: Long)
}