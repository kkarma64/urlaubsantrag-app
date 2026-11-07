package de.bauhaupt.urlaubsschein.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.bauhaupt.urlaubsschein.data.db.entity.Arbeitsmodell

@Dao
interface ArbeitsmodellDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(modell: Arbeitsmodell)

    @Query("SELECT * FROM arbeitsmodell WHERE profil_id = 1")
    suspend fun lade(): Arbeitsmodell?
}