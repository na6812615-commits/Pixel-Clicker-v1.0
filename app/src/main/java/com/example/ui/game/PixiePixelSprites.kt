package com.example.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextMuted
import kotlin.math.sin

enum class PixieRelicType {
    TAP_POWER,
    MULTIPLIER,
    COOLDOWN,
    DISCOUNT,
    MENU_SPEED,
    TOAST_SPEED,
    AUTO_UNLOCK,
    AUTO_POWER,
    AUTO_SPEED
}

/**
 * Renders crisp 12x12 retro pixel-art RPG sprites directly on Compose Canvas
 * so every shop upgrade, HUD resource, and fairy deck looks bespoke and detailed.
 */
@Composable
fun PixieRelicSprite(
    type: PixieRelicType,
    isActive: Boolean = true,
    modifier: Modifier = Modifier.size(32.dp)
) {
    val grid = when (type) {
        PixieRelicType.TAP_POWER -> listOf(
            ".....OO.....",
            "....OMMO....",
            "...OMWWMO...",
            "...OMWWMO...",
            "..OMCMMCMO..",
            "..OMCCCCMO..",
            "..OMCCMMMO..",
            "...OMMMMO...",
            "...OGGGGO...",
            "....OGGO....",
            "...OYYYYO...",
            "....OOOO...."
        )
        PixieRelicType.MULTIPLIER -> listOf(
            "....OOOO....",
            "...ORRYYO...",
            "..ORWWWWRO..",
            ".ORRWPPWRRO.",
            ".ORWPPPPWRO.",
            ".ORWPPPPWRO.",
            "..ORWPPWRO..",
            "...ORWWRO...",
            "..OORRRROO..",
            ".OGGGGGGGGO.",
            "..OOOOOOOO..",
            "............"
        )
        PixieRelicType.COOLDOWN -> listOf(
            "..OOOOOOOO..",
            ".OYYYYYYYYO.",
            "..OCCCCCCOO.",
            "...OCWWCO...",
            "....OCCO....",
            ".....OO.....",
            "....OYYO....",
            "...OYYYYO...",
            "..OCYYYYCO..",
            ".OYYYYYYYYO.",
            "..OOOOOOOO..",
            "............"
        )
        PixieRelicType.DISCOUNT -> listOf(
            "....OOOO....",
            "..OOYYYYOO..",
            ".OYWWYYYYYO.",
            ".OYWOOOOYYO.",
            ".OYYYYYYYYO.",
            ".OYYOYYOYYO.",
            ".OYYYYYYYYO.",
            "..OYYYYYYO..",
            "...ORRRRO...",
            "..ORRWWYYO..",
            "...OOOOOO...",
            "............"
        )
        PixieRelicType.MENU_SPEED -> listOf(
            "......OOOO..",
            "....OOWWPPO.",
            "...OWWPPPPO.",
            "..OWWPPPRO..",
            ".OWWPPPRRO..",
            ".OCWPPPRRO..",
            "..OCCPRRO...",
            "...OCCRO....",
            "..OYYCO.....",
            ".OYYYO......",
            ".OOOO.......",
            "............"
        )
        PixieRelicType.TOAST_SPEED -> listOf(
            ".....OO.....",
            "....OYYO....",
            "...OMMMMO...",
            "..OMWWWWMO..",
            "..OMMMMMMO..",
            "..OMMMMMMO..",
            ".OMMMMMMMMO.",
            ".OMMMMMMMMO.",
            "OYYYYYYYYYYO",
            ".OOOOOOOOOO.",
            "....OYYO....",
            ".....OO....."
        )
        PixieRelicType.AUTO_UNLOCK -> listOf(
            ".OO......OO.",
            "OWWO....OWWO",
            "OWCWO..OWCWO",
            ".OWCOOOOCWO.",
            "..OOCWWCOO..",
            "...OCWWCO...",
            "..OOCCCCOO..",
            ".OWCCCCCCWO.",
            "..OCCCCCCOO.",
            "...OCCCCO...",
            "....O..O....",
            "............"
        )
        PixieRelicType.AUTO_POWER -> listOf(
            ".......OOO..",
            "......OWCYO.",
            ".....OWCCWO.",
            "....OWCCOO..",
            "...OWCCO....",
            "..OYYYO.....",
            ".OYYYYO.....",
            "..OYYO......",
            ".OYYYO......",
            "OYYYO.......",
            "OOOO........",
            "............"
        )
        PixieRelicType.AUTO_SPEED -> listOf(
            "....OOOO....",
            "..OOMMMMO...",
            ".OMCCWWCMO..",
            ".OMCCWWCMO..",
            "OMCCYYYYCMO.",
            "OMCCYYCCCMO.",
            "OMCCCCYCCMO.",
            ".OMCCCMMMO..",
            ".OMMMMMMMO..",
            "..OOMMMMO...",
            "....OOOO....",
            "............"
        )
    }

    Canvas(modifier = modifier) {
        drawPixelGrid(grid = grid, isActive = isActive)
    }
}

@Composable
fun PixieCoinSprite(modifier: Modifier = Modifier.size(22.dp)) {
    val grid = listOf(
        "...OOOOOO...",
        "..OYYYYYYO..",
        ".OYWWWWYYYO.",
        ".OYWYYOYYYO.",
        "OYYWYYOYYYYO",
        "OYYWYYYYYYYO",
        "OYYWYYOYYYYO",
        "OYYYYYOYYYYO",
        ".OYYYYYYYYO.",
        ".OYYGGGGYYO.",
        "..OYYYYYYO..",
        "...OOOOOO..."
    )
    Canvas(modifier = modifier) {
        drawPixelGrid(grid = grid, isActive = true)
    }
}

@Composable
fun PixieStarGemSprite(modifier: Modifier = Modifier.size(22.dp)) {
    val grid = listOf(
        ".....OO.....",
        "....OMMO....",
        "...OMWWMO...",
        ".OOMWWWWMO..",
        "OMMWWWCCCMO.",
        ".OMMMCCCCMO.",
        "..OMCCCCMO..",
        "..OMCCMMMO..",
        ".OMMM..MMMO.",
        ".OMMO..OMMO.",
        "..OO....OO..",
        "............"
    )
    Canvas(modifier = modifier) {
        drawPixelGrid(grid = grid, isActive = true)
    }
}

private fun DrawScope.drawPixelGrid(
    grid: List<String>,
    isActive: Boolean
) {
    val rows = grid.size
    val cols = grid.firstOrNull()?.length ?: return
    val pxW = size.width / cols
    val pxH = size.height / rows

    for (r in 0 until rows) {
        val row = grid[r]
        for (c in 0 until cols) {
            val ch = row[c]
            if (ch == '.') continue
            val color = when (ch) {
                'O' -> Color(0xFF0C0818)
                'W' -> if (isActive) Color(0xFFFFFFFF) else Color(0xFFC8C2D8)
                'M' -> if (isActive) PixieMint else PixieTextMuted
                'C' -> if (isActive) PixieCyan else Color(0xFF6B6580)
                'R' -> if (isActive) PixieRose else Color(0xFF7B6578)
                'P' -> if (isActive) Color(0xFFFFB8E6) else Color(0xFF938496)
                'Y' -> if (isActive) PixieGold else Color(0xFF8A7F60)
                'G' -> if (isActive) Color(0xFFC98A1A) else Color(0xFF5D5340)
                else -> PixieBgDeep
            }
            drawRect(
                color = color,
                topLeft = Offset(c * pxW, r * pxH),
                size = Size(pxW + 0.6f, pxH + 0.6f)
            )
        }
    }
}

/**
 * Draws an animated pixel-art Pixie Fairy sprite with fluttering wings and glowing trail.
 */
fun DrawScope.drawPixieFairySprite(
    center: Offset,
    wingFlapPhase: Float,
    bodyColor: Color,
    wingColor: Color,
    scalePx: Float
) {
    val wingSpread = 0.55f + 0.45f * sin(wingFlapPhase)
    val wingW = 10f * scalePx
    val wingH = (7f * wingSpread + 3f) * scalePx

    // Outer glow halo
    drawCircle(
        color = bodyColor.copy(alpha = 0.24f),
        radius = 16f * scalePx,
        center = center
    )

    // Left upper & lower translucent wings
    val leftWing = Path().apply {
        moveTo(center.x - 1f * scalePx, center.y)
        lineTo(center.x - wingW, center.y - wingH)
        lineTo(center.x - wingW * 0.7f, center.y + wingH * 0.45f)
        close()
    }
    drawPath(leftWing, color = wingColor.copy(alpha = 0.78f))

    // Right upper & lower translucent wings
    val rightWing = Path().apply {
        moveTo(center.x + 1f * scalePx, center.y)
        lineTo(center.x + wingW, center.y - wingH)
        lineTo(center.x + wingW * 0.7f, center.y + wingH * 0.45f)
        close()
    }
    drawPath(rightWing, color = wingColor.copy(alpha = 0.78f))

    // Dark outline pixel core + glowing pixie body
    drawCircle(
        color = Color(0xFF0C0818),
        radius = 5.2f * scalePx,
        center = center
    )
    drawCircle(
        color = bodyColor,
        radius = 3.8f * scalePx,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 1.6f * scalePx,
        center = Offset(center.x - 1f * scalePx, center.y - 1f * scalePx)
    )
}
