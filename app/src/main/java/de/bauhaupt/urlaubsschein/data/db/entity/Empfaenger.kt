package de.bauhaupt.urlaubsschein.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

@Entity(
    tableName = "empfaenger",
    indices = [Index(value = ["rolle", "email"], unique = true)]
)
data class Empfaenger(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "rolle")
    val rolle: String,

    @ColumnInfo(name = "typ")
    val typ: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "anzeigename", defaultValue = "NULL")
    val anzeigename: String? = null,

    @ColumnInfo(name = "aktiv", defaultValue = "1")
    val aktiv: Boolean = true,

    @ColumnInfo(name = "sort_index", defaultValue = "0")
    val sortIndex: Int = 0
)