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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.AppLanguage
import com.example.ui.i18n.PixieStrings
import com.example.ui.theme.PixieBgCard
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieBorder
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieTextMuted
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary

@Composable
fun LanguagePickerCard(
    selectedLanguage: AppLanguage,
    strings: PixieStrings,
    onSelectLanguage: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }

    PixieCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = PixieGold.copy(alpha = 0.85f),
        glowColor = PixieGold
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PixieBgDeep)
                    .border(1.5.dp, PixieGold.copy(alpha = 0.75f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = PixieGold,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.settingsLanguageTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = PixieTextPrimary
                )
                Text(
                    text = strings.settingsLanguageDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixieTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Compact selector button showing currently chosen language; tap to open full language list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            PixieGold.copy(alpha = 0.22f),
                            PixieBgDeep.copy(alpha = 0.96f)
                        )
                    )
                )
                .border(2.dp, PixieGold, RoundedCornerShape(9.dp))
                .clickable(
                    role = Role.Button,
                    indication = ripple(bounded = true, color = PixieGold),
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { showLanguageDialog = true }
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("language_selector_button"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(PixieGold)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = selectedLanguage.code,
                    style = MaterialTheme.typography.labelMedium,
                    color = PixieBgDeep,
                    maxLines = 1,
                    softWrap = false
                )
            }
            Text(
                text = selectedLanguage.nativeName,
                style = MaterialTheme.typography.titleMedium,
                color = PixieGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = strings.settingsLanguageTitle,
                tint = PixieGold,
                modifier = Modifier.size(22.dp)
            )
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = selectedLanguage,
            strings = strings,
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { lang ->
                showLanguageDialog = false
                onSelectLanguage(lang)
            }
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    selectedLanguage: AppLanguage,
    strings: PixieStrings,
    onDismiss: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PixieCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            borderColor = PixieGold,
            containerColor = PixieBgCard,
            glowColor = PixieGold,
            contentPadding = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Language,
                    contentDescription = null,
                    tint = PixieGold,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = strings.settingsLanguageTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = PixieGold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("language_dialog_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = strings.confirmCancel,
                        tint = PixieTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .testTag("language_dialog_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = AppLanguage.entries,
                    key = { it.name }
                ) { lang ->
                    val isSelected = selectedLanguage == lang
                    val accent = if (isSelected) PixieGold else PixieMint
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) PixieGold.copy(alpha = 0.22f) else PixieBgDeep
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) PixieGold else PixieBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable(
                                role = Role.RadioButton,
                                indication = ripple(bounded = true, color = accent),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = { onSelectLanguage(lang) }
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("lang_option_${lang.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) PixieGold else PixieBorder.copy(alpha = 0.50f)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lang.code,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) PixieBgDeep else PixieTextPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            text = lang.nativeName,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) PixieGold else PixieTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = lang.nativeName,
                                tint = PixieGold,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(PixieTextMuted.copy(alpha = 0.4f))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text(
                        text = strings.confirmCancel,
                        style = MaterialTheme.typography.labelLarge,
                        color = PixieTextSecondary
                    )
                }
            }
        }
    }
}
