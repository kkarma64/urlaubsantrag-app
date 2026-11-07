package de.bauhaupt.urlaubsschein.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.bauhaupt.urlaubsschein.data.db.entity.Antrag
import de.bauhaupt.urlaubsschein.data.db.relation.AntragMitDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface AntragDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(antrag: Antrag): Long

    @Update
    suspend fun update(antrag: Antrag)

    @Delete
    suspend fun delete(antrag: Antrag)

    @Transaction
    @Query("SELECT * FROM antrag WHERE id = :id")
    fun beobachteMitDetails(id: Long): Flow<AntragMitDetails?>

    @Transaction
    @Query("SELECT * FROM antrag WHERE id = :id")
    suspend fun ladeMitDetails(id: Long): AntragMitDetails?

    @Query("SELECT * FROM antrag WHERE status = :status ORDER BY tarif_von DESC")
    fun beobachteNachStatus(status: String): Flow<List<Antrag>>

    @Query("SELECT * FROM antrag ORDER BY erstellt_am DESC")
    fun beobachteAlle(): Flow<List<Antrag>>

    @Query("SELECT * FROM antrag ORDER BY erstellt_am DESC")
    suspend fun ladeAlleList(): List<Antrag>

    @Query("SELECT * FROM antrag WHERE antrags_nr = :nr LIMIT 1")
    suspend fun findeNachNr(nr: String): Antrag?

    @Query("SELECT * FROM antrag WHERE id = :id LIMIT 1")
    suspend fun ladeAntragNachId(id: Long): Antrag?

    @Query("SELECT COUNT(*) FROM antrag WHERE jahr = :jahr")
    suspend fun anzahlImJahr(jahr: Int): Int

    @Query("SELECT * FROM antrag WHERE mail_status = :mailStatus ORDER BY erstellt_am ASC")
    suspend fun ladeNachMailStatus(mailStatus: String): List<Antrag>
}
