package com.example.ui.game

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameProgress
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.i18n.PixieStrings
import com.example.ui.theme.PixieBgCard
import com.example.ui.theme.PixieBgCardHighlight
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
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextMuted
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

private data class AmbientStar(
    val xFraction: Float,
    val yFraction: Float,
    val radiusDp: Float,
    val phase: Float,
    val color: Color,
    val driftSpeed: Float,
    val isCrossStar: Boolean
)

private object PixieBitmapCache {
    private val cache = ConcurrentHashMap<Long, ImageBitmap>()

    fun key(resId: Int, maxPx: Int): Long = (resId.toLong() shl 32) or (maxPx.toLong() and 0xFFFFFFFFL)

    fun get(resId: Int, maxPx: Int): ImageBitmap? = cache[key(resId, maxPx)]

    fun put(resId: Int, maxPx: Int, bitmap: ImageBitmap) {
        cache[key(resId, maxPx)] = bitmap
    }
}

/**
 * Decodes drawable artwork off the main thread with power-of-2 downsampling and RGB_565
 * so cold-start frame 1 and tab transitions never block MainActivity or SurfaceSyncGroup.
 */
@Composable
fun PixieArtworkImage(
    resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    targetMaxPx: Int = 512
) {
    val context = LocalContext.current
    val cached = remember(resId, targetMaxPx) { PixieBitmapCache.get(resId, targetMaxPx) }
    val bitmap by produceState(initialValue = cached, resId, targetMaxPx) {
        if (value == null) {
            value = withContext(Dispatchers.IO) {
                PixieBitmapCache.get(resId, targetMaxPx) ?: runCatching {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeResource(context.resources, resId, bounds)
                    val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
                    var sample = 1
                    if (maxDim > targetMaxPx && targetMaxPx > 0) {
                        while ((maxDim / (sample * 2)) >= targetMaxPx) {
                            sample *= 2
                        }
                    }
                    val opts = BitmapFactory.Options().apply {
                        inSampleSize = sample
                        inPreferredConfig = Bitmap.Config.RGB_565
                    }
                    BitmapFactory.decodeResource(context.resources, resId, opts)
                        ?.asImageBitmap()
                        ?.also { PixieBitmapCache.put(resId, targetMaxPx, it) }
                }.getOrNull()
            }
        }
    }

    val loaded = bitmap
    if (loaded != null) {
        Image(
            bitmap = loaded,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Box(modifier = modifier.background(PixieBgDeep))
    }
}

@Composable
fun PixieAmbientBackground(
    modifier: Modifier = Modifier,
    ambientParticlesEnabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val stars = remember {
        listOf(
            AmbientStar(0.11f, 0.08f, 2.4f, 0.0f, PixieMint, 0.6f, true),
            AmbientStar(0.84f, 0.11f, 2.8f, 1.2f, PixieRose, 0.5f, true),
            AmbientStar(0.28f, 0.22f, 1.8f, 2.1f, PixieCyan, 0.8f, false),
            AmbientStar(0.68f, 0.19f, 2.2f, 3.4f, PixieGold, 0.4f, true),
            AmbientStar(0.08f, 0.43f, 2.4f, 4.1f, PixieCyan, 0.7f, false),
            AmbientStar(0.91f, 0.38f, 2.3f, 0.9f, PixieMint, 0.55f, true),
            AmbientStar(0.45f, 0.52f, 1.7f, 2.7f, PixieRose, 0.65f, false),
            AmbientStar(0.19f, 0.69f, 2.5f, 5.0f, PixieGold, 0.45f, true),
            AmbientStar(0.77f, 0.64f, 2.2f, 1.8f, PixieMint, 0.75f, false),
            AmbientStar(0.55f, 0.84f, 2.8f, 3.9f, PixieRose, 0.5f, true),
            AmbientStar(0.33f, 0.91f, 2.0f, 0.4f, PixieCyan, 0.6f, false),
            AmbientStar(0.88f, 0.88f, 2.4f, 2.2f, PixieGold, 0.7f, true),
            AmbientStar(0.51f, 0.14f, 1.6f, 1.5f, PixieTextPrimary, 0.9f, false),
            AmbientStar(0.15f, 0.82f, 1.8f, 4.6f, PixieCyan, 0.6f, false)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val bgBrush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0716), Color(0xFF17112B), Color(0xFF0D091B))
                )
                val roseGlowCenter = Offset(size.width * 0.18f, size.height * 0.22f)
                val roseGlowRadius = size.width * 0.68f
                val roseGlow = Brush.radialGradient(
                    colors = listOf(PixieRose.copy(alpha = 0.11f), Color.Transparent),
                    center = roseGlowCenter,
                    radius = roseGlowRadius
                )
                val cyanGlowCenter = Offset(size.width * 0.82f, size.height * 0.62f)
                val cyanGlowRadius = size.width * 0.74f
                val cyanGlow = Brush.radialGradient(
                    colors = listOf(PixieCyan.copy(alpha = 0.10f), Color.Transparent),
                    center = cyanGlowCenter,
                    radius = cyanGlowRadius
                )
                val mintGlowCenter = Offset(size.width * 0.48f, size.height * 0.92f)
                val mintGlowRadius = size.width * 0.65f
                val mintGlow = Brush.radialGradient(
                    colors = listOf(PixieMint.copy(alpha = 0.07f), Color.Transparent),
                    center = mintGlowCenter,
                    radius = mintGlowRadius
                )
                val step = 36.dp.toPx()
                val horizonY = size.height * 0.78f
                val strokeW = 1.dp.toPx()

                onDrawBehind {
                    drawRect(brush = bgBrush)
                    drawCircle(brush = roseGlow, radius = roseGlowRadius, center = roseGlowCenter)
                    drawCircle(brush = cyanGlow, radius = cyanGlowRadius, center = cyanGlowCenter)
                    drawCircle(brush = mintGlow, radius = mintGlowRadius, center = mintGlowCenter)
                    var x = 0f
                    while (x < size.width) {
                        drawLine(
                            color = PixieBorder.copy(alpha = 0.08f),
                            start = Offset(x, horizonY),
                            end = Offset(x, size.height),
                            strokeWidth = strokeW
                        )
                        x += step
                    }
                }
            }
    ) {
        if (ambientParticlesEnabled) {
            val transition = rememberInfiniteTransition(label = "pixie_ambient")
            val phase by transition.animateFloat(
                initialValue = 0f,
                targetValue = (2.0 * PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 9000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "ambient_phase"
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                stars.forEach { star ->
                    val pulse = 0.35f + 0.65f * ((sin(phase * star.driftSpeed + star.phase) + 1f) * 0.5f)
                    val dx = cos(phase + star.phase) * 8.dp.toPx()
                    val dy = sin(phase * 0.7f + star.phase) * 10.dp.toPx()
                    val cx = size.width * star.xFraction + dx
                    val cy = size.height * star.yFraction + dy
                    val r = star.radiusDp.dp.toPx()

                    drawCircle(
                        color = star.color.copy(alpha = 0.18f * pulse),
                        radius = r * 3.0f,
                        center = Offset(cx, cy)
                    )
                    if (star.isCrossStar) {
                        val arm = r * 2.4f * pulse
                        drawRect(
                            color = star.color.copy(alpha = 0.9f * pulse),
                            topLeft = Offset(cx - arm, cy - r * 0.45f),
                            size = Size(arm * 2f, r * 0.9f)
                        )
                        drawRect(
                            color = star.color.copy(alpha = 0.9f * pulse),
                            topLeft = Offset(cx - r * 0.45f, cy - arm),
                            size = Size(r * 0.9f, arm * 2f)
                        )
                    }
                    drawRect(
                        color = Color.White.copy(alpha = 0.92f * pulse),
                        topLeft = Offset(cx - r * 0.7f, cy - r * 0.7f),
                        size = Size(r * 1.4f, r * 1.4f)
                    )
                }
            }
        }
        content()
    }
}

@Composable
fun PixieCard(
    modifier: Modifier = Modifier,
    borderColor: Color = PixieBorder,
    containerColor: Color = PixieBgCard,
    glowColor: Color = borderColor,
    contentPadding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(
                            red = (containerColor.red * 1.12f).coerceIn(0f, 1f),
                            green = (containerColor.green * 1.12f).coerceIn(0f, 1f),
                            blue = (containerColor.blue * 1.12f).coerceIn(0f, 1f),
                            alpha = containerColor.alpha
                        ),
                        containerColor,
                        Color(
                            red = (containerColor.red * 0.74f).coerceIn(0f, 1f),
                            green = (containerColor.green * 0.74f).coerceIn(0f, 1f),
                            blue = (containerColor.blue * 0.82f).coerceIn(0f, 1f),
                            alpha = containerColor.alpha
                        )
                    )
                )
            )
            .drawBehind {
                val outerStroke = 2.dp.toPx()
                val corner = 10.dp.toPx()

                // Subtle top-left diagonal enchanted highlight wash
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.09f),
                            Color.Transparent
                        ),
                        start = Offset.Zero,
                        end = Offset(size.width * 0.6f, size.height * 0.6f)
                    )
                )

                // Dark outer outline for RPG pixel contrast
                drawRoundRect(
                    color = Color(0xFF0A0716),
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = CornerRadius(corner, corner),
                    style = Stroke(width = 3.dp.toPx())
                )

                // Main beveled frame border
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset(outerStroke * 0.75f, outerStroke * 0.75f),
                    size = Size(size.width - outerStroke * 1.5f, size.height - outerStroke * 1.5f),
                    cornerRadius = CornerRadius(corner - 1.dp.toPx(), corner - 1.dp.toPx()),
                    style = Stroke(width = outerStroke)
                )

                // Recessed inner trim groove
                val innerInset = 4.dp.toPx()
                drawRoundRect(
                    color = borderColor.copy(alpha = 0.25f),
                    topLeft = Offset(innerInset, innerInset),
                    size = Size(size.width - innerInset * 2f, size.height - innerInset * 2f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Top specular gleam
                drawLine(
                    color = Color.White.copy(alpha = 0.24f),
                    start = Offset(14.dp.toPx(), 2.5.dp.toPx()),
                    end = Offset(size.width - 14.dp.toPx(), 2.5.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Ornate 4-corner pixel L-brackets + glowing diamond rivets
                val bracketLen = 8.dp.toPx()
                val bInset = 3.dp.toPx()
                val jewel = 3.5.dp.toPx()
                val jewelColor = glowColor.copy(alpha = 0.95f)
                val bStroke = 2.dp.toPx()

                // Top-left bracket & rivet
                drawLine(jewelColor, Offset(bInset, bInset + bracketLen), Offset(bInset, bInset), bStroke)
                drawLine(jewelColor, Offset(bInset, bInset), Offset(bInset + bracketLen, bInset), bStroke)
                drawRect(Color.White.copy(alpha = 0.85f), Offset(bInset + 2.dp.toPx(), bInset + 2.dp.toPx()), Size(jewel, jewel))

                // Top-right bracket & rivet
                drawLine(jewelColor, Offset(size.width - bInset - bracketLen, bInset), Offset(size.width - bInset, bInset), bStroke)
                drawLine(jewelColor, Offset(size.width - bInset, bInset), Offset(size.width - bInset, bInset + bracketLen), bStroke)
                drawRect(Color.White.copy(alpha = 0.85f), Offset(size.width - bInset - 2.dp.toPx() - jewel, bInset + 2.dp.toPx()), Size(jewel, jewel))

                // Bottom-left bracket & rivet
                drawLine(jewelColor, Offset(bInset, size.height - bInset - bracketLen), Offset(bInset, size.height - bInset), bStroke)
                drawLine(jewelColor, Offset(bInset, size.height - bInset), Offset(bInset + bracketLen, size.height - bInset), bStroke)
                drawRect(jewelColor, Offset(bInset + 2.dp.toPx(), size.height - bInset - 2.dp.toPx() - jewel), Size(jewel, jewel))

                // Bottom-right bracket & rivet
                drawLine(jewelColor, Offset(size.width - bInset - bracketLen, size.height - bInset), Offset(size.width - bInset, size.height - bInset), bStroke)
                drawLine(jewelColor, Offset(size.width - bInset, size.height - bInset - bracketLen), Offset(size.width - bInset, size.height - bInset), bStroke)
                drawRect(jewelColor, Offset(size.width - bInset - 2.dp.toPx() - jewel, size.height - bInset - 2.dp.toPx() - jewel), Size(jewel, jewel))
            }
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun PixieSectionHeader(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(alpha = 0.7f))
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(accentColor, RoundedCornerShape(1.dp))
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = accentColor,
            maxLines = 1,
            softWrap = false
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(accentColor, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
fun PixieArcadeButton(
    onClick: () -> Unit,
    accentColor: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    contentColor: Color = PixieBgDeep,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val lipHeight = if (isPressed && enabled) 1.dp else 4.dp
    val faceColor = if (enabled) accentColor else PixieBgCardHighlight
    val shadowColor = if (enabled) {
        Color(
            red = (accentColor.red * 0.45f).coerceIn(0f, 1f),
            green = (accentColor.green * 0.45f).coerceIn(0f, 1f),
            blue = (accentColor.blue * 0.45f).coerceIn(0f, 1f),
            alpha = 1f
        )
    } else {
        Color(0xFF171229)
    }

    Box(
        propagateMinConstraints = true,
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minWidth = 74.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0A0716))
            .padding(1.5.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(shadowColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White),
                enabled = true,
                role = Role.Button,
                onClick = onClick
            )
            .padding(bottom = lipHeight)
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(
                                red = (faceColor.red * 1.08f).coerceIn(0f, 1f),
                                green = (faceColor.green * 1.08f).coerceIn(0f, 1f),
                                blue = (faceColor.blue * 1.08f).coerceIn(0f, 1f),
                                alpha = 1f
                            ),
                            faceColor,
                            Color(
                                red = (faceColor.red * 0.84f).coerceIn(0f, 1f),
                                green = (faceColor.green * 0.84f).coerceIn(0f, 1f),
                                blue = (faceColor.blue * 0.84f).coerceIn(0f, 1f),
                                alpha = 1f
                            )
                        )
                    )
                )
                .drawBehind {
                    // Top bright pixel rim
                    drawLine(
                        color = Color.White.copy(alpha = if (enabled) 0.55f else 0.12f),
                        start = Offset(4.dp.toPx(), 1.5.dp.toPx()),
                        end = Offset(size.width - 4.dp.toPx(), 1.5.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                    // Subtle diagonal arcade sheen strip
                    if (enabled) {
                        val sheen = Path().apply {
                            moveTo(size.width * 0.12f, 0f)
                            lineTo(size.width * 0.28f, 0f)
                            lineTo(size.width * 0.14f, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(sheen, color = Color.White.copy(alpha = 0.14f))
                    }
                }
                .padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

private fun pixieRankInfo(progress: GameProgress, s: PixieStrings): Pair<String, Color> {
    return when {
        progress.lv3 >= 5 -> s.rankMythic.uppercase() to PixieRose
        progress.lv3 >= 2 -> s.rankSage.uppercase() to PixieGold
        progress.lv3 >= 1 || !progress.gioihan -> s.rankMage.uppercase() to PixieCyan
        progress.diem >= 500.0 || progress.lv >= 3 -> s.rankSprite.uppercase() to PixieMint
        else -> s.rankNovice.uppercase() to PixieTextSecondary
    }
}

@Composable
fun PixieStatsHeader(
    progress: GameProgress,
    isSyncing: Boolean,
    hasUnsavedChanges: Boolean,
    onManualSync: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    compactNumbers: Boolean = false,
    isSettingsOpen: Boolean = false
) {
    val s = LocalPixieStrings.current
    val (rankTitle, rankColor) = pixieRankInfo(progress, s)

    PixieCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = PixieBorderBright,
        containerColor = PixieBgSurface,
        glowColor = PixieCyan,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.weight(1f)
            ) {
                PixieStarGemSprite(modifier = Modifier.size(20.dp))
                Text(
                    text = s.statsTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = PixieCyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(rankColor.copy(alpha = 0.18f))
                        .border(1.dp, rankColor.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = rankTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = rankColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                if (progress.lv3 > 0L) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixieRose.copy(alpha = 0.24f))
                            .border(1.5.dp, PixieRose, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "R${progress.lv3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixieRose,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                if (!progress.gioihan && progress.autoEnabled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(PixieCyan.copy(alpha = 0.22f))
                            .border(1.dp, PixieCyan, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "AUTO",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixieCyan,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onManualSync,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("sync_now_button")
                ) {
                    Icon(
                        imageVector = if (isSyncing || hasUnsavedChanges) Icons.Filled.CloudSync else Icons.Filled.CloudDone,
                        contentDescription = s.saveNow,
                        tint = if (isSyncing) PixieGold else if (hasUnsavedChanges) PixieCyan else PixieMint
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("open_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = s.openSettings,
                        tint = if (isSettingsOpen) PixieGold else PixieTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatTile(
                label = s.statsPoints,
                value = GameEngine.formatGameNumber(progress.diem, compactNumbers),
                accent = PixieMint,
                isCoin = false,
                modifier = Modifier.weight(1f)
            )
            StatTile(
                label = s.statsCash,
                value = "$${GameEngine.formatCash(progress.cashDo)}",
                accent = PixieGold,
                isCoin = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniStatPill(
                label = s.statsMultiplier,
                value = "x${GameEngine.round2(progress.muti)}",
                color = PixieRose,
                modifier = Modifier.weight(1f)
            )
            MiniStatPill(
                label = s.statsPerClick,
                value = "${GameEngine.pointsPerManualClick(progress)}",
                color = PixieCyan,
                modifier = Modifier.weight(1f)
            )
            MiniStatPill(
                label = s.statsClicks,
                value = GameEngine.formatGameCount(progress.clickCount, compactNumbers),
                color = PixieTextPrimary,
                modifier = Modifier.weight(1f)
            )
            MiniStatPill(
                label = s.statsCountdown,
                value = "${GameEngine.formatSeconds(progress.cd)}s",
                color = PixieGold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    isCoin: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(accent.copy(alpha = 0.20f), PixieBgDeep.copy(alpha = 0.92f))
                )
            )
            .border(1.5.dp, accent.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
            .drawBehind {
                drawLine(
                    color = accent.copy(alpha = 0.5f),
                    start = Offset(6.dp.toPx(), 1.dp.toPx()),
                    end = Offset(size.width - 6.dp.toPx(), 1.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
            }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.34f), PixieBgDeep)
                    )
                )
                .border(1.5.dp, accent.copy(alpha = 0.8f), RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isCoin) {
                PixieCoinSprite(modifier = Modifier.size(22.dp))
            } else {
                PixieStarGemSprite(modifier = Modifier.size(22.dp))
            }
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PixieTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MiniStatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(PixieBgDeep.copy(alpha = 0.82f))
            .border(1.dp, color.copy(alpha = 0.42f), RoundedCornerShape(6.dp))
            .drawBehind {
                drawRect(
                    color = color.copy(alpha = 0.78f),
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, 2.dp.toPx())
                )
            }
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PixieTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PixieToastBanner(
    toast: PixieToast?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        if (toast != null) {
            val bg = if (toast.isSuccess) PixieMintDark else PixieDangerDark
            val border = if (toast.isSuccess) PixieMint else PixieDanger
            val icon = if (toast.isSuccess) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bg)
                    .border(2.dp, border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = border,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = toast.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixieTextPrimary
                )
            }
        }
    }
}

@Composable
fun PixieUpgradeRow(
    title: String,
    subtitle: String,
    levelLabel: String?,
    costLabel: String,
    canAfford: Boolean,
    accentColor: Color,
    buttonText: String,
    testTag: String,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.AutoAwesome,
    relicType: PixieRelicType? = null
) {
    PixieCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = if (canAfford) accentColor else PixieBorder,
        containerColor = if (canAfford) {
            Color(
                red = (PixieBgCard.red * 0.85f + accentColor.red * 0.15f).coerceIn(0f, 1f),
                green = (PixieBgCard.green * 0.85f + accentColor.green * 0.15f).coerceIn(0f, 1f),
                blue = (PixieBgCard.blue * 0.85f + accentColor.blue * 0.15f).coerceIn(0f, 1f),
                alpha = 1f
            )
        } else {
            PixieBgCard
        },
        glowColor = if (canAfford) accentColor else PixieBorderBright
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Ornate RPG Item Relic Pedestal
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (canAfford) 0.36f else 0.12f),
                                PixieBgDeep
                            )
                        )
                    )
                    .border(
                        2.dp,
                        accentColor.copy(alpha = if (canAfford) 0.95f else 0.42f),
                        RoundedCornerShape(9.dp)
                    )
                    .drawBehind {
                        // Pedestal corner glow dots
                        val dot = 2.5.dp.toPx()
                        val dotColor = if (canAfford) Color.White.copy(alpha = 0.7f) else accentColor.copy(alpha = 0.3f)
                        drawRect(dotColor, Offset(4.dp.toPx(), 4.dp.toPx()), Size(dot, dot))
                        drawRect(dotColor, Offset(size.width - 4.dp.toPx() - dot, 4.dp.toPx()), Size(dot, dot))
                    },
                contentAlignment = Alignment.Center
            ) {
                if (relicType != null) {
                    PixieRelicSprite(
                        type = relicType,
                        isActive = canAfford,
                        modifier = Modifier.size(34.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (canAfford) accentColor else PixieTextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = PixieTextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (levelLabel != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.22f))
                                .border(1.dp, accentColor.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = levelLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixieTextSecondary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            if (canAfford) accentColor.copy(alpha = 0.16f) else PixieBgDeep.copy(alpha = 0.7f)
                        )
                        .border(
                            1.dp,
                            if (canAfford) accentColor.copy(alpha = 0.55f) else PixieBorder.copy(alpha = 0.45f),
                            RoundedCornerShape(5.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = costLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (canAfford) accentColor else PixieTextMuted
                    )
                }
            }

            PixieArcadeButton(
                onClick = onBuy,
                accentColor = accentColor,
                enabled = canAfford,
                modifier = Modifier.testTag(testTag)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canAfford) PixieBgDeep else PixieTextMuted,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
