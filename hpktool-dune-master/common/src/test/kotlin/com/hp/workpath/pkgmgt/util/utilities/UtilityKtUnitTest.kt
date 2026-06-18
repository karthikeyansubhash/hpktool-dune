package com.hp.workpath.pkgmgt.util.utilities

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class UtilityKtUnitTest {
    @Test
    fun `getOSType returns correct OS type`() {
        val result = getOsType()
        assertNotEquals(OsType.Mac, result)
        assertNotEquals(OsType.Unknown, result)
    }

    @Test
    fun `windowsPathToWslPath converts Windows path to Wsl path correctly`() {
        val windowsPath = "C:\\Users\\Test"
        val expectedWslPath = "/mnt/c/Users/Test"
        assertEquals(expectedWslPath, windowsPathToWslPath(windowsPath))
    }

    @Test
    fun `isValidUuid returns true for valid UUID`() {
        val uuid = "123e4567-e89b-12d3-a456-426614174000"
        assertTrue(isValidUuid(uuid))
    }

    @Test
    fun `isValidUuid returns false for invalid UUID`() {
        val uuid = "123e4567-e89b-12d3-a456-42661417400"
        assertFalse(isValidUuid(uuid))
    }

    @Test
    fun `getLocalizations returns list of localizations`() {
        val localizations = getLocalizations()
        assertTrue(localizations.isNotEmpty())
        assertTrue(localizations.contains("en-US"))
    }

    @Test
    fun `getIconSizes returns list of iconSizes`() {
        val iconSizes = getIconSizes()
        assertTrue(iconSizes.isNotEmpty())
        assertTrue(iconSizes.contains("90x90"))
    }

    @Test
    fun `isValidJson returns true for valid JSON`() {
        val json = "{\"key\":\"value\"}"
        assertTrue(isValidJson(json))
    }

    @Test
    fun `isValidJson returns false for invalid JSON`() {
        val json = "{key:\"value\"}"
        assertFalse(isValidJson(json))
    }

    @Test
    fun `checkIconFileExtension returns true for supported extensions`() {
        var file = Paths.get("icon.png").toFile()
        assertTrue(checkIconFileExtension(file))
        file = Paths.get("icon.jpeg").toFile()
        assertTrue(checkIconFileExtension(file))
        file = Paths.get("icon.jpg").toFile()
        assertTrue(checkIconFileExtension(file))
    }

    @Test
    fun `checkIconFileExtension returns false for unsupported extensions`() {
        val file = Paths.get("icon.txt").toFile()
        assertFalse(checkIconFileExtension(file))
    }

    @Test
    fun `getIconFileType returns correct type for supported extensions`() {
        var file = Paths.get("icon.png")
        assertEquals(ICON_TYPE_PNG, getIconFileType(file))
        file = Paths.get("icon.jpeg")
        assertEquals(ICON_TYPE_JPG, getIconFileType(file))
    }

    @Test
    fun `getIconFileType throws exception for unsupported extensions`() {
        val file = Paths.get("icon.txt")
        assertThrows(IllegalArgumentException::class.java) {
            getIconFileType(file)
        }
    }

    @Test
    fun `sanitizeJsonString returns unchanged for already valid JSON`() {
        val json = "{\"key\":\"value\",\"number\":42}"
        val result = sanitizeJsonString(json)
        assertEquals(json, result)
    }

    @Test
    fun `sanitizeJsonString handles CMD-mangled JSON with unquoted keys and string values`() {
        val cmdMangledJson = "{key:value,name:test}"
        val result = sanitizeJsonString(cmdMangledJson)
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        val parsed = mapper.readValue(result, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {})
        assertEquals("value", parsed["key"])
        assertEquals("test", parsed["name"])
    }

    @Test
    fun `sanitizeJsonString normalizes Windows paths with backslashes to forward slashes`() {
        val windowsPathJson = "{icon:C:\\temp\\icon.png,lib:D:\\libs\\lib.jar}"
        val result = sanitizeJsonString(windowsPathJson)
        assertTrue(result.contains("C:/temp/icon.png"))
        assertTrue(result.contains("D:/libs/lib.jar"))
        assertTrue(!result.contains("\\"))
    }

    @Test
    fun `sanitizeJsonString preserves JSON primitive types - numbers`() {
        val json = "{count:42,price:19.99,scientific:1.5e10}"
        val result = sanitizeJsonString(json)
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        val parsed = mapper.readValue(result, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {})
        assertEquals(42, (parsed["count"] as Number).toInt())
        assertEquals(19.99, (parsed["price"] as Number).toDouble())
    }

    @Test
    fun `sanitizeJsonString preserves JSON primitive types - booleans and null`() {
        val json = "{active:true,disabled:false,optional:null}"
        val result = sanitizeJsonString(json)
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        val parsed = mapper.readValue(result, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {})
        assertEquals(true, parsed["active"])
        assertEquals(false, parsed["disabled"])
        assertNull(parsed["optional"])
    }

    @Test
    fun `sanitizeJsonString handles mixed primitives and string values`() {
        val json = "{id:123,active:true,path:D:\\workspace\\project,description:Sample}"
        val result = sanitizeJsonString(json)
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        val parsed = mapper.readValue(result, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {})
        assertEquals(123, (parsed["id"] as Number).toInt())
        assertEquals(true, parsed["active"])
        assertEquals("D:/workspace/project", parsed["path"])
        assertEquals("Sample", parsed["description"])
    }

    @Test
    fun `sanitizeJsonString handles icon-set with Windows paths and preserves structure`() {
        val iconSetJson = "{90x90:C:\\icons\\90x90\\ic_launcher.png,179x179:C:\\icons\\179x179\\ic_launcher.png}"
        val result = sanitizeJsonString(iconSetJson)
        assertTrue(result.contains("90x90"))
        assertTrue(result.contains("179x179"))
        assertTrue(result.contains("C:/icons/90x90/ic_launcher.png"))
        assertTrue(result.contains("C:/icons/179x179/ic_launcher.png"))
    }

    @Test
    fun `sanitizeJsonString throws exception for malformed JSON without recovery option`() {
        val malformedJson = "{key:value,novalue}"
        assertThrows(IllegalArgumentException::class.java) {
            sanitizeJsonString(malformedJson)
        }
    }

    @Test
    fun `sanitizeJsonString handles properly formatted JSON with escaped quotes`() {
        val properJson = "{\"key\":\"value\",\"path\":\"C:/temp/file.txt\"}"
        val result = sanitizeJsonString(properJson)
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        val parsed = mapper.readValue(result, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {})
        assertEquals("value", parsed["key"])
        assertEquals("C:/temp/file.txt", parsed["path"])
    }

    @Test
    fun `sanitizeJsonString strips single quotes when surrounded by them`() {
        val singleQuotedJson = "'{\"key\":\"value\"}'"
        val result = sanitizeJsonString(singleQuotedJson)
        assertEquals("{\"key\":\"value\"}", result)
    }

    @Test
    fun `sanitizeJsonString preserves negative numbers`() {
        val json = "{temperature:-15,balance:-1000.50}"
        val result = sanitizeJsonString(json)
        val mapper = com.fasterxml.jackson.databind.ObjectMapper()
        val parsed = mapper.readValue(result, object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {})
        assertEquals(-15, (parsed["temperature"] as Number).toInt())
        assertEquals(-1000.50, (parsed["balance"] as Number).toDouble())
    }
}