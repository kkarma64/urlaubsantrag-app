package de.bauhaupt.urlaubsschein.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.Instant

@Entity(tableName = "ics_quelle")
data class IcsQuelle(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "warnung_aktiv", defaultValue = "1")
    val warnungAktiv: Boolean = true,

    @ColumnInfo(name = "letzter_abruf", defaultValue = "NULL")
    val letzterAbruf: Instant? = null,

    @ColumnInfo(name = "aktiv", defaultValue = "1")
    val aktiv: Boolean = true
)