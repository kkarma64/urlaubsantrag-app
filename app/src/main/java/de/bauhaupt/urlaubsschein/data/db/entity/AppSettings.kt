package de.bauhaupt.urlaubsschein.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.Instant

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long = 1L,

    @ColumnInfo(name = "default_bundesland", defaultValue = "'HE'")
    val defaultBundesland: String = "HE",

    @ColumnInfo(name = "default_arbeitstage", defaultValue = "5")
    val defaultArbeitstage: Int = 5,

    @ColumnInfo(name = "sprache", defaultValue = "'de'")
    val sprache: String = "de",

    @ColumnInfo(name = "onboarding_abgeschlossen", defaultValue = "0")
    val onboardingAbgeschlossen: Boolean = false,

    @ColumnInfo(name = "einwilligung_unterschrift", defaultValue = "0")
    val einwilligungUnterschrift: Boolean = false,

    @ColumnInfo(name = "einwilligung_am", defaultValue = "NULL")
    val einwilligungAm: Instant? = null,

    @ColumnInfo(name = "app_lock_aktiv", defaultValue = "1")
    val appLockAktiv: Boolean = true,

    @ColumnInfo(name = "biometrie_bevorzugt", defaultValue = "1")
    val biometrieBevorzugt: Boolean = true,

    @ColumnInfo(name = "lohnbuero_mail_getestet", defaultValue = "0")
    val lohnbueroMailGetestet: Boolean = false,

    @ColumnInfo(name = "letztes_backup_am", defaultValue = "NULL")
    val letztesBackupAm: Instant? = null,

    @ColumnInfo(name = "art9_einwilligung", defaultValue = "NULL")
    val art9Einwilligung: Boolean? = null,

    @ColumnInfo(name = "art9_einwilligung_am", defaultValue = "NULL")
    val art9EinwilligungAm: Instant? = null
)