package de.bauhaupt.urlaubsschein.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.Instant

@Entity(
    tableName = "status_protokoll",
    foreignKeys = [
        ForeignKey(
            entity = Antrag::class,
            parentColumns = ["id"],
            childColumns = ["antrag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("antrag_id"), Index("zeitpunkt")]
)
data class StatusProtokoll(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "antrag_id")
    val antragId: Long,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "zeitpunkt")
    val zeitpunkt: Instant,

    @ColumnInfo(name = "kommentar", defaultValue = "NULL")
    val kommentar: String? = null,

    @ColumnInfo(name = "quelle", defaultValue = "SYSTEM")
    val quelle: String = "SYSTEM"
)