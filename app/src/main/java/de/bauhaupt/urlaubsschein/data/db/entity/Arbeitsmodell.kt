package de.bauhaupt.urlaubsschein.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "arbeitsmodell")
data class Arbeitsmodell(
    @PrimaryKey
    @ColumnInfo(name = "profil_id")
    val profilId: Long = 1L,

    @ColumnInfo(name = "sechs_tage_woche")
    val sechsTageWoche: Boolean = false,

    @ColumnInfo(name = "arbeitstage_maske", defaultValue = "31")
    val arbeitstageMaske: Int = 31,

    @ColumnInfo(name = "wochen_start_iso", defaultValue = "1")
    val wochenStartIso: Int = 1
)