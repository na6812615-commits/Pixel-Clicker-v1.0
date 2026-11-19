package com.example.ui.auth

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.ui.game.PixieAmbientBackground
import com.example.ui.game.PixieArcadeButton
import com.example.ui.game.PixieArtworkImage
import com.example.ui.game.PixieCard
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieBorderBright
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val s = LocalPixieStrings.current

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(750L)
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    PixieAmbientBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, PixieCyan, RoundedCornerShape(12.dp))
                ) {
                    PixieArtworkImage(
                        resId = R.drawable.img_pixie_sanctuary,
                        contentDescription = s.sanctuaryBadge,
                        contentScale = ContentScale.Crop,
                        targetMaxPx = 640,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, PixieBgDeep.copy(alpha = 0.92f))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PixieArtworkImage(
                            resId = R.drawable.img_pixie_crystal,
                            contentDescription = s.appTitleUppercase,
                            contentScale = ContentScale.Crop,
                            targetMaxPx = 256,
                            modifier = Modifier
                                .size(62.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(2.dp, PixieMint, RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = s.appTitleUppercase,
                                style = MaterialTheme.typography.headlineMedium,
                                color = PixieMint
                            )
                            Text(
                                text = s.tagline,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PixieTextPrimary
                            )
                        }
                    }
                }

                PixieCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = PixieBorderBright,
                    glowColor = PixieGold
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PixieArtworkImage(
                            resId = R.drawable.img_pixie_chest,
                            contentDescription = s.mechanicsHeading,
                            contentScale = ContentScale.Crop,
                            targetMaxPx = 192,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.5.dp, PixieGold, RoundedCornerShape(6.dp))
                        )
                        Text(
                            text = s.mechanicsHeading,
                            style = MaterialTheme.typography.titleMedium,
                            color = PixieGold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FeatureLine(
                            icon = Icons.Filled.AutoAwesome,
                            tint = PixieMint,
                            text = s.featureTap
                        )
                        FeatureLine(
                            icon = Icons.Filled.MonetizationOn,
                            tint = PixieGold,
                            text = s.featureDollar
                        )
                        FeatureLine(
                            icon = Icons.Filled.Bolt,
                            tint = PixieCyan,
                            text = s.featureAuto
                        )
                        FeatureLine(
                            icon = Icons.Filled.Refresh,
                            tint = PixieRose,
                            text = s.featureRestart
                        )
                        FeatureLine(
                            icon = Icons.Filled.CloudDone,
                            tint = PixieMint,
                            text = s.featureCloud
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(12.dp)
                    )
                }

                PixieArcadeButton(
                    onClick = {
                        errorMessage = null
                        isLoading = true
                        onGoogleSignInClicked(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = {
                                isLoading = false
                                onAuthSuccess()
                            },
                            onAuthError = { msg ->
                                isLoading = false
                                errorMessage = msg
                            },
                            scope = scope,
                            onAuthCancelled = {
                                isLoading = false
                            }
                        )
                    },
                    accentColor = PixieMint,
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("google_sign_in_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = PixieBgDeep,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = s.signingIn,
                            style = MaterialTheme.typography.titleMedium,
                            color = PixieBgDeep
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Login,
                            contentDescription = null,
                            tint = PixieBgDeep,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = s.signInGoogle,
                            style = MaterialTheme.typography.titleMedium,
                            color = PixieBgDeep
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureLine(
    icon: ImageVector,
    tint: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = PixieTextSecondary
        )
    }
}
