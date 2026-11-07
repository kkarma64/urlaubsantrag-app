package de.bauhaupt.urlaubsschein.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "vertretung",
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
data class Vertretung(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "antrag_id")
    val antragId: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "pnr", defaultValue = "NULL")
    val pnr: String? = null,

    @ColumnInfo(name = "email", defaultValue = "NULL")
    val email: String? = null,

    @ColumnInfo(name = "sort_index", defaultValue = "0")
    val sortIndex: Int = 0
)