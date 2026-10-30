package com.chrona.mobile.data

import android.content.Context
import com.chrona.shared.Catalog
import com.chrona.shared.FaceCategory
import com.chrona.shared.WatchFaceItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Catalog + APK access. Today the catalog is bundled; swap [faces] for a
 * network-backed Flow (Retrofit/Ktor) without touching the UI layer.
 */
@Singleton
class FaceRepository @Inject constructor(@ApplicationContext private val context: Context) {

    val faces: Flow<List<WatchFaceItem>> = flowOf(Catalog.faces)

    fun byId(id: String): WatchFaceItem? = Catalog.byId(id)
    fun byCategory(cat: FaceCategory?): List<WatchFaceItem> = Catalog.byCategory(cat)

    /** Copies the bundled APK to cache and returns it with its SHA-256, ready to stream. */
    suspend fun prepareApk(face: WatchFaceItem): PreparedApk = withContext(Dispatchers.IO) {
        val out = File(context.cacheDir, "faces/${face.id}.apk").apply { parentFile?.mkdirs() }
        context.assets.open(face.apkAsset).use { input -> out.outputStream().use { input.copyTo(it) } }
        val digest = MessageDigest.getInstance("SHA-256")
        out.inputStream().use { s ->
            val buf = ByteArray(64 * 1024)
            var n: Int
            while (s.read(buf).also { n = it } > 0) digest.update(buf, 0, n)
        }
        PreparedApk(out, digest.digest().joinToString("") { "%02x".format(it) })
    }

    data class PreparedApk(val file: File, val sha256: String)
}
