package com.hp.workpath.pkgmgt.util.utilities

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern
import kotlin.io.path.*

enum class OsType {
    Unknown, Windows, Mac, Linux;
}

fun getOsType(): OsType {
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("win") -> OsType.Windows
        os.contains("mac") || os.contains("darwin") -> OsType.Mac
        os.contains("nix") || os.contains("nux") || os.contains("aix") -> OsType.Linux
        else -> OsType.Unknown
    }
}

fun windowsPathToWslPath(windowsPath: String): String {
    var wslPath = windowsPath
    for (drive in 'a'..'z') {
        if (wslPath.startsWith("$drive:\\", true)) {
            wslPath = "/mnt/$drive/" + wslPath.substring(3).replace('\\', '/')
            break
        }
    }
    return wslPath
}

// https://www.ietf.org/rfc/rfc4122.txt
fun isValidUuid(uuid: String): Boolean {
    val pattern = Pattern
        .compile("(?i)^[0-9a-f]{8}-?[0-9a-f]{4}-?[0-5][0-9a-f]{3}-?[089ab][0-9a-f]{3}-?[0-9a-f]{12}$")
    return uuid.trim { it <= ' ' }.length > 31 && pattern.matcher(uuid).matches()
}

private lateinit var localizations: List<String>
fun getLocalizations(): List<String> {
    if (::localizations.isInitialized) {
        return localizations
    }
    val localizationsList = mutableListOf<String>()
    val input = ClassLoader.getSystemResourceAsStream("localizations.txt")
    if (input != null) {
        val bufferedReader = BufferedReader(InputStreamReader(input))
        try {
            bufferedReader.readLines().forEach { line ->
                localizationsList.addAll(line.split(","))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bufferedReader.close()
        }
    }
    localizations = localizationsList.toList()
    return localizations
}

private lateinit var iconSizes: List<String>
fun getIconSizes(): List<String> {
    if (::iconSizes.isInitialized) {
        return iconSizes
    }
    val iconSizeList = mutableListOf<String>()
    val input = ClassLoader.getSystemResourceAsStream("iconSizes.txt")
    if (input != null) {
        val bufferedReader = BufferedReader(InputStreamReader(input))
        try {
            bufferedReader.readLines().forEach { line ->
                iconSizeList.addAll(line.split(","))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bufferedReader.close()
        }
    }
    iconSizes = iconSizeList.toList()
    return iconSizes
}

fun isValidJson(input: String): Boolean {
    val mapper = ObjectMapper()
    return try {
        mapper.readValue(input, object : TypeReference<Map<*, *>>() {})
        true
    } catch (exception: Exception) {
        false
    }
}

/**
 * Normalizes a JSON argument string that may have been mangled by Windows CMD.
 *
 * Windows CMD strips inner double-quotes from a double-quoted argument, e.g.:
 *   CMD input : --config-data "{"test":"value"}"
 *   JVM receives: {test:value}
 *
 * This function attempts to restore the original JSON.
 * LIMITATION: If a value contains spaces, CMD splits it into separate arguments
 * before the JVM receives anything. In that case use the ""key"":""value"" format.
 *
 * Windows path handling: bare values recovered from CMD-mangled JSON may contain
 * backslashes (e.g. D:\path\icon.png). These are normalized to forward slashes
 * so the reconstructed JSON string is valid (backslashes require escaping in JSON).
 *
 * @throws IllegalArgumentException if the string cannot be recovered as valid JSON
 */
fun sanitizeJsonString(input: String): String {
    var s = input.trim()

    // Strip surrounding single quotes (some shells pass them literally)
    if (s.length >= 2 && s.startsWith("'") && s.endsWith("'")) {
        s = s.substring(1, s.length - 1)
    }

    val mapper = ObjectMapper()

    // Fast path: already valid standard JSON
    try {
        mapper.readTree(s)
        return s
    } catch (_: Exception) { }

    // Lenient parse: handles unquoted field names and single-quoted strings
    val lenientMapper = ObjectMapper()
        .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
    try {
        return mapper.writeValueAsString(lenientMapper.readTree(s))
    } catch (_: Exception) { }

    // Last resort: restore quotes to bare {key:value} form produced by CMD.
    // Only works when values contain no spaces (CMD splits on spaces before JVM receives them).
    // Backslashes in values (Windows paths) are normalized to forward slashes before quoting.
    try {
        val normalized = normalizeUnquotedJson(s)
        mapper.readTree(normalized)
        return normalized
    } catch (_: Exception) { }

    throw IllegalArgumentException(
        "Invalid JSON format. " +
        "On Windows CMD, inner double-quotes are stripped. " +
        "Use escaped quotes: \"{\\\"key\\\":\\\"value\\\"}\" " +
        "or double-double-quotes: {\"\"key\"\":\"\"value\"\"}. " +
        "Received: $input"
    )
}

// Restores {key:value,...} into {"key":"value",...}.
// Preserves JSON primitive types (numbers, booleans, null) and only quotes string-like values.
// Backslashes inside string values are normalized to forward slashes (Windows paths).
private fun normalizeUnquotedJson(input: String): String {
    var s = input.trim()
    // Quote bare keys: {key: or ,key:
    s = s.replace(Regex("""([{,]\s*)([^"'\s{}\[\]:,]+)(\s*:)""")) { m ->
        "${m.groupValues[1]}\"${m.groupValues[2].trim()}\"${m.groupValues[3]}"
    }
    // Process bare values: preserve primitives, quote string-like values, normalize backslashes
    s = s.replace(Regex("""(:\s*)([^"'\s{}\[\],}][^{}\[\],}]*)(\s*[,}])""")) { m ->
        val bareValue = m.groupValues[2].trim()
        val quotedValue = if (isPrimitiveType(bareValue)) {
            // Preserve JSON primitives: numbers, booleans, null
            bareValue
        } else {
            // Quote string-like values and normalize backslashes (Windows paths)
            "\"${bareValue.replace("\\", "/")}\""
        }
        "${m.groupValues[1]}$quotedValue${m.groupValues[3]}"
    }
    return s
}

// Determines if a bare token represents a JSON primitive type.
// Returns true for: numbers, booleans (true/false), null.
// Returns false for string-like values (paths, file names, etc.).
private fun isPrimitiveType(value: String): Boolean {
    return when (value.lowercase()) {
        "true", "false", "null" -> true
        else -> {
            // Check if it's a valid JSON number (integer or decimal)
            value.matches(Regex("""^-?(?:0|[1-9]\d*)(?:\.\d+)?(?:[eE][+-]?\d+)?$"""))
        }
    }
}

fun checkIconFileExtension(input: File): Boolean {
    val iconExtensions: List<String> = listOf(FILE_EXTENSION_PNG, FILE_EXTENSION_JPEG, FILE_EXTENSION_JPG)
    var ret = false
    for (extension in iconExtensions) {
        if (input.extension.equals(extension, true)) {
            ret = true
            break
        }
    }
    return ret
}

fun getIconFileType(input: Path): String {
    if (input.extension == FILE_EXTENSION_PNG) {
        return ICON_TYPE_PNG
    } else if (input.extension == FILE_EXTENSION_JPEG || input.extension == FILE_EXTENSION_JPG) {
        return ICON_TYPE_JPG
    }
    throw IllegalArgumentException("$EXCEPTION_UNSUPPORTED_IMAGE_TYPE:$input")
}

fun deleteTempDirectory() {
    val tempDir: File = Path.of(Paths.get("").absolutePathString(), DIR_TEMP).toFile()
    if (tempDir.exists()) {
        if (tempDir.isDirectory) {
            tempDir.deleteRecursively()
        } else {
            tempDir.delete()
        }
    }
}
