package com.chrona.mobile

import com.chrona.mobile.ui.UiState
import com.chrona.shared.Catalog
import com.chrona.shared.FaceCategory
import com.chrona.shared.InstalledFace
import com.chrona.shared.WatchState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStateTest {
    private val watch = WatchState("n", "Pixel Watch", 36, "Wear OS 6", true, true,
        listOf(InstalledFace("slot1", "mono_digital", "com.chrona.watchfacepush.mono_digital", 1, true)))

    @Test fun `category and query filters compose`() {
        val s = UiState(faces = Catalog.faces, category = FaceCategory.SPORT)
        assertEquals(listOf("pulse_sport"), s.visibleFaces.map { it.id })
        assertEquals(listOf("mono_digital"), UiState(faces = Catalog.faces, query = "mono").visibleFaces.map { it.id })
    }
    @Test fun `installed and active derive from watch state`() {
        val s = UiState(faces = Catalog.faces, watch = watch)
        assertTrue(s.isInstalled("mono_digital")); assertTrue(s.isActive("mono_digital"))
        assertEquals("slot1", s.slotFor("mono_digital")); assertTrue(!s.isInstalled("pulse_sport"))
    }
}
