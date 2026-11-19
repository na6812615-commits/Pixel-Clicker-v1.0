package com.example.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppLanguage
import com.example.data.AutoSyncInterval
import com.example.data.CrystalAuraTheme
import com.example.data.GameProgress
import com.example.data.GameSettings
import com.example.data.VfxIntensity
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieBorder
import com.example.ui.theme.PixieBorderBright
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieDanger
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary

@Composable
fun SettingsTab(
    settings: GameSettings,
    progress: GameProgress,
    userEmail: String?,
    isSyncing: Boolean,
    onBack: () -> Unit,
    onSetVfxIntensity: (VfxIntensity) -> Unit,
    onSetCrystalAuraTheme: (CrystalAuraTheme) -> Unit,
    onToggleAmbientParticles: (Boolean) -> Unit,
    onToggleComboCounter: (Boolean) -> Unit,
    onToggleFloatingNumbers: (Boolean) -> Unit,
    onToggleHaptic: (Boolean) -> Unit,
    onToggleScreenShake: (Boolean) -> Unit,
    onToggleConfirmRestart: (Boolean) -> Unit,
    onToggleCompactNumbers: (Boolean) -> Unit,
    onToggleAutoCloudSync: (Boolean) -> Unit,
    onManualSync: () -> Unit,
    onResetSettings: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSetLanguage: (AppLanguage) -> Unit = {},
    onToggleArcaneRings: (Boolean) -> Unit = {},
    onToggleAutoFloatingNumbers: (Boolean) -> Unit = {},
    onToggleSoundEffects: (Boolean) -> Unit = {},
    onToggleUpgradeBadges: (Boolean) -> Unit = {},
    onToggleDollarProgressCard: (Boolean) -> Unit = {},
    onSetAutoSyncInterval: (AutoSyncInterval) -> Unit = {}
) {
    val s = LocalPixieStrings.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Card with Back Navigation + Auto-Saved Badge
        PixieCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = PixieCyan,
            glowColor = PixieCyan
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PixieBgDeep)
                        .border(1.5.dp, PixieCyan.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = s.settingsBack,
                        tint = PixieCyan
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.settingsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = PixieCyan
                    )
                    Text(
                        text = s.settingsSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixieTextSecondary
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = null,
                    tint = PixieGold,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixieMint.copy(alpha = 0.14f))
                    .border(1.dp, PixieMint.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("settings_auto_saved_badge"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = PixieMint,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = s.settingsAutoSavedBadge,
                    style = MaterialTheme.typography.labelMedium,
                    color = PixieMint
                )
            }
        }

        // Section 0: Language Selector
        PixieSectionHeader(
            title = s.settingsSectionLanguage,
            accentColor = PixieGold
        )

        LanguagePickerCard(
            selectedLanguage = settings.language,
            strings = s,
            onSelectLanguage = onSetLanguage
        )

        // Section 1: Visuals & VFX
        PixieSectionHeader(
            title = s.settingsSectionVfx,
            accentColor = PixieMint
        )

        VfxIntensityPickerCard(
            selectedIntensity = settings.vfxIntensity,
            strings = s,
            onSelectIntensity = onSetVfxIntensity
        )

        CrystalAuraPickerCard(
            selectedTheme = settings.crystalAuraTheme,
            strings = s,
            onSelectTheme = onSetCrystalAuraTheme
        )

        // Visual Toggle Switches
        PixieCard(modifier = Modifier.fillMaxWidth(), borderColor = PixieBorderBright) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Filled.NightsStay,
                    accent = PixieCyan,
                    title = s.ambientTitle,
                    subtitle = s.ambientDesc,
                    checked = settings.ambientParticlesEnabled,
                    onCheckedChange = onToggleAmbientParticles,
                    testTag = "setting_ambient_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.AutoFixHigh,
                    accent = PixieGold,
                    title = s.arcaneRingsTitle,
                    subtitle = s.arcaneRingsDesc,
                    checked = settings.showArcaneRings,
                    onCheckedChange = onToggleArcaneRings,
                    testTag = "setting_arcane_rings_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.FormatListNumbered,
                    accent = PixieRose,
                    title = s.comboTitle,
                    subtitle = s.comboDesc,
                    checked = settings.showComboCounter,
                    onCheckedChange = onToggleComboCounter,
                    testTag = "setting_combo_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.Numbers,
                    accent = PixieMint,
                    title = s.floatingTitle,
                    subtitle = s.floatingDesc,
                    checked = settings.showFloatingNumbers,
                    onCheckedChange = onToggleFloatingNumbers,
                    testTag = "setting_floating_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.Stars,
                    accent = PixieCyan,
                    title = s.autoFloatingTitle,
                    subtitle = s.autoFloatingDesc,
                    checked = settings.showAutoFloatingNumbers,
                    onCheckedChange = onToggleAutoFloatingNumbers,
                    testTag = "setting_auto_floating_switch"
                )
            }
        }

        // Section 2: Gameplay & Tactile Feedback
        PixieSectionHeader(
            title = s.settingsSectionGameplay,
            accentColor = PixieGold
        )

        PixieCard(modifier = Modifier.fillMaxWidth(), borderColor = PixieGold.copy(alpha = 0.65f)) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsSwitchRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    accent = PixieMint,
                    title = s.soundEffectsTitle,
                    subtitle = s.soundEffectsDesc,
                    checked = settings.soundEffectsEnabled,
                    onCheckedChange = onToggleSoundEffects,
                    testTag = "setting_sound_effects_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.Vibration,
                    accent = PixieGold,
                    title = s.hapticTitle,
                    subtitle = s.hapticDesc,
                    checked = settings.hapticEnabled,
                    onCheckedChange = onToggleHaptic,
                    testTag = "setting_haptic_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.Waves,
                    accent = PixieRose,
                    title = s.shakeTitle,
                    subtitle = s.shakeDesc,
                    checked = settings.screenShakeEnabled,
                    onCheckedChange = onToggleScreenShake,
                    testTag = "setting_shake_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.NotificationsActive,
                    accent = PixieMint,
                    title = s.upgradeBadgesTitle,
                    subtitle = s.upgradeBadgesDesc,
                    checked = settings.showUpgradeBadges,
                    onCheckedChange = onToggleUpgradeBadges,
                    testTag = "setting_upgrade_badges_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.MonetizationOn,
                    accent = PixieGold,
                    title = s.dollarMeterTitle,
                    subtitle = s.dollarMeterDesc,
                    checked = settings.showDollarProgressCard,
                    onCheckedChange = onToggleDollarProgressCard,
                    testTag = "setting_dollar_progress_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.Shield,
                    accent = PixieRose,
                    title = s.confirmRestartTitle,
                    subtitle = s.confirmRestartDesc,
                    checked = settings.confirmBeforeRestart,
                    onCheckedChange = onToggleConfirmRestart,
                    testTag = "setting_confirm_restart_switch"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    icon = Icons.Filled.Tune,
                    accent = PixieCyan,
                    title = s.compactNumbersTitle,
                    subtitle = s.compactNumbersDesc,
                    checked = settings.compactNumbers,
                    onCheckedChange = onToggleCompactNumbers,
                    testTag = "setting_compact_numbers_switch"
                )
            }
        }

        // Section 3: Cloud & Account
        PixieSectionHeader(
            title = s.settingsSectionCloud,
            accentColor = PixieCyan
        )

        PixieCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = PixieCyan.copy(alpha = 0.7f),
            glowColor = PixieCyan
        ) {
            SettingsSwitchRow(
                icon = Icons.Filled.CloudSync,
                accent = PixieCyan,
                title = s.autoSyncTitle,
                subtitle = s.autoSyncDesc,
                checked = settings.autoCloudSyncEnabled,
                onCheckedChange = onToggleAutoCloudSync,
                testTag = "setting_auto_sync_switch"
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(12.dp))

            AutoSyncIntervalSelector(
                selectedInterval = settings.autoSyncInterval,
                enabled = settings.autoCloudSyncEnabled,
                strings = s,
                onSelectInterval = onSetAutoSyncInterval
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingsDivider()
            Spacer(modifier = Modifier.height(12.dp))

            PlayerSnapshotCard(
                progress = progress,
                compactNumbers = settings.compactNumbers,
                strings = s
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PixieBgDeep)
                        .border(1.5.dp, PixieCyan, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = PixieCyan
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userEmail?.takeIf { it.isNotBlank() } ?: s.linkedGoogleAccount,
                        style = MaterialTheme.typography.titleMedium,
                        color = PixieTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = s.accountSummary(progress.clickCount, progress.lv3),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixieTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixieArcadeButton(
                    onClick = onManualSync,
                    accentColor = PixieMint,
                    enabled = !isSyncing,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("settings_sync_now_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudSync,
                        contentDescription = null,
                        tint = PixieBgDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSyncing) s.syncingBtn else s.syncNowBtn,
                        style = MaterialTheme.typography.labelLarge,
                        color = PixieBgDeep,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                PixieArcadeButton(
                    onClick = onResetSettings,
                    accentColor = PixieGold,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestartAlt,
                        contentDescription = null,
                        tint = PixieBgDeep,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = s.resetDefaultsBtn,
                        style = MaterialTheme.typography.labelLarge,
                        color = PixieBgDeep,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            PixieArcadeButton(
                onClick = onSignOutClick,
                accentColor = PixieDanger,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = PixieBgDeep,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s.signOut,
                    style = MaterialTheme.typography.labelLarge,
                    color = PixieBgDeep,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
