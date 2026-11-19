package com.example.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameProgress
import com.example.data.GameSettings
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.theme.PixieBgCard
import com.example.ui.theme.PixieBgCardHighlight
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieBorder
import com.example.ui.theme.PixieBorderBright
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieCyanDark
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextMuted
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TapArenaTab(
    progress: GameProgress,
    cooldownFraction: Float,
    particlesFlow: SharedFlow<TapParticleEvent>,
    onManualTap: () -> Unit,
    onToggleAuto: () -> Unit,
    onGoToDollarShop: () -> Unit,
    modifier: Modifier = Modifier,
    settings: GameSettings = GameSettings()
) {
    val s = LocalPixieStrings.current
    val haptics = LocalHapticFeedback.current
    val currentSettings by rememberUpdatedState(settings)
    val auraPrimary = settings.crystalAuraTheme.primaryColor
    val auraSecondary = settings.crystalAuraTheme.secondaryColor
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var lastManualTapMs by remember { mutableLongStateOf(0L) }
    var lastCooldownTapMs by remember { mutableLongStateOf(0L) }
    var manualComboCount by remember { mutableIntStateOf(0) }
    var ringSurgeAngle by remember { mutableFloatStateOf(0f) }

    // Exact touch offset relative to crystal center (updated non-invasively via PointerEventPass.Initial)
    var lastTouchOffsetFromCenter by remember { mutableStateOf(Offset.Zero) }
    var stageRootPos by remember { mutableStateOf(Offset.Zero) }
    var crystalCenterInRoot by remember { mutableStateOf<Offset?>(null) }

    val bursts = remember { mutableStateListOf<FloatingBurst>() }
    val shockwaves = remember { mutableStateListOf<TapShockwave>() }
    val sparks = remember { mutableStateListOf<SparkShard>() }
    val beams = remember { mutableStateListOf<TapLightBeam>() }
    val slashArcs = remember { mutableStateListOf<TapSlashArc>() }
    val starFlashes = remember { mutableStateListOf<TapStarFlash>() }
    val manaWisps = remember { mutableStateListOf<ManaWispParticle>() }

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isRecentManualImpact by remember { mutableStateOf(false) }

    LaunchedEffect(lastManualTapMs) {
        if (lastManualTapMs > 0L) {
            isRecentManualImpact = true
            delay(180L)
            isRecentManualImpact = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.86f
            isRecentManualImpact -> 1.12f
            cooldownFraction > 0.55f -> 1.04f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.34f, stiffness = 560f),
        label = "crystal_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "sanctuary_rings")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )
    val bobPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "crystal_bob"
    )
    val sweepFraction by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gem_sheen"
    )

    LaunchedEffect(particlesFlow) {
        particlesFlow.collect { event ->
            val t = System.currentTimeMillis()
            nowMs = t
            if (!event.isAuto) {
                if (t - lastManualTapMs <= 2000L) {
                    manualComboCount += 1
                } else {
                    manualComboCount = 1
                }
                lastManualTapMs = t
                ringSurgeAngle = (ringSurgeAngle + 28f) % 360f
            }
            spawnManualTapEffects(
                event = event,
                nowMs = t,
                touchOffsetFromCrystalCenter = lastTouchOffsetFromCenter,
                comboCount = if (currentSettings.showComboCounter) manualComboCount else 0,
                vfxIntensity = currentSettings.vfxIntensity,
                auraTheme = currentSettings.crystalAuraTheme,
                bursts = bursts,
                shockwaves = shockwaves,
                sparks = sparks,
                beams = beams,
                slashArcs = slashArcs,
                starFlashes = starFlashes,
                manaWisps = manaWisps
            )
        }
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            val t = System.currentTimeMillis()
            val hasActiveEffects = bursts.isNotEmpty() ||
                shockwaves.isNotEmpty() ||
                sparks.isNotEmpty() ||
                beams.isNotEmpty() ||
                slashArcs.isNotEmpty() ||
                starFlashes.isNotEmpty() ||
                manaWisps.isNotEmpty() ||
                (lastManualTapMs > 0L && t - lastManualTapMs < 280L) ||
                (lastCooldownTapMs > 0L && t - lastCooldownTapMs < 220L)

            if (hasActiveEffects) {
                nowMs = t
                bursts.removeAll { t - it.createdAtMs > 840L }
                shockwaves.removeAll { t - it.createdAtMs > it.durationMs }
                sparks.removeAll { t - it.createdAtMs > it.durationMs }
                beams.removeAll { t - it.createdAtMs > it.durationMs }
                slashArcs.removeAll { t - it.createdAtMs > it.durationMs }
                starFlashes.removeAll { t - it.createdAtMs > it.durationMs }
                manaWisps.removeAll { t - it.createdAtMs > it.durationMs }
                if (manualComboCount > 0 && t - lastManualTapMs > 2000L) {
                    manualComboCount = 0
                }
                delay(16L)
            } else {
                if (manualComboCount > 0 && t - lastManualTapMs > 2000L) {
                    manualComboCount = 0
                }
                delay(80L)
            }
        }
    }

    val dollarProgress = ((progress.diem % 1000.0) / 1000.0).toFloat().coerceIn(0f, 1f)
    val bobOffsetY = sin(bobPhase) * 5f
    val animatedRingAngle = ringRotation + ringSurgeAngle

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Enchanted Pixie Sanctuary Stage (Vivid 16-bit Pixel Art Viewport + Full-Stage VFX Overlay!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    stageRootPos = coords.positionInRoot()
                }
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = if (isRecentManualImpact) auraPrimary else auraSecondary.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp)
                )
                .drawBehind {
                    drawRoundRect(
                        color = Color(0xFF0A0716),
                        topLeft = Offset.Zero,
                        size = size,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
        ) {
            PixieArtworkImage(
                resId = R.drawable.img_pixie_sanctuary,
                contentDescription = s.sanctuaryBadge,
                contentScale = ContentScale.Crop,
                targetMaxPx = 640,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.92f)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                PixieBgDeep.copy(alpha = 0.52f),
                                PixieBgDeep.copy(alpha = 0.14f),
                                PixieBgDeep.copy(alpha = 0.20f),
                                PixieBgDeep.copy(alpha = 0.80f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PixieBgDeep.copy(alpha = 0.84f))
                            .border(1.5.dp, PixieMint.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = s.sanctuaryBadge,
                            style = MaterialTheme.typography.labelLarge,
                            color = PixieMint
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = settings.showComboCounter && manualComboCount >= 2,
                            enter = scaleIn(spring(dampingRatio = 0.45f)) + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PixieRose.copy(alpha = 0.30f))
                                    .border(1.5.dp, PixieRose, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = s.comboLabel(manualComboCount),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PixieRose
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PixieBgDeep.copy(alpha = 0.84f))
                                .border(1.5.dp, PixieGold.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = s.discountLabel(GameEngine.round2(progress.giamgia).toString()),
                                style = MaterialTheme.typography.labelMedium,
                                color = PixieGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive Floating Crystal Altar + Behind-Crystal Aura/Beams
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(268.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val outerR = size.minDimension * 0.47f
                        val midR = size.minDimension * 0.415f

                        val shadowScale = 1f - (bobOffsetY / 24f)
                        drawOval(
                            color = Color.Black.copy(alpha = 0.45f),
                            topLeft = Offset(
                                center.x - 54.dp.toPx() * shadowScale,
                                size.height - 22.dp.toPx()
                            ),
                            size = Size(108.dp.toPx() * shadowScale, 14.dp.toPx())
                        )

                        val flashAlpha = computeTapFlashAlpha(nowMs, lastManualTapMs)
                        val comboBoost = (if (settings.showComboCounter) manualComboCount.coerceAtMost(10) * 0.03f else 0f) + flashAlpha * 0.4f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    auraPrimary.copy(alpha = (0.34f * pulseGlow + comboBoost).coerceIn(0f, 0.88f)),
                                    auraSecondary.copy(alpha = (0.16f * pulseGlow + comboBoost * 0.5f).coerceIn(0f, 0.6f)),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = outerR
                            ),
                            radius = outerR,
                            center = center
                        )

                        // Rotating outer arcane celestial ring + 24 clock ticks + cardinal gems
                        if (settings.showArcaneRings) {
                            rotate(animatedRingAngle, pivot = center) {
                                drawCircle(
                                    color = if (flashAlpha > 0.1f) auraPrimary else auraSecondary.copy(alpha = 0.68f),
                                    radius = outerR,
                                    center = center,
                                    style = Stroke(
                                        width = (2.dp + (flashAlpha * 1.5f).dp).toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                                    )
                                )
                                for (t in 0 until 24) {
                                    val ang = (t * 15.0) * PI / 180.0
                                    val r1 = outerR - 5.dp.toPx()
                                    val r2 = outerR + (if (t % 3 == 0) 4.dp.toPx() else 1.5.dp.toPx())
                                    drawLine(
                                        color = if (t % 6 == 0) PixieGold else auraSecondary.copy(alpha = 0.48f),
                                        start = Offset(
                                            center.x + (r1 * cos(ang)).toFloat(),
                                            center.y + (r1 * sin(ang)).toFloat()
                                        ),
                                        end = Offset(
                                            center.x + (r2 * cos(ang)).toFloat(),
                                            center.y + (r2 * sin(ang)).toFloat()
                                        ),
                                        strokeWidth = if (t % 6 == 0) 2.dp.toPx() else 1.dp.toPx()
                                    )
                                }
                                for (i in 0 until 4) {
                                    val angle = (i * 90.0) * PI / 180.0
                                    val rx = center.x + (outerR * cos(angle)).toFloat()
                                    val ry = center.y + (outerR * sin(angle)).toFloat()
                                    drawRect(
                                        color = Color(0xFF0C0818),
                                        topLeft = Offset(rx - 5.dp.toPx(), ry - 5.dp.toPx()),
                                        size = Size(10.dp.toPx(), 10.dp.toPx())
                                    )
                                    drawRect(
                                        color = if (i % 2 == 0) auraPrimary else auraSecondary,
                                        topLeft = Offset(rx - 3.5.dp.toPx(), ry - 3.5.dp.toPx()),
                                        size = Size(7.dp.toPx(), 7.dp.toPx())
                                    )
                                }
                            }

                            rotate(-animatedRingAngle * 1.35f, pivot = center) {
                                drawCircle(
                                    color = PixieRose.copy(alpha = 0.50f),
                                    radius = midR,
                                    center = center,
                                    style = Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
                                    )
                                )
                                for (sq in 0 until 2) {
                                    val rotOffset = sq * 45.0
                                    val path = Path()
                                    for (v in 0 until 4) {
                                        val ang = (v * 90.0 + rotOffset) * PI / 180.0
                                        val px = center.x + (midR * cos(ang)).toFloat()
                                        val py = center.y + (midR * sin(ang)).toFloat()
                                        if (v == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    }
                                    path.close()
                                    drawPath(
                                        path = path,
                                        color = (if (sq == 0) PixieMint else PixieGold).copy(alpha = 0.28f),
                                        style = Stroke(width = 1.2.dp.toPx())
                                    )
                                }
                            }
                        }

                        drawBehindCrystalEffects(
                            center = center,
                            nowMs = nowMs,
                            lastManualTapMs = lastManualTapMs,
                            beams = beams
                        )

                        // Orbiting Animated Pixie Fairies
                        if (settings.showArcaneRings) {
                            if (!progress.gioihan && progress.autoEnabled) {
                                for (w in 0 until 2) {
                                    val baseDeg = animatedRingAngle * 2.2f + w * 180f
                                    val angleRad = baseDeg * PI / 180.0
                                    val wx = center.x + (outerR * 0.94f * cos(angleRad)).toFloat()
                                    val wy = center.y + (outerR * 0.94f * sin(angleRad)).toFloat()
                                    val bodyC = if (w == 0) PixieCyan else PixieGold
                                    val wingC = if (w == 0) PixieMint else PixieRose

                                    for (trail in 1..4) {
                                        val tAngle = (baseDeg - trail * 9f) * PI / 180.0
                                        val tx = center.x + (outerR * 0.94f * cos(tAngle)).toFloat()
                                        val ty = center.y + (outerR * 0.94f * sin(tAngle)).toFloat()
                                        val pSize = (4f - trail * 0.7f).dp.toPx()
                                        drawRect(
                                            color = bodyC.copy(alpha = 0.65f - trail * 0.14f),
                                            topLeft = Offset(tx - pSize / 2f, ty - pSize / 2f),
                                            size = Size(pSize, pSize)
                                        )
                                    }

                                    drawPixieFairySprite(
                                        center = Offset(wx, wy),
                                        wingFlapPhase = bobPhase * 4f + w * 1.6f,
                                        bodyColor = bodyC,
                                        wingColor = wingC,
                                        scalePx = density
                                    )
                                }
                            } else {
                                val wAngle = animatedRingAngle * 0.8f * PI / 180.0
                                val wx = center.x + (outerR * 0.92f * cos(wAngle)).toFloat()
                                val wy = center.y + (outerR * 0.92f * sin(wAngle)).toFloat() + sin(bobPhase * 2f) * 4.dp.toPx()
                                drawPixieFairySprite(
                                    center = Offset(wx, wy),
                                    wingFlapPhase = bobPhase * 3f,
                                    bodyColor = PixieRose,
                                    wingColor = PixieCyan,
                                    scalePx = density * 0.85f
                                )
                            }
                        }
                    }

                    // Tap Target Levitating Crystal Orb + Exact Touch Position Tracker
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset {
                                val t = nowMs
                                val tapAge = (t - lastManualTapMs).coerceAtLeast(0L)
                                val rx = if (!settings.screenShakeEnabled) {
                                    0f
                                } else {
                                    when {
                                        lastManualTapMs > 0L && tapAge < 230L -> {
                                            val decay = 1f - (tapAge / 230f)
                                            sin(tapAge * 0.09f) * 10f * decay * decay
                                        }
                                        lastCooldownTapMs > 0L && (t - lastCooldownTapMs) in 0L..180L -> {
                                            val cdAge = t - lastCooldownTapMs
                                            val decay = 1f - (cdAge / 180f)
                                            sin(cdAge * 0.14f) * 6f * decay
                                        }
                                        else -> 0f
                                    }
                                }
                                val ry = if (settings.screenShakeEnabled && lastManualTapMs > 0L && tapAge < 210L) {
                                    val decay = 1f - (tapAge / 210f)
                                    cos(tapAge * 0.075f) * 6f * decay * decay
                                } else {
                                    0f
                                }
                                IntOffset(rx.roundToInt(), (bobOffsetY + ry).roundToInt())
                            }
                            .size(196.dp)
                            .onGloballyPositioned { coords ->
                                val pos = coords.positionInRoot()
                                crystalCenterInRoot = Offset(
                                    x = pos.x + coords.size.width / 2f,
                                    y = pos.y + coords.size.height / 2f
                                )
                            }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        val down = event.changes.firstOrNull { it.pressed }
                                        if (down != null) {
                                            val boxCenter = Offset(size.width / 2f, size.height / 2f)
                                            lastTouchOffsetFromCenter = down.position - boxCenter
                                        }
                                    }
                                }
                            }
                            .scale(scale)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = true, color = auraPrimary),
                                role = Role.Button,
                                onClickLabel = s.tapHintAccessibility,
                                onClick = {
                                    if (cooldownFraction > 0f) {
                                        val t = System.currentTimeMillis()
                                        nowMs = t
                                        lastCooldownTapMs = t
                                        spawnCooldownTapEffects(
                                            nowMs = t,
                                            touchOffsetFromCrystalCenter = lastTouchOffsetFromCenter,
                                            shockwaves = shockwaves,
                                            sparks = sparks
                                        )
                                    } else if (settings.hapticEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onManualTap()
                                }
                            )
                            .testTag("tap_crystal_button")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 9.dp.toPx()
                            val pad = strokeWidth / 2f
                            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                            drawArc(
                                color = PixieBgDeep.copy(alpha = 0.88f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = Offset(pad, pad),
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                            val readySweep = (1f - cooldownFraction) * 360f
                            drawArc(
                                brush = Brush.sweepGradient(
                                    colors = if (cooldownFraction > 0f) {
                                        listOf(PixieGold, PixieRose, PixieGold)
                                    } else {
                                        listOf(auraPrimary, auraSecondary, auraPrimary)
                                    }
                                ),
                                startAngle = -90f,
                                sweepAngle = readySweep,
                                useCenter = false,
                                topLeft = Offset(pad, pad),
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isRecentManualImpact) 3.5.dp else 3.dp,
                                    color = if (isRecentManualImpact) Color.White else auraPrimary.copy(alpha = 0.9f),
                                    shape = CircleShape
                                )
                        ) {
                            PixieArtworkImage(
                                resId = R.drawable.img_pixie_crystal,
                                contentDescription = s.tapHintAccessibility,
                                contentScale = ContentScale.Crop,
                                targetMaxPx = 384,
                                modifier = Modifier.fillMaxSize()
                            )
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val bandCenter = size.width * sweepFraction
                                val sheen = Path().apply {
                                    moveTo(bandCenter - 26.dp.toPx(), 0f)
                                    lineTo(bandCenter + 10.dp.toPx(), 0f)
                                    lineTo(bandCenter - 36.dp.toPx(), size.height)
                                    lineTo(bandCenter - 72.dp.toPx(), size.height)
                                    close()
                                }
                                drawPath(
                                    path = sheen,
                                    color = Color.White.copy(alpha = 0.16f)
                                )
                                val flashAlpha = computeTapFlashAlpha(nowMs, lastManualTapMs)
                                if (flashAlpha > 0.01f) {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = flashAlpha),
                                                auraPrimary.copy(alpha = flashAlpha * 0.72f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Rising Bouncy +N Score Popups with Super-Combo Styling
                    bursts.forEach { burst ->
                        val shouldRender = if (burst.isAuto) {
                            settings.showFloatingNumbers && settings.showAutoFloatingNumbers
                        } else {
                            settings.showFloatingNumbers
                        }
                        if (shouldRender) {
                            val ageFraction = ((nowMs - burst.createdAtMs).toFloat() / 840f).coerceIn(0f, 1f)
                            val risePx = (burst.startY - (1f - (1f - ageFraction).pow(1.8f)) * 145f).roundToInt()
                            val driftPx = (burst.startX + burst.driftX * ageFraction).roundToInt()
                            val popScale = when {
                                burst.isAuto -> 0.96f
                                ageFraction < 0.18f -> 0.6f + (ageFraction / 0.18f) * 0.85f
                                ageFraction < 0.34f -> 1.45f - ((ageFraction - 0.18f) / 0.16f) * 0.40f
                                else -> 1.05f
                            }
                            val alpha = (1f - ageFraction * ageFraction).coerceIn(0f, 1f)
                            val c = when {
                                burst.isAuto -> auraSecondary
                                burst.isSuperCombo -> PixieGold
                                else -> auraPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(driftPx, risePx) }
                                    .scale(popScale)
                                    .alpha(alpha)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(PixieBgDeep.copy(alpha = 0.92f))
                                    .border(2.dp, c, RoundedCornerShape(7.dp))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = burst.text,
                                    style = if (burst.isSuperCombo) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                                    color = c
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .scale(if (isRecentManualImpact) 1.05f else 1.0f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PixieBgDeep.copy(alpha = 0.88f))
                        .border(
                            1.5.dp,
                            if (cooldownFraction > 0f) PixieGold else auraPrimary,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    val perClick = GameEngine.pointsPerManualClick(progress)
                    val perClickText = if (settings.compactNumbers && perClick >= 1000.0) {
                        GameEngine.formatGameNumber(perClick, true)
                    } else {
                        perClick.toString()
                    }
                    Text(
                        text = if (cooldownFraction > 0f) {
                            s.cooldownWait(GameEngine.formatSeconds(progress.cd))
                        } else {
                            s.tapCrystalCta(perClickText)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = if (cooldownFraction > 0f) PixieGold else auraPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Full-Stage Unclipped Foreground Impact Overlay Canvas (drawn OVER crystal & fingertip!)
            Canvas(modifier = Modifier.matchParentSize()) {
                val fallbackCenter = Offset(size.width / 2f, size.height * 0.48f)
                val centerInOverlay = crystalCenterInRoot?.let { it - stageRootPos } ?: fallbackCenter
                drawForegroundImpactOverlay(
                    crystalCenterInOverlay = centerInOverlay,
                    nowMs = nowMs,
                    shockwaves = shockwaves,
                    sparks = sparks,
                    slashArcs = slashArcs,
                    starFlashes = starFlashes,
                    manaWisps = manaWisps
                )
            }
        }

        // Next $1 Exchange Mint Progress Meter
        if (settings.showDollarProgressCard) {
            PixieCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dollar_progress_card"),
                borderColor = PixieGold.copy(alpha = 0.72f),
                glowColor = PixieGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PixieCoinSprite(modifier = Modifier.size(22.dp))
                        Text(
                            text = s.nextDollarTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = PixieTextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(PixieBgDeep)
                            .border(1.dp, PixieGold.copy(alpha = 0.6f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${(progress.diem % 1000.0).toInt()} / 1000",
                            style = MaterialTheme.typography.labelLarge,
                            color = PixieGold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { if (progress.diem >= 1000.0) 1f else dollarProgress },
                    color = if (progress.diem >= 1000.0) PixieMint else PixieGold,
                    trackColor = PixieBgCardHighlight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(9.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                if (progress.diem >= 1000.0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = s.canExchangeDollarHint((progress.diem / 1000.0).toInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixieMint
                    )
                }
            }
        }

        // Auto Clicker Control Deck
        PixieCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (!progress.gioihan && progress.autoEnabled) PixieCyan else PixieBorder,
            containerColor = if (!progress.gioihan && progress.autoEnabled) PixieCyanDark.copy(alpha = 0.55f) else PixieBgCard,
            glowColor = if (!progress.gioihan && progress.autoEnabled) PixieCyan else PixieBorderBright
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        (if (progress.gioihan) PixieTextMuted else if (progress.autoEnabled) PixieCyan else PixieGold).copy(alpha = 0.28f),
                                        PixieBgDeep
                                    )
                                )
                            )
                            .border(
                                2.dp,
                                if (progress.gioihan) PixieTextMuted else if (progress.autoEnabled) PixieCyan else PixieGold,
                                RoundedCornerShape(9.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        PixieRelicSprite(
                            type = PixieRelicType.AUTO_UNLOCK,
                            isActive = !progress.gioihan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column {
                        Text(
                            text = when {
                                progress.gioihan -> s.autoLockedTitle
                                progress.autoEnabled -> s.autoActiveTitle
                                else -> s.autoPausedTitle
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = PixieTextPrimary
                        )
                        Text(
                            text = if (progress.gioihan) {
                                s.autoUnlockHint
                            } else {
                                s.autoRateHint(
                                    GameEngine.pointsPerAutoClick(progress).toString(),
                                    GameEngine.formatSeconds(progress.cd2)
                                )
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = PixieTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (progress.gioihan) {
                    PixieArcadeButton(
                        onClick = onGoToDollarShop,
                        accentColor = PixieGold,
                        modifier = Modifier.testTag("goto_dollar_shop_button")
                    ) {
                        Text(
                            text = s.tabDollarShop,
                            style = MaterialTheme.typography.labelLarge,
                            color = PixieBgDeep,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    PixieArcadeButton(
                        onClick = onToggleAuto,
                        accentColor = if (progress.autoEnabled) PixieCyan else PixieRose,
                        modifier = Modifier.testTag("arena_toggle_auto_button")
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
        }
    }
}

private fun computeTapFlashAlpha(nowMs: Long, lastManualTapMs: Long): Float {
    if (lastManualTapMs <= 0L) return 0f
    val tapAgeMs = (nowMs - lastManualTapMs).coerceAtLeast(0L)
    return if (tapAgeMs < 240L) {
        (1f - tapAgeMs / 240f).pow(1.4f) * 0.68f
    } else {
        0f
    }
}

