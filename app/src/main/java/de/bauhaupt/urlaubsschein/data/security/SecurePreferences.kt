package de.bauhaupt.urlaubsschein.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurePreferences(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var vorname: String
        get() = sharedPreferences.getString("key_vorname", "Max") ?: "Max"
        set(value) = sharedPreferences.edit().putString("key_vorname", value).apply()

    var name: String
        get() = sharedPreferences.getString("key_name", "Mustermann") ?: "Mustermann"
        set(value) = sharedPreferences.edit().putString("key_name", value).apply()

    var personalnummer: String
        get() = sharedPreferences.getString("key_pnr", "12345") ?: "12345"
        set(value) = sharedPreferences.edit().putString("key_pnr", value).apply()

    var kostenstelle: String
        get() = sharedPreferences.getString("key_kostenstelle", "700.501") ?: "700.501"
        set(value) = sharedPreferences.edit().putString("key_kostenstelle", value).apply()

    var signaturePath: String
        get() = sharedPreferences.getString("key_signature_path", "") ?: ""
        set(value) = sharedPreferences.edit().putString("key_signature_path", value).apply()

    var signatureNormY: Float
        get() = sharedPreferences.getFloat("key_signature_norm_y", 1.0f)
        set(value) = sharedPreferences.edit().putFloat("key_signature_norm_y", value).apply()

    var signatureScale: Float
        get() = sharedPreferences.getFloat("key_signature_scale", 1.0f)
        set(value) = sharedPreferences.edit().putFloat("key_signature_scale", value).apply()

    var resturlaubVorjahr: Int
        get() = sharedPreferences.getInt("key_resturlaub", 5)
        set(value) = sharedPreferences.edit().putInt("key_resturlaub", value).apply()

    var jahresanspruch: Int
        get() = sharedPreferences.getInt("key_anspruch", 30)
        set(value) = sharedPreferences.edit().putInt("key_anspruch", value).apply()

    var baustelleBundesland: String
        get() = sharedPreferences.getString("key_bundesland", "HE") ?: "HE"
        set(value) = sharedPreferences.edit().putString("key_bundesland", value).apply()

    var standardBundesland: String
        get() = sharedPreferences.getString("key_std_bundesland", "HE") ?: "HE"
        set(value) = sharedPreferences.edit().putString("key_std_bundesland", value).apply()

    var standardPolier: String
        get() = sharedPreferences.getString("key_std_polier", "Heiko Schneider") ?: "Heiko Schneider"
        set(value) = sharedPreferences.edit().putString("key_std_polier", value).apply()

    var standardPolierEmail: String
        get() = sharedPreferences.getString("key_std_polier_email", "polier@nameunternehmen.de") ?: "polier@nameunternehmen.de"
        set(value) = sharedPreferences.edit().putString("key_std_polier_email", value).apply()

    var useBiometrics: Boolean
        get() = sharedPreferences.getBoolean("key_use_biometrics", false)
        set(value) = sharedPreferences.edit().putBoolean("key_use_biometrics", value).apply()
}
