package de.bauhaupt.urlaubsschein.logic

import android.content.Context
import android.net.Uri
import com.google.gson.*
import de.bauhaupt.urlaubsschein.data.db.AppDatabase
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.LocalDate

data class AppBackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val antraege: List<de.bauhaupt.urlaubsschein.data.db.entity.Antrag>
)

object BackupManager {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ -> LocalDate.parse(json.asString) })
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer { src: LocalDate, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(Instant::class.java, JsonDeserializer { json, _, _ -> Instant.parse(json.asString) })
        .registerTypeAdapter(Instant::class.java, JsonSerializer { src: Instant, _, _ -> JsonPrimitive(src.toString()) })
        .create()

    suspend fun exportBackup(context: Context, db: AppDatabase, uri: Uri): Boolean {
        return try {
            val antraege = db.antragDao().ladeAlleList()
            val backupData = AppBackupData(antraege = antraege)
            val jsonString = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importBackup(context: Context, db: AppDatabase, uri: Uri, merge: Boolean): Boolean {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream, Charsets.UTF_8).use { reader ->
                    reader.readText()
                }
            } ?: return false

            val backupData = gson.fromJson(jsonString, AppBackupData::class.java)
                ?: return false

            if (!merge) {
                val existing = db.antragDao().ladeAlleList()
                existing.forEach { db.antragDao().delete(it) }
            }

            backupData.antraege.forEach { antrag ->
                try {
                    db.antragDao().insert(antrag)
                } catch (e: Exception) {
                    try {
                        db.antragDao().update(antrag)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
