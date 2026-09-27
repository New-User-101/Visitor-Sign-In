package stu.gpt.signing.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.Instant

object Storage {
    private const val HISTORY_FILE = "history.json"
    private const val VISITORS_FILE = "visitors.json"
    private const val REGULAR_LIST_STATE_FILE = "regular_list_state.json"
    private const val REGULAR_LIST_MEMBERS_FILE = "regular_list_members.json"
    private const val QUESTION_LINES_FILE = "question_lines.json"
    private const val NAMES_FILE = "names.json"
    private const val ROLES_FILE = "roles.json"
    private const val LOCATIONS_FILE = "locations.json"

    fun loadHistory(context: Context): List<HistoryEntry> {
        return try {
            val file = File(context.filesDir, HISTORY_FILE)
            if (!file.exists()) return emptyList()
            val text = BufferedReader(FileReader(file)).use { it.readText() }
            val arr = JSONArray(text)
            val list = mutableListOf<HistoryEntry>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    HistoryEntry(
                        name = o.optString("name"),
                        roleOrLocation = o.optString("roleOrLocation"),
                        arrivalEpochMillis = o.optLong("arrivalEpochMillis"),
                        departureEpochMillis = o.optLong("departureEpochMillis", Instant.now().toEpochMilli()),
                        vehicleReg = o.optString("vehicleReg", null as String?)
                    )
                )
            }
            list
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveHistory(context: Context, items: List<HistoryEntry>) {
        try {
            val file = File(context.filesDir, HISTORY_FILE)
            val arr = JSONArray()
            items.forEach { e ->
                val o = JSONObject()
                o.put("name", e.name)
                o.put("roleOrLocation", e.roleOrLocation)
                o.put("arrivalEpochMillis", e.arrivalEpochMillis)
                o.put("departureEpochMillis", e.departureEpochMillis)
                e.vehicleReg?.let { o.put("vehicleReg", it) }
                arr.put(o)
            }
            FileWriter(file, false).use { fw -> fw.write(arr.toString()) }
        } catch (_: Throwable) { }
    }

    fun loadVisitors(context: Context): List<VisitorEntry> {
        return try {
            val file = File(context.filesDir, VISITORS_FILE)
            if (!file.exists()) return emptyList()
            val text = BufferedReader(FileReader(file)).use { it.readText() }
            val arr = JSONArray(text)
            val list = mutableListOf<VisitorEntry>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    VisitorEntry(
                        name = o.optString("name"),
                        roleOrLocation = o.optString("roleOrLocation"),
                        arrivalEpochMillis = o.optLong("arrivalEpochMillis", Instant.now().toEpochMilli()),
                        vehicleReg = o.optString("vehicleReg", null as String?)
                    )
                )
            }
            list
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveVisitors(context: Context, items: List<VisitorEntry>) {
        try {
            val file = File(context.filesDir, VISITORS_FILE)
            val arr = JSONArray()
            items.forEach { e ->
                val o = JSONObject()
                o.put("name", e.name)
                o.put("roleOrLocation", e.roleOrLocation)
                o.put("arrivalEpochMillis", e.arrivalEpochMillis)
                e.vehicleReg?.let { o.put("vehicleReg", it) }
                arr.put(o)
            }
            FileWriter(file, false).use { fw -> fw.write(arr.toString()) }
        } catch (_: Throwable) { }
    }

    data class StageState(
        val active: Map<String, Long>,
        val tappedAt: Map<String, Long>,
        val vehicleReg: Map<String, String>
    )

    fun loadRegularListState(context: Context): StageState {
        return try {
            val regFile = File(context.filesDir, REGULAR_LIST_STATE_FILE)
            if (regFile.exists()) {
                val text = BufferedReader(FileReader(regFile)).use { it.readText() }
                val obj = JSONObject(text)
                val activeObj = obj.optJSONObject("active") ?: JSONObject()
                val tappedObj = obj.optJSONObject("tappedAt") ?: JSONObject()
                val vehicleObj = obj.optJSONObject("vehicleReg") ?: JSONObject()
                val active = mutableMapOf<String, Long>()
                val tapped = mutableMapOf<String, Long>()
                val vehicle = mutableMapOf<String, String>()
                activeObj.keys().forEach { k -> active[k] = activeObj.optLong(k, 0L) }
                tappedObj.keys().forEach { k -> tapped[k] = tappedObj.optLong(k, 0L) }
                vehicleObj.keys().forEach { k -> vehicle[k] = vehicleObj.optString(k, null as String?) ?: "" }
                StageState(active, tapped, vehicle.filterValues { it.isNotBlank() })
            } else {
                // Fallback to old stage_state.json if regular_list_state.json doesn't exist
                val oldFile = File(context.filesDir, "stage_state.json")
                if (oldFile.exists()) {
                    val text = BufferedReader(FileReader(oldFile)).use { it.readText() }
                    val obj = JSONObject(text)
                    val activeObj = obj.optJSONObject("active") ?: JSONObject()
                    val tappedObj = obj.optJSONObject("tappedAt") ?: JSONObject()
                    val active = mutableMapOf<String, Long>()
                    val tapped = mutableMapOf<String, Long>()
                    activeObj.keys().forEach { k -> active[k] = activeObj.optLong(k, 0L) }
                    tappedObj.keys().forEach { k -> tapped[k] = tappedObj.optLong(k, 0L) }
                    StageState(active, tapped, emptyMap())
                } else StageState(emptyMap(), emptyMap(), emptyMap())
            }
        } catch (_: Throwable) {
            StageState(emptyMap(), emptyMap(), emptyMap())
        }
    }

    fun saveRegularListState(context: Context, active: Map<String, Long>, tappedAt: Map<String, Long>, vehicleReg: Map<String, String> = emptyMap()) {
        try {
            val file = File(context.filesDir, REGULAR_LIST_STATE_FILE)
            val obj = JSONObject()
            val activeObj = JSONObject()
            active.forEach { (k, v) -> activeObj.put(k, v) }
            val tappedObj = JSONObject()
            tappedAt.forEach { (k, v) -> tappedObj.put(k, v) }
            val vehicleObj = JSONObject()
            vehicleReg.forEach { (k, v) -> if (v.isNotBlank()) vehicleObj.put(k, v) }
            obj.put("active", activeObj)
            obj.put("tappedAt", tappedObj)
            if (vehicleObj.length() > 0) obj.put("vehicleReg", vehicleObj)
            FileWriter(file, false).use { fw -> fw.write(obj.toString()) }
        } catch (_: Throwable) { }
    }

    fun loadRegularListMembers(context: Context): List<String> {
        return try {
            val regFile = File(context.filesDir, REGULAR_LIST_MEMBERS_FILE)
            val file = if (regFile.exists()) regFile else File(context.filesDir, "stage_members.json")
            if (!file.exists()) return emptyList()
            val text = BufferedReader(FileReader(file)).use { it.readText() }
            val arr = JSONArray(text)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val v = arr.optString(i)
                if (v.isNotBlank()) list.add(v)
            }
            list
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveRegularListMembers(context: Context, members: List<String>) {
        try {
            val file = File(context.filesDir, REGULAR_LIST_MEMBERS_FILE)
            val arr = JSONArray()
            members.forEach { arr.put(it) }
            FileWriter(file, false).use { fw -> fw.write(arr.toString()) }
        } catch (_: Throwable) { }
    }

    fun removeLegacyStageFilesIfMigrated(context: Context) {
        try {
            val regState = File(context.filesDir, REGULAR_LIST_STATE_FILE)
            val regMembers = File(context.filesDir, REGULAR_LIST_MEMBERS_FILE)
            if (regState.exists()) {
                File(context.filesDir, "stage_state.json").takeIf { it.exists() }?.delete()
            }
            if (regMembers.exists()) {
                File(context.filesDir, "stage_members.json").takeIf { it.exists() }?.delete()
            }
        } catch (_: Throwable) { }
    }

    fun loadQuestionLines(context: Context): List<String> {
        return try {
            val file = File(context.filesDir, QUESTION_LINES_FILE)
            val oldFile = File(context.filesDir, "warning_lines.json")
            if (!file.exists() && oldFile.exists()) {
                oldFile.renameTo(file)
            }
            if (!file.exists()) return emptyList()
            val text = BufferedReader(FileReader(file)).use { it.readText() }
            val arr = JSONArray(text)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val v = arr.optString(i)
                if (v.isNotBlank()) list.add(v)
            }
            list
        } catch (_: Throwable) {
            emptyList()
        }
    }

    fun saveQuestionLines(context: Context, lines: List<String>) {
        try {
            val file = File(context.filesDir, QUESTION_LINES_FILE)
            val arr = JSONArray()
            lines.forEach { arr.put(it) }
            FileWriter(file, false).use { fw -> fw.write(arr.toString()) }
        } catch (_: Throwable) { }
    }

    // Generic list persistence helpers
    fun loadNames(context: Context): List<String> = loadSimpleList(context, NAMES_FILE)
    fun saveNames(context: Context, items: List<String>) = saveSimpleList(context, NAMES_FILE, items)
    
    fun loadRoles(context: Context): List<String> = loadSimpleList(context, ROLES_FILE)
    fun saveRoles(context: Context, items: List<String>) = saveSimpleList(context, ROLES_FILE, items)
    
    fun loadLocations(context: Context): List<String> = loadSimpleList(context, LOCATIONS_FILE)
    fun saveLocations(context: Context, items: List<String>) = saveSimpleList(context, LOCATIONS_FILE, items)

    private fun loadSimpleList(context: Context, filename: String): List<String> {
        return try {
            val file = File(context.filesDir, filename)
            if (!file.exists()) return emptyList()
            val text = BufferedReader(FileReader(file)).use { it.readText() }
            val arr = JSONArray(text)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val v = arr.optString(i)
                if (v.isNotBlank()) list.add(v)
            }
            list
        } catch (_: Throwable) {
            emptyList()
        }
    }

    private fun saveSimpleList(context: Context, filename: String, items: List<String>) {
        try {
            val file = File(context.filesDir, filename)
            val arr = JSONArray()
            items.forEach { arr.put(it) }
            FileWriter(file, false).use { fw -> fw.write(arr.toString()) }
        } catch (_: Throwable) { }
    }
}
