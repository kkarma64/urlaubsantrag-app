package de.bauhaupt.urlaubsschein.data.db

import android.content.Context
import androidx.room.Room
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            // Für den Anfang nutzen wir einen festen Schlüssel.
            // In v1.1 bauen wir das auf Android Keystore um.
            val passphrase: ByteArray = SQLiteDatabase.getBytes("mein_geheimer_schluessel_123".toCharArray())
            val factory = SupportFactory(passphrase)

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "urlaubsschein_database"
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration() // Erlaubt Neu-Erstellung während der Entwicklung
                .build()

            INSTANCE = instance
            instance
        }
    }
}