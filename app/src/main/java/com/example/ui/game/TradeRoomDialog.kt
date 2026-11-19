package com.example.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GameProgress
import com.example.data.TradeOfferValidation
import com.example.data.TradeRules
import com.example.data.TradeSession
import com.example.data.TradeStatus
import com.example.ui.i18n.LocalPixieSocialStrings
import com.example.ui.theme.PixieBgCard
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieBgSurface
import com.example.ui.theme.PixieBorder
import com.example.ui.theme.PixieBorderBright
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieDanger
import com.example.ui.theme.PixieDangerDark
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieMintDark
import com.example.ui.theme.PixieTextMuted
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary
import kotlin.math.floor

@Composable
fun TradeRoomDialog(
    trade: TradeSession,
    currentUserId: String,
    progress: GameProgress,
    compactNumbers: Boolean,
    isTradeBusy: Boolean,
    onSetOffer: (String, String) -> Unit,
    onToggleAgree: (String, String) -> Unit,
    onCancelAndLeave: () -> Unit,
    onDismissTerminal: () -> Unit
) {
    val social = LocalPixieSocialStrings.current
    val status = trade.parsedStatus
    val partnerName = trade.partnerNameFor(currentUserId)

    Dialog(
        onDismissRequest = {
            if (status == TradeStatus.COMPLETED || status == TradeStatus.CANCELLED) {
                onDismissTerminal()
            } else {
                onCancelAndLeave()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        PixieCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .widthIn(max = 560.dp)
                .heightIn(max = 760.dp),
            borderColor = when (status) {
                TradeStatus.CANCELLED -> PixieDanger
                TradeStatus.COMPLETED -> PixieMint
                else -> PixieGold
            },
            containerColor = PixieBgSurface,
            glowColor = when (status) {
                TradeStatus.CANCELLED -> PixieDanger
                TradeStatus.COMPLETED -> PixieMint
                else -> PixieGold
            },
            contentPadding = 14.dp
        ) {
            when (status) {
                TradeStatus.CANCELLED -> {
                    TerminalTradeView(
                        icon = Icons.Filled.Cancel,
                        accent = PixieDanger,
                        title = social.tradePartnerCancelledTitle,
                        body = social.tradePartnerCancelledBody(partnerName),
                        closeLabel = social.serverBtnClose,
                        onClose = onDismissTerminal
                    )
                }

                TradeStatus.COMPLETED -> {
                    TerminalTradeView(
                        icon = Icons.Filled.CheckCircle,
                        accent = PixieMint,
                        title = social.tradeCompletedTitle,
                        body = social.tradeCompletedBody,
                        closeLabel = social.serverBtnClose,
                        summaryLine = formatCompletedSummary(trade, currentUserId, partnerName, compactNumbers),
                        onClose = onDismissTerminal
                    )
                }

                TradeStatus.ACTIVE, TradeStatus.PENDING -> {
                    ActiveTradeRoomContent(
                        trade = trade,
                        currentUserId = currentUserId,
                        progress = progress,
                        compactNumbers = compactNumbers,
                        isTradeBusy = isTradeBusy,
                        onSetOffer = onSetOffer,
                        onToggleAgree = onToggleAgree,
                        onCancelAndLeave = onCancelAndLeave
                    )
                }
            }
        }
    }
}

@Composable
private fun TerminalTradeView(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    title: String,
    body: String,
    closeLabel: String,
    summaryLine: String? = null,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = accent,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = accent
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = PixieTextPrimary
        )
        if (!summaryLine.isNullOrBlank()) {
            PixieCard(
                borderColor = accent.copy(alpha = 0.6f),
                containerColor = PixieBgCard,
                contentPadding = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = summaryLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = PixieTextSecondary
                )
            }
        }
        PixieArcadeButton(
            onClick = onClose,
            accentColor = accent,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trade_close_button")
        ) {
            Text(
                text = closeLabel,
                style = MaterialTheme.typography.labelLarge,
                color = PixieBgDeep,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun ActiveTradeRoomContent(
    trade: TradeSession,
    currentUserId: String,
    progress: GameProgress,
    compactNumbers: Boolean,
    isTradeBusy: Boolean,
    onSetOffer: (String, String) -> Unit,
    onToggleAgree: (String, String) -> Unit,
    onCancelAndLeave: () -> Unit
) {
    val social = LocalPixieSocialStrings.current
    val partnerName = trade.partnerNameFor(currentUserId)
    val myOfferPts = trade.myOfferPoints(currentUserId)
    val myOfferCash = trade.myOfferCash(currentUserId)
    val partnerOfferPts = trade.partnerOfferPoints(currentUserId)
    val partnerOfferCash = trade.partnerOfferCash(currentUserId)
    val myAccepted = trade.myAccepted(currentUserId)
    val partnerAccepted = trade.partnerAccepted(currentUserId)

    val availablePts = progress.diem + trade.myEscrowPoints(currentUserId)
    val availableCash = progress.cashDo + trade.myEscrowCash(currentUserId)

    var pointsText by remember(trade.id) { mutableStateOf(formatInputNumber(myOfferPts)) }
    var cashText by remember(trade.id) { mutableStateOf(formatInputNumber(myOfferCash)) }

    // Keep text fields synchronized when server updates our committed offer or locks on agree
    LaunchedEffect(trade.id, myOfferPts, myOfferCash, myAccepted) {
        if (myAccepted) {
            pointsText = formatInputNumber(myOfferPts)
            cashText = formatInputNumber(myOfferCash)
        }
    }

    val validation = remember(pointsText, cashText, availablePts, availableCash) {
        TradeRules.validateOfferInputs(pointsText, cashText, availablePts, availableCash)
    }

    val inlineError: String? = when (validation) {
        TradeOfferValidation.InvalidNumber -> social.tradeErrInvalidNumber
        TradeOfferValidation.ExceedsPoints -> social.tradeErrExceedsPoints(
            GameEngine.formatGameNumber(availablePts, compactNumbers)
        )
        TradeOfferValidation.ExceedsCash -> social.tradeErrExceedsCash(
            GameEngine.formatCash(availableCash)
        )
        is TradeOfferValidation.Valid -> {
            val totalOffered = validation.points + validation.cash + partnerOfferPts + partnerOfferCash
            if (totalOffered <= 0.0001) social.tradeErrEmptyBothSides else null
        }
    }

    val canApplyOffer = !myAccepted && !isTradeBusy && validation is TradeOfferValidation.Valid
    val canAgree = !isTradeBusy && (
        myAccepted || (
            validation is TradeOfferValidation.Valid &&
                (validation.points + validation.cash + partnerOfferPts + partnerOfferCash) > 0.0001
            )
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = social.tradeRoomTitle,
                tint = PixieGold,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = social.tradeRoomTitle,
                style = MaterialTheme.typography.titleMedium,
                color = PixieGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Partner Panel
        PixieCard(
            borderColor = if (partnerAccepted) PixieMint else PixieBorderBright,
            containerColor = PixieBgCard,
            glowColor = if (partnerAccepted) PixieMint else PixieBorderBright,
            contentPadding = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trade_partner_panel")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = social.tradePartnerLabel(partnerName),
                    style = MaterialTheme.typography.titleSmall,
                    color = PixieTextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusPill(
                    agreed = partnerAccepted,
                    agreedLabel = social.tradeStatusAgreed,
                    waitingLabel = social.tradeStatusWaiting,
                    testTag = "trade_partner_status"
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OfferReadoutTile(
                    label = social.tradePointsShort,
                    value = GameEngine.formatGameNumber(partnerOfferPts, compactNumbers),
                    accent = PixieMint,
                    modifier = Modifier.weight(1f)
                )
                OfferReadoutTile(
                    label = social.tradeCashShort,
                    value = "$${GameEngine.formatCash(partnerOfferCash)}",
                    accent = PixieGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Your Panel
        PixieCard(
            borderColor = if (myAccepted) PixieMint else PixieCyan,
            containerColor = PixieBgCard,
            glowColor = if (myAccepted) PixieMint else PixieCyan,
            contentPadding = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trade_you_panel")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = social.tradeYouLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = PixieCyan
                )
                StatusPill(
                    agreed = myAccepted,
                    agreedLabel = social.tradeStatusAgreed,
                    waitingLabel = social.tradeStatusWaiting,
                    testTag = "trade_my_status"
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = social.tradeBagLabel(
                    GameEngine.formatGameNumber(availablePts, compactNumbers),
                    GameEngine.formatCash(availableCash)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = PixieMint
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = pointsText,
                onValueChange = { if (!myAccepted) pointsText = it },
                enabled = !myAccepted && !isTradeBusy,
                label = { Text(social.tradeOfferPointsHint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixieMint,
                    unfocusedBorderColor = PixieBorderBright,
                    focusedTextColor = PixieTextPrimary,
                    unfocusedTextColor = PixieTextPrimary,
                    disabledTextColor = PixieTextSecondary,
                    focusedContainerColor = PixieBgDeep,
                    unfocusedContainerColor = PixieBgDeep,
                    disabledContainerColor = PixieBgDeep.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trade_offer_points_input")
            )

            if (!myAccepted && availablePts > 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickFillChip(
                        label = "+100",
                        accent = PixieMint,
                        onClick = {
                            val base = pointsText.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                            pointsText = formatInputNumber(minOf(base + 100.0, floor(availablePts * 100.0) / 100.0))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickFillChip(
                        label = "+1K",
                        accent = PixieMint,
                        onClick = {
                            val base = pointsText.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                            pointsText = formatInputNumber(minOf(base + 1000.0, floor(availablePts * 100.0) / 100.0))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickFillChip(
                        label = "MAX",
                        accent = PixieCyan,
                        onClick = {
                            pointsText = formatInputNumber(floor(availablePts * 100.0) / 100.0)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = cashText,
                onValueChange = { if (!myAccepted) cashText = it },
                enabled = !myAccepted && !isTradeBusy,
                label = { Text(social.tradeOfferCashHint) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixieGold,
                    unfocusedBorderColor = PixieBorderBright,
                    focusedTextColor = PixieTextPrimary,
                    unfocusedTextColor = PixieTextPrimary,
                    disabledTextColor = PixieTextSecondary,
                    focusedContainerColor = PixieBgDeep,
                    unfocusedContainerColor = PixieBgDeep,
                    disabledContainerColor = PixieBgDeep.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trade_offer_cash_input")
            )

            if (!myAccepted && availableCash > 0.0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickFillChip(
                        label = "+\$5",
                        accent = PixieGold,
                        onClick = {
                            val base = cashText.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                            cashText = formatInputNumber(minOf(base + 5.0, floor(availableCash * 100.0) / 100.0))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickFillChip(
                        label = "+\$25",
                        accent = PixieGold,
                        onClick = {
                            val base = cashText.trim().replace(',', '.').toDoubleOrNull() ?: 0.0
                            cashText = formatInputNumber(minOf(base + 25.0, floor(availableCash * 100.0) / 100.0))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickFillChip(
                        label = "MAX",
                        accent = PixieCyan,
                        onClick = {
                            cashText = formatInputNumber(floor(availableCash * 100.0) / 100.0)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (myAccepted) {
                Text(
                    text = social.tradeOfferLockedNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = PixieGold
                )
            } else {
                PixieArcadeButton(
                    onClick = { if (canApplyOffer) onSetOffer(pointsText, cashText) },
                    accentColor = PixieCyan,
                    enabled = canApplyOffer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trade_set_offer_button")
                ) {
                    Text(
                        text = social.tradeBtnSetOffer,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (canApplyOffer) PixieBgDeep else PixieTextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        if (inlineError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(PixieDangerDark)
                    .border(1.5.dp, PixieDanger, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("trade_validation_error")
            ) {
                Text(
                    text = inlineError,
                    style = MaterialTheme.typography.bodySmall,
                    color = PixieTextPrimary
                )
            }
        }

        Text(
            text = social.tradeRuleTip,
            style = MaterialTheme.typography.bodySmall,
            color = PixieTextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PixieArcadeButton(
                onClick = { if (canAgree) onToggleAgree(pointsText, cashText) },
                accentColor = if (myAccepted) PixieGold else PixieMint,
                enabled = canAgree,
                modifier = Modifier
                    .weight(1f)
                    .testTag("trade_agree_button")
            ) {
                Text(
                    text = if (myAccepted) social.tradeBtnUnagree else social.tradeBtnAgree,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canAgree) PixieBgDeep else PixieTextMuted,
                    maxLines = 1,
                    softWrap = false
                )
            }

            PixieArcadeButton(
                onClick = onCancelAndLeave,
                accentColor = PixieDanger,
                enabled = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("trade_cancel_leave_button")
            ) {
                Text(
                    text = social.tradeBtnCancelLeave,
                    style = MaterialTheme.typography.labelLarge,
                    color = PixieTextPrimary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    agreed: Boolean,
    agreedLabel: String,
    waitingLabel: String,
    testTag: String
) {
    val color = if (agreed) PixieMint else PixieTextMuted
    val bg = if (agreed) PixieMintDark else PixieBgDeep
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, color, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
            .testTag(testTag)
    ) {
        Text(
            text = if (agreed) agreedLabel else waitingLabel,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun OfferReadoutTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PixieBgDeep)
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PixieTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun QuickFillChip(
    label: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            maxLines = 1,
            softWrap = false
        )
    }
}

private fun formatInputNumber(value: Double): String {
    if (value <= 0.0) return "0"
    val rounded = GameEngine.round2(value)
    return if (rounded == rounded.toLong().toDouble()) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}

private fun formatCompletedSummary(
    trade: TradeSession,
    currentUserId: String,
    partnerName: String,
    compactNumbers: Boolean
): String {
    val givenPts = GameEngine.formatGameNumber(trade.myOfferPoints(currentUserId), compactNumbers)
    val givenCash = GameEngine.formatCash(trade.myOfferCash(currentUserId))
    val receivedPts = GameEngine.formatGameNumber(trade.partnerOfferPoints(currentUserId), compactNumbers)
    val receivedCash = GameEngine.formatCash(trade.partnerOfferCash(currentUserId))
    return "-> $partnerName: $givenPts pts, \$$givenCash\n<- $partnerName: $receivedPts pts, \$$receivedCash"
}
