package app.collectionsaddon.patches

import app.soundaddon.patches.soundButtonActionsPatch
import org.junit.Assert.*
import org.junit.Test

class CompatibilityTest {
    @Test fun onlyTheInspectedGlobal4753FixtureIsAdvertised() {
        val patches = listOf(enhancedCollectionsPatch, soundButtonActionsPatch)
        assertEquals(2, patches.map { it.name }.distinct().size)
        for (patch in patches) {
            val compatibility = patch.compatibility!!.single()
            assertEquals("com.zhiliaoapp.musically", compatibility.packageName)
            assertEquals(setOf("9041803e91bcb814b4b4399fb5c85a91640b755e5e8ba76813814bf4cf2ab5ba"), compatibility.signatures)
            val target = compatibility.targets.single()
            assertEquals("47.5.3", target.version)
            assertEquals(setOf(2024705030L), target.versionCodes!!.values.map { it.toLong() }.toSet())
            assertEquals(23, target.minSdk)
            assertTrue(target.isExperimental)
        }
        assertTrue(enhancedCollectionsPatch.default)
        assertFalse(soundButtonActionsPatch.default)
    }
}
