package com.k3h.yoke

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.k3h.yoke.ui.theme.YokeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object AirlineLogos {
    private val cache = LruCache<String, ImageBitmap>(64)
    private val missing = mutableSetOf<String>()      // airlines the server has no logo for
    private val mutex = Mutex()                       // one download at a time, so duplicates aren't fetched twice

    fun cached(code: String): ImageBitmap? = cache.get(code)

    suspend fun load(code: String): ImageBitmap? = mutex.withLock {
        cache.get(code)?.let { return@withLock it }
        if (code in missing) return@withLock null

        var notFound = false
        val bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val conn = URL("https://pics.avs.io/96/96/$code.png").openConnection() as HttpURLConnection
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                try {
                    if (conn.responseCode !in 200..299) {
                        notFound = true
                        null
                    } else {
                        BitmapFactory.decodeStream(conn.inputStream)?.asImageBitmap()
                    }
                } finally {
                    conn.disconnect()
                }
            }.getOrNull()
        }

        if (bitmap != null) cache.put(code, bitmap) else if (notFound) missing += code
        bitmap
    }
}

/** Airline logo by 2-letter IATA code, with a code-in-a-box placeholder until (or unless) it loads. */
@Composable
fun AirlineLogo(
    code: String,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    var logo by remember(code) { mutableStateOf(AirlineLogos.cached(code)) }
    LaunchedEffect(code) {
        if (logo == null && code.isNotBlank()) logo = AirlineLogos.load(code)
    }

    val image = logo
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = "$name logo",
            contentScale = ContentScale.Fit,
            modifier = modifier
                .size(size)
                .background(Color.White, YokeStyle.smallShape)   // keeps dark logos visible in dark mode
                .padding(2.dp)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .border(1.dp, MaterialTheme.colorScheme.outline, YokeStyle.smallShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = code.take(2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}