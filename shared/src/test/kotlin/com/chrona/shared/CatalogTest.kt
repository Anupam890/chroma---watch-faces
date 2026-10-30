package com.chrona.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTest {
    @Test fun `ids are unique and match push package convention`() {
        val ids = Catalog.faces.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        ids.forEach { assertTrue(it.matches(Regex("[a-z][a-z0-9_]*"))) }
        assertEquals("com.chrona.watchfacepush.classic_aurora", Catalog.byId("classic_aurora")!!.pushPackageName("com.chrona"))
    }
    @Test fun `compatibility filter respects WFF versions`() {
        assertTrue(Catalog.compatibleWith(33).none { it.wffVersion != WffVersion.V1 })
        assertEquals(Catalog.faces.size, Catalog.compatibleWith(36).size)
    }
    @Test fun `models round-trip through json`() {
        val m = TransferManifest("x", "com.chrona.watchfacepush.x", 10, "ab", "tok")
        assertEquals(m, ChronaJson.decodeFromString(TransferManifest.serializer(), ChronaJson.encodeToString(TransferManifest.serializer(), m)))
    }
    @Test fun `wear os labels`() { assertEquals("Wear OS 6", apiToWearOsLabel(36)); assertEquals("Wear OS 4", apiToWearOsLabel(33)) }
}
