package com.chrona.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.chrona.mobile.ui.ChronaRoot
import com.chrona.mobile.ui.theme.ChronaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val deepLinkFaceId = intent?.data?.takeIf { it.scheme == "chrona" }?.lastPathSegment
        setContent {
            ChronaTheme {
                ChronaRoot(startFaceId = deepLinkFaceId)
            }
        }
    }
}
