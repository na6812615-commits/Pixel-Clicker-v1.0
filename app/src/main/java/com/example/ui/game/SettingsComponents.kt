package com.example.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AutoSyncInterval
import com.example.data.CrystalAuraTheme
import com.example.data.GameProgress
import com.example.data.VfxIntensity
import com.example.ui.i18n.PixieStrings
import com.example.ui.theme.PixieBgCardHighlight
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieBorder
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextMuted
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary

@Composable
internal fun SettingsSwitchRow(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PixieBgDeep)
                .border(1.dp, accent.copy(alpha = if (checked) 0.8f else 0.3f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) accent else PixieTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PixieTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = PixieTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PixieBgDeep,
                checkedTrackColor = accent,
                uncheckedThumbColor = PixieTextSecondary,
                uncheckedTrackColor = PixieBgCardHighlight,
                uncheckedBorderColor = PixieBorder
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
internal fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(PixieBorder.copy(alpha = 0.45f))
    )
}

@Composable
internal fun AutoSyncIntervalSelector(
    selectedInterval: AutoSyncInterval,
    enabled: Boolean,
    strings: PixieStrings,
    onSelectInterval: (AutoSyncInterval) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Schedule,
                contentDescription = null,
                tint = if (enabled) PixieCyan else PixieTextMuted,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.syncIntervalTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) PixieTextPrimary else PixieTextMuted
                )
                Text(
                    text = strings.syncIntervalDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixieTextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AutoSyncInterval.entries.forEach { interval ->
                val selected = selectedInterval == interval
                val accent = when (interval) {
                    AutoSyncInterval.FAST_4S -> PixieGold
                    AutoSyncInterval.STANDARD_8S -> PixieCyan
                    AutoSyncInterval.RELAXED_20S -> PixieMint
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected && enabled) accent.copy(alpha = 0.22f) else PixieBgDeep
                        )
                        .border(
                            width = if (selected && enabled) 2.dp else 1.dp,
                            color = if (selected && enabled) accent else PixieBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            enabled = enabled,
                            role = Role.RadioButton,
                            indication = ripple(bounded = true, color = accent),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onSelectInterval(interval) }
                        )
                        .testTag("sync_interval_${interval.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.syncIntervalLabel(interval),
                        style = MaterialTheme.typography.labelLarge,
                        color = when {
                            !enabled -> PixieTextMuted
                            selected -> accent
                            else -> PixieTextSecondary
                        },
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
internal fun PlayerSnapshotCard(
    progress: GameProgress,
    compactNumbers: Boolean,
    strings: PixieStrings
) {
    val discount = GameEngine.round2(progress.giamgia).toString()
    val autoRateText = when {
        progress.gioihan -> strings.statusNotOwned
        !progress.autoEnabled -> strings.autoStatusOff
        else -> {
            val pts = GameEngine.formatGameNumber(GameEngine.pointsPerAutoClick(progress), compactNumbers)
            "+$pts/${GameEngine.formatSeconds(progress.cd2)}s"
        }
    }
    val cdText = "${GameEngine.formatSeconds(progress.cd)}s"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(PixieBgDeep)
            .border(1.dp, PixieGold.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .testTag("settings_player_snapshot"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = null,
            tint = PixieGold,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strings.playerSnapshotTitle,
                style = MaterialTheme.typography.labelLarge,
                color = PixieGold
            )
            Text(
                text = strings.playerSnapshotDetail(discount, autoRateText, cdText),
                style = MaterialTheme.typography.bodyMedium,
                color = PixieTextSecondary
            )
        }
    }
}

@Composable
internal fun VfxIntensityPickerCard(
    selectedIntensity: VfxIntensity,
    strings: PixieStrings,
    onSelectIntensity: (VfxIntensity) -> Unit
) {
    PixieCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = PixieMint.copy(alpha = 0.75f),
        glowColor = PixieMint
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = PixieMint,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.vfxIntensityTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = PixieTextPrimary
                )
                Text(
                    text = strings.vfxIntensityDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixieTextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VfxIntensity.entries.forEach { level ->
                val selected = selectedIntensity == level
                val accent = when (level) {
                    VfxIntensity.LOW -> PixieCyan
                    VfxIntensity.NORMAL -> PixieMint
                    VfxIntensity.ULTRA -> PixieRose
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) accent.copy(alpha = 0.24f) else PixieBgDeep)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) accent else PixieBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            role = Role.RadioButton,
                            indication = ripple(bounded = true, color = accent),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onSelectIntensity(level) }
                        )
                        .testTag("vfx_intensity_${level.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.vfxLabel(level),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) accent else PixieTextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
internal fun CrystalAuraPickerCard(
    selectedTheme: CrystalAuraTheme,
    strings: PixieStrings,
    onSelectTheme: (CrystalAuraTheme) -> Unit
) {
    PixieCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = selectedTheme.primaryColor.copy(alpha = 0.8f),
        glowColor = selectedTheme.primaryColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Palette,
                contentDescription = null,
                tint = selectedTheme.primaryColor,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.auraThemeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = PixieTextPrimary
                )
                Text(
                    text = strings.auraThemeDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixieTextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CrystalAuraTheme.entries.forEach { aura ->
                val selected = selectedTheme == aura
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) aura.primaryColor.copy(alpha = 0.20f) else PixieBgDeep)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) aura.primaryColor else PixieBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(
                            role = Role.RadioButton,
                            indication = ripple(bounded = true, color = aura.primaryColor),
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { onSelectTheme(aura) }
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .testTag("aura_theme_${aura.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(aura.primaryColor, aura.secondaryColor)
                                )
                            )
                            .border(1.5.dp, Color.White.copy(alpha = if (selected) 0.95f else 0.35f), CircleShape)
                    )
                    Text(
                        text = strings.auraName(aura),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) aura.primaryColor else PixieTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
