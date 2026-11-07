package de.bauhaupt.urlaubsschein.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.bauhaupt.urlaubsschein.data.db.dao.AnhangDao
import de.bauhaupt.urlaubsschein.data.db.dao.AntragDao
import de.bauhaupt.urlaubsschein.data.db.dao.ArbeitsmodellDao
import de.bauhaupt.urlaubsschein.data.db.dao.EmpfaengerDao
import de.bauhaupt.urlaubsschein.data.db.dao.IcsQuelleDao
import de.bauhaupt.urlaubsschein.data.db.dao.ProfilDao
import de.bauhaupt.urlaubsschein.data.db.dao.StatusProtokollDao
import de.bauhaupt.urlaubsschein.data.db.dao.VertretungDao
import de.bauhaupt.urlaubsschein.data.db.entity.Anhang
import de.bauhaupt.urlaubsschein.data.db.entity.Antrag
import de.bauhaupt.urlaubsschein.data.db.entity.AppSettings
import de.bauhaupt.urlaubsschein.data.db.entity.Arbeitsmodell
import de.bauhaupt.urlaubsschein.data.db.entity.Empfaenger
import de.bauhaupt.urlaubsschein.data.db.entity.IcsQuelle
import de.bauhaupt.urlaubsschein.data.db.entity.Profil
import de.bauhaupt.urlaubsschein.data.db.entity.StatusProtokoll
import de.bauhaupt.urlaubsschein.data.db.entity.Vertretung

@Database(
    entities = [
        Profil::class,
        Arbeitsmodell::class,
        Antrag::class,
        Vertretung::class,
        Anhang::class,
        StatusProtokoll::class,
        Empfaenger::class,
        IcsQuelle::class,
        AppSettings::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profilDao(): ProfilDao
    abstract fun arbeitsmodellDao(): ArbeitsmodellDao
    abstract fun antragDao(): AntragDao
    abstract fun vertretungDao(): VertretungDao
    abstract fun anhangDao(): AnhangDao
    abstract fun statusProtokollDao(): StatusProtokollDao
    abstract fun empfaengerDao(): EmpfaengerDao
    abstract fun icsQuelleDao(): IcsQuelleDao
}
