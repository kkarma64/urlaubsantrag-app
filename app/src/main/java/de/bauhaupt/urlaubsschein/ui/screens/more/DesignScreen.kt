package de.bauhaupt.urlaubsschein.ui.screens.more

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import de.bauhaupt.urlaubsschein.data.security.SecurePreferences
import de.bauhaupt.urlaubsschein.logic.PasswordHasher
import de.bauhaupt.urlaubsschein.ui.components.GridBackground
import de.bauhaupt.urlaubsschein.ui.theme.StatusGreen
import de.bauhaupt.urlaubsschein.ui.theme.ThemeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val appPrefs = remember { AppPreferencesRepository(context) }
    val securePrefs = remember { SecurePreferences(context) }
    val themeViewModel: ThemeViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val darkModeSetting by themeViewModel.isDarkMode.collectAsState(initial = null)
    val selectedOption = when (darkModeSetting) {
        true -> "Dunkel"
        false -> "Hell"
        else -> "Systemstandard"
    }

    val lockTimeoutMs by appPrefs.lockTimeout.collectAsState(initial = 60000L)
    val storedHash by appPrefs.passwortHash.collectAsState(initial = null)

    var timeoutDropdownExpanded by remember { mutableStateOf(false) }

    var passwordActive by remember { mutableStateOf(false) }
    var fingerprintActive by remember { mutableStateOf(securePrefs.useBiometrics) }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var dialogError by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(storedHash) {
        passwordActive = !storedHash.isNullOrBlank()
        if (!passwordActive) {
            fingerprintActive = false
            securePrefs.useBiometrics = false
        }
    }

    val timeoutOptions = listOf(
        0L to "0 Sekunden (Sofort)",
        15000L to "15 Sekunden",
        60000L to "60 Sekunden (Standard)",
        300000L to "300 Sekunden (5 Minuten)",
        Long.MAX_VALUE to "Unendlich (Nie sperren)"
    )

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                if (storedHash.isNullOrBlank()) {
                    passwordActive = false
                }
            },
            title = { Text("Sicherheitspasswort festlegen", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; dialogError = null },
                        label = { Text("Neues Passwort (min. 6 Zeichen)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; dialogError = null },
                        label = { Text("Neues Passwort wiederholen") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (dialogError != null) {
                        Text(text = dialogError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword.length < 6) {
                            dialogError = "Das Passwort muss mindestens 6 Zeichen lang sein."
                            return@Button
                        }
                        if (newPassword != confirmPassword) {
                            dialogError = "Die Passwörter stimmen nicht überein."
                            return@Button
                        }
                        scope.launch {
                            val newHash = PasswordHasher.hash(newPassword)
                            appPrefs.setPasswortHash(newHash)
                            passwordActive = true
                            showPasswordDialog = false
                            newPassword = ""
                            confirmPassword = ""
                            successMessage = "✓ Passwort erfolgreich gespeichert!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Speichern", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    if (storedHash.isNullOrBlank()) {
                        passwordActive = false
                    }
                }) {
                    Text("Abbrechen", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    GridBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Design & Sicherheit",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onBack) {
                    Text("Zurück", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            // --- ERSCHEINUNGSBILD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("ERSCHEINUNGSBILD WÄHLEN", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    val options = listOf("Systemstandard", "Hell", "Dunkel")

                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (option) {
                                        "Systemstandard" -> themeViewModel.setDarkMode(null)
                                        "Hell" -> themeViewModel.setDarkMode(false)
                                        "Dunkel" -> themeViewModel.setDarkMode(true)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = {
                                    when (option) {
                                        "Systemstandard" -> themeViewModel.setDarkMode(null)
                                        "Hell" -> themeViewModel.setDarkMode(false)
                                        "Dunkel" -> themeViewModel.setDarkMode(true)
                                    }
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // --- SICHERHEIT & PASSWORT ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("APP-SICHERHEIT", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Passwort zum Entsperren", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Schützt deine Anträge vor unbefugtem Zugriff", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                        Switch(
                            checked = passwordActive,
                            onCheckedChange = { enabled ->
                                if (!enabled) {
                                    // Sofort ohne Dialog deaktivieren
                                    passwordActive = false
                                    fingerprintActive = false
                                    securePrefs.useBiometrics = false
                                    scope.launch {
                                        appPrefs.setPasswortHash("")
                                        successMessage = "✓ Passwort deaktiviert"
                                    }
                                } else {
                                    passwordActive = true
                                    showPasswordDialog = true
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    if (passwordActive) {
                        Button(
                            onClick = { showPasswordDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Passwort ändern", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fingerabdruck verwenden", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Biometrische Entsperrung aktivieren", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                        Switch(
                            checked = fingerprintActive,
                            enabled = passwordActive,
                            onCheckedChange = { enabled ->
                                fingerprintActive = enabled
                                securePrefs.useBiometrics = enabled
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }

            // --- SPERRBILDSCHIRM & TIMEOUT ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("SPERRBILDSCHIRM-TIMEOUT", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                    Text("Automatisches Sperren nach Inaktivität im Hintergrund:", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)

                    Box {
                        OutlinedButton(
                            onClick = { timeoutDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = timeoutOptions.find { it.first == lockTimeoutMs }?.second ?: "60 Sekunden (Standard)",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("▼", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }

                        DropdownMenu(
                            expanded = timeoutDropdownExpanded,
                            onDismissRequest = { timeoutDropdownExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            timeoutOptions.forEach { (ms, label) ->
                                DropdownMenuItem(
                                    text = { Text(text = label, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp) },
                                    onClick = {
                                        timeoutDropdownExpanded = false
                                        scope.launch {
                                            appPrefs.setLockTimeout(ms)
                                        }
                                    },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                )
                            }
                        }
                    }
                }
            }

            if (successMessage != null) {
                Text(
                    text = successMessage!!,
                    color = StatusGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
