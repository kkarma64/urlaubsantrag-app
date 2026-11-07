package de.bauhaupt.urlaubsschein.logic

import java.time.DayOfWeek
import java.time.LocalDate

object FeiertagsEngine {

    /**
     * Berechnet alle gesetzlichen Feiertage für ein bestimmtes Jahr und Bundesland.
     */
    fun getFeiertage(jahr: Int, bundesland: Bundesland): Set<LocalDate> {
        val feiertage = mutableSetOf<LocalDate>()

        // Feste Feiertage (für alle Bundesländer)
        feiertage.add(LocalDate.of(jahr, 1, 1))   // Neujahr
        feiertage.add(LocalDate.of(jahr, 5, 1))   // Tag der Arbeit
        feiertage.add(LocalDate.of(jahr, 10, 3))  // Tag der Deutschen Einheit
        feiertage.add(LocalDate.of(jahr, 12, 25)) // 1. Weihnachtstag
        feiertage.add(LocalDate.of(jahr, 12, 26)) // 2. Weihnachtstag

        // Berechnung des Ostersonntags (Gauß/Meeus-Algorithmus)
        val ostersonntag = berechneOstersonntag(jahr)

        // Bewegliche Feiertage (für alle Bundesländer)
        feiertage.add(ostersonntag.minusDays(2))  // Karfreitag
        feiertage.add(ostersonntag.plusDays(1))   // Ostermontag
        feiertage.add(ostersonntag.plusDays(39))  // Christi Himmelfahrt
        feiertage.add(ostersonntag.plusDays(50))  // Pfingstmontag

        // Bundesland-spezifische Feiertage
        when (bundesland) {
            Bundesland.BW, Bundesland.BY, Bundesland.SL -> {
                feiertage.add(LocalDate.of(jahr, 1, 6))   // Heilige Drei Könige
                feiertage.add(ostersonntag.plusDays(60))  // Fronleichnam
                feiertage.add(LocalDate.of(jahr, 11, 1))  // Allerheiligen
            }
            Bundesland.HE -> {
                // Fronleichnam in Hessen (WICHTIG: Nur in katholisch geprägten Landkreisen)
                // Die App berechnet es vorsichtshalber als Feiertag, der Nutzer muss es ggf. anpassen.
                feiertage.add(ostersonntag.plusDays(60))
            }
            Bundesland.NW, Bundesland.RP -> {
                feiertage.add(ostersonntag.plusDays(60))  // Fronleichnam
                feiertage.add(LocalDate.of(jahr, 11, 1))  // Allerheiligen
            }
            Bundesland.SN -> {
                feiertage.add(LocalDate.of(jahr, 11, 23)) // Buß- und Bettag (Sachsen)
            }
            Bundesland.BB -> {
                feiertage.add(ostersonntag)               // Ostersonntag (Brandenburg)
                feiertage.add(ostersonntag.plusDays(49))  // Pfingstsonntag (Brandenburg)
            }
            Bundesland.MV, Bundesland.SN, Bundesland.ST, Bundesland.TH,
            Bundesland.BE, Bundesland.HB, Bundesland.HH, Bundesland.NI, Bundesland.SH -> {
                // Reformationstag (seit 2018 in Norddeutschland)
                feiertage.add(LocalDate.of(jahr, 10, 31))
            }
            // ÄNDERUNG: 'else -> {}' entfernt, da 'when' über ein Enum exhaustiv ist
        }

        return feiertage
    }

    /**
     * Gauß'sche Osterformel (Meeus-Algorithmus für den Gregorianischen Kalender).
     */
    fun berechneOstersonntag(jahr: Int): LocalDate {
        val a = jahr % 19
        val b = jahr / 100
        val c = jahr % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(jahr, month, day)
    }

    /**
     * Berechnet das Rückkehrdatum (den nächsten Arbeitstag) nach dem Urlaub.
     */
    fun berechneRueckkehrdatum(
        urlaubBis: LocalDate,
        sechsTageWoche: Boolean,
        bundesland: Bundesland
    ): LocalDate {
        var rueckkehr = urlaubBis.plusDays(1)
        while (true) {
            val istWochenende = if (sechsTageWoche) {
                rueckkehr.dayOfWeek == DayOfWeek.SUNDAY
            } else {
                rueckkehr.dayOfWeek == DayOfWeek.SATURDAY || rueckkehr.dayOfWeek == DayOfWeek.SUNDAY
            }
            val feiertage = getFeiertage(rueckkehr.year, bundesland)
            val istFeiertag = feiertage.contains(rueckkehr)

            if (!istWochenende && !istFeiertag) {
                break
            }
            rueckkehr = rueckkehr.plusDays(1)
        }
        return rueckkehr
    }

    /**
     * Berechnet die Anzahl der Arbeitstage (Urlaubstage) zwischen zwei Daten.
     */
    fun berechneArbeitstage(
        von: LocalDate,
        bis: LocalDate,
        sechsTageWoche: Boolean,
        bundesland: Bundesland
    ): Int {
        var arbeitstage = 0
        var aktuellerTag = von
        while (!aktuellerTag.isAfter(bis)) {
            val istWochenende = if (sechsTageWoche) {
                aktuellerTag.dayOfWeek == DayOfWeek.SUNDAY
            } else {
                aktuellerTag.dayOfWeek == DayOfWeek.SATURDAY || aktuellerTag.dayOfWeek == DayOfWeek.SUNDAY
            }
            val feiertage = getFeiertage(aktuellerTag.year, bundesland)
            val istFeiertag = feiertage.contains(aktuellerTag)

            if (!istWochenende && !istFeiertag) {
                arbeitstage++
            }
            aktuellerTag = aktuellerTag.plusDays(1)
        }
        return arbeitstage
    }
}