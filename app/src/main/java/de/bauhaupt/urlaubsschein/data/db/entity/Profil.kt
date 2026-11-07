package de.bauhaupt.urlaubsschein.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.Instant

@Entity(tableName = "profil")
data class Profil(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long = 1L,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "pnr")
    val pnr: String,

    @ColumnInfo(name = "baustelle")
    val baustelle: String,

    @ColumnInfo(name = "bundesland")
    val bundesland: String,

    @ColumnInfo(name = "gewerk")
    val gewerk: String,

    @ColumnInfo(name = "kolonne")
    val kolonne: String,

    @ColumnInfo(name = "polier")
    val polier: String,

    @ColumnInfo(name = "bauleiter")
    val bauleiter: String,

    @ColumnInfo(name = "polier_email", defaultValue = "NULL")
    val polierEmail: String? = null,

    @ColumnInfo(name = "bauleiter_email", defaultValue = "NULL")
    val bauleiterEmail: String? = null,

    @ColumnInfo(name = "technischer_leiter_email", defaultValue = "NULL")
    val technischerLeiterEmail: String? = null,

    @ColumnInfo(name = "lohnbuero_email", defaultValue = "NULL")
    val lohnbueroEmail: String? = null,

    @ColumnInfo(name = "bcc_email", defaultValue = "NULL")
    val bccEmail: String? = null,

    @ColumnInfo(name = "erstellt_am")
    val erstelltAm: Instant,

    @ColumnInfo(name = "geaendert_am")
    val geaendertAm: Instant
)