package de.bauhaupt.urlaubsschein.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.bauhaupt.urlaubsschein.data.db.entity.Profil
import de.bauhaupt.urlaubsschein.data.db.relation.ProfilMitArbeitsmodell
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfilDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profil: Profil)

    @Update
    suspend fun update(profil: Profil)

    @Transaction
    @Query("SELECT * FROM profil WHERE id = 1")
    fun beobachte(): Flow<ProfilMitArbeitsmodell?>

    @Transaction
    @Query("SELECT * FROM profil WHERE id = 1")
    suspend fun lade(): ProfilMitArbeitsmodell?
}