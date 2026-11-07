package de.bauhaupt.urlaubsschein

import de.bauhaupt.urlaubsschein.logic.Bundesland
import de.bauhaupt.urlaubsschein.logic.FeiertagsEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FeiertagsTest {

    @Test
    fun testOstersonntagBerechnung() {
        // Bekannte Ostersonntage
        assertEquals(LocalDate.of(2024, 3, 31), FeiertagsEngine.berechneOstersonntag(2024))
        assertEquals(LocalDate.of(2025, 4, 20), FeiertagsEngine.berechneOstersonntag(2025))
        assertEquals(LocalDate.of(2026, 4, 5), FeiertagsEngine.berechneOstersonntag(2026))
    }

    @Test
    fun testRueckkehrdatumMitFeiertag() {
        // Szenario: Hessen, 5-Tage-Woche. Urlaub endet am Mittwoch, 30.04.2025.
        // Der 01.05.2025 ist ein Donnerstag und Tag der Arbeit (Feiertag).
        // Der 02.05.2025 ist ein Freitag (Arbeitstag).
        val urlaubBis = LocalDate.of(2025, 4, 30)
        val rueckkehr = FeiertagsEngine.berechneRueckkehrdatum(
            urlaubBis = urlaubBis,
            sechsTageWoche = false,
            bundesland = Bundesland.HE
        )
        // Erwartung: 02.05.2025
        assertEquals(LocalDate.of(2025, 5, 2), rueckkehr)
    }
}