package de.bauhaupt.urlaubsschein.data.repository

import de.bauhaupt.urlaubsschein.data.db.dao.AntragDao
import de.bauhaupt.urlaubsschein.data.db.dao.StatusProtokollDao
import de.bauhaupt.urlaubsschein.data.db.dao.VertretungDao
import de.bauhaupt.urlaubsschein.data.db.entity.Antrag
import de.bauhaupt.urlaubsschein.data.db.entity.StatusProtokoll
import de.bauhaupt.urlaubsschein.data.db.entity.Vertretung
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class AntragRepository(
    private val antragDao: AntragDao,
    private val statusProtokollDao: StatusProtokollDao,
    private val vertretungDao: VertretungDao
) {
    // Holt alle Anträge für den Home-Screen
    fun getAllAntraege(): Flow<List<Antrag>> = antragDao.beobachteAlle()

    suspend fun ladeAlleAntraegeList(): List<Antrag> = antragDao.ladeAlleList()

    suspend fun ladeAntragNachId(id: Long): Antrag? = antragDao.ladeAntragNachId(id)

    suspend fun updateAntrag(antrag: Antrag) {
        antragDao.update(antrag)
    }

    // Speichert einen neuen Antrag aus dem Wizard
    suspend fun saveAntragFromWizard(
        name: String,
        pnr: String,
        baustelle: String,
        kostenstelle: String,
        gewerk: String,
        kolonne: String,
        polier: String,
        bauleiter: String,
        bundesland: String,
        tarifVon: java.time.LocalDate,
        tarifBis: java.time.LocalDate,
        tarifTage: Int,
        rueckkehrdatum: java.time.LocalDate,
        vertretungName: String,
        initialStatus: String = "Entwurf",
        istVersendet: Boolean = false
    ): Long {
        val jahr = tarifVon.year
        val laufendeNummer = (antragDao.anzahlImJahr(jahr) + 1).toString().padStart(4, '0')
        val monat = tarifVon.monthValue.toString().padStart(2, '0')
        val antragsNr = "$jahr-$laufendeNummer-$monat"

        val jetzt = Instant.now()

        val neuerAntrag = Antrag(
            antragsNr = antragsNr,
            jahr = jahr,
            name = name,
            pnr = pnr,
            baustelle = baustelle,
            kostenstelle = kostenstelle,
            gewerk = gewerk,
            kolonne = kolonne,
            polier = polier,
            bauleiter = bauleiter,
            bundesland = bundesland,
            tarifVon = tarifVon,
            tarifBis = tarifBis,
            tarifTage = tarifTage,
            rueckkehrdatum = rueckkehrdatum,
            status = initialStatus,
            istVersendet = istVersendet,
            mailStatus = if (istVersendet) "SENT" else "PENDING",
            erstelltAm = jetzt,
            geaendertAm = jetzt,
            eingereichtAm = if (istVersendet) jetzt else null
        )

        // 1. Antrag speichern
        val antragId = antragDao.insert(neuerAntrag)

        // 2. Vertretung speichern
        if (vertretungName.isNotBlank()) {
            val vertretung = Vertretung(
                antragId = antragId,
                name = vertretungName
            )
            vertretungDao.insert(vertretung)
        }

        // 3. Status-Protokoll speichern
        val protokoll = StatusProtokoll(
            antragId = antragId,
            status = initialStatus,
            zeitpunkt = jetzt,
            kommentar = if (istVersendet) "Antrag per E-Mail eingereicht" else "Antrag als Entwurf gespeichert",
            quelle = "SYSTEM"
        )
        statusProtokollDao.insert(protokoll)

        return antragId
    }
}
