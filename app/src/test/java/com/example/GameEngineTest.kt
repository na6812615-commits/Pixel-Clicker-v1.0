package com.example

import com.example.data.GameProgress
import com.example.data.GameProgressRepository
import com.example.data.GameSettings
import com.example.data.InMemorySettingsStore
import com.example.ui.audio.PixieSoundEffect
import com.example.ui.audio.PixieSoundSynthesizer
import com.example.ui.game.GameEngine
import com.example.ui.game.GameUiState
import com.example.ui.game.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun manualClick_respectsMultiplierAndClickCount() {
        val initial = GameProgress(userId = "u1", diem = 0.0, x = 2.0, muti = 1.5, clickCount = 3L)
        val after = GameEngine.manualClick(initial)
        assertEquals(3.0, after.diem, 0.0001)
        assertEquals(4L, after.clickCount)
    }

    @Test
    fun shopUpgrades_matchPythonFormulas() {
        val start = GameProgress(userId = "u1", diem = 200.0)
        // Buy Item 1 (+x, cost 25, x->2, gia->75, lv->1)
        val r1 = GameEngine.buyShopPower(start)
        assertTrue(r1.succeeded)
        assertEquals(175.0, r1.nextProgress.diem, 0.0001)
        assertEquals(2.0, r1.nextProgress.x, 0.0001)
        assertEquals(75.0, r1.nextProgress.gia, 0.0001)
        assertEquals(1L, r1.nextProgress.lv)

        // Buy Item 2 (+0.1 muti, cost 50, muti->1.1, gia2->150, lv2->1)
        val r2 = GameEngine.buyShopMultiplier(r1.nextProgress)
        assertTrue(r2.succeeded)
        assertEquals(125.0, r2.nextProgress.diem, 0.0001)
        assertEquals(1.1, r2.nextProgress.muti, 0.0001)
        assertEquals(150.0, r2.nextProgress.gia2, 0.0001)
        assertEquals(1L, r2.nextProgress.lv2)

        // Buy Item 3 (cooldown, cost 75, cd: 0.25 - 0.125 = 0.125, thay: 0.0625, lv4->1)
        val r3 = GameEngine.buyShopCooldown(r2.nextProgress)
        assertTrue(r3.succeeded)
        assertEquals(50.0, r3.nextProgress.diem, 0.0001)
        assertEquals(0.125, r3.nextProgress.cd, 0.0001)
        assertEquals(0.0625, r3.nextProgress.thay, 0.0001)
        assertEquals(225.0, r3.nextProgress.gia4, 0.0001)
        assertEquals(1L, r3.nextProgress.lv4)
    }

    @Test
    fun dollarExchangeAndAutoClickAndRestart_matchPython() {
        val p0 = GameProgress(userId = "u1", diem = 26500.0)
        val ex1 = GameEngine.exchange1000(p0).nextProgress
        assertEquals(25500.0, ex1.diem, 0.0001)
        assertEquals(1.0, ex1.cashDo, 0.0001)

        val exAll = GameEngine.exchangeAll(ex1).nextProgress
        assertEquals(0.0, exAll.diem, 0.0001)
        assertEquals(26.5, exAll.cashDo, 0.0001)

        // Unlock auto click ($25)
        val autoBought = GameEngine.buyDollarAutoUnlock(exAll)
        assertTrue(autoBought.succeeded)
        assertFalse(autoBought.nextProgress.gioihan)
        assertTrue(autoBought.nextProgress.autoEnabled)
        assertEquals(1.5, autoBought.nextProgress.cashDo, 0.0001)

        // Auto click tick
        val ticked = GameEngine.autoClickTick(autoBought.nextProgress)
        assertEquals(1.0, ticked.diem, 0.0001)
        assertEquals(1L, ticked.clickCount)

        // Restart when ready
        val readyForRestart = ticked.copy(diem = 150.0)
        val restarted = GameEngine.performRestart(readyForRestart)
        assertTrue(restarted.succeeded)
        assertEquals(1.5, restarted.nextProgress.muti, 0.0001)
        assertEquals(1.5, restarted.nextProgress.muti2, 0.0001)
        assertEquals(1L, restarted.nextProgress.lv3)
        assertEquals(450.0, restarted.nextProgress.gia3, 0.0001)
        assertEquals(0.0, restarted.nextProgress.diem, 0.0001)
        assertTrue(restarted.nextProgress.gioihan)
        assertFalse(restarted.nextProgress.autoEnabled)
    }

    @Test
    fun formatGameNumbers_supportsFullAndCompactModes() {
        assertEquals("1500", GameEngine.formatGameCount(1500L, compact = false))
        assertEquals("999", GameEngine.formatGameCount(999L, compact = true))
        assertEquals("1.50K", GameEngine.formatGameCount(1500L, compact = true))
        assertEquals("2.50M", GameEngine.formatGameNumber(2_500_000.4, compact = true))
        assertEquals("3.25B", GameEngine.formatGameCount(3_250_000_000L, compact = true))
    }

    @Test
    fun inMemorySettingsStore_updatesAndResets() = kotlinx.coroutines.test.runTest {
        val store = com.example.data.InMemorySettingsStore()
        val initial = store.settingsFlow.first()
        assertEquals(com.example.data.VfxIntensity.NORMAL, initial.vfxIntensity)
        assertTrue(initial.confirmBeforeRestart)
        assertTrue(initial.showArcaneRings)
        assertTrue(initial.showAutoFloatingNumbers)
        assertTrue(initial.soundEffectsEnabled)
        assertTrue(initial.showUpgradeBadges)
        assertTrue(initial.showDollarProgressCard)
        assertEquals(com.example.data.AutoSyncInterval.STANDARD_8S, initial.autoSyncInterval)
        assertFalse(initial.compactNumbers)

        store.update {
            it.copy(
                language = com.example.data.AppLanguage.JA,
                vfxIntensity = com.example.data.VfxIntensity.ULTRA,
                crystalAuraTheme = com.example.data.CrystalAuraTheme.SOLAR_GOLD,
                showArcaneRings = false,
                showAutoFloatingNumbers = false,
                soundEffectsEnabled = false,
                showUpgradeBadges = false,
                showDollarProgressCard = false,
                autoSyncInterval = com.example.data.AutoSyncInterval.FAST_4S,
                compactNumbers = true,
                confirmBeforeRestart = false
            )
        }
        val updated = store.settingsFlow.first()
        assertEquals(com.example.data.AppLanguage.JA, updated.language)
        assertEquals(com.example.data.VfxIntensity.ULTRA, updated.vfxIntensity)
        assertEquals(com.example.data.CrystalAuraTheme.SOLAR_GOLD, updated.crystalAuraTheme)
        assertFalse(updated.showArcaneRings)
        assertFalse(updated.showAutoFloatingNumbers)
        assertFalse(updated.soundEffectsEnabled)
        assertFalse(updated.showUpgradeBadges)
        assertFalse(updated.showDollarProgressCard)
        assertEquals(com.example.data.AutoSyncInterval.FAST_4S, updated.autoSyncInterval)
        assertTrue(updated.compactNumbers)
        assertFalse(updated.confirmBeforeRestart)

        store.reset()
        val afterReset = store.settingsFlow.first()
        assertEquals(com.example.data.AppLanguage.VI, afterReset.language)
        assertEquals(com.example.data.VfxIntensity.NORMAL, afterReset.vfxIntensity)
        assertEquals(com.example.data.CrystalAuraTheme.PIXIE_MINT, afterReset.crystalAuraTheme)
        assertTrue(afterReset.showArcaneRings)
        assertTrue(afterReset.soundEffectsEnabled)
        assertEquals(com.example.data.AutoSyncInterval.STANDARD_8S, afterReset.autoSyncInterval)
        assertFalse(afterReset.compactNumbers)
    }

    @Test
    fun allTwelveLanguages_mapAndTranslateEngineMessages() {
        val languages = com.example.data.AppLanguage.entries
        assertTrue("Expected at least 10 languages, found ${languages.size}", languages.size >= 10)

        val seenTitles = mutableSetOf<String>()
        languages.forEach { lang ->
            val s = com.example.ui.i18n.pixieStringsFor(lang)
            assertTrue(s.settingsTitle.isNotBlank())
            assertTrue(s.shopTitle.isNotBlank())
            assertTrue(s.tabTap.isNotBlank())
            seenTitles.add(s.settingsTitle)

            val res = GameEngine.buyShopPower(GameProgress(userId = "u1", diem = 50.0), s)
            assertEquals(s.msgShopPowerOk, res.message)
        }
        assertEquals(languages.size, seenTitles.size)
    }

    @Test
    fun soundSynthesizer_buildsValidWavForAllEffects() {
        PixieSoundEffect.entries.forEach { effect ->
            val wav = PixieSoundSynthesizer.buildWavBytes(effect)
            assertTrue("WAV too short for $effect", wav.size > 44)
            assertEquals("RIFF", String(wav.copyOfRange(0, 4), Charsets.US_ASCII))
            assertEquals("WAVE", String(wav.copyOfRange(8, 12), Charsets.US_ASCII))
            assertEquals("fmt ", String(wav.copyOfRange(12, 16), Charsets.US_ASCII))
            assertEquals("data", String(wav.copyOfRange(36, 40), Charsets.US_ASCII))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun gameViewModel_emitsTapAndPurchaseSuccessAndFailSounds() {
        val dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        try {
            runTest(dispatcher) {
                val fakeRepo = object : GameProgressRepository {
                    override suspend fun loadOrCreateProgress(userId: String): Result<GameProgress> =
                        Result.success(GameProgress(userId = userId, diem = 30.0))

                    override suspend fun saveProgress(userId: String, progress: GameProgress): Result<Unit> =
                        Result.success(Unit)
                }
                val store = InMemorySettingsStore(GameSettings(soundEffectsEnabled = true))
                val vm = GameViewModel(fakeRepo, "u1", store)
                val emitted = mutableListOf<PixieSoundEffect>()
                val collectorJob = launch {
                    vm.soundEvents.collect { emitted.add(it) }
                }

                runCurrent()
                assertTrue(vm.uiState.value is GameUiState.Ready)

                // 1. Manual click emits MANUAL_TAP
                vm.onManualTap()
                runCurrent()
                // 2. Affordable shop upgrade (cost 25, diem >= 31) emits PURCHASE_SUCCESS
                vm.buyShopPower()
                runCurrent()
                // 3. Unaffordable shop upgrade (cost 75, remaining ~6) emits PURCHASE_FAIL
                vm.buyShopPower()
                runCurrent()

                assertEquals(
                    listOf(
                        PixieSoundEffect.MANUAL_TAP,
                        PixieSoundEffect.PURCHASE_SUCCESS,
                        PixieSoundEffect.PURCHASE_FAIL
                    ),
                    emitted
                )

                // 4. Disabling soundEffectsEnabled suppresses new manual tap and purchase sounds
                vm.setSoundEffectsEnabled(false)
                runCurrent()
                advanceTimeBy(300L)
                runCurrent()
                vm.onManualTap()
                vm.buyShopMultiplier()
                runCurrent()
                assertEquals(3, emitted.size)

                vm.flushAndPrepareSignOut {}
                runCurrent()
                collectorJob.cancel()
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
}
