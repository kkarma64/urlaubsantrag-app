package de.bauhaupt.urlaubsschein

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog as ComposeAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.javiersantos.appupdater.AppUpdater
import de.bauhaupt.urlaubsschein.data.repository.AppPreferencesRepository
import de.bauhaupt.urlaubsschein.ui.navigation.MainScaffold
import de.bauhaupt.urlaubsschein.ui.screens.AuthViewModel
import de.bauhaupt.urlaubsschein.ui.screens.HelpScreen
import de.bauhaupt.urlaubsschein.ui.screens.LockScreen
import de.bauhaupt.urlaubsschein.ui.screens.OnboardingScreen
import de.bauhaupt.urlaubsschein.ui.screens.PasswortEinrichtenScreen
import de.bauhaupt.urlaubsschein.ui.screens.SplashScreen
import de.bauhaupt.urlaubsschein.ui.screens.UpdateViewModel
import de.bauhaupt.urlaubsschein.ui.theme.ThemeViewModel
import de.bauhaupt.urlaubsschein.ui.theme.UrlaubsscheinBauTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var backgroundTimestamp = 0L
    private var isAppInBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Locale.setDefault(Locale.GERMAN) // Startet die Kalenderwoche mit Montag
        enableEdgeToEdge()

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_PAUSE -> {
                    backgroundTimestamp = System.currentTimeMillis()
                    isAppInBackground = true
                }
                else -> {}
            }
        }
        lifecycle.addObserver(lifecycleObserver)

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val updateViewModel: UpdateViewModel = viewModel()

            val darkModeSetting by themeViewModel.isDarkMode.collectAsState(initial = null)
            val onboardingDone by authViewModel.onboardingAbgeschlossen.collectAsState(initial = null)
            val passwordHash by authViewModel.passwortHash.collectAsState(initial = null)
            val dialogMessage by updateViewModel.dialogMessage.collectAsState()
            val pendingApkUrl by updateViewModel.pendingApkUrl.collectAsState()

            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = darkModeSetting ?: systemDark

            val navController = rememberNavController()

            if (dialogMessage != null) {
                ComposeAlertDialog(
                    onDismissRequest = { updateViewModel.showMessage(null) },
                    title = { Text("Update-Prüfung", color = MaterialTheme.colorScheme.onSurface) },
                    text = { Text(dialogMessage!!, color = MaterialTheme.colorScheme.onSurface) },
                    confirmButton = {
                        Button(
                            onClick = { updateViewModel.showMessage(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("OK", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            if (pendingApkUrl != null) {
                ComposeAlertDialog(
                    onDismissRequest = { updateViewModel.setPendingApkUrl(null) },
                    title = { Text("Neue Version verfügbar!", color = MaterialTheme.colorScheme.onSurface) },
                    text = { Text("Es gibt ein Update für die Urlaubsantrags-App. Möchtest du es jetzt installieren?", color = MaterialTheme.colorScheme.onSurface) },
                    confirmButton = {
                        Button(
                            onClick = {
                                val url = pendingApkUrl!!
                                updateViewModel.setPendingApkUrl(null)
                                AppUpdater(this, url).start()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Jetzt aktualisieren", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { updateViewModel.setPendingApkUrl(null) }
                        ) {
                            Text("Später", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                        if (isAppInBackground) {
                            isAppInBackground = false
                            val elapsed = System.currentTimeMillis() - backgroundTimestamp
                            CoroutineScope(Dispatchers.IO).launch {
                                val appPrefs = AppPreferencesRepository(applicationContext)
                                val timeoutMs = appPrefs.lockTimeout.first()
                                val passHash = appPrefs.passwortHash.first()

                                if (!passHash.isNullOrBlank() && timeoutMs != Long.MAX_VALUE && (timeoutMs == 0L || elapsed > timeoutMs)) {
                                    withContext(Dispatchers.Main) {
                                        try {
                                            navController.navigate("lock") {
                                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                lifecycle.addObserver(observer)
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

            UrlaubsscheinBauTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (onboardingDone == null || passwordHash == null) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        val startDest = when {
                            onboardingDone != true -> "onboarding"
                            passwordHash == null -> "passwort_einrichten"
                            passwordHash!!.isBlank() -> "splash"
                            else -> "lock"
                        }

                        NavHost(
                            navController = navController,
                            startDestination = startDest
                        ) {
                            composable("onboarding") {
                                OnboardingScreen(
                                    onOnboardingAbgeschlossen = {
                                        navController.navigate("passwort_einrichten") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("passwort_einrichten") {
                                PasswortEinrichtenScreen(
                                    onPasswortEingerichtet = {
                                        navController.navigate("splash") {
                                            popUpTo("passwort_einrichten") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("lock") {
                                LockScreen(
                                    onUnlocked = {
                                        navController.navigate("splash") {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("splash") {
                                SplashScreen(
                                    onStartClick = {
                                        navController.navigate("main_scaffold") {
                                            popUpTo("splash") { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable("help_screen_splash") {
                                HelpScreen(onBack = { navController.popBackStack() })
                            }
                            composable("main_scaffold") {
                                MainScaffold()
                            }
                        }
                    }
                }
            }
        }

        // Automatische Update-Prüfung beim Start
        checkForUpdates("https://github.com/kkarma64/urlaubsantrag-app/releases/download/v1.0/version.json", false)
    }

    fun triggerManualUpdateCheck() {
        checkForUpdates("https://github.com/kkarma64/urlaubsantrag-app/releases/download/v1.0/version.json", true)
    }

    private fun showInfoDialog(message: String) {
        UpdateViewModel.instance?.showMessage(message)
    }

    private fun checkForUpdates(jsonUrl: String, manualCheck: Boolean) {
        Log.d("UpdateCheck", "Start checking updates from $jsonUrl (manual: $manualCheck)")
        if (manualCheck) {
            runOnUiThread {
                Toast.makeText(this, "Suche nach Updates...", Toast.LENGTH_SHORT).show()
            }
        }
        @Suppress("DEPRECATION")
        AsyncTask.execute {
            try {
                val url = URL(jsonUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                Log.d("UpdateCheck", "Response code: $responseCode")

                if (responseCode == 200) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonObject = JSONObject(jsonString)

                    val remoteVersionCode = jsonObject.optInt("versionCode", 0)
                    val apkUrl = jsonObject.optString("apkUrl", "")
                    Log.d("UpdateCheck", "Remote versionCode: $remoteVersionCode, apkUrl: $apkUrl")

                    val currentVersionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getPackageInfo(packageName, 0).versionCode
                    }
                    Log.d("UpdateCheck", "Current versionCode: $currentVersionCode")

                    runOnUiThread {
                        if (remoteVersionCode > currentVersionCode && apkUrl.isNotBlank()) {
                            showUpdateDialog(apkUrl)
                        } else if (manualCheck) {
                            showInfoDialog("Die neueste Version ist bereits installiert.")
                        }
                    }
                } else {
                    Log.d("UpdateCheck", "Server returned non-200 response code: $responseCode")
                    if (manualCheck) {
                        runOnUiThread {
                            showInfoDialog("Der Update-Server ist derzeit nicht erreichbar.")
                        }
                    }
                }
            } catch (e: UnknownHostException) {
                Log.d("UpdateCheck", "UnknownHostException: ${e.message}", e)
                if (manualCheck) {
                    runOnUiThread {
                        showInfoDialog("Update-Prüfung fehlgeschlagen. Bitte prüfe deine Internetverbindung.")
                    }
                }
            } catch (e: SocketTimeoutException) {
                Log.d("UpdateCheck", "SocketTimeoutException: ${e.message}", e)
                if (manualCheck) {
                    runOnUiThread {
                        showInfoDialog("Der Update-Server ist derzeit nicht erreichbar.")
                    }
                }
            } catch (e: Exception) {
                Log.d("UpdateCheck", "Exception caught: ${e.message}", e)
                e.printStackTrace()
                if (manualCheck) {
                    runOnUiThread {
                        showInfoDialog("Ein unerwarteter Fehler ist aufgetreten.")
                    }
                }
            }
        }
    }

    private fun showUpdateDialog(apkUrl: String) {
        UpdateViewModel.instance?.setPendingApkUrl(apkUrl)
    }
}
