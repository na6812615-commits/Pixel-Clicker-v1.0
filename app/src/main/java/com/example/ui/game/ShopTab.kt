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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameProgress
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary

@Composable
fun ShopTab(
    progress: GameProgress,
    onBuyPower: () -> Unit,
    onBuyMultiplier: () -> Unit,
    onBuyCooldown: () -> Unit,
    modifier: Modifier = Modifier,
    compactNumbers: Boolean = false
) {
    val s = LocalPixieStrings.current
    val costPower = GameEngine.effectiveCost(progress.gia, progress.giamgia)
    val costMulti = GameEngine.effectiveCost(progress.gia2, progress.giamgia)
    val costCd = GameEngine.effectiveCost(progress.gia4, progress.giamgia)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Illustrated 16-bit Citadel Shop Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, PixieMint.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
        ) {
            PixieArtworkImage(
                resId = R.drawable.img_pixie_banner,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                targetMaxPx = 512,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.72f)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PixieBgDeep.copy(alpha = 0.88f),
                                PixieBgDeep.copy(alpha = 0.56f),
                                PixieBgDeep.copy(alpha = 0.78f)
                            )
                        )
                    )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                PixieArtworkImage(
                    resId = R.drawable.img_pixie_chest,
                    contentDescription = s.shopTitle,
                    contentScale = ContentScale.Crop,
                    targetMaxPx = 256,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(2.dp, PixieGold, RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = s.shopTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = PixieMint
                    )
                    Text(
                        text = s.shopBannerDesc(GameEngine.round2(progress.giamgia).toString()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixieTextPrimary
                    )
                }
            }
        }

        PixieSectionHeader(
            title = s.shopSectionHeader,
            accentColor = PixieMint
        )

        PixieUpgradeRow(
            title = s.shopPowerTitle(GameEngine.formatGameCount(progress.x.toLong(), compactNumbers)),
            subtitle = s.shopPowerSub(
                GameEngine.formatGameCount(progress.x.toLong(), compactNumbers),
                GameEngine.formatGameCount((progress.x * 2).toLong(), compactNumbers)
            ),
            levelLabel = s.levelBadge(progress.lv),
            costLabel = s.costPoints(GameEngine.formatGameCount(costPower, compactNumbers)),
            canAfford = progress.diem >= costPower,
            accentColor = PixieMint,
            buttonText = s.btnBuy,
            testTag = "shop_buy_power_button",
            onBuy = onBuyPower,
            relicType = PixieRelicType.TAP_POWER
        )

        PixieUpgradeRow(
            title = s.shopMultiTitle,
            subtitle = s.shopMultiSub(
                GameEngine.round2(progress.muti).toString(),
                GameEngine.round2(progress.muti + 0.1).toString()
            ),
            levelLabel = s.levelBadge(progress.lv2),
            costLabel = s.costPoints(GameEngine.formatGameCount(costMulti, compactNumbers)),
            canAfford = progress.diem >= costMulti,
            accentColor = PixieRose,
            buttonText = s.btnBuy,
            testTag = "shop_buy_multiplier_button",
            onBuy = onBuyMultiplier,
            relicType = PixieRelicType.MULTIPLIER
        )

        PixieUpgradeRow(
            title = s.shopCooldownTitle,
            subtitle = s.shopCooldownSub(
                GameEngine.formatSeconds(progress.cd),
                GameEngine.formatSeconds((progress.cd - progress.thay).coerceAtLeast(1e-6))
            ),
            levelLabel = s.levelBadge(progress.lv4),
            costLabel = s.costPoints(GameEngine.formatGameCount(costCd, compactNumbers)),
            canAfford = progress.diem >= costCd,
            accentColor = PixieGold,
            buttonText = s.btnBuy,
            testTag = "shop_buy_cooldown_button",
            onBuy = onBuyCooldown,
            relicType = PixieRelicType.COOLDOWN
        )

        PixieCard(modifier = Modifier.fillMaxWidth(), borderColor = PixieCyan.copy(alpha = 0.65f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PixieStarGemSprite(modifier = Modifier.size(22.dp))
                Text(
                    text = s.shopTip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixieTextSecondary
                )
            }
        }
    }
}
