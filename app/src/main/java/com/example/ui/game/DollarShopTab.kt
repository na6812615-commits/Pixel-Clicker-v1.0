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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameProgress
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.theme.PixieBgCardHighlight
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieBorder
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieCyanDark
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieGoldDark
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextMuted
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary

@Composable
fun DollarShopTab(
    progress: GameProgress,
    onExchange1000: () -> Unit,
    onExchangeAll: () -> Unit,
    onBuyDiscount: () -> Unit,
    onBuyMenuSpeed: () -> Unit,
    onBuyToastSpeed: () -> Unit,
    onBuyAutoUnlock: () -> Unit,
    onToggleAutoClick: () -> Unit,
    onBuyAutoPower: () -> Unit,
    onBuyAutoSpeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val s = LocalPixieStrings.current
    val dollarRatio = ((progress.diem % 1000.0) / 1000.0).toFloat().coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1: Quy doi diem -> tien $
        PixieCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = PixieGold,
            containerColor = PixieGoldDark.copy(alpha = 0.45f),
            glowColor = PixieGold
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PixieArtworkImage(
                    resId = R.drawable.img_pixie_chest,
                    contentDescription = s.dollarExchangeTitle,
                    contentScale = ContentScale.Crop,
                    targetMaxPx = 256,
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .border(2.dp, PixieGold, RoundedCornerShape(9.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PixieCoinSprite(modifier = Modifier.size(18.dp))
                        Text(
                            text = s.dollarExchangeTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = PixieGold
                        )
                    }
                    Text(
                        text = s.exchangeAllDesc(GameEngine.formatCash(progress.diem / 1000.0)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixieTextSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { if (progress.diem >= 1000.0) 1f else dollarRatio },
                color = if (progress.diem >= 1000.0) PixieMint else PixieGold,
                trackColor = PixieBgCardHighlight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PixieArcadeButton(
                    onClick = onExchange1000,
                    accentColor = PixieGold,
                    enabled = progress.diem >= 1000.0,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("exchange_1000_button")
                ) {
                    Text(
                        text = s.btnExchange1000,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (progress.diem >= 1000.0) PixieBgDeep else PixieTextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                PixieArcadeButton(
                    onClick = onExchangeAll,
                    accentColor = PixieMint,
                    enabled = progress.diem > 0.0,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("exchange_all_button")
                ) {
                    Text(
                        text = s.btnExchangeAll,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (progress.diem > 0.0) PixieBgDeep else PixieTextMuted,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Section 2: $Shop$
        PixieSectionHeader(
            title = s.dollarShopTitle,
            accentColor = PixieGold
        )

        PixieUpgradeRow(
            title = s.dollarDiscountTitle,
            subtitle = s.dollarDiscountSub(GameEngine.round2(progress.thay2).toString()),
            levelLabel = s.levelBadge(progress.lv5),
            costLabel = s.costDollar(GameEngine.formatCash(progress.gia5)),
            canAfford = progress.cashDo >= progress.gia5,
            accentColor = PixieGold,
            buttonText = s.btnBuy,
            testTag = "dollar_buy_discount_button",
            onBuy = onBuyDiscount,
            relicType = PixieRelicType.DISCOUNT
        )

        PixieUpgradeRow(
            title = s.dollarMenuSpeedTitle,
            subtitle = s.dollarMenuSpeedSub(
                GameEngine.formatSeconds(progress.cd3),
                GameEngine.formatSeconds(progress.cd3 / 2.0)
            ),
            levelLabel = s.levelBadge(progress.lv6),
            costLabel = s.costDollar(GameEngine.formatCash(progress.gia6)),
            canAfford = progress.cashDo >= progress.gia6,
            accentColor = PixieRose,
            buttonText = s.btnBuy,
            testTag = "dollar_buy_menu_speed_button",
            onBuy = onBuyMenuSpeed,
            relicType = PixieRelicType.MENU_SPEED
        )

        PixieUpgradeRow(
            title = s.dollarToastSpeedTitle,
            subtitle = s.dollarToastSpeedSub(
                GameEngine.formatSeconds(progress.cd4),
                GameEngine.formatSeconds(progress.cd4 / 2.0)
            ),
            levelLabel = s.levelBadge(progress.lv7),
            costLabel = s.costDollar(GameEngine.formatCash(progress.gia7)),
            canAfford = progress.cashDo >= progress.gia7,
            accentColor = PixieMint,
            buttonText = s.btnBuy,
            testTag = "dollar_buy_toast_speed_button",
            onBuy = onBuyToastSpeed,
            relicType = PixieRelicType.TOAST_SPEED
        )

        PixieUpgradeRow(
            title = s.dollarAutoTitle,
            subtitle = if (progress.gioihan) s.dollarAutoSubLocked else s.dollarAutoSubOwned,
            levelLabel = if (progress.gioihan) s.statusNotOwned else s.statusOwned,
            costLabel = if (progress.gioihan) s.costDollar("25") else s.costBought,
            canAfford = progress.gioihan && progress.cashDo >= 25.0,
            accentColor = PixieCyan,
            buttonText = if (progress.gioihan) s.btnBuy else s.btnDone,
            testTag = "dollar_buy_auto_button",
            onBuy = onBuyAutoUnlock,
            relicType = PixieRelicType.AUTO_UNLOCK
        )

        // Section 3: Menu Nang Cap Auto Click
        Spacer(modifier = Modifier.height(2.dp))
        PixieSectionHeader(
            title = s.autoUpgradeTitle,
            accentColor = PixieCyan
        )

        if (progress.gioihan) {
            PixieCard(modifier = Modifier.fillMaxWidth(), borderColor = PixieBorder) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = PixieTextMuted
                    )
                    Text(
                        text = s.autoLockedNotice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixieTextSecondary
                    )
                }
            }
        } else {
            PixieCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = PixieCyan,
                containerColor = PixieCyanDark.copy(alpha = 0.52f),
                glowColor = PixieCyan
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(PixieCyan.copy(alpha = 0.3f), PixieBgDeep)
                                    )
                                )
                                .border(2.dp, PixieCyan, RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            PixieRelicSprite(
                                type = PixieRelicType.AUTO_UNLOCK,
                                isActive = progress.autoEnabled,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = s.autoToggleTitle,
                                style = MaterialTheme.typography.titleMedium,
                                color = PixieTextPrimary
                            )
                            Text(
                                text = s.autoToggleDesc(
                                    progress.autoEnabled,
                                    GameEngine.pointsPerAutoClick(progress).toString(),
                                    GameEngine.formatSeconds(progress.cd2)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = PixieTextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    PixieArcadeButton(
                        onClick = onToggleAutoClick,
                        accentColor = if (progress.autoEnabled) PixieCyan else PixieRose,
                        modifier = Modifier.testTag("auto_toggle_button")
                    ) {
                        Text(
                            text = if (progress.autoEnabled) s.btnTurnOff else s.btnTurnOn,
                            style = MaterialTheme.typography.labelLarge,
                            color = PixieBgDeep,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            PixieUpgradeRow(
                title = s.autoPowerTitle(progress.x2fake.toLong()),
                subtitle = s.autoPowerSub(progress.x2.toLong(), (progress.x2 + progress.x2fake).toLong()),
                levelLabel = s.levelBadge(progress.lv8),
                costLabel = s.costDollar(GameEngine.formatCash(progress.gia8)),
                canAfford = progress.cashDo >= progress.gia8,
                accentColor = PixieCyan,
                buttonText = s.btnBuy,
                testTag = "auto_buy_power_button",
                onBuy = onBuyAutoPower,
                relicType = PixieRelicType.AUTO_POWER
            )

            PixieUpgradeRow(
                title = s.autoSpeedTitle,
                subtitle = s.autoSpeedSub(
                    GameEngine.formatSeconds(progress.cd2),
                    GameEngine.formatSeconds((progress.cd2 / 2.0).coerceAtLeast(1e-6))
                ),
                levelLabel = s.levelBadge(progress.lv9),
                costLabel = s.costDollar(GameEngine.formatCash(progress.gia9)),
                canAfford = progress.cashDo >= progress.gia9,
                accentColor = PixieMint,
                buttonText = s.btnBuy,
                testTag = "auto_buy_speed_button",
                onBuy = onBuyAutoSpeed,
                relicType = PixieRelicType.AUTO_SPEED
            )
        }
    }
}
