package de.bauhaupt.urlaubsschein.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.bauhaupt.urlaubsschein.logic.Bundesland
import de.bauhaupt.urlaubsschein.logic.FeiertagsEngine
import de.bauhaupt.urlaubsschein.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CustomDatePickerDialog(
    initialDate: LocalDate,
    startDate: LocalDate? = null,
    endDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var displayedYearMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Zeitraum wählen",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    color = AccentYellow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monat / Jahr Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayedYearMonth = displayedYearMonth.minusMonths(1) }) {
                        Text("◀", color = AccentYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${displayedYearMonth.month.getDisplayName(TextStyle.FULL, Locale.GERMAN)} ${displayedYearMonth.year}",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { displayedYearMonth = displayedYearMonth.plusMonths(1) }) {
                        Text("▶", color = AccentYellow, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Wochentage (Beginnend mit Montag!)
                val weekdays = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    weekdays.forEach { day ->
                        val isWeekend = day == "Sa" || day == "So"
                        Text(
                            text = day,
                            color = if (isWeekend) Color.Red.copy(alpha = 0.8f) else TextGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Tage-Gitter
                val firstDayOfMonth = displayedYearMonth.atDay(1)
                val daysInMonth = displayedYearMonth.lengthOfMonth()
                val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
                val totalCells = ((firstDayOfWeek - 1) + daysInMonth + 6) / 7 * 7

                val bundesland = Bundesland.HE
                val feiertage = remember(displayedYearMonth.year) { FeiertagsEngine.getFeiertage(displayedYearMonth.year, bundesland) }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (week in 0 until (totalCells / 7)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            for (dayOfWeek in 1..7) {
                                val dayIndex = week * 7 + dayOfWeek - 1
                                val dayNumber = dayIndex - (firstDayOfWeek - 2)
                                if (dayNumber in 1..daysInMonth) {
                                    val date = displayedYearMonth.atDay(dayNumber)
                                    val isSelected = date == selectedDate
                                    val isStart = startDate != null && date == startDate
                                    val isEnd = endDate != null && date == endDate
                                    val isInRange = startDate != null && endDate != null && !date.isBefore(startDate) && !date.isAfter(endDate)

                                    val isWeekend = date.dayOfWeek.value >= 6
                                    val isFeiertag = feiertage.contains(date)

                                    val bgColor = when {
                                        isSelected || isStart || isEnd -> AccentYellow
                                        isInRange -> AccentYellow.copy(alpha = 0.35f)
                                        isFeiertag -> Color(0xFFD9383A).copy(alpha = 0.3f)
                                        isWeekend -> Color.DarkGray.copy(alpha = 0.3f)
                                        else -> Color.Transparent
                                    }

                                    val textColor = when {
                                        isSelected || isStart || isEnd -> DarkBackground
                                        isInRange -> TextWhite
                                        isFeiertag -> Color(0xFFFF6B6B)
                                        isWeekend -> Color.LightGray
                                        else -> TextWhite
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(bgColor, CircleShape)
                                            .clickable { selectedDate = date },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            color = textColor,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected || isStart || isEnd || isFeiertag) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDateSelected(selectedDate) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Auswählen", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }
    )
}
