package com.example.ui.game

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.GameProgress
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.theme.PixieBgCard
import com.example.ui.theme.PixieBgCardHighlight
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextMuted
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

@Composable
fun RestartsTab(
    progress: GameProgress,
    onPerformRestart: () -> Unit,
    modifier: Modifier = Modifier,
    confirmBeforeRestart: Boolean = true,
    compactNumbers: Boolean = false
) {
    val s = LocalPixieStrings.current
    var showConfirmDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val requiredPoints = GameEngine.effectiveCost(progress.gia3, progress.giamgia)
    val canRestart = progress.diem >= requiredPoints
    val ratio = if (requiredPoints > 0L) {
        (progress.diem / requiredPoints.toDouble()).toFloat().coerceIn(0f, 1f)
    } else {
        1f
    }
    val nextBaseMuti = GameEngine.round2(progress.muti2 + 0.5)

    val transition = rememberInfiniteTransition(label = "portal_anim")
    val portalRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "portal_rotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Illustrated 16-bit Cosmic Rebirth Portal Stage
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, PixieRose.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
        ) {
            PixieArtworkImage(
                resId = R.drawable.img_pixie_portal,
                contentDescription = s.restartsTitle,
                contentScale = ContentScale.Crop,
                targetMaxPx = 512,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0.88f)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                PixieBgDeep.copy(alpha = 0.60f),
                                PixieBgDeep.copy(alpha = 0.45f),
                                PixieBgDeep.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Animated Rebirth Portal Crest
                Box(
                    modifier = Modifier.size(88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(PixieRose.copy(alpha = 0.44f), Color.Transparent),
                                center = center,
                                radius = size.minDimension * 0.5f
                            ),
                            radius = size.minDimension * 0.5f,
                            center = center
                        )
                        rotate(portalRotation, pivot = center) {
                            val r1 = size.minDimension * 0.45f
                            drawCircle(
                                color = PixieRose,
                                radius = r1,
                                center = center,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                                )
                            )
                            for (i in 0 until 4) {
                                val a = (i * 90.0) * PI / 180.0
                                val px = center.x + (r1 * cos(a)).toFloat()
                                val py = center.y + (r1 * sin(a)).toFloat()
                                drawCircle(color = PixieCyan, radius = 3.dp.toPx(), center = Offset(px, py))
                            }
                        }
                        rotate(-portalRotation * 1.5f, pivot = center) {
                            drawCircle(
                                color = PixieGold.copy(alpha = 0.88f),
                                radius = size.minDimension * 0.33f,
                                center = center,
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                                )
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = PixieGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "LV ${progress.lv3}",
                            style = MaterialTheme.typography.labelLarge,
                            color = PixieTextPrimary
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = s.restartsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = PixieRose
                    )
                    Text(
                        text = s.restartsPermMultiplierLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PixieTextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PixieBgDeep.copy(alpha = 0.9f))
                                .border(1.dp, PixieTextMuted, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "x${GameEngine.round2(progress.muti2)}",
                                style = MaterialTheme.typography.labelLarge,
                                color = PixieTextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PixieRose,
                            modifier = Modifier.size(18.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PixieMint.copy(alpha = 0.25f))
                                .border(1.5.dp, PixieMint, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "x$nextBaseMuti",
                                style = MaterialTheme.typography.labelLarge,
                                color = PixieMint
                            )
                        }
                    }
                }
            }
        }

        PixieCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (canRestart) PixieRose else PixieBgCardHighlight,
            glowColor = if (canRestart) PixieMint else PixieRose
        ) {
            Text(
                text = s.restartsRequired(GameEngine.formatGameCount(requiredPoints, compactNumbers)),
                style = MaterialTheme.typography.titleMedium,
                color = if (canRestart) PixieMint else PixieTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { ratio },
                color = if (canRestart) PixieMint else PixieRose,
                trackColor = PixieBgCardHighlight,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (canRestart) {
                    s.restartsReadyDesc
                } else {
                    val missing = (requiredPoints - progress.diem).roundToLong().coerceAtLeast(1L)
                    s.restartsMissingDesc(GameEngine.formatGameCount(missing, compactNumbers))
                },
                style = MaterialTheme.typography.bodyMedium,
                color = PixieTextSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
            PixieArcadeButton(
                onClick = {
                    if (canRestart && confirmBeforeRestart) {
                        showConfirmDialog.value = true
                    } else {
                        onPerformRestart()
                    }
                },
                accentColor = PixieRose,
                enabled = canRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restart_action_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = if (canRestart) PixieBgDeep else PixieTextMuted
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s.restartsActionBtn,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (canRestart) PixieBgDeep else PixieTextMuted
                )
            }
        }

        PixieCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = s.restartsRulesTitle,
                style = MaterialTheme.typography.titleMedium,
                color = PixieRose
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = s.restartsRulesBody(
                    GameEngine.round2(progress.muti2).toString(),
                    nextBaseMuti.toString()
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = PixieTextSecondary
            )
        }
    }

    if (showConfirmDialog.value) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog.value = false },
            containerColor = PixieBgCard,
            title = {
                Text(
                    text = s.restartsConfirmTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = PixieRose
                )
            },
            text = {
                Text(
                    text = s.restartsConfirmBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixieTextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog.value = false
                        onPerformRestart()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixieRose,
                        contentColor = PixieBgDeep
                    ),
                    modifier = Modifier.testTag("confirm_restart_button")
                ) {
                    Text(s.confirmYes)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog.value = false }) {
                    Text(
                        text = s.confirmCancel,
                        color = PixieTextSecondary
                    )
                }
            }
        )
    }
}
