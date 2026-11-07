package de.bauhaupt.urlaubsschein.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.bauhaupt.urlaubsschein.data.db.entity.Anhang

@Dao
interface AnhangDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(anhang: Anhang): Long

    @Query("SELECT * FROM anhang WHERE antrag_id = :antragId")
    suspend fun ladeFuerAntrag(antragId: Long): List<Anhang>

    @Query("SELECT * FROM anhang WHERE antrag_id = :antragId AND typ = :typ")
    suspend fun ladeFuerAntragUndTyp(antragId: Long, typ: String): List<Anhang>
}