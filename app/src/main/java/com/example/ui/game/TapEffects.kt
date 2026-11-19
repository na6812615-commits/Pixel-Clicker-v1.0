package com.example.ui.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.data.CrystalAuraTheme
import com.example.data.VfxIntensity
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

data class FloatingBurst(
    val id: Long,
    val text: String,
    val isAuto: Boolean,
    val isSuperCombo: Boolean,
    val startX: Float,
    val startY: Float,
    val driftX: Float,
    val createdAtMs: Long
)

data class TapShockwave(
    val id: Long,
    val originOffsetFromCenter: Offset,
    val color: Color,
    val startRadiusDp: Float,
    val maxExpandDp: Float,
    val strokeDp: Float,
    val durationMs: Long,
    val createdAtMs: Long
)

data class TapLightBeam(
    val angleDeg: Float,
    val lengthDp: Float,
    val widthDp: Float,
    val color: Color,
    val durationMs: Long,
    val createdAtMs: Long
)

data class TapSlashArc(
    val originOffsetFromCenter: Offset,
    val radiusDp: Float,
    val startAngleDeg: Float,
    val sweepDeg: Float,
    val thicknessDp: Float,
    val color: Color,
    val durationMs: Long,
    val createdAtMs: Long
)

data class TapStarFlash(
    val originOffsetFromCenter: Offset,
    val rotationDeg: Float,
    val maxArmDp: Float,
    val color: Color,
    val durationMs: Long,
    val createdAtMs: Long
)

data class ManaWispParticle(
    val startOffsetFromCenter: Offset,
    val controlOffsetFromCenter: Offset,
    val endOffsetFromCenter: Offset,
    val color: Color,
    val sizeDp: Float,
    val durationMs: Long,
    val createdAtMs: Long
)

enum class SparkShape {
    COMET_STREAK,
    CROSS_STAR,
    FACETED_GEM
}

data class SparkShard(
    val originOffsetFromCenter: Offset,
    val angleRad: Float,
    val startRadiusDp: Float,
    val travelDp: Float,
    val gravityDp: Float,
    val sizeDp: Float,
    val color: Color,
    val shape: SparkShape,
    val spinDeg: Float,
    val durationMs: Long,
    val createdAtMs: Long
)

fun spawnManualTapEffects(
    event: TapParticleEvent,
    nowMs: Long,
    touchOffsetFromCrystalCenter: Offset,
    comboCount: Int,
    vfxIntensity: VfxIntensity,
    auraTheme: CrystalAuraTheme,
    bursts: MutableList<FloatingBurst>,
    shockwaves: MutableList<TapShockwave>,
    sparks: MutableList<SparkShard>,
    beams: MutableList<TapLightBeam>,
    slashArcs: MutableList<TapSlashArc>,
    starFlashes: MutableList<TapStarFlash>,
    manaWisps: MutableList<ManaWispParticle>
) {
    val isSuperCombo = !event.isAuto && comboCount >= 5 && comboCount % 5 == 0
    val burstPrefix = when {
        event.isAuto -> ""
        isSuperCombo -> "✦ "
        else -> "★ "
    }
    bursts.add(
        FloatingBurst(
            id = event.id,
            text = "$burstPrefix${event.amountText}",
            isAuto = event.isAuto,
            isSuperCombo = isSuperCombo,
            startX = if (event.isAuto) Random.nextInt(-72, 73).toFloat() else (touchOffsetFromCrystalCenter.x * 0.45f + Random.nextInt(-36, 37)),
            startY = if (event.isAuto) -40f else (touchOffsetFromCrystalCenter.y * 0.35f - 28f),
            driftX = Random.nextInt(-32, 33).toFloat(),
            createdAtMs = nowMs
        )
    )
    if (bursts.size > 16) bursts.removeAt(0)

    val primary = auraTheme.primaryColor
    val secondary = auraTheme.secondaryColor

    if (event.isAuto) {
        if (vfxIntensity == VfxIntensity.LOW) {
            return
        }
        shockwaves.add(
            TapShockwave(
                id = event.id,
                originOffsetFromCenter = Offset.Zero,
                color = secondary,
                startRadiusDp = 76f,
                maxExpandDp = 42f,
                strokeDp = 2.5f,
                durationMs = 460L,
                createdAtMs = nowMs
            )
        )
        repeat(if (vfxIntensity == VfxIntensity.ULTRA) 8 else 5) { idx ->
            val baseAngle = (idx.toFloat() / 6f) * (2f * PI.toFloat())
            sparks.add(
                SparkShard(
                    originOffsetFromCenter = Offset.Zero,
                    angleRad = baseAngle + Random.nextFloat() * 0.35f - 0.17f,
                    startRadiusDp = 64f,
                    travelDp = 52f,
                    gravityDp = 6f,
                    sizeDp = 4.5f,
                    color = if (idx % 2 == 0) secondary else PixieGold,
                    shape = SparkShape.COMET_STREAK,
                    spinDeg = 0f,
                    durationMs = 460L,
                    createdAtMs = nowMs
                )
            )
        }
    } else {
        // 1. Crisp fingertip Star-Flash + Ripple right where finger struck the crystal
        starFlashes.add(
            TapStarFlash(
                originOffsetFromCenter = touchOffsetFromCrystalCenter,
                rotationDeg = Random.nextFloat() * 30f - 15f,
                maxArmDp = when (vfxIntensity) {
                    VfxIntensity.LOW -> 28f
                    VfxIntensity.NORMAL -> if (isSuperCombo) 58f else 44f
                    VfxIntensity.ULTRA -> if (isSuperCombo) 72f else 58f
                },
                color = if (isSuperCombo) PixieGold else primary,
                durationMs = 260L,
                createdAtMs = nowMs
            )
        )

        // Fingertip ripple + outer shockwaves
        shockwaves.add(
            TapShockwave(
                id = event.id * 4,
                originOffsetFromCenter = touchOffsetFromCrystalCenter,
                color = Color.White,
                startRadiusDp = 8f,
                maxExpandDp = 56f,
                strokeDp = 3.5f,
                durationMs = 340L,
                createdAtMs = nowMs
            )
        )
        if (vfxIntensity != VfxIntensity.LOW) {
            shockwaves.add(
                TapShockwave(
                    id = event.id * 4 + 1,
                    originOffsetFromCenter = Offset.Zero,
                    color = if (isSuperCombo) PixieGold else primary,
                    startRadiusDp = 74f,
                    maxExpandDp = 68f,
                    strokeDp = 4.5f,
                    durationMs = 460L,
                    createdAtMs = nowMs
                )
            )
            shockwaves.add(
                TapShockwave(
                    id = event.id * 4 + 2,
                    originOffsetFromCenter = Offset.Zero,
                    color = secondary,
                    startRadiusDp = 82f,
                    maxExpandDp = 86f,
                    strokeDp = 2.8f,
                    durationMs = 620L,
                    createdAtMs = nowMs
                )
            )
        }
        if (vfxIntensity == VfxIntensity.ULTRA) {
            shockwaves.add(
                TapShockwave(
                    id = event.id * 4 + 3,
                    originOffsetFromCenter = Offset.Zero,
                    color = PixieRose,
                    startRadiusDp = 88f,
                    maxExpandDp = 104f,
                    strokeDp = 2.2f,
                    durationMs = 740L,
                    createdAtMs = nowMs
                )
            )
        }

        // 2. Curved Neon Crescent Slash Arcs around the tap impact
        val slashCount = when (vfxIntensity) {
            VfxIntensity.LOW -> 0
            VfxIntensity.NORMAL -> if (isSuperCombo) 3 else 2
            VfxIntensity.ULTRA -> 4
        }
        val slashBase = Random.nextFloat() * 360f
        repeat(slashCount) { s ->
            slashArcs.add(
                TapSlashArc(
                    originOffsetFromCenter = touchOffsetFromCrystalCenter * 0.5f,
                    radiusDp = 54f + s * 20f,
                    startAngleDeg = slashBase + s * (360f / slashCount.coerceAtLeast(1)),
                    sweepDeg = 95f + Random.nextFloat() * 35f,
                    thicknessDp = if (s == 0) 5.5f else 3.8f,
                    color = when (s % 3) {
                        0 -> if (isSuperCombo) PixieGold else primary
                        1 -> secondary
                        else -> PixieRose
                    },
                    durationMs = 310L + s * 55L,
                    createdAtMs = nowMs
                )
            )
        }

        // 3. Prismatic Light Pillars bursting behind the crystal
        val beamCount = when (vfxIntensity) {
            VfxIntensity.LOW -> 0
            VfxIntensity.NORMAL -> 10
            VfxIntensity.ULTRA -> 14
        }
        val baseOffset = Random.nextFloat() * 36f
        for (b in 0 until beamCount) {
            beams.add(
                TapLightBeam(
                    angleDeg = baseOffset + b * (360f / beamCount) + Random.nextFloat() * 10f - 5f,
                    lengthDp = (if (vfxIntensity == VfxIntensity.ULTRA) 72f else 58f) + Random.nextFloat() * 32f,
                    widthDp = if (b % 2 == 0) 7.5f else 4.5f,
                    color = when (b % 3) {
                        0 -> primary
                        1 -> secondary
                        else -> if (isSuperCombo) PixieGold else PixieRose
                    },
                    durationMs = 310L,
                    createdAtMs = nowMs
                )
            )
        }

        // 4. High-Energy Comet Sparks, 4-point Stars & Spinning Faceted Crystal Shards
        val sparkCount = when (vfxIntensity) {
            VfxIntensity.LOW -> 6
            VfxIntensity.NORMAL -> if (isSuperCombo) 26 else 20
            VfxIntensity.ULTRA -> 36
        }
        for (idx in 0 until sparkCount) {
            val baseAngle = (idx.toFloat() / sparkCount) * (2f * PI.toFloat())
            val jitter = Random.nextFloat() * 0.34f - 0.17f
            val shape = when (idx % 3) {
                0 -> SparkShape.COMET_STREAK
                1 -> SparkShape.FACETED_GEM
                else -> SparkShape.CROSS_STAR
            }
            sparks.add(
                SparkShard(
                    originOffsetFromCenter = touchOffsetFromCrystalCenter * 0.65f,
                    angleRad = baseAngle + jitter,
                    startRadiusDp = 18f + Random.nextFloat() * 24f,
                    travelDp = (if (vfxIntensity == VfxIntensity.ULTRA) 94f else 76f) + Random.nextFloat() * 68f,
                    gravityDp = 14f + Random.nextFloat() * 16f,
                    sizeDp = if (shape == SparkShape.FACETED_GEM) 6.5f + Random.nextFloat() * 3f else 5f + Random.nextFloat() * 3f,
                    color = when (idx % 4) {
                        0 -> primary
                        1 -> secondary
                        2 -> PixieRose
                        else -> PixieGold
                    },
                    shape = shape,
                    spinDeg = Random.nextFloat() * 260f - 130f,
                    durationMs = 460L + Random.nextLong(0L, 180L),
                    createdAtMs = nowMs
                )
            )
        }

        // 5. Curving Mana Siphon Wisps flying upward toward the Score HUD
        val wispCount = when (vfxIntensity) {
            VfxIntensity.LOW -> 0
            VfxIntensity.NORMAL -> if (isSuperCombo) 5 else 3
            VfxIntensity.ULTRA -> 7
        }
        repeat(wispCount) { w ->
            val side = if (w % 2 == 0) -1f else 1f
            manaWisps.add(
                ManaWispParticle(
                    startOffsetFromCenter = touchOffsetFromCrystalCenter * 0.5f,
                    controlOffsetFromCenter = Offset(
                        x = side * (60f + Random.nextFloat() * 65f),
                        y = -30f - Random.nextFloat() * 40f
                    ),
                    endOffsetFromCenter = Offset(
                        x = Random.nextFloat() * 80f - 40f,
                        y = -185f - Random.nextFloat() * 25f
                    ),
                    color = when (w % 3) {
                        0 -> primary
                        1 -> secondary
                        else -> PixieGold
                    },
                    sizeDp = 5.5f,
                    durationMs = 560L + w * 55L,
                    createdAtMs = nowMs
                )
            )
        }
    }

    while (shockwaves.size > 20) shockwaves.removeAt(0)
    while (beams.size > 40) beams.removeAt(0)
    while (slashArcs.size > 16) slashArcs.removeAt(0)
    while (starFlashes.size > 8) starFlashes.removeAt(0)
    while (manaWisps.size > 24) manaWisps.removeAt(0)
    while (sparks.size > 120) sparks.removeAt(0)
}

fun spawnCooldownTapEffects(
    nowMs: Long,
    touchOffsetFromCrystalCenter: Offset,
    shockwaves: MutableList<TapShockwave>,
    sparks: MutableList<SparkShard>
) {
    shockwaves.add(
        TapShockwave(
            id = nowMs,
            originOffsetFromCenter = touchOffsetFromCrystalCenter,
            color = PixieGold,
            startRadiusDp = 6f,
            maxExpandDp = 34f,
            strokeDp = 2.5f,
            durationMs = 260L,
            createdAtMs = nowMs
        )
    )
    repeat(6) { idx ->
        val angle = (idx / 6f) * (2f * PI.toFloat()) + Random.nextFloat() * 0.3f
        sparks.add(
            SparkShard(
                originOffsetFromCenter = touchOffsetFromCrystalCenter,
                angleRad = angle,
                startRadiusDp = 6f,
                travelDp = 36f,
                gravityDp = 8f,
                sizeDp = 4f,
                color = PixieGold,
                shape = SparkShape.COMET_STREAK,
                spinDeg = 0f,
                durationMs = 280L,
                createdAtMs = nowMs
            )
        )
    }
    while (shockwaves.size > 16) shockwaves.removeAt(0)
    while (sparks.size > 90) sparks.removeAt(0)
}

fun DrawScope.drawBehindCrystalEffects(
    center: Offset,
    nowMs: Long,
    lastManualTapMs: Long,
    beams: List<TapLightBeam>
) {
    // 1. Prismatic radial god-rays shooting outward behind crystal
    beams.forEach { beam ->
        val frac = ((nowMs - beam.createdAtMs).toFloat() / beam.durationMs.toFloat()).coerceIn(0f, 1f)
        if (frac < 1f) {
            val alpha = (1f - frac).pow(1.5f) * 0.85f
            val innerR = 62.dp.toPx() + frac * 18.dp.toPx()
            val outerR = innerR + beam.lengthDp.dp.toPx() * (1f - frac * 0.25f)
            rotate(beam.angleDeg, pivot = center) {
                val halfW = (beam.widthDp * (1f - frac * 0.45f)).dp.toPx() / 2f
                val outerPath = Path().apply {
                    moveTo(center.x + innerR, center.y - halfW)
                    lineTo(center.x + outerR, center.y)
                    lineTo(center.x + innerR, center.y + halfW)
                    close()
                }
                drawPath(path = outerPath, color = beam.color.copy(alpha = alpha * 0.55f))

                val corePath = Path().apply {
                    moveTo(center.x + innerR, center.y - halfW * 0.38f)
                    lineTo(center.x + outerR * 0.94f, center.y)
                    lineTo(center.x + innerR, center.y + halfW * 0.38f)
                    close()
                }
                drawPath(path = corePath, color = Color.White.copy(alpha = alpha * 0.85f))
            }
        }
    }

    // 2. Impact bloom surge behind the orb
    val tapAge = nowMs - lastManualTapMs
    if (lastManualTapMs > 0L && tapAge in 0L..280L) {
        val f = (tapAge.toFloat() / 280f).coerceIn(0f, 1f)
        val haloR = 104.dp.toPx() + f * 38.dp.toPx()
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = (1f - f) * 0.48f),
                    PixieMint.copy(alpha = (1f - f) * 0.40f),
                    PixieCyan.copy(alpha = (1f - f) * 0.18f),
                    Color.Transparent
                ),
                center = center,
                radius = haloR
            ),
            radius = haloR,
            center = center
        )
    }
}

fun DrawScope.drawForegroundImpactOverlay(
    crystalCenterInOverlay: Offset,
    nowMs: Long,
    shockwaves: List<TapShockwave>,
    sparks: List<SparkShard>,
    slashArcs: List<TapSlashArc>,
    starFlashes: List<TapStarFlash>,
    manaWisps: List<ManaWispParticle>
) {
    // 1. Expanding Shockwaves (both fingertip and crystal-centered)
    shockwaves.forEach { wave ->
        val frac = ((nowMs - wave.createdAtMs).toFloat() / wave.durationMs.toFloat()).coerceIn(0f, 1f)
        val eased = 1f - (1f - frac).pow(2.2f)
        val origin = crystalCenterInOverlay + wave.originOffsetFromCenter
        val r = wave.startRadiusDp.dp.toPx() + eased * wave.maxExpandDp.dp.toPx()
        val alpha = (1f - frac).pow(1.35f) * 0.88f
        drawCircle(
            color = wave.color.copy(alpha = alpha),
            radius = r,
            center = origin,
            style = Stroke(width = (wave.strokeDp * (1f - frac * 0.65f)).dp.toPx())
        )
    }

    // 2. Curved Neon Crescent Slash Arcs sweeping over the crystal
    slashArcs.forEach { slash ->
        val frac = ((nowMs - slash.createdAtMs).toFloat() / slash.durationMs.toFloat()).coerceIn(0f, 1f)
        val eased = 1f - (1f - frac).pow(2f)
        val origin = crystalCenterInOverlay + slash.originOffsetFromCenter
        val r = slash.radiusDp.dp.toPx() + eased * 18.dp.toPx()
        val alpha = (1f - frac).pow(1.4f)
        val startAngle = slash.startAngleDeg + eased * 28f
        val sweep = slash.sweepDeg * (1f - frac * 0.35f)
        val topLeft = Offset(origin.x - r, origin.y - r)
        val arcSize = Size(r * 2f, r * 2f)

        // Outer neon glow arc
        drawArc(
            color = slash.color.copy(alpha = alpha * 0.72f),
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = slash.thicknessDp.dp.toPx() * (1f - frac * 0.5f), cap = StrokeCap.Round)
        )
        // Bright white inner filament
        drawArc(
            color = Color.White.copy(alpha = alpha * 0.92f),
            startAngle = startAngle + sweep * 0.1f,
            sweepAngle = sweep * 0.8f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = (slash.thicknessDp * 0.42f).dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // 3. Fingertip 4-Point Anime Star Flash right at impact site
    starFlashes.forEach { flash ->
        val frac = ((nowMs - flash.createdAtMs).toFloat() / flash.durationMs.toFloat()).coerceIn(0f, 1f)
        val envelope = if (frac < 0.25f) frac / 0.25f else (1f - (frac - 0.25f) / 0.75f)
        val arm = flash.maxArmDp.dp.toPx() * envelope.coerceIn(0f, 1f)
        val thick = (6.dp.toPx() * (1f - frac * 0.6f)).coerceAtLeast(1.5f)
        val origin = crystalCenterInOverlay + flash.originOffsetFromCenter
        val alpha = (1f - frac * frac).coerceIn(0f, 1f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha * 0.85f),
                    flash.color.copy(alpha = alpha * 0.45f),
                    Color.Transparent
                ),
                center = origin,
                radius = (arm * 0.75f).coerceAtLeast(4f)
            ),
            radius = (arm * 0.75f).coerceAtLeast(4f),
            center = origin
        )

        rotate(flash.rotationDeg, pivot = origin) {
            val starPath = Path().apply {
                moveTo(origin.x, origin.y - arm)
                lineTo(origin.x + thick, origin.y - thick)
                lineTo(origin.x + arm, origin.y)
                lineTo(origin.x + thick, origin.y + thick)
                lineTo(origin.x, origin.y + arm)
                lineTo(origin.x - thick, origin.y + thick)
                lineTo(origin.x - arm, origin.y)
                lineTo(origin.x - thick, origin.y - thick)
                close()
            }
            drawPath(path = starPath, color = flash.color.copy(alpha = alpha * 0.8f))
            drawCircle(color = Color.White.copy(alpha = alpha), radius = thick * 0.9f, center = origin)
        }
    }

    // 4. High-Speed Comet Streaks, Faceted Crystal Shards & Cross Stars
    sparks.forEach { spark ->
        val frac = ((nowMs - spark.createdAtMs).toFloat() / spark.durationMs.toFloat()).coerceIn(0f, 1f)
        val prevFrac = (frac - 0.10f).coerceAtLeast(0f)

        val eased = 1f - (1f - frac).pow(2.4f)
        val prevEased = 1f - (1f - prevFrac).pow(2.4f)

        val origin = crystalCenterInOverlay + spark.originOffsetFromCenter
        val startR = spark.startRadiusDp.dp.toPx()
        val travel = spark.travelDp.dp.toPx()
        val grav = spark.gravityDp.dp.toPx()

        val dist = startR + eased * travel
        val prevDist = startR + prevEased * travel

        val sx = origin.x + cos(spark.angleRad) * dist
        val sy = origin.y + sin(spark.angleRad) * dist + frac * frac * grav
        val px = origin.x + cos(spark.angleRad) * prevDist
        val py = origin.y + sin(spark.angleRad) * prevDist + prevFrac * prevFrac * grav

        val sz = spark.sizeDp.dp.toPx() * (1f - frac * 0.45f)
        val alpha = (1f - frac * frac).coerceIn(0f, 1f)
        val pColor = spark.color.copy(alpha = alpha)

        when (spark.shape) {
            SparkShape.COMET_STREAK -> {
                drawLine(
                    color = pColor.copy(alpha = alpha * 0.78f),
                    start = Offset(px, py),
                    end = Offset(sx, sy),
                    strokeWidth = sz * 0.75f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha * 0.95f),
                    start = Offset((px + sx) * 0.5f, (py + sy) * 0.5f),
                    end = Offset(sx, sy),
                    strokeWidth = sz * 0.35f,
                    cap = StrokeCap.Round
                )
            }
            SparkShape.CROSS_STAR -> {
                val arm = sz * 1.15f
                val thick = sz * 0.36f
                drawRect(
                    color = pColor,
                    topLeft = Offset(sx - arm, sy - thick / 2f),
                    size = Size(arm * 2f, thick)
                )
                drawRect(
                    color = pColor,
                    topLeft = Offset(sx - thick / 2f, sy - arm),
                    size = Size(thick, arm * 2f)
                )
                drawRect(
                    color = Color.White.copy(alpha = alpha),
                    topLeft = Offset(sx - thick * 0.5f, sy - thick * 0.5f),
                    size = Size(thick, thick)
                )
            }
            SparkShape.FACETED_GEM -> {
                rotate(45f + spark.spinDeg * frac, pivot = Offset(sx, sy)) {
                    drawRect(
                        color = Color(0xFF0C0818).copy(alpha = alpha),
                        topLeft = Offset(sx - sz * 0.65f, sy - sz * 0.65f),
                        size = Size(sz * 1.3f, sz * 1.3f)
                    )
                    drawRect(
                        color = pColor,
                        topLeft = Offset(sx - sz * 0.45f, sy - sz * 0.45f),
                        size = Size(sz * 0.9f, sz * 0.9f)
                    )
                    drawRect(
                        color = Color.White.copy(alpha = alpha * 0.9f),
                        topLeft = Offset(sx - sz * 0.35f, sy - sz * 0.35f),
                        size = Size(sz * 0.35f, sz * 0.35f)
                    )
                }
            }
        }
    }

    // 5. Quadratic-Bezier Mana Siphon Wisps curving upward toward the Score HUD
    manaWisps.forEach { wisp ->
        val t = ((nowMs - wisp.createdAtMs).toFloat() / wisp.durationMs.toFloat()).coerceIn(0f, 1f)
        val p0 = crystalCenterInOverlay + wisp.startOffsetFromCenter
        val p1 = crystalCenterInOverlay + Offset(wisp.controlOffsetFromCenter.x.dp.toPx(), wisp.controlOffsetFromCenter.y.dp.toPx())
        val p2 = crystalCenterInOverlay + Offset(wisp.endOffsetFromCenter.x.dp.toPx(), wisp.endOffsetFromCenter.y.dp.toPx())

        for (step in 0..3) {
            val st = (t - step * 0.055f).coerceIn(0f, 1f)
            val u = 1f - st
            val pos = Offset(
                x = u * u * p0.x + 2f * u * st * p1.x + st * st * p2.x,
                y = u * u * p0.y + 2f * u * st * p1.y + st * st * p2.y
            )
            val fade = (1f - t * t) * (1f - step * 0.22f)
            val r = wisp.sizeDp.dp.toPx() * (1f - step * 0.18f) * (1f - t * 0.35f)
            drawCircle(
                color = wisp.color.copy(alpha = fade * 0.45f),
                radius = r * 1.9f,
                center = pos
            )
            drawCircle(
                color = if (step == 0) Color.White.copy(alpha = fade) else wisp.color.copy(alpha = fade),
                radius = r * 0.85f,
                center = pos
            )
        }
    }
}
