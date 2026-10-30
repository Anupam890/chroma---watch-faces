package com.chrona.wear.push

import android.content.Context
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.wear.watchface.push.WatchFacePushManager
import androidx.wear.watchface.push.WatchFacePushManagerFactory
import com.chrona.shared.InstalledFace
import com.chrona.shared.TransferManifest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin, testable wrapper over the Watch Face Push API. All API-36 calls are
 * isolated here so the rest of the app compiles and runs on Wear OS 4/5
 * (where [isSupported] is false and the UI falls back to Play Store links).
 */
@Singleton
class WatchFaceInstaller @Inject constructor(@ApplicationContext private val context: Context) {

    val isSupported: Boolean get() = Build.VERSION.SDK_INT >= 36

    val hasPermission: Boolean
        get() = isSupported && ContextCompat.checkSelfPermission(context, PERMISSION) == android.content.pm.PackageManager.PERMISSION_GRANTED

    @RequiresApi(36)
    private val manager: WatchFacePushManager by lazy { WatchFacePushManagerFactory.createWatchFacePushManager(context) }

    sealed interface Outcome {
        data class Success(val slotId: String) : Outcome
        data class Failure(val code: String, val message: String) : Outcome
    }

    suspend fun install(apk: File, manifest: TransferManifest): Outcome = withContext(Dispatchers.IO) {
        if (!isSupported) return@withContext Outcome.Failure("UNSUPPORTED", "Watch Face Push needs Wear OS 6")
        if (!hasPermission) return@withContext Outcome.Failure("NO_PERMISSION", "Grant Chrona permission to install watch faces")
        try {
            ParcelFileDescriptor.open(apk, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                val details = if (manifest.replaceSlotId != null) {
                    manager.updateWatchFace(manifest.replaceSlotId, pfd, manifest.validationToken)
                } else {
                    manager.addWatchFace(pfd, manifest.validationToken)
                }
                if (manifest.setActiveAfterInstall) runCatching { manager.setWatchFaceAsActive(details.slotId) }
                Outcome.Success(details.slotId)
            }
        } catch (e: WatchFacePushManager.AddWatchFaceException) {
            Log.e(TAG, "addWatchFace failed", e)
            Outcome.Failure("ADD_${e.errorCode}", friendly(e.errorCode))
        } catch (e: WatchFacePushManager.UpdateWatchFaceException) {
            Outcome.Failure("UPDATE_${e.errorCode}", e.message ?: "Update failed")
        } catch (t: Throwable) {
            Log.e(TAG, "install crashed", t)
            Outcome.Failure("UNKNOWN", t.message ?: "Install failed")
        } finally {
            apk.delete()
        }
    }

    suspend fun remove(slotId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isSupported) return@withContext false
        runCatching { manager.removeWatchFace(slotId); true }.getOrDefault(false)
    }

    suspend fun setActive(slotId: String): Boolean = withContext(Dispatchers.IO) {
        if (!isSupported) return@withContext false
        runCatching { manager.setWatchFaceAsActive(slotId); true }.getOrDefault(false)
    }

    /** Lists faces this app installed, mapped back to catalog ids via the package-name convention. */
    suspend fun installed(): Pair<List<InstalledFace>, Int> = withContext(Dispatchers.IO) {
        if (!isSupported || !hasPermission) return@withContext emptyList<InstalledFace>() to 0
        runCatching {
            val resp = manager.listWatchFaces()
            val active = runCatching { manager.isWatchFaceActive(context.packageName) }.getOrDefault(false)
            val list = resp.installedWatchFaceDetails.map { d ->
                InstalledFace(
                    slotId = d.slotId,
                    faceId = d.packageName.substringAfterLast(".watchfacepush."),
                    packageName = d.packageName,
                    versionCode = d.versionCode,
                    isActive = active && d.slotId == resp.installedWatchFaceDetails.firstOrNull()?.slotId
                )
            }
            list to resp.remainingSlotCount
        }.getOrDefault(emptyList<InstalledFace>() to 0)
    }

    private fun friendly(code: Int) = when (code) {
        WatchFacePushManager.AddWatchFaceException.ERROR_INVALID_PACKAGE_NAME -> "Watch face package name is invalid"
        WatchFacePushManager.AddWatchFaceException.ERROR_MALFORMED_WATCHFACE_APK -> "Watch face file is corrupted"
        WatchFacePushManager.AddWatchFaceException.ERROR_INVALID_VALIDATION_TOKEN -> "Validation token mismatch — rebuild and revalidate this face"
        WatchFacePushManager.AddWatchFaceException.ERROR_SLOT_LIMIT_REACHED -> "No free slots. Remove a face first"
        WatchFacePushManager.AddWatchFaceException.ERROR_UNSUPPORTED_WATCHFACE_PACKAGE -> "This face isn't supported on your watch"
        else -> "Install failed (code $code)"
    }

    companion object {
        private const val TAG = "WatchFaceInstaller"
        const val PERMISSION = "com.google.wear.permission.PUSH_WATCH_FACES"
    }
}
