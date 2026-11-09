package de.bauhaupt.urlaubsschein.ui.wizard

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.bauhaupt.urlaubsschein.data.db.DatabaseProvider
import de.bauhaupt.urlaubsschein.data.repository.AntragRepository
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import de.bauhaupt.urlaubsschein.logic.Bundesland
import de.bauhaupt.urlaubsschein.logic.FeiertagsEngine
import de.bauhaupt.urlaubsschein.logic.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.LocalDate

object SonderurlaubGründe {
    const val EHE = "Eheschließung (3 Tage)"
    const val GEBURT = "Geburt des Kindes (2 Tage)"
    const val TOD = "Tod naher Angehöriger (2 Tage)"
    const val UMZUG = "Wohnungswechsel (2 Tage)"
    const val KRANKHEIT = "Erkrankung Familienmitglied (1 Tag)"
    const val ARZT = "Arztbesuche/Behördengänge"
    const val EIGENER = "Eigener Grund"

    fun alle(): List<String> = listOf(EHE, GEBURT, TOD, UMZUG, KRANKHEIT, ARZT, EIGENER)
}

data class Urlaubsperiode(
    val von: LocalDate = LocalDate.now(),
    val bis: LocalDate = LocalDate.now(),
    val tage: Int = 1
)

data class WizardUiState(
    val urlaubsArt: String = "Tarifurlaub",
    val sonderurlaubGrund: String = "",
    val urlaubsPerioden: List<Urlaubsperiode> = listOf(Urlaubsperiode()),
    val vonDatum: LocalDate = LocalDate.now(),
    val bisDatum: LocalDate = LocalDate.now(),
    val berechneteTage: Int = 1,
    val rueckkehrDatum: LocalDate = LocalDate.now().plusDays(1),
    val baustelle: String = "Kita Niestetal",
    val kostenstelle: String = "4400",
    val baustelleBundesland: Bundesland = Bundesland.HE,
    val gewerk: String = "Maurer",
    val kolonne: String = "Kolonne 3",
    val polier: String = "Meier",
    val vertretung: String = "Schmidt",
    val unterschriftVorhanden: Boolean = false,
    val pdfGeneriert: Boolean = false,
    val zeichnungPunkte: List<List<Offset>> = emptyList(),
    val schreibLinieYNorm: Float = 0.71875f
)

class WizardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DatabaseProvider.getDatabase(application)
    private val repository = AntragRepository(db.antragDao(), db.statusProtokollDao(), db.vertretungDao())
    private val _uiState = MutableStateFlow(WizardUiState())
    val uiState: StateFlow<WizardUiState> = _uiState.asStateFlow()

    private val istSechsTageWoche = false

    init {
        berechneDaten()
    }

    fun setUrlaubsArt(art: String) {
        _uiState.update { it.copy(urlaubsArt = art) }
        berechneDaten()
    }

    fun setSonderurlaubGrund(grund: String) {
        _uiState.update { it.copy(sonderurlaubGrund = grund) }
    }

    fun addUrlaubsperiode() {
        _uiState.update { state ->
            if (state.urlaubsPerioden.size < 3) {
                val lastBis = state.urlaubsPerioden.last().bis
                val neueVon = lastBis.plusDays(1)
                val neueBis = neueVon
                val neueTage = FeiertagsEngine.berechneArbeitstage(neueVon, neueBis, istSechsTageWoche, state.baustelleBundesland)
                val neuePerioden = state.urlaubsPerioden + Urlaubsperiode(von = neueVon, bis = neueBis, tage = neueTage)
                state.copy(urlaubsPerioden = neuePerioden)
            } else state
        }
        berechneDaten()
    }

    fun removeUrlaubsperiode(index: Int) {
        _uiState.update { state ->
            if (state.urlaubsPerioden.size > 1) {
                val neuePerioden = state.urlaubsPerioden.filterIndexed { i, _ -> i != index }
                state.copy(urlaubsPerioden = neuePerioden)
            } else state
        }
        berechneDaten()
    }

    fun updateUrlaubsperiodeVon(index: Int, datum: LocalDate) {
        _uiState.update { state ->
            val liste = state.urlaubsPerioden.toMutableList()
            if (index in liste.indices) {
                val p = liste[index]
                val neuesBis = if (datum.isAfter(p.bis)) datum else p.bis
                val tage = FeiertagsEngine.berechneArbeitstage(datum, neuesBis, istSechsTageWoche, state.baustelleBundesland)
                liste[index] = p.copy(von = datum, bis = neuesBis, tage = tage)
            }
            state.copy(urlaubsPerioden = liste)
        }
        berechneDaten()
    }

    fun updateUrlaubsperiodeBis(index: Int, datum: LocalDate) {
        _uiState.update { state ->
            val liste = state.urlaubsPerioden.toMutableList()
            if (index in liste.indices) {
                val p = liste[index]
                val neuesVon = if (datum.isBefore(p.von)) datum else p.von
                val tage = FeiertagsEngine.berechneArbeitstage(neuesVon, datum, istSechsTageWoche, state.baustelleBundesland)
                liste[index] = p.copy(von = neuesVon, bis = datum, tage = tage)
            }
            state.copy(urlaubsPerioden = liste)
        }
        berechneDaten()
    }

    fun setVonDatum(datum: LocalDate) {
        updateUrlaubsperiodeVon(0, datum)
    }

    fun setBisDatum(datum: LocalDate) {
        updateUrlaubsperiodeBis(0, datum)
    }

    fun updateDetails(
        baustelle: String,
        kostenstelle: String,
        bundesland: Bundesland,
        polier: String
    ) {
        _uiState.update {
            it.copy(
                baustelle = baustelle,
                kostenstelle = kostenstelle,
                baustelleBundesland = bundesland,
                polier = polier
            )
        }
        berechneDaten()
    }

    fun setUnterschriftGezeichnet() {
        _uiState.update { it.copy(unterschriftVorhanden = true) }
    }

    fun updateZeichnung(punkte: List<List<Offset>>) {
        _uiState.update { it.copy(zeichnungPunkte = punkte) }
    }

    fun updateSchreibLinieY(normY: Float) {
        _uiState.update { it.copy(schreibLinieYNorm = normY) }
    }

    suspend fun hatUeberschneidung(): Boolean {
        val state = _uiState.value
        val alle = repository.ladeAlleAntraegeList()
        for (a in alle) {
            val v1 = state.vonDatum
            val b1 = state.bisDatum
            val v2 = a.tarifVon
            val b2 = a.tarifBis
            if (!(b1.isBefore(v2) || v1.isAfter(b2))) {
                return true
            }
        }
        return false
    }

    suspend fun createDraftAntrag(): Long {
        val state = _uiState.value
        return repository.saveAntragFromWizard(
            name = "Rainer",
            pnr = "1042",
            baustelle = state.baustelle,
            kostenstelle = state.kostenstelle,
            gewerk = state.gewerk,
            kolonne = state.kolonne,
            polier = state.polier,
            bauleiter = "Müller",
            bundesland = state.baustelleBundesland.kuerzel,
            tarifVon = state.vonDatum,
            tarifBis = state.bisDatum,
            tarifTage = state.berechneteTage,
            rueckkehrdatum = state.rueckkehrDatum,
            vertretungName = state.vertretung,
            initialStatus = "Entwurf",
            istVersendet = false
        )
    }

    suspend fun markAsSent(antragId: Long) {
        val antrag = repository.ladeAntragNachId(antragId)
        if (antrag != null) {
            val updated = antrag.copy(
                istVersendet = true,
                status = "EINGEREICHT",
                mailStatus = "SENT",
                eingereichtAm = Instant.now()
            )
            repository.updateAntrag(updated)
        }
    }

    fun submitAntrag(context: android.content.Context, signatureFile: File?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            try {
                val antragId = createDraftAntrag()
                val pdfFile = PdfGenerator.generatePdf(context, state, signatureFile)
                _uiState.update { it.copy(pdfGeneriert = true) }

                val appPrefs = AppPreferencesRepository(context)
                val empfaenger = appPrefs.empfaengerEmail.first() ?: "lohnbuero@nameunternehmen.de"
                val securePrefs = de.bauhaupt.urlaubsschein.data.security.SecurePreferences(context)
                val polierEmail = securePrefs.standardPolierEmail

                val sent = sendUrlaubMailWithRecipients(context, pdfFile, antragId, empfaenger, polierEmail)
                if (sent) {
                    markAsSent(antragId)
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitAntragWithRecipients(
        context: android.content.Context,
        signatureFile: File?,
        lohnbueroEmail: String,
        polierEmail: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val state = _uiState.value
            try {
                val antragId = createDraftAntrag()
                val pdfFile = PdfGenerator.generatePdf(context, state, signatureFile)
                _uiState.update { it.copy(pdfGeneriert = true) }

                val sent = sendUrlaubMailWithRecipients(context, pdfFile, antragId, lohnbueroEmail, polierEmail)
                if (sent) {
                    markAsSent(antragId)
                }
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun sendUrlaubMailWithRecipients(
        context: android.content.Context,
        pdfFile: File,
        antragId: Long,
        lohnbueroEmail: String,
        polierEmail: String
    ): Boolean {
        return try {
            val appPrefs = AppPreferencesRepository(context)
            val betreffFormat = appPrefs.betreffFormat.first()

            val antrag = repository.ladeAntragNachId(antragId)
            val nameVal = antrag?.name ?: "Mitarbeiter"
            val zeitraumVal = "${antrag?.tarifVon?.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))} - ${antrag?.tarifBis?.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
            val datumVal = antrag?.tarifVon?.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: ""

            val betreff = betreffFormat
                .replace("{Name}", nameVal)
                .replace("{Zeitraum}", zeitraumVal)
                .replace("{Datum}", datumVal)

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val recipients = listOf(lohnbueroEmail).filter { it.isNotBlank() }.toTypedArray()
            val ccRecipients = listOf(polierEmail).filter { it.isNotBlank() }.toTypedArray()

            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "application/pdf"
                if (recipients.isNotEmpty()) {
                    putExtra(android.content.Intent.EXTRA_EMAIL, recipients)
                }
                if (ccRecipients.isNotEmpty()) {
                    putExtra(android.content.Intent.EXTRA_CC, ccRecipients)
                }
                putExtra(android.content.Intent.EXTRA_SUBJECT, betreff)
                putExtra(android.content.Intent.EXTRA_TEXT, "Anbei mein Urlaubsantrag für den Zeitraum $zeitraumVal.")
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(android.content.Intent.createChooser(intent, "Urlaubsantrag versenden..."))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resetWizard() {
        _uiState.value = WizardUiState()
        berechneDaten()
    }

    private fun berechneDaten() {
        val state = _uiState.value
        val bundesland = state.baustelleBundesland
        val gesamtTage = state.urlaubsPerioden.sumOf { p ->
            if (p.bis.isBefore(p.von)) 0 else FeiertagsEngine.berechneArbeitstage(p.von, p.bis, istSechsTageWoche, bundesland)
        }
        val letzteBis = state.urlaubsPerioden.maxOf { it.bis }
        val rueckkehr = FeiertagsEngine.berechneRueckkehrdatum(
            urlaubBis = letzteBis,
            sechsTageWoche = istSechsTageWoche,
            bundesland = bundesland
        )
        val ersteVon = state.urlaubsPerioden.first().von
        val letzteBisPeriode = state.urlaubsPerioden.last().bis
        _uiState.update { it.copy(
            vonDatum = ersteVon,
            bisDatum = letzteBisPeriode,
            berechneteTage = gesamtTage,
            rueckkehrDatum = rueckkehr
        ) }
    }
}
