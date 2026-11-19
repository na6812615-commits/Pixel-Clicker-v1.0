package com.example.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppLanguage
import com.example.data.AutoSyncInterval
import com.example.data.CrystalAuraTheme
import com.example.data.GameProgress
import com.example.data.GameProgressRepository
import com.example.data.GameSettings
import com.example.data.InMemorySettingsStore
import com.example.data.SettingsStore
import com.example.data.VfxIntensity
import com.example.ui.audio.PixieSoundEffect
import com.example.ui.i18n.PixieStrings
import com.example.ui.i18n.pixieStringsFor
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class GameTab {
    TAP,
    SHOP,
    DOLLAR_SHOP,
    RESTARTS
}

data class PixieToast(
    val id: Long,
    val message: String,
    val isSuccess: Boolean
)

data class TapParticleEvent(
    val id: Long,
    val amountText: String,
    val isAuto: Boolean
)

sealed interface GameUiState {
    data object Loading : GameUiState
    data class Ready(
        val progress: GameProgress,
        val activeTab: GameTab = GameTab.TAP,
        val showSettings: Boolean = false,
        val settings: GameSettings = GameSettings(),
        val cooldownFraction: Float = 0f,
        val toast: PixieToast? = null,
        val isSyncing: Boolean = false,
        val hasUnsavedChanges: Boolean = false,
        val syncError: String? = null
    ) : GameUiState

    data class Error(val message: String) : GameUiState
}

class GameViewModel(
    private val repository: GameProgressRepository,
    private val currentUserId: String,
    private val settingsStore: SettingsStore = InMemorySettingsStore()
) : ViewModel() {

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _particles = MutableSharedFlow<TapParticleEvent>(extraBufferCapacity = 32)
    val particles: SharedFlow<TapParticleEvent> = _particles.asSharedFlow()

    private val _soundEvents = MutableSharedFlow<PixieSoundEffect>(extraBufferCapacity = 32)
    val soundEvents: SharedFlow<PixieSoundEffect> = _soundEvents.asSharedFlow()

    private var latestSettings = GameSettings()
    private var autoWorkerJob: Job? = null
    private var cooldownJob: Job? = null
    private var toastJob: Job? = null
    private var periodicSyncJob: Job? = null
    private var particleCounter = 0L
    private var toastCounter = 0L

    init {
        observeSettings()
        loadGame()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsStore.settingsFlow.collect { nextSettings ->
                latestSettings = nextSettings
                val current = _uiState.value as? GameUiState.Ready ?: return@collect
                _uiState.value = current.copy(settings = nextSettings)
            }
        }
    }

    fun loadGame() {
        _uiState.value = GameUiState.Loading
        viewModelScope.launch {
            val initialSettings = runCatching { settingsStore.settingsFlow.first() }
                .getOrDefault(latestSettings)
            latestSettings = initialSettings
            val result = repository.loadOrCreateProgress(currentUserId)
            result.fold(
                onSuccess = { loaded ->
                    _uiState.value = GameUiState.Ready(
                        progress = loaded,
                        settings = initialSettings
                    )
                    startAutoWorkerLoop()
                    startPeriodicSyncLoop()
                },
                onFailure = { error ->
                    val s = pixieStringsFor(initialSettings.language)
                    _uiState.value = GameUiState.Error(
                        error.localizedMessage ?: s.errLoadGame
                    )
                }
            )
        }
    }

    fun selectTab(tab: GameTab) {
        val current = _uiState.value as? GameUiState.Ready ?: return
        if (current.activeTab == tab && !current.showSettings) return
        _uiState.value = current.copy(activeTab = tab, showSettings = false)
    }

    fun openSettings() {
        val current = _uiState.value as? GameUiState.Ready ?: return
        _uiState.value = current.copy(showSettings = true)
    }

    fun closeSettings() {
        val current = _uiState.value as? GameUiState.Ready ?: return
        _uiState.value = current.copy(showSettings = false)
    }

    fun setLanguage(language: AppLanguage) = updateSettings { it.copy(language = language) }
    fun setVfxIntensity(intensity: VfxIntensity) = updateSettings { it.copy(vfxIntensity = intensity) }
    fun setCrystalAuraTheme(theme: CrystalAuraTheme) = updateSettings { it.copy(crystalAuraTheme = theme) }
    fun setAmbientParticlesEnabled(enabled: Boolean) = updateSettings { it.copy(ambientParticlesEnabled = enabled) }
    fun setShowArcaneRings(enabled: Boolean) = updateSettings { it.copy(showArcaneRings = enabled) }
    fun setShowComboCounter(enabled: Boolean) = updateSettings { it.copy(showComboCounter = enabled) }
    fun setShowFloatingNumbers(enabled: Boolean) = updateSettings { it.copy(showFloatingNumbers = enabled) }
    fun setShowAutoFloatingNumbers(enabled: Boolean) = updateSettings { it.copy(showAutoFloatingNumbers = enabled) }
    fun setSoundEffectsEnabled(enabled: Boolean) = updateSettings { it.copy(soundEffectsEnabled = enabled) }
    fun setHapticEnabled(enabled: Boolean) = updateSettings { it.copy(hapticEnabled = enabled) }
    fun setScreenShakeEnabled(enabled: Boolean) = updateSettings { it.copy(screenShakeEnabled = enabled) }
    fun setShowUpgradeBadges(enabled: Boolean) = updateSettings { it.copy(showUpgradeBadges = enabled) }
    fun setShowDollarProgressCard(enabled: Boolean) = updateSettings { it.copy(showDollarProgressCard = enabled) }
    fun setConfirmBeforeRestart(enabled: Boolean) = updateSettings { it.copy(confirmBeforeRestart = enabled) }
    fun setCompactNumbers(enabled: Boolean) = updateSettings { it.copy(compactNumbers = enabled) }
    fun setAutoCloudSyncEnabled(enabled: Boolean) = updateSettings { it.copy(autoCloudSyncEnabled = enabled) }
    fun setAutoSyncInterval(interval: AutoSyncInterval) = updateSettings { it.copy(autoSyncInterval = interval) }

    fun resetSettings() {
        viewModelScope.launch {
            settingsStore.reset()
            val current = _uiState.value as? GameUiState.Ready ?: return@launch
            val s = pixieStringsFor(AppLanguage.VI)
            val toast = PixieToast(++toastCounter, s.toastResetSettings, true)
            _uiState.value = current.copy(toast = toast)
            scheduleToastDismiss(current.progress.cd4, toast.id)
        }
    }

    private fun updateSettings(transform: (GameSettings) -> GameSettings) {
        viewModelScope.launch {
            settingsStore.update(transform)
        }
    }

    fun onManualTap() {
        val current = _uiState.value as? GameUiState.Ready ?: return
        if (current.cooldownFraction > 0f) return

        val next = GameEngine.manualClick(current.progress)
        val gained = GameEngine.pointsPerManualClick(current.progress)
        _uiState.value = current.copy(
            progress = next,
            cooldownFraction = 1f,
            hasUnsavedChanges = true
        )
        _particles.tryEmit(
            TapParticleEvent(
                id = ++particleCounter,
                amountText = "+${formatGained(gained)}",
                isAuto = false
            )
        )
        if (current.settings.soundEffectsEnabled) {
            _soundEvents.tryEmit(PixieSoundEffect.MANUAL_TAP)
        }
        startManualCooldown(next.cd)
    }

    private fun startManualCooldown(cdSeconds: Double) {
        cooldownJob?.cancel()
        val durationMs = (cdSeconds * 1000.0).toLong()
        if (durationMs <= 16L) {
            val state = _uiState.value as? GameUiState.Ready ?: return
            _uiState.value = state.copy(cooldownFraction = 0f)
            return
        }
        cooldownJob = viewModelScope.launch {
            val start = System.currentTimeMillis()
            while (isActive) {
                val elapsed = System.currentTimeMillis() - start
                val remaining = (1f - elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
                val state = _uiState.value as? GameUiState.Ready ?: break
                _uiState.value = state.copy(cooldownFraction = remaining)
                if (remaining <= 0f) break
                delay(16L)
            }
        }
    }

    private fun startAutoWorkerLoop() {
        autoWorkerJob?.cancel()
        autoWorkerJob = viewModelScope.launch {
            while (isActive) {
                val state = _uiState.value as? GameUiState.Ready
                if (state != null && state.progress.autoEnabled && !state.progress.gioihan) {
                    val waitMs = (state.progress.cd2 * 1000.0).toLong().coerceAtLeast(16L)
                    delay(waitMs)
                    val latest = _uiState.value as? GameUiState.Ready ?: continue
                    if (latest.progress.autoEnabled && !latest.progress.gioihan) {
                        val next = GameEngine.autoClickTick(latest.progress)
                        val gained = GameEngine.pointsPerAutoClick(latest.progress)
                        _uiState.value = latest.copy(
                            progress = next,
                            hasUnsavedChanges = true
                        )
                        _particles.tryEmit(
                            TapParticleEvent(
                                id = ++particleCounter,
                                amountText = "+${formatGained(gained)}",
                                isAuto = true
                            )
                        )
                    }
                } else {
                    delay(100L)
                }
            }
        }
    }

    private fun startPeriodicSyncLoop() {
        periodicSyncJob?.cancel()
        periodicSyncJob = viewModelScope.launch {
            var elapsedMs = 0L
            while (isActive) {
                delay(1_000L)
                elapsedMs += 1_000L
                val state = _uiState.value as? GameUiState.Ready ?: continue
                val targetMs = state.settings.autoSyncInterval.intervalMs
                if (elapsedMs >= targetMs) {
                    elapsedMs = 0L
                    if (state.settings.autoCloudSyncEnabled && state.hasUnsavedChanges && !state.isSyncing) {
                        syncProgressToCloud()
                    }
                }
            }
        }
    }

    fun buyShopPower() = applyAction { p, s -> GameEngine.buyShopPower(p, s) }
    fun buyShopMultiplier() = applyAction { p, s -> GameEngine.buyShopMultiplier(p, s) }
    fun buyShopCooldown() = applyAction { p, s -> GameEngine.buyShopCooldown(p, s) }

    fun exchange1000() = applyAction { p, s -> GameEngine.exchange1000(p, s) }
    fun exchangeAll() = applyAction { p, s -> GameEngine.exchangeAll(p, s) }

    fun buyDollarDiscount() = applyAction { p, s -> GameEngine.buyDollarDiscount(p, s) }
    fun buyDollarMenuSpeed() = applyAction { p, s -> GameEngine.buyDollarMenuSpeed(p, s) }
    fun buyDollarToastSpeed() = applyAction { p, s -> GameEngine.buyDollarToastSpeed(p, s) }
    fun buyDollarAutoUnlock() = applyAction { p, s -> GameEngine.buyDollarAutoUnlock(p, s) }

    fun toggleAutoClick() = applyAction { p, s -> GameEngine.toggleAutoClick(p, s) }
    fun buyAutoPower() = applyAction { p, s -> GameEngine.buyAutoPower(p, s) }
    fun buyAutoSpeed() = applyAction { p, s -> GameEngine.buyAutoSpeed(p, s) }

    fun performRestart() = applyAction { p, s -> GameEngine.performRestart(p, s) }

    private inline fun applyAction(transform: (GameProgress, PixieStrings) -> GameActionResult) {
        val current = _uiState.value as? GameUiState.Ready ?: return
        val strings = pixieStringsFor(current.settings.language)
        val outcome = transform(current.progress, strings)
        val toast = PixieToast(
            id = ++toastCounter,
            message = outcome.message,
            isSuccess = outcome.succeeded
        )
        _uiState.value = current.copy(
            progress = outcome.nextProgress,
            toast = toast,
            hasUnsavedChanges = current.hasUnsavedChanges || outcome.shouldSyncNow
        )
        if (current.settings.soundEffectsEnabled) {
            _soundEvents.tryEmit(
                if (outcome.succeeded) PixieSoundEffect.PURCHASE_SUCCESS else PixieSoundEffect.PURCHASE_FAIL
            )
        }
        scheduleToastDismiss(outcome.nextProgress.cd4, toast.id)
        if (outcome.shouldSyncNow && current.settings.autoCloudSyncEnabled) {
            viewModelScope.launch { syncProgressToCloud() }
        }
    }

    private fun scheduleToastDismiss(cd4Seconds: Double, toastId: Long) {
        toastJob?.cancel()
        val durationMs = (cd4Seconds * 1000.0).toLong().coerceIn(250L, 2500L)
        toastJob = viewModelScope.launch {
            delay(durationMs)
            val current = _uiState.value as? GameUiState.Ready ?: return@launch
            if (current.toast?.id == toastId) {
                _uiState.value = current.copy(toast = null)
            }
        }
    }

    fun manualSync() {
        viewModelScope.launch {
            syncProgressToCloud(showSuccessToast = true)
        }
    }

    private suspend fun syncProgressToCloud(showSuccessToast: Boolean = false) {
        val current = _uiState.value as? GameUiState.Ready ?: return
        if (current.isSyncing) return
        val snapshotProgress = current.progress
        _uiState.value = current.copy(isSyncing = true, syncError = null)

        val result = repository.saveProgress(currentUserId, snapshotProgress)
        val after = _uiState.value as? GameUiState.Ready ?: return
        val strings = pixieStringsFor(after.settings.language)
        result.fold(
            onSuccess = {
                val toast = if (showSuccessToast) {
                    val t = PixieToast(++toastCounter, strings.toastSavedCloud, true)
                    scheduleToastDismiss(after.progress.cd4, t.id)
                    t
                } else {
                    after.toast
                }
                val stillDirty = after.progress != snapshotProgress
                _uiState.value = after.copy(
                    isSyncing = false,
                    hasUnsavedChanges = stillDirty,
                    syncError = null,
                    toast = toast
                )
            },
            onFailure = { error ->
                _uiState.value = after.copy(
                    isSyncing = false,
                    syncError = error.localizedMessage ?: strings.errSyncCloud
                )
            }
        )
    }

    fun flushAndPrepareSignOut(onComplete: () -> Unit) {
        autoWorkerJob?.cancel()
        periodicSyncJob?.cancel()
        cooldownJob?.cancel()
        val current = _uiState.value as? GameUiState.Ready
        if (current == null || !current.hasUnsavedChanges) {
            onComplete()
            return
        }
        viewModelScope.launch {
            withContext(NonCancellable) {
                repository.saveProgress(currentUserId, current.progress)
            }
            onComplete()
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoWorkerJob?.cancel()
        cooldownJob?.cancel()
        toastJob?.cancel()
        periodicSyncJob?.cancel()
    }

    private fun formatGained(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }
}
