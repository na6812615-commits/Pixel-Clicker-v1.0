package com.example.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GameServer
import com.example.data.ServerPlayer
import com.example.data.TradeRules
import com.example.data.TradeSession
import com.example.ui.i18n.LocalPixieSocialStrings
import com.example.ui.theme.PixieBgCard
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieBgSurface
import com.example.ui.theme.PixieBorder
import com.example.ui.theme.PixieBorderBright
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieDanger
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextMuted
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary

@Composable
fun ServerBrowserDialog(
    servers: List<GameServer>,
    currentServerId: String?,
    currentServerPlayers: List<ServerPlayer>,
    currentUserId: String,
    searchQuery: String,
    isJoiningServer: Boolean,
    incomingTrades: List<TradeSession>,
    outgoingPendingTrades: List<TradeSession>,
    compactNumbers: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onJoinServer: (String) -> Unit,
    onInviteTrade: (ServerPlayer) -> Unit,
    onAcceptInvite: (String) -> Unit,
    onDeclineOrCancelInvite: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val social = LocalPixieSocialStrings.current
    val filteredServers = remember(servers, searchQuery) {
        TradeRules.filterServers(servers, searchQuery)
    }
    val currentServer = remember(servers, currentServerId) {
        servers.firstOrNull { it.id == currentServerId }
    }
    val otherPlayers = remember(currentServerPlayers, currentUserId) {
        currentServerPlayers.filter { it.userId != currentUserId }
    }
    val invitedReceiverIds = remember(outgoingPendingTrades) {
        outgoingPendingTrades.mapTo(mutableSetOf()) { it.receiverId }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PixieCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .widthIn(max = 580.dp)
                .fillMaxHeight(0.88f),
            borderColor = PixieCyan,
            containerColor = PixieBgSurface,
            glowColor = PixieCyan,
            contentPadding = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Public,
                        contentDescription = social.serverMenuTitle,
                        tint = PixieCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = social.serverMenuTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = PixieCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = social.serverMenuSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = PixieTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("server_menu_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = social.serverBtnClose,
                        tint = PixieTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = social.serverSearchPlaceholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixieTextMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = PixieCyan
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { onSearchQueryChange("") },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("server_search_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = social.serverSearchClear,
                                tint = PixieTextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PixieCyan,
                    unfocusedBorderColor = PixieBorderBright,
                    focusedTextColor = PixieTextPrimary,
                    unfocusedTextColor = PixieTextPrimary,
                    focusedContainerColor = PixieBgDeep,
                    unfocusedContainerColor = PixieBgDeep,
                    cursorColor = PixieCyan
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .testTag("server_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("server_browser_list"),
                contentPadding = PaddingValues(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (incomingTrades.isNotEmpty() || outgoingPendingTrades.isNotEmpty()) {
                    item(key = "invites_header") {
                        PixieSectionHeader(
                            title = social.tradeInvitesHeading(incomingTrades.size + outgoingPendingTrades.size),
                            accentColor = PixieGold
                        )
                    }
                    items(incomingTrades, key = { "in_${it.id}" }) { trade ->
                        IncomingInviteCard(
                            trade = trade,
                            onAccept = { onAcceptInvite(trade.id) },
                            onDecline = { onDeclineOrCancelInvite(trade.id) }
                        )
                    }
                    items(outgoingPendingTrades, key = { "out_${it.id}" }) { trade ->
                        OutgoingInviteCard(
                            trade = trade,
                            onCancel = { onDeclineOrCancelInvite(trade.id) }
                        )
                    }
                }

                if (filteredServers.isEmpty()) {
                    item(key = "no_servers") {
                        PixieCard(
                            borderColor = PixieBorder,
                            containerColor = PixieBgCard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("server_no_results")
                        ) {
                            Text(
                                text = social.serverNoResults,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PixieTextSecondary
                            )
                        }
                    }
                } else {
                    items(filteredServers, key = { "srv_${it.id}" }) { server ->
                        val isCurrent = server.id == currentServerId
                        ServerRowCard(
                            server = server,
                            isCurrent = isCurrent,
                            isJoining = isJoiningServer,
                            onJoin = { onJoinServer(server.id) }
                        )
                    }
                }

                if (currentServer != null) {
                    item(key = "players_header") {
                        Spacer(modifier = Modifier.height(4.dp))
                        PixieSectionHeader(
                            title = social.serverPlayersHeading(
                                currentServer.name,
                                currentServer.playerCount,
                                currentServer.maxPlayers
                            ),
                            accentColor = PixieMint
                        )
                    }

                    if (otherPlayers.isEmpty()) {
                        item(key = "empty_players") {
                            PixieCard(
                                borderColor = PixieBorder,
                                containerColor = PixieBgCard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("server_empty_players_hint")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Groups,
                                        contentDescription = null,
                                        tint = PixieTextMuted,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = social.serverEmptyOthersHint,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PixieTextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        items(otherPlayers, key = { "ply_${it.userId}" }) { player ->
                            ServerPlayerRowCard(
                                player = player,
                                alreadyInvited = player.userId in invitedReceiverIds,
                                compactNumbers = compactNumbers,
                                onInvite = { onInviteTrade(player) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            PixieArcadeButton(
                onClick = onDismiss,
                accentColor = PixieBorderBright,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("server_menu_bottom_close_button")
            ) {
                Text(
                    text = social.serverBtnClose,
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
private fun IncomingInviteCard(
    trade: TradeSession,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val social = LocalPixieSocialStrings.current
    PixieCard(
        borderColor = PixieGold,
        containerColor = PixieBgCard,
        glowColor = PixieGold,
        contentPadding = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("incoming_trade_card_${trade.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.SwapHoriz,
                contentDescription = null,
                tint = PixieGold,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = social.tradeIncomingBannerTitle(trade.senderName),
                style = MaterialTheme.typography.titleSmall,
                color = PixieTextPrimary,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PixieArcadeButton(
                onClick = onAccept,
                accentColor = PixieMint,
                modifier = Modifier
                    .weight(1f)
                    .testTag("invite_accept_${trade.id}")
            ) {
                Text(
                    text = social.tradeBtnAccept,
                    style = MaterialTheme.typography.labelLarge,
                    color = PixieBgDeep,
                    maxLines = 1,
                    softWrap = false
                )
            }
            PixieArcadeButton(
                onClick = onDecline,
                accentColor = PixieDanger,
                modifier = Modifier
                    .weight(1f)
                    .testTag("invite_decline_${trade.id}")
            ) {
                Text(
                    text = social.tradeBtnDecline,
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
private fun OutgoingInviteCard(
    trade: TradeSession,
    onCancel: () -> Unit
) {
    val social = LocalPixieSocialStrings.current
    PixieCard(
        borderColor = PixieCyan,
        containerColor = PixieBgCard,
        contentPadding = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("outgoing_trade_card_${trade.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = social.tradeWaitingPartnerBanner(trade.receiverName),
                style = MaterialTheme.typography.bodyMedium,
                color = PixieCyan,
                modifier = Modifier.weight(1f)
            )
            PixieArcadeButton(
                onClick = onCancel,
                accentColor = PixieDanger,
                modifier = Modifier.testTag("invite_cancel_${trade.id}")
            ) {
                Text(
                    text = social.tradeBtnDecline,
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
private fun ServerRowCard(
    server: GameServer,
    isCurrent: Boolean,
    isJoining: Boolean,
    onJoin: () -> Unit
) {
    val social = LocalPixieSocialStrings.current
    val borderColor = when {
        isCurrent -> PixieMint
        server.isFull -> PixieDanger.copy(alpha = 0.7f)
        else -> PixieBorderBright
    }
    val capacityColor = when {
        server.isFull -> PixieDanger
        isCurrent -> PixieMint
        else -> PixieCyan
    }

    PixieCard(
        borderColor = borderColor,
        containerColor = PixieBgCard,
        glowColor = borderColor,
        contentPadding = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_card_${server.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = PixieTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TagChip(text = server.region, color = PixieRose)
                    TagChip(
                        text = social.serverCapacity(server.playerCount, server.maxPlayers),
                        color = capacityColor
                    )
                }
            }

            val buttonEnabled = !isCurrent && !server.isFull && !isJoining
            val buttonColor = when {
                isCurrent -> PixieMint
                server.isFull -> PixieDanger
                else -> PixieCyan
            }
            val buttonText = when {
                isCurrent -> social.serverCurrentBadge
                server.isFull -> social.serverFullBadge
                else -> social.serverBtnJoin
            }
            PixieArcadeButton(
                onClick = { if (buttonEnabled) onJoin() },
                accentColor = buttonColor,
                enabled = buttonEnabled,
                modifier = Modifier.testTag("server_join_${server.id}")
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (buttonEnabled) PixieBgDeep else PixieTextMuted,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun ServerPlayerRowCard(
    player: ServerPlayer,
    alreadyInvited: Boolean,
    compactNumbers: Boolean,
    onInvite: () -> Unit
) {
    val social = LocalPixieSocialStrings.current
    PixieCard(
        borderColor = PixieBorder,
        containerColor = PixieBgCard,
        contentPadding = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_player_${player.userId}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = player.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = PixieTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = social.serverPlayerStats(
                        GameEngine.formatGameNumber(player.diem, compactNumbers),
                        GameEngine.formatCash(player.cashDo),
                        player.restartsLv
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = PixieMint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            PixieArcadeButton(
                onClick = { if (!alreadyInvited) onInvite() },
                accentColor = PixieGold,
                enabled = !alreadyInvited,
                modifier = Modifier.testTag("invite_trade_${player.userId}")
            ) {
                Text(
                    text = if (alreadyInvited) social.serverBtnInvited else social.serverBtnInviteTrade,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (!alreadyInvited) PixieBgDeep else PixieTextMuted,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun TagChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            softWrap = false
        )
    }
}
