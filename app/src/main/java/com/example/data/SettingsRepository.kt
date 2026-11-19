package com.example.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

enum class VfxIntensity(val label: String) {
    LOW("NHẸ"),
    NORMAL("CHUẨN"),
    ULTRA("SIÊU CẤP")
}

enum class CrystalAuraTheme(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    PIXIE_MINT("Ngọc Bích", PixieMint, PixieCyan),
    ROSE_NOVA("Hồng Tinh", PixieRose, PixieGold),
    CYBER_CYAN("Băng Lam", PixieCyan, PixieMint),
    SOLAR_GOLD("Hoàng Kim", PixieGold, PixieRose)
}

enum class AutoSyncInterval(val intervalMs: Long) {
    FAST_4S(4_000L),
    STANDARD_8S(8_000L),
    RELAXED_20S(20_000L)
}

enum class AppLanguage(
    val code: String,
    val nativeName: String
) {
    VI("VI", "Tiếng Việt"),
    EN("EN", "English"),
    JA("JA", "日本語"),
    KO("KO", "한국어"),
    ZH("ZH", "中文"),
    ES("ES", "Español"),
    FR("FR", "Français"),
    DE("DE", "Deutsch"),
    PT("PT", "Português"),
    RU("RU", "Русский"),
    TH("TH", "ไทย"),
    ID("ID", "Bahasa Indonesia")
}

data class GameSettings(
    val language: AppLanguage = AppLanguage.VI,
    val vfxIntensity: VfxIntensity = VfxIntensity.NORMAL,
    val crystalAuraTheme: CrystalAuraTheme = CrystalAuraTheme.PIXIE_MINT,
    val ambientParticlesEnabled: Boolean = true,
    val showArcaneRings: Boolean = true,
    val showComboCounter: Boolean = true,
    val showFloatingNumbers: Boolean = true,
    val showAutoFloatingNumbers: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val screenShakeEnabled: Boolean = true,
    val showUpgradeBadges: Boolean = true,
    val showDollarProgressCard: Boolean = true,
    val confirmBeforeRestart: Boolean = true,
    val compactNumbers: Boolean = false,
    val autoCloudSyncEnabled: Boolean = true,
    val autoSyncInterval: AutoSyncInterval = AutoSyncInterval.STANDARD_8S
)

interface SettingsStore {
    val settingsFlow: Flow<GameSettings>
    suspend fun update(transform: (GameSettings) -> GameSettings)
    suspend fun reset()
}

private val Context.pixieSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pixie_clicker_settings"
)

class DataStoreSettingsRepository(
    private val context: Context
) : SettingsStore {

    private object Keys {
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val VFX_INTENSITY = stringPreferencesKey("vfx_intensity")
        val CRYSTAL_AURA = stringPreferencesKey("crystal_aura_theme")
        val AMBIENT_PARTICLES = booleanPreferencesKey("ambient_particles")
        val SHOW_ARCANE_RINGS = booleanPreferencesKey("show_arcane_rings")
        val SHOW_COMBO = booleanPreferencesKey("show_combo")
        val SHOW_FLOATING = booleanPreferencesKey("show_floating")
        val SHOW_AUTO_FLOATING = booleanPreferencesKey("show_auto_floating")
        val SOUND_EFFECTS = booleanPreferencesKey("sound_effects_enabled")
        val HAPTIC = booleanPreferencesKey("haptic_enabled")
        val SCREEN_SHAKE = booleanPreferencesKey("screen_shake_enabled")
        val SHOW_UPGRADE_BADGES = booleanPreferencesKey("show_upgrade_badges")
        val SHOW_DOLLAR_PROGRESS = booleanPreferencesKey("show_dollar_progress")
        val CONFIRM_RESTART = booleanPreferencesKey("confirm_restart")
        val COMPACT_NUMBERS = booleanPreferencesKey("compact_numbers")
        val AUTO_CLOUD_SYNC = booleanPreferencesKey("auto_cloud_sync")
        val AUTO_SYNC_INTERVAL = stringPreferencesKey("auto_sync_interval")
    }

    override val settingsFlow: Flow<GameSettings> = context.pixieSettingsDataStore.data.map { prefs ->
        fromPreferences(prefs)
    }

    override suspend fun update(transform: (GameSettings) -> GameSettings) {
        context.pixieSettingsDataStore.edit { prefs ->
            val current = fromPreferences(prefs)
            val next = transform(current)
            prefs[Keys.APP_LANGUAGE] = next.language.name
            prefs[Keys.VFX_INTENSITY] = next.vfxIntensity.name
            prefs[Keys.CRYSTAL_AURA] = next.crystalAuraTheme.name
            prefs[Keys.AMBIENT_PARTICLES] = next.ambientParticlesEnabled
            prefs[Keys.SHOW_ARCANE_RINGS] = next.showArcaneRings
            prefs[Keys.SHOW_COMBO] = next.showComboCounter
            prefs[Keys.SHOW_FLOATING] = next.showFloatingNumbers
            prefs[Keys.SHOW_AUTO_FLOATING] = next.showAutoFloatingNumbers
            prefs[Keys.SOUND_EFFECTS] = next.soundEffectsEnabled
            prefs[Keys.HAPTIC] = next.hapticEnabled
            prefs[Keys.SCREEN_SHAKE] = next.screenShakeEnabled
            prefs[Keys.SHOW_UPGRADE_BADGES] = next.showUpgradeBadges
            prefs[Keys.SHOW_DOLLAR_PROGRESS] = next.showDollarProgressCard
            prefs[Keys.CONFIRM_RESTART] = next.confirmBeforeRestart
            prefs[Keys.COMPACT_NUMBERS] = next.compactNumbers
            prefs[Keys.AUTO_CLOUD_SYNC] = next.autoCloudSyncEnabled
            prefs[Keys.AUTO_SYNC_INTERVAL] = next.autoSyncInterval.name
        }
    }

    override suspend fun reset() {
        context.pixieSettingsDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    private fun fromPreferences(prefs: Preferences): GameSettings {
        val lang = prefs[Keys.APP_LANGUAGE]?.let { name ->
            AppLanguage.entries.firstOrNull { it.name == name }
        } ?: AppLanguage.VI

        val vfx = prefs[Keys.VFX_INTENSITY]?.let { name ->
            VfxIntensity.entries.firstOrNull { it.name == name }
        } ?: VfxIntensity.NORMAL

        val aura = prefs[Keys.CRYSTAL_AURA]?.let { name ->
            CrystalAuraTheme.entries.firstOrNull { it.name == name }
        } ?: CrystalAuraTheme.PIXIE_MINT

        val syncInterval = prefs[Keys.AUTO_SYNC_INTERVAL]?.let { name ->
            AutoSyncInterval.entries.firstOrNull { it.name == name }
        } ?: AutoSyncInterval.STANDARD_8S

        return GameSettings(
            language = lang,
            vfxIntensity = vfx,
            crystalAuraTheme = aura,
            ambientParticlesEnabled = prefs[Keys.AMBIENT_PARTICLES] ?: true,
            showArcaneRings = prefs[Keys.SHOW_ARCANE_RINGS] ?: true,
            showComboCounter = prefs[Keys.SHOW_COMBO] ?: true,
            showFloatingNumbers = prefs[Keys.SHOW_FLOATING] ?: true,
            showAutoFloatingNumbers = prefs[Keys.SHOW_AUTO_FLOATING] ?: true,
            soundEffectsEnabled = prefs[Keys.SOUND_EFFECTS] ?: true,
            hapticEnabled = prefs[Keys.HAPTIC] ?: true,
            screenShakeEnabled = prefs[Keys.SCREEN_SHAKE] ?: true,
            showUpgradeBadges = prefs[Keys.SHOW_UPGRADE_BADGES] ?: true,
            showDollarProgressCard = prefs[Keys.SHOW_DOLLAR_PROGRESS] ?: true,
            confirmBeforeRestart = prefs[Keys.CONFIRM_RESTART] ?: true,
            compactNumbers = prefs[Keys.COMPACT_NUMBERS] ?: false,
            autoCloudSyncEnabled = prefs[Keys.AUTO_CLOUD_SYNC] ?: true,
            autoSyncInterval = syncInterval
        )
    }
}

class InMemorySettingsStore(initial: GameSettings = GameSettings()) : SettingsStore {
    private val state = MutableStateFlow(initial)
    override val settingsFlow: Flow<GameSettings> = state.asStateFlow()

    override suspend fun update(transform: (GameSettings) -> GameSettings) {
        state.value = transform(state.value)
    }

    override suspend fun reset() {
        state.value = GameSettings()
    }
}
