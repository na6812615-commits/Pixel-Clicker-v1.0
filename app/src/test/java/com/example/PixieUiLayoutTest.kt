package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppLanguage
import com.example.data.AutoSyncInterval
import com.example.data.CrystalAuraTheme
import com.example.data.DataStoreSettingsRepository
import com.example.data.GameProgress
import com.example.data.GameServer
import com.example.data.GameSettings
import com.example.data.ServerPlayer
import com.example.data.TradeSession
import com.example.data.TradeStatus
import com.example.data.VfxIntensity
import com.example.ui.game.DollarShopTab
import com.example.ui.game.PixieStatsHeader
import com.example.ui.game.RestartsTab
import com.example.ui.game.ServerBrowserDialog
import com.example.ui.game.SettingsTab
import com.example.ui.game.ShopTab
import com.example.ui.game.TapArenaTab
import com.example.ui.game.TapParticleEvent
import com.example.ui.game.TradeRoomDialog
import com.example.ui.i18n.LocalPixieSocialStrings
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.i18n.ViSocialStrings
import com.example.ui.i18n.pixieStringsFor
import com.example.ui.theme.PixieClickerTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PixieUiLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shopTab_displaysUpgradeTitlesAndCostsWithCompactButton() {
        val progress = GameProgress(userId = "test_uid", diem = 100.0)
        composeTestRule.setContent {
            PixieClickerTheme {
                ShopTab(
                    progress = progress,
                    onBuyPower = {},
                    onBuyMultiplier = {},
                    onBuyCooldown = {}
                )
            }
        }

        val powerTitle = composeTestRule.onNodeWithText("1: +1 điểm nhận được")
        val btn = composeTestRule.onNodeWithTag("shop_buy_power_button")
        val titleBounds = powerTitle.getUnclippedBoundsInRoot()
        val btnBounds = btn.getUnclippedBoundsInRoot()

        powerTitle.assertIsDisplayed()
        composeTestRule.onNodeWithText("Nhân đôi điểm cơ bản mỗi tap (1 → 2)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Giá: 25 điểm").assertIsDisplayed()
        composeTestRule.onNodeWithText("2: Thêm hệ số nhân (+0.1)").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("3: Giảm countdown click").performScrollTo().assertIsDisplayed()

        val buttonWidth = btnBounds.right - btnBounds.left
        val availableTextColumnWidth = btnBounds.left - titleBounds.left
        assertTrue("button width was $buttonWidth", buttonWidth <= 110.dp)
        assertTrue("available text width was $availableTextColumnWidth", availableTextColumnWidth >= 130.dp)
    }

    @Test
    fun dollarShopTab_displaysItemsWithCompactButtons() {
        val progress = GameProgress(
            userId = "test_uid",
            diem = 2000.0,
            cashDo = 30.0,
            gioihan = false,
            autoEnabled = true
        )
        composeTestRule.setContent {
            PixieClickerTheme {
                DollarShopTab(
                    progress = progress,
                    onExchange1000 = {},
                    onExchangeAll = {},
                    onBuyDiscount = {},
                    onBuyMenuSpeed = {},
                    onBuyToastSpeed = {},
                    onBuyAutoUnlock = {},
                    onToggleAutoClick = {},
                    onBuyAutoPower = {},
                    onBuyAutoSpeed = {}
                )
            }
        }

        val discountTitle = composeTestRule.onNodeWithText("1: Giảm giá")
        val btnBounds = composeTestRule.onNodeWithTag("dollar_buy_discount_button").getUnclippedBoundsInRoot()
        val titleBounds = discountTitle.getUnclippedBoundsInRoot()

        discountTitle.performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("2: Giảm thời gian rời menu").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("3: Giảm thời gian thông báo").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("4: Auto Click").performScrollTo().assertIsDisplayed()

        val buttonWidth = btnBounds.right - btnBounds.left
        val availableTextColumnWidth = btnBounds.left - titleBounds.left
        assertTrue("button width was $buttonWidth", buttonWidth <= 110.dp)
        assertTrue("available text width was $availableTextColumnWidth", availableTextColumnWidth >= 130.dp)

        val autoToggleTitle = composeTestRule.onNodeWithText("1: Bật / Tắt Auto Click")
        autoToggleTitle.performScrollTo()
        autoToggleTitle.assertIsDisplayed()
        val toggleBtnBounds = composeTestRule.onNodeWithTag("auto_toggle_button").getUnclippedBoundsInRoot()
        assertTrue("auto toggle button width was ${toggleBtnBounds.right - toggleBtnBounds.left}", (toggleBtnBounds.right - toggleBtnBounds.left) <= 110.dp)
    }

    @Test
    fun tapArenaTab_displaysAutoClickCardTextWithCompactButton() {
        val progress = GameProgress(userId = "test_uid", diem = 50.0)
        val particles = MutableSharedFlow<TapParticleEvent>()
        composeTestRule.setContent {
            PixieClickerTheme {
                TapArenaTab(
                    progress = progress,
                    cooldownFraction = 0f,
                    particlesFlow = particles,
                    onManualTap = {},
                    onToggleAuto = {},
                    onGoToDollarShop = {}
                )
            }
        }

        val autoTitle = composeTestRule.onNodeWithText("Auto Click: Chưa mở khóa (\$25)")
        autoTitle.performScrollTo()
        autoTitle.assertIsDisplayed()

        val titleBounds = autoTitle.getUnclippedBoundsInRoot()
        val btnBounds = composeTestRule.onNodeWithTag("goto_dollar_shop_button").getUnclippedBoundsInRoot()
        val buttonWidth = btnBounds.right - btnBounds.left
        val availableTextColumnWidth = btnBounds.left - titleBounds.left

        assertTrue("button width was $buttonWidth", buttonWidth <= 120.dp)
        assertTrue("available text width was $availableTextColumnWidth", availableTextColumnWidth >= 130.dp)
    }

    @Test
    fun settingsTab_rendersAllSectionsAndFiresCallbacks() {
        var selectedVfx: VfxIntensity? = null
        var selectedAura: CrystalAuraTheme? = null
        var compactEnabled: Boolean? = null
        var signOutCalled = false
        var backCalled = false

        composeTestRule.setContent {
            PixieClickerTheme {
                SettingsTab(
                    settings = GameSettings(),
                    progress = GameProgress(userId = "test_uid", clickCount = 42L, lv3 = 2L),
                    userEmail = "pixie@example.com",
                    isSyncing = false,
                    onBack = { backCalled = true },
                    onSetVfxIntensity = { selectedVfx = it },
                    onSetCrystalAuraTheme = { selectedAura = it },
                    onToggleAmbientParticles = {},
                    onToggleComboCounter = {},
                    onToggleFloatingNumbers = {},
                    onToggleHaptic = {},
                    onToggleScreenShake = {},
                    onToggleConfirmRestart = {},
                    onToggleCompactNumbers = { compactEnabled = it },
                    onToggleAutoCloudSync = {},
                    onManualSync = {},
                    onResetSettings = {},
                    onSignOutClick = { signOutCalled = true }
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_back_button").performClick()
        assertTrue(backCalled)

        composeTestRule.onNodeWithTag("vfx_intensity_ultra").performScrollTo().performClick()
        assertEquals(VfxIntensity.ULTRA, selectedVfx)

        composeTestRule.onNodeWithTag("aura_theme_rose_nova").performScrollTo().performClick()
        assertEquals(CrystalAuraTheme.ROSE_NOVA, selectedAura)

        composeTestRule.onNodeWithTag("setting_compact_numbers_switch").performScrollTo().performClick()
        assertEquals(true, compactEnabled)

        composeTestRule.onNodeWithText("pixie@example.com").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_out_button").performScrollTo().performClick()
        assertTrue(signOutCalled)
    }

    @Test
    fun restartsTab_skipsDialogWhenConfirmDisabled() {
        var restarted = false
        composeTestRule.setContent {
            PixieClickerTheme {
                RestartsTab(
                    progress = GameProgress(userId = "u1", diem = 200.0, gia3 = 150.0),
                    onPerformRestart = { restarted = true },
                    confirmBeforeRestart = false
                )
            }
        }

        composeTestRule.onNodeWithTag("restart_action_button").performScrollTo().performClick()
        assertTrue(restarted)
    }

    @Test
    fun settingsTab_rendersAllTwelveLanguagesAndSelectsLanguage() {
        var pickedLanguage: AppLanguage? = null
        composeTestRule.setContent {
            PixieClickerTheme {
                SettingsTab(
                    settings = GameSettings(language = AppLanguage.VI),
                    progress = GameProgress(userId = "u1"),
                    userEmail = "pixie@example.com",
                    isSyncing = false,
                    onBack = {},
                    onSetLanguage = { pickedLanguage = it },
                    onSetVfxIntensity = {},
                    onSetCrystalAuraTheme = {},
                    onToggleAmbientParticles = {},
                    onToggleComboCounter = {},
                    onToggleFloatingNumbers = {},
                    onToggleHaptic = {},
                    onToggleScreenShake = {},
                    onToggleConfirmRestart = {},
                    onToggleCompactNumbers = {},
                    onToggleAutoCloudSync = {},
                    onManualSync = {},
                    onResetSettings = {},
                    onSignOutClick = {}
                )
            }
        }

        // Before tapping the language button, options are tucked inside the picker dialog
        composeTestRule.onNodeWithTag("lang_option_ja").assertDoesNotExist()
        composeTestRule.onNodeWithTag("language_selector_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Tiếng Việt").assertIsDisplayed()

        // Open dialog and verify all 12 languages + select JA
        composeTestRule.onNodeWithTag("language_selector_button").performClick()
        AppLanguage.entries.forEach { lang ->
            val tag = "lang_option_${lang.name.lowercase()}"
            composeTestRule.onNodeWithTag("language_dialog_list")
                .performScrollToNode(hasTestTag(tag))
            composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
        }

        composeTestRule.onNodeWithTag("language_dialog_list")
            .performScrollToNode(hasTestTag("lang_option_ja"))
        composeTestRule.onNodeWithTag("lang_option_ja").performClick()
        assertEquals(AppLanguage.JA, pickedLanguage)
        // Dialog closes automatically after selecting a language
        composeTestRule.onNodeWithTag("language_dialog_list").assertDoesNotExist()

        // Open again and select EN
        composeTestRule.onNodeWithTag("language_selector_button").performClick()
        composeTestRule.onNodeWithTag("language_dialog_list")
            .performScrollToNode(hasTestTag("lang_option_en"))
        composeTestRule.onNodeWithTag("lang_option_en").performClick()
        assertEquals(AppLanguage.EN, pickedLanguage)
    }

    @Test
    fun shopTab_rendersTranslatedEnglishCopyWhenProvided() {
        val en = pixieStringsFor(AppLanguage.EN)
        composeTestRule.setContent {
            PixieClickerTheme {
                CompositionLocalProvider(LocalPixieStrings provides en) {
                    ShopTab(
                        progress = GameProgress(userId = "u1", diem = 100.0),
                        onBuyPower = {},
                        onBuyMultiplier = {},
                        onBuyCooldown = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("POINT SHOP").assertIsDisplayed()
        composeTestRule.onNodeWithText("1: +1 points per tap").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cost: 25 pts").assertIsDisplayed()
    }

    @Test
    fun settingsTab_rendersNewControlsAndPlayerSnapshot() {
        var arcaneRingsToggled: Boolean? = null
        var soundToggled: Boolean? = null
        var badgesToggled: Boolean? = null
        var intervalPicked: AutoSyncInterval? = null

        composeTestRule.setContent {
            PixieClickerTheme {
                SettingsTab(
                    settings = GameSettings(),
                    progress = GameProgress(userId = "u1", giamgia = 2.0, cd = 0.125),
                    userEmail = "pixie@example.com",
                    isSyncing = false,
                    onBack = {},
                    onSetVfxIntensity = {},
                    onSetCrystalAuraTheme = {},
                    onToggleAmbientParticles = {},
                    onToggleArcaneRings = { arcaneRingsToggled = it },
                    onToggleComboCounter = {},
                    onToggleFloatingNumbers = {},
                    onToggleAutoFloatingNumbers = {},
                    onToggleSoundEffects = { soundToggled = it },
                    onToggleHaptic = {},
                    onToggleScreenShake = {},
                    onToggleUpgradeBadges = { badgesToggled = it },
                    onToggleDollarProgressCard = {},
                    onToggleConfirmRestart = {},
                    onToggleCompactNumbers = {},
                    onToggleAutoCloudSync = {},
                    onSetAutoSyncInterval = { intervalPicked = it },
                    onManualSync = {},
                    onResetSettings = {},
                    onSignOutClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("settings_auto_saved_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("setting_arcane_rings_switch").performScrollTo().performClick()
        assertEquals(false, arcaneRingsToggled)

        composeTestRule.onNodeWithTag("setting_sound_effects_switch").performScrollTo().performClick()
        assertEquals(false, soundToggled)

        composeTestRule.onNodeWithTag("setting_upgrade_badges_switch").performScrollTo().performClick()
        assertEquals(false, badgesToggled)

        composeTestRule.onNodeWithTag("sync_interval_fast_4s").performScrollTo().performClick()
        assertEquals(AutoSyncInterval.FAST_4S, intervalPicked)

        composeTestRule.onNodeWithTag("settings_player_snapshot").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun tapArenaTab_hidesDollarProgressCardWhenDisabledInSettings() {
        val particles = MutableSharedFlow<TapParticleEvent>()
        composeTestRule.setContent {
            PixieClickerTheme {
                TapArenaTab(
                    progress = GameProgress(userId = "u1", diem = 250.0),
                    cooldownFraction = 0f,
                    particlesFlow = particles,
                    onManualTap = {},
                    onToggleAuto = {},
                    onGoToDollarShop = {},
                    settings = GameSettings(showDollarProgressCard = false)
                )
            }
        }

        composeTestRule.onNodeWithTag("dollar_progress_card").assertDoesNotExist()
    }

    @Test
    fun dataStoreSettingsRepository_persistsAcrossInstances() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val writerRepo = DataStoreSettingsRepository(context)
        writerRepo.reset()
        writerRepo.update {
            it.copy(
                language = AppLanguage.FR,
                showArcaneRings = false,
                soundEffectsEnabled = false,
                autoSyncInterval = AutoSyncInterval.RELAXED_20S,
                showDollarProgressCard = false
            )
        }

        val readerRepo = DataStoreSettingsRepository(context)
        val loaded = readerRepo.settingsFlow.first()
        assertEquals(AppLanguage.FR, loaded.language)
        assertFalse(loaded.showArcaneRings)
        assertFalse(loaded.soundEffectsEnabled)
        assertEquals(AutoSyncInterval.RELAXED_20S, loaded.autoSyncInterval)
        assertFalse(loaded.showDollarProgressCard)

        writerRepo.reset()
    }

    @Test
    fun statsHeader_displaysServerButtonAndOpensMenu() {
        var opened = false
        val srv = GameServer(id = "server_1", name = "Pixie Forest #1", playerCount = 7L, maxPlayers = 20L)
        composeTestRule.setContent {
            PixieClickerTheme {
                CompositionLocalProvider(LocalPixieSocialStrings provides ViSocialStrings) {
                    PixieStatsHeader(
                        progress = GameProgress(userId = "u1"),
                        isSyncing = false,
                        hasUnsavedChanges = false,
                        onManualSync = {},
                        onOpenSettings = {},
                        currentServer = srv,
                        pendingInviteCount = 1,
                        onOpenServerMenu = { opened = true }
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("open_server_menu_button").assertIsDisplayed().performClick()
        assertTrue(opened)
        composeTestRule.onNodeWithText("Pixie Forest #1").assertIsDisplayed()
        composeTestRule.onNodeWithText("7/20").assertIsDisplayed()
    }

    @Test
    fun serverBrowserDialog_searchesServersBlocksFullJoinAndInvitesSameServerPlayer() {
        var joinedServerId: String? = null
        var invitedPlayer: ServerPlayer? = null
        val s1 = GameServer(id = "s1", name = "Pixie Forest #1", region = "SEA", playerCount = 4L, maxPlayers = 20L)
        val s2Full = GameServer(id = "s2", name = "Crystal Peak #2", region = "EU", playerCount = 20L, maxPlayers = 20L)
        val s3Open = GameServer(id = "s3", name = "Aurora Oasis #3", region = "Global", playerCount = 12L, maxPlayers = 20L)
        val partner = ServerPlayer(userId = "u2", serverId = "s1", displayName = "Luna", diem = 300.0, cashDo = 5.0)

        composeTestRule.setContent {
            var query by remember { mutableStateOf("") }
            PixieClickerTheme {
                CompositionLocalProvider(LocalPixieSocialStrings provides ViSocialStrings) {
                    ServerBrowserDialog(
                        servers = listOf(s1, s2Full, s3Open),
                        currentServerId = "s1",
                        currentServerPlayers = listOf(partner),
                        currentUserId = "u1",
                        searchQuery = query,
                        isJoiningServer = false,
                        incomingTrades = emptyList(),
                        outgoingPendingTrades = emptyList(),
                        compactNumbers = false,
                        onSearchQueryChange = { query = it },
                        onJoinServer = { joinedServerId = it },
                        onInviteTrade = { invitedPlayer = it },
                        onAcceptInvite = {},
                        onDeclineOrCancelInvite = {},
                        onDismiss = {}
                    )
                }
            }
        }

        // Full server cannot be joined
        composeTestRule.onNodeWithTag("server_browser_list").performScrollToNode(hasTestTag("server_join_s2"))
        composeTestRule.onNodeWithTag("server_join_s2").performClick()
        assertEquals(null, joinedServerId)

        // Open server can be joined
        composeTestRule.onNodeWithTag("server_browser_list").performScrollToNode(hasTestTag("server_join_s3"))
        composeTestRule.onNodeWithTag("server_join_s3").performClick()
        assertEquals("s3", joinedServerId)

        // Same-server player can be invited to trade
        composeTestRule.onNodeWithTag("server_browser_list").performScrollToNode(hasTestTag("invite_trade_u2"))
        composeTestRule.onNodeWithTag("invite_trade_u2").performClick()
        assertEquals("u2", invitedPlayer?.userId)

        // Searching narrows server list live
        composeTestRule.onNodeWithTag("server_search_input").performTextInput("Aurora")
        composeTestRule.onNodeWithTag("server_browser_list").performScrollToNode(hasTestTag("server_card_s3"))
        composeTestRule.onNodeWithTag("server_card_s3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("server_card_s2").assertDoesNotExist()
    }

    @Test
    fun tradeRoomDialog_enforcesBagCapNonEmptyRuleAndImmediateCancel() {
        var agreedPair: Pair<String, String>? = null
        var cancelled = false
        val activeTrade = TradeSession(
            id = "trade_1",
            serverId = "s1",
            senderId = "u1",
            senderName = "Hero",
            receiverId = "u2",
            receiverName = "Nova",
            participantIds = listOf("u1", "u2"),
            status = TradeStatus.ACTIVE.firestoreValue
        )

        composeTestRule.setContent {
            PixieClickerTheme {
                CompositionLocalProvider(LocalPixieSocialStrings provides ViSocialStrings) {
                    TradeRoomDialog(
                        trade = activeTrade,
                        currentUserId = "u1",
                        progress = GameProgress(userId = "u1", diem = 200.0, cashDo = 10.0),
                        compactNumbers = false,
                        isTradeBusy = false,
                        onSetOffer = { _, _ -> },
                        onToggleAgree = { pts, cash -> agreedPair = pts to cash },
                        onCancelAndLeave = { cancelled = true },
                        onDismissTerminal = {}
                    )
                }
            }
        }

        // Empty both sides -> shows error banner & agree callback does NOT fire
        composeTestRule.onNodeWithTag("trade_validation_error").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trade_agree_button").performScrollTo().performClick()
        assertEquals(null, agreedPair)

        // Exceeding bag points (500 > 200) -> blocks agree callback
        composeTestRule.onNodeWithTag("trade_offer_points_input").performScrollTo().performTextReplacement("500")
        composeTestRule.onNodeWithTag("trade_validation_error").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("trade_agree_button").performScrollTo().performClick()
        assertEquals(null, agreedPair)

        // Valid one-sided offer (100 <= 200) -> allows agreeing!
        composeTestRule.onNodeWithTag("trade_offer_points_input").performScrollTo().performTextReplacement("100")
        composeTestRule.onNodeWithTag("trade_agree_button").performScrollTo().performClick()
        assertEquals("100" to "0", agreedPair)

        // Cancel & leave fires immediately
        composeTestRule.onNodeWithTag("trade_cancel_leave_button").performScrollTo().performClick()
        assertTrue(cancelled)
    }

    @Test
    fun tradeRoomDialog_showsPartnerCancelledNotice() {
        var dismissed = false
        val cancelledTrade = TradeSession(
            id = "trade_2",
            senderId = "u1",
            senderName = "Hero",
            receiverId = "u2",
            receiverName = "Nova",
            participantIds = listOf("u1", "u2"),
            status = TradeStatus.CANCELLED.firestoreValue,
            cancelledBy = "u2"
        )

        composeTestRule.setContent {
            PixieClickerTheme {
                CompositionLocalProvider(LocalPixieSocialStrings provides ViSocialStrings) {
                    TradeRoomDialog(
                        trade = cancelledTrade,
                        currentUserId = "u1",
                        progress = GameProgress(userId = "u1"),
                        compactNumbers = false,
                        isTradeBusy = false,
                        onSetOffer = { _, _ -> },
                        onToggleAgree = { _, _ -> },
                        onCancelAndLeave = {},
                        onDismissTerminal = { dismissed = true }
                    )
                }
            }
        }

        composeTestRule.onNodeWithText(ViSocialStrings.tradePartnerCancelledTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag("trade_close_button").performClick()
        assertTrue(dismissed)
    }
}
