package de.bauhaupt.urlaubsschein.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "antrag",
    indices = [
        Index("status"),
        Index("jahr"),
        Index(value = ["antrags_nr"], unique = true)
    ]
)
data class Antrag(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,

    @ColumnInfo(name = "antrags_nr")
    val antragsNr: String,

    @ColumnInfo(name = "jahr")
    val jahr: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "pnr")
    val pnr: String,

    @ColumnInfo(name = "baustelle")
    val baustelle: String,

    @ColumnInfo(name = "kostenstelle", defaultValue = "NULL")
    val kostenstelle: String? = null,

    @ColumnInfo(name = "gewerk")
    val gewerk: String,

    @ColumnInfo(name = "kolonne")
    val kolonne: String,

    @ColumnInfo(name = "polier")
    val polier: String,

    @ColumnInfo(name = "bauleiter")
    val bauleiter: String,

    @ColumnInfo(name = "bundesland")
    val bundesland: String,

    @ColumnInfo(name = "tarif_von")
    val tarifVon: LocalDate,

    @ColumnInfo(name = "tarif_bis")
    val tarifBis: LocalDate,

    @ColumnInfo(name = "tarif_tage")
    val tarifTage: Int,

    @ColumnInfo(name = "ist_halber_tag", defaultValue = "0")
    val istHalberTag: Boolean = false,

    @ColumnInfo(name = "tages_teil", defaultValue = "NULL")
    val tagesTeil: String? = null,

    @ColumnInfo(name = "sonderurlaub", defaultValue = "0")
    val sonderurlaub: Boolean = false,

    @ColumnInfo(name = "sonderurlaub_von", defaultValue = "NULL")
    val sonderurlaubVon: LocalDate? = null,

    @ColumnInfo(name = "sonderurlaub_bis", defaultValue = "NULL")
    val sonderurlaubBis: LocalDate? = null,

    @ColumnInfo(name = "sonderurlaub_tage", defaultValue = "NULL")
    val sonderurlaubTage: Int? = null,

    @ColumnInfo(name = "sonderurlaub_grund", defaultValue = "NULL")
    val sonderurlaubGrund: String? = null,

    @ColumnInfo(name = "rueckkehrdatum")
    val rueckkehrdatum: LocalDate,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "ist_versendet", defaultValue = "0")
    val istVersendet: Boolean = false,

    @ColumnInfo(name = "mail_status", defaultValue = "'PENDING'")
    val mailStatus: String = "PENDING",

    @ColumnInfo(name = "letzter_versand_versuch", defaultValue = "NULL")
    val letzterVersandVersuch: Instant? = null,

    @ColumnInfo(name = "unterschrift_uri", defaultValue = "NULL")
    val unterschriftUri: String? = null,

    @ColumnInfo(name = "unterschrift_art", defaultValue = "NULL")
    val unterschriftArt: String? = null,

    @ColumnInfo(name = "betreff", defaultValue = "NULL")
    val betreff: String? = null,

    @ColumnInfo(name = "dateiname", defaultValue = "NULL")
    val dateiname: String? = null,

    @ColumnInfo(name = "erstellt_am")
    val erstelltAm: Instant,

    @ColumnInfo(name = "geaendert_am")
    val geaendertAm: Instant,

    @ColumnInfo(name = "eingereicht_am", defaultValue = "NULL")
    val eingereichtAm: Instant? = null,

    @ColumnInfo(name = "genehmigt_am", defaultValue = "NULL")
    val genehmigtAm: Instant? = null,

    @ColumnInfo(name = "storniert_am", defaultValue = "NULL")
    val storniertAm: Instant? = null,

    @ColumnInfo(name = "storno_grund", defaultValue = "NULL")
    val stornoGrund: String? = null,

    @ColumnInfo(name = "halbe_tage_json", defaultValue = "NULL")
    val halbeTageJson: String? = null,

    @ColumnInfo(name = "abwesenheitstext", defaultValue = "NULL")
    val abwesenheitstext: String? = null,

    @ColumnInfo(name = "paragraph9_hinweis", defaultValue = "NULL")
    val paragraph9Hinweis: String? = null
)
