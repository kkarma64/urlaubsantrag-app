package de.bauhaupt.urlaubsschein.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.Instant

@Entity(
    tableName = "anhang",
    foreignKeys = [
        ForeignKey(
            entity = Antrag::class,
            parentColumns = ["id"],
            childColumns = ["antrag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("antrag_id")]
)
data class Anhang(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "antrag_id")
    val antragId: Long,

    @ColumnInfo(name = "typ")
    val typ: String,

    @ColumnInfo(name = "dateiname")
    val dateiname: String,

    @ColumnInfo(name = "mime", defaultValue = "NULL")
    val mime: String? = null,

    @ColumnInfo(name = "datei_uri")
    val dateiUri: String,

    @ColumnInfo(name = "groesse_bytes", defaultValue = "0")
    val groesseBytes: Long = 0L,

    @ColumnInfo(name = "erstellt_am")
    val erstelltAm: Instant,

    @ColumnInfo(name = "art9_einwilligung", defaultValue = "NULL")
    val art9Einwilligung: Boolean? = null
)