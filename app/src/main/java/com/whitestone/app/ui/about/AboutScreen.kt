package com.whitestone.app.ui.about

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.whitestone.app.ui.theme.BrownAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val feedbackEmail = "peijing.teh.dev@gmail.com"
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName ?: "unknown"
    val versionCode = packageInfo.longVersionCode

    val feedbackBody = buildString {
        append("\n\n---")
        append("\nApp Version: $versionName ($versionCode)")
        append("\nAndroid: ${Build.VERSION.RELEASE}")
        append("\nDevice: ${Build.MANUFACTURER} ${Build.MODEL}")
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("About") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "White Stone is a mental tracking app that helps spiritual practitioners track the goodness of their thoughts & actions throughout a day.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "This app is inspired by Upagupta, the spiritual teacher of the ancient Indian Emperor Ashoka.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "\u201CUpagupta \u2026 was a native of the Madura country. His instructor \u2026 told him to keep black and white pebbles. When he had a bad thought he was to throw down into a basket a black pebble; when he had a good thought he was to throw down a white pebble. Upagupta did as he was told. At first bad thoughts abounded, and black pebbles were very numerous.\nThen the white and black were about equal.\nOn the seventh day there were only white pebbles.\n(His instructor) then undertook to expound to him the four truths.\u201D",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\u2014 Chinese Buddhism, Joseph Edkins, 1893, p.68",
                style = MaterialTheme.typography.bodySmall,
                color = BrownAccent
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "What is a good thought?",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Thoughts of letting go, kindness and gentleness.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "What is a bad thought?",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Thoughts of sensual desire, ill will and ruthlessness.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "But you are free to decide for yourself what are good thoughts or bad thoughts that you will be tracking with White Stone.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Feedback",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Have a suggestion, complaint, or comment? We'd love to hear from you.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = feedbackEmail,
                style = MaterialTheme.typography.bodyLarge,
                color = BrownAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_SENDTO,
                        Uri.parse(
                            "mailto:$feedbackEmail" +
                                "?subject=${Uri.encode("White Stone Feedback")}" +
                                "&body=${Uri.encode(feedbackBody)}"
                        )
                    )
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        val feedbackText = buildString {
                            append(feedbackEmail)
                            append(feedbackBody)
                        }
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText("White Stone feedback", feedbackText)
                        )
                        Toast.makeText(
                            context,
                            "No mail app found. Feedback email copied to clipboard.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            ) {
                Text("Send Feedback")
            }
        }
    }
}
