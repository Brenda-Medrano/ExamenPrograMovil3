package com.example.exaprogramolvil3

import com.example.exaprogramolvil3.remote.data.RemotePayloadParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemotePayloadParserTest {
    @Test fun `lee features y banners`() {
        val data = RemotePayloadParser.parse("""{
            "features": [{"key":"a", "label":"Uno", "enabled":true}],
            "banners": [{"id":"x", "title":"Hola", "subtitle":"Demo"}]
        }""")
        assertEquals(1, data.features.size)
        assertTrue(data.features.single().enabled)
        assertEquals("Hola", data.banners.single().title)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rechaza claves duplicadas`() {
        RemotePayloadParser.parse("""{
            "features": [{"key":"a", "label":"A", "enabled":true},
                         {"key":"a", "label":"B", "enabled":false}],
            "banners": []
        }""")
    }

    @Test fun `acepta lista vacia de banners`() {
        val data = RemotePayloadParser.parse("""{"features": [], "banners": []}""")
        assertFalse(data.banners.isNotEmpty())
    }
}
