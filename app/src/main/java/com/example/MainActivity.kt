package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.DataStoreSettingsRepository
import com.example.data.FirestoreServerTradeRepository
import com.example.data.GameRepository
import com.example.data.GameSettings
import com.example.data.ServerPlayer
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.game.GameViewModel
import com.example.ui.game.PixieGameScreen
import com.example.ui.i18n.LocalPixieSocialStrings
import com.example.ui.i18n.LocalPixieStrings
import com.example.ui.i18n.pixieSocialStringsFor
import com.example.ui.i18n.pixieStringsFor
import com.example.ui.theme.PixieClickerTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixieClickerTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val appContext = LocalContext.current.applicationContext
    val settingsStore = remember(appContext) { DataStoreSettingsRepository(appContext) }
    val appSettings by settingsStore.settingsFlow.collectAsStateWithLifecycle(initialValue = GameSettings())
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

    CompositionLocalProvider(
        LocalPixieStrings provides pixieStringsFor(appSettings.language),
        LocalPixieSocialStrings provides pixieSocialStringsFor(appSettings.language)
    ) {
        val user = currentUser
        if (user == null) {
            AuthScreen(onAuthSuccess = {})
        } else {
            key(user.uid) {
                val playerName = ServerPlayer.sanitizePlayerName(
                    user.displayName ?: user.email,
                    user.uid
                )
                val viewModel: GameViewModel = viewModel(
                    key = user.uid,
                    factory = viewModelFactory {
                        initializer {
                            val app = checkNotNull(this[APPLICATION_KEY]) {
                                "APPLICATION_KEY missing from CreationExtras"
                            }
                            val databaseId = app.getString(R.string.firestore_database_id)
                            val db = FirebaseFirestore.getInstance(databaseId)
                            GameViewModel(
                                repository = GameRepository(db),
                                currentUserId = user.uid,
                                settingsStore = settingsStore,
                                serverTradeRepository = FirestoreServerTradeRepository(db),
                                currentUserDisplayName = playerName
                            )
                        }
                    }
                )
                PixieGameScreen(viewModel = viewModel)
            }
        }
    }
}
