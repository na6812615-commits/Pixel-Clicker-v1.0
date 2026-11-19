package com.example.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.GameProgress
import com.example.ui.audio.PixieSoundPlayer
import com.example.ui.auth.signOutUser
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.i18n.PixieStrings
import com.example.ui.i18n.pixieStringsFor
import com.example.ui.theme.PixieBgCard
import com.example.ui.theme.PixieBgDeep
import com.example.ui.theme.PixieBgSurface
import com.example.ui.theme.PixieBorder
import com.example.ui.theme.PixieCyan
import com.example.ui.theme.PixieGold
import com.example.ui.theme.PixieMint
import com.example.ui.theme.PixieMintDark
import com.example.ui.theme.PixieRose
import com.example.ui.theme.PixieTextPrimary
import com.example.ui.theme.PixieTextSecondary
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

private data class TabMeta(
    val tab: GameTab,
    val labelSelector: (PixieStrings) -> String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val accent: Color,
    val testTag: String
)

private val gameTabs = listOf(
    TabMeta(GameTab.TAP, { it.tabTap }, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, PixieMint, "nav_tab_tap"),
    TabMeta(GameTab.SHOP, { it.tabShop }, Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, PixieMint, "nav_tab_shop"),
    TabMeta(GameTab.DOLLAR_SHOP, { it.tabDollarShop }, Icons.Filled.AttachMoney, Icons.Outlined.AttachMoney, PixieGold, "nav_tab_dollar_shop"),
    TabMeta(GameTab.RESTARTS, { it.tabRestarts }, Icons.Filled.Refresh, Icons.Outlined.Refresh, PixieRose, "nav_tab_restarts")
)

private fun hasBadgeForTab(tab: GameTab, p: GameProgress): Boolean {
    return when (tab) {
        GameTab.TAP -> false
        GameTab.SHOP -> p.diem >= GameEngine.effectiveCost(p.gia, p.giamgia) ||
            p.diem >= GameEngine.effectiveCost(p.gia2, p.giamgia) ||
            p.diem >= GameEngine.effectiveCost(p.gia4, p.giamgia)
        GameTab.DOLLAR_SHOP -> p.diem >= 1000.0 ||
            p.cashDo >= p.gia5 ||
            p.cashDo >= p.gia6 ||
            p.cashDo >= p.gia7 ||
            (p.gioihan && p.cashDo >= 25.0) ||
            (!p.gioihan && (p.cashDo >= p.gia8 || p.cashDo >= p.gia9))
        GameTab.RESTARTS -> p.diem >= GameEngine.effectiveCost(p.gia3, p.giamgia)
    }
}

@Composable
fun PixieGameScreen(
    viewModel: GameViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val soundPlayer = remember(context) { PixieSoundPlayer(context) }
    val rootStrings = LocalPixieStrings.current

    DisposableEffect(soundPlayer) {
        onDispose { soundPlayer.release() }
    }

    LaunchedEffect(viewModel, soundPlayer) {
        viewModel.soundEvents.collect { effect ->
            val currentReady = viewModel.uiState.value as? GameUiState.Ready
            if (currentReady?.settings?.soundEffectsEnabled != false) {
                soundPlayer.play(effect)
            }
        }
    }

    when (val state = uiState) {
        is GameUiState.Loading -> {
            PixieAmbientBackground {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PixieMint)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = rootStrings.loadingWorld,
                            style = MaterialTheme.typography.titleMedium,
                            color = PixieTextPrimary
                        )
                    }
                }
            }
        }

        is GameUiState.Error -> {
            PixieAmbientBackground {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PixieCard(modifier = Modifier.widthIn(max = 420.dp)) {
                        Text(
                            text = rootStrings.loadErrorTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PixieTextSecondary,
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.loadGame() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PixieMint,
                                    contentColor = PixieBgDeep
                                ),
                                modifier = Modifier.testTag("retry_load_button")
                            ) {
                                Text(rootStrings.retry)
                            }
                            Button(
                                onClick = {
                                    signOutUser(credentialManager, scope)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PixieBgCard,
                                    contentColor = PixieTextPrimary
                                )
                            ) {
                                Text(rootStrings.signOut)
                            }
                        }
                    }
                }
            }
        }

        is GameUiState.Ready -> {
            CompositionLocalProvider(
                LocalPixieStrings provides pixieStringsFor(state.settings.language)
            ) {
                val s = LocalPixieStrings.current
                BackHandler(enabled = state.showSettings || state.activeTab != GameTab.TAP) {
                    if (state.showSettings) {
                        viewModel.closeSettings()
                    } else {
                        viewModel.selectTab(GameTab.TAP)
                    }
                }

                PixieAmbientBackground(
                    ambientParticlesEnabled = state.settings.ambientParticlesEnabled
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val useNavigationRail = maxWidth >= 600.dp
                        val transitionMs = (state.progress.cd3 * 220.0).toInt().coerceIn(60, 260)

                    Scaffold(
                        containerColor = Color.Transparent,
                        contentWindowInsets = WindowInsets.safeDrawing,
                        bottomBar = {
                            if (!useNavigationRail) {
                                PixieBottomDock(
                                    activeTab = if (state.showSettings) null else state.activeTab,
                                    progress = state.progress,
                                    showUpgradeBadges = state.settings.showUpgradeBadges,
                                    onSelectTab = { viewModel.selectTab(it) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (useNavigationRail) {
                                NavigationRail(
                                    containerColor = PixieBgSurface,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    gameTabs.forEach { item ->
                                        val selected = !state.showSettings && state.activeTab == item.tab
                                        val label = item.labelSelector(s)
                                        NavigationRailItem(
                                            selected = selected,
                                            onClick = { viewModel.selectTab(item.tab) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                                                    contentDescription = label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            colors = NavigationRailItemDefaults.colors(
                                                selectedIconColor = item.accent,
                                                selectedTextColor = item.accent,
                                                indicatorColor = PixieMintDark,
                                                unselectedIconColor = PixieTextSecondary,
                                                unselectedTextColor = PixieTextSecondary
                                            ),
                                            modifier = Modifier.testTag(item.testTag)
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    NavigationRailItem(
                                        selected = state.showSettings,
                                        onClick = { viewModel.openSettings() },
                                        icon = {
                                            Icon(
                                                imageVector = if (state.showSettings) Icons.Filled.Settings else Icons.Outlined.Settings,
                                                contentDescription = s.openSettings
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = s.tabSettings,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = PixieCyan,
                                            selectedTextColor = PixieCyan,
                                            indicatorColor = PixieMintDark,
                                            unselectedIconColor = PixieTextSecondary,
                                            unselectedTextColor = PixieTextSecondary
                                        ),
                                        modifier = Modifier.testTag("nav_tab_settings")
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    modifier = Modifier
                                        .widthIn(max = 600.dp)
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PixieStatsHeader(
                                        progress = state.progress,
                                        isSyncing = state.isSyncing,
                                        hasUnsavedChanges = state.hasUnsavedChanges,
                                        onManualSync = { viewModel.manualSync() },
                                        onOpenSettings = {
                                            if (state.showSettings) {
                                                viewModel.closeSettings()
                                            } else {
                                                viewModel.openSettings()
                                            }
                                        },
                                        compactNumbers = state.settings.compactNumbers,
                                        isSettingsOpen = state.showSettings
                                    )

                                    PixieToastBanner(toast = state.toast)

                                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        Crossfade(
                                            targetState = if (state.showSettings) null else state.activeTab,
                                            animationSpec = tween(durationMillis = transitionMs),
                                            label = "pixie_tab_crossfade"
                                        ) { tab ->
                                            when (tab) {
                                                null -> SettingsTab(
                                                    settings = state.settings,
                                                    progress = state.progress,
                                                    userEmail = Firebase.auth.currentUser?.email,
                                                    isSyncing = state.isSyncing,
                                                    onBack = { viewModel.closeSettings() },
                                                    onSetLanguage = { viewModel.setLanguage(it) },
                                                    onSetVfxIntensity = { viewModel.setVfxIntensity(it) },
                                                    onSetCrystalAuraTheme = { viewModel.setCrystalAuraTheme(it) },
                                                    onToggleAmbientParticles = { viewModel.setAmbientParticlesEnabled(it) },
                                                    onToggleArcaneRings = { viewModel.setShowArcaneRings(it) },
                                                    onToggleComboCounter = { viewModel.setShowComboCounter(it) },
                                                    onToggleFloatingNumbers = { viewModel.setShowFloatingNumbers(it) },
                                                    onToggleAutoFloatingNumbers = { viewModel.setShowAutoFloatingNumbers(it) },
                                                    onToggleSoundEffects = { viewModel.setSoundEffectsEnabled(it) },
                                                    onToggleHaptic = { viewModel.setHapticEnabled(it) },
                                                    onToggleScreenShake = { viewModel.setScreenShakeEnabled(it) },
                                                    onToggleUpgradeBadges = { viewModel.setShowUpgradeBadges(it) },
                                                    onToggleDollarProgressCard = { viewModel.setShowDollarProgressCard(it) },
                                                    onToggleConfirmRestart = { viewModel.setConfirmBeforeRestart(it) },
                                                    onToggleCompactNumbers = { viewModel.setCompactNumbers(it) },
                                                    onToggleAutoCloudSync = { viewModel.setAutoCloudSyncEnabled(it) },
                                                    onSetAutoSyncInterval = { viewModel.setAutoSyncInterval(it) },
                                                    onManualSync = { viewModel.manualSync() },
                                                    onResetSettings = { viewModel.resetSettings() },
                                                    onSignOutClick = {
                                                        viewModel.flushAndPrepareSignOut {
                                                            signOutUser(credentialManager, scope)
                                                        }
                                                    }
                                                )

                                                GameTab.TAP -> TapArenaTab(
                                                    progress = state.progress,
                                                    cooldownFraction = state.cooldownFraction,
                                                    particlesFlow = viewModel.particles,
                                                    onManualTap = { viewModel.onManualTap() },
                                                    onToggleAuto = { viewModel.toggleAutoClick() },
                                                    onGoToDollarShop = { viewModel.selectTab(GameTab.DOLLAR_SHOP) },
                                                    settings = state.settings
                                                )

                                                GameTab.SHOP -> ShopTab(
                                                    progress = state.progress,
                                                    onBuyPower = { viewModel.buyShopPower() },
                                                    onBuyMultiplier = { viewModel.buyShopMultiplier() },
                                                    onBuyCooldown = { viewModel.buyShopCooldown() },
                                                    compactNumbers = state.settings.compactNumbers
                                                )

                                                GameTab.DOLLAR_SHOP -> DollarShopTab(
                                                    progress = state.progress,
                                                    onExchange1000 = { viewModel.exchange1000() },
                                                    onExchangeAll = { viewModel.exchangeAll() },
                                                    onBuyDiscount = { viewModel.buyDollarDiscount() },
                                                    onBuyMenuSpeed = { viewModel.buyDollarMenuSpeed() },
                                                    onBuyToastSpeed = { viewModel.buyDollarToastSpeed() },
                                                    onBuyAutoUnlock = { viewModel.buyDollarAutoUnlock() },
                                                    onToggleAutoClick = { viewModel.toggleAutoClick() },
                                                    onBuyAutoPower = { viewModel.buyAutoPower() },
                                                    onBuyAutoSpeed = { viewModel.buyAutoSpeed() }
                                                )

                                                GameTab.RESTARTS -> RestartsTab(
                                                    progress = state.progress,
                                                    onPerformRestart = { viewModel.performRestart() },
                                                    confirmBeforeRestart = state.settings.confirmBeforeRestart,
                                                    compactNumbers = state.settings.compactNumbers
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun PixieBottomDock(
    activeTab: GameTab?,
    progress: GameProgress,
    showUpgradeBadges: Boolean,
    onSelectTab: (GameTab) -> Unit
) {
    val s = LocalPixieStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(PixieBgSurface, PixieBgDeep)
                )
            )
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(com.example.ui.theme.PixieBorderBright)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PixieBgDeep)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            gameTabs.forEach { item ->
                val selected = activeTab == item.tab
                val showPip = showUpgradeBadges && hasBadgeForTab(item.tab, progress)
                val label = item.labelSelector(s)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (selected) {
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        item.accent.copy(alpha = 0.28f),
                                        item.accent.copy(alpha = 0.08f)
                                    )
                                )
                            } else {
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Transparent)
                                )
                            }
                        )
                        .then(
                            if (selected) {
                                Modifier.border(1.5.dp, item.accent.copy(alpha = 0.88f), RoundedCornerShape(8.dp))
                            } else {
                                Modifier.border(1.dp, PixieBorder.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            }
                        )
                        .clickable(
                            role = Role.Tab,
                            indication = ripple(bounded = true, color = item.accent),
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            onClick = { onSelectTab(item.tab) }
                        )
                        .testTag(item.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth(0.55f)
                                .height(3.dp)
                                .background(item.accent, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box {
                            Icon(
                                imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                                contentDescription = label,
                                tint = if (selected) item.accent else PixieTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                            if (showPip) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-2).dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(item.accent)
                                        .border(1.dp, PixieBgDeep, CircleShape)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) item.accent else PixieTextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
