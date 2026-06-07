package world.gregs.voidps.tools.convert

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.jetbrains.exposed.sql.transactions.transaction
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.InventoryDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.InventoryDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.exchange.ExchangeOffer
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.storage.DatabaseStorage
import world.gregs.voidps.type.Tile
import java.io.File

object RsmodSaveMigrator {

    private const val DEFAULT_SOURCE = "/home/pixel/Documents/Projects/game/data/saves/"
    private const val DEFAULT_ITEMS_YML = "/home/pixel/Documents/Projects/game/data/cfg/items.yml"
    private const val BATCH_SIZE = 200

    @JvmStatic
    fun main(args: Array<String>) {
        val limit = args.argInt("--limit") ?: Int.MAX_VALUE
        val sourceDir = File(args.argString("--source") ?: DEFAULT_SOURCE)
        val itemsYml = File(args.argString("--items-yml") ?: DEFAULT_ITEMS_YML)
        val dryRun = args.contains("--dry-run")
        val skipPlayed = args.contains("--skip-played")

        require(sourceDir.isDirectory) { "rsmod saves dir not found: $sourceDir" }
        require(itemsYml.isFile) { "rsmod items.yml not found: $itemsYml" }

        Settings.load()
        val cache = CacheDelegate(Settings["storage.cache.path"])
        val files = configFiles()

        ItemDefinitions.init(ItemDecoder().load(cache))
            .load(files.list(Settings["definitions.items"]))
        InventoryDefinitions.init(InventoryDecoder().load(cache))
            .load(files.list(Settings["definitions.inventories"]), files.list(Settings["definitions.shops"]))

        val needsDb = !dryRun || skipPlayed
        val storage: Storage? = if (needsDb) {
            DatabaseStorage.connect(
                Settings["storage.database.username"],
                Settings["storage.database.password"],
                Settings["storage.database.driver"],
                Settings["storage.database.jdbcUrl"],
                Settings["storage.database.poolSize", 2],
            )
            if (dryRun) println("[dry-run] DB connected for reads only; no rows will be written")
            DatabaseStorage()
        } else {
            println("[dry-run] DB connect skipped; no rows will be written")
            null
        }

        val playedLowercase: Set<String> = if (skipPlayed) loadBcryptNames() else emptySet()
        if (skipPlayed) println("skip-played: ${playedLowercase.size} bcrypt accounts will be preserved")

        val itemMap = buildItemMap(itemsYml)

        val jsonMapper = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        val droppedItems = Object2IntOpenHashMap<Int>()
        droppedItems.defaultReturnValue(0)
        val parseErrors = mutableListOf<String>()
        val skippedEmpty = mutableListOf<String>()
        val batch = ArrayList<PlayerSave>(BATCH_SIZE)
        var processed = 0
        var skippedPlayed = 0

        val saveFiles = sourceDir.listFiles()
            ?.filter { it.isFile }
            ?.sortedBy { it.name }
            ?.take(limit)
            ?: emptyList()

        for (file in saveFiles) {
            try {
                val raw: RsmodSave = jsonMapper.readValue(file)
                if (raw.username.isBlank()) {
                    skippedEmpty.add(file.name)
                    continue
                }
                if (skipPlayed && raw.username.lowercase() in playedLowercase) {
                    skippedPlayed++
                    continue
                }
                batch.add(raw.toPlayerSave(itemMap, droppedItems))
                if (batch.size >= BATCH_SIZE) {
                    storage?.save(batch)
                    processed += batch.size
                    batch.clear()
                    println("processed=$processed")
                }
            } catch (e: Exception) {
                parseErrors.add("${file.name}: ${e.javaClass.simpleName}: ${e.message}")
            }
        }
        if (batch.isNotEmpty()) {
            storage?.save(batch)
            processed += batch.size
        }

        println()
        println("=== Migration complete ===")
        println("processed=$processed skipped_played=$skippedPlayed parse_errors=${parseErrors.size} skipped_empty=${skippedEmpty.size}")
        if (droppedItems.isNotEmpty()) {
            println("--- top 50 dropped rsmod item ids (no void match) ---")
            droppedItems.entries
                .sortedByDescending { it.value }
                .take(50)
                .forEach { (rsmodId, count) -> println("  $rsmodId: $count occurrences") }
        }
        if (parseErrors.isNotEmpty()) {
            println("--- first 20 parse errors ---")
            parseErrors.take(20).forEach { println("  $it") }
        }
    }

    private fun loadBcryptNames(): Set<String> = transaction {
        val names = HashSet<String>()
        exec("SELECT LOWER(name) FROM accounts WHERE password_hash LIKE '\$2%'") { rs ->
            while (rs.next()) names.add(rs.getString(1))
        }
        names
    }

    private fun buildItemMap(itemsYml: File): Int2ObjectOpenHashMap<String> {
        val loaderOptions = org.yaml.snakeyaml.LoaderOptions().apply {
            codePointLimit = 64 * 1024 * 1024
        }
        val yamlFactory = YAMLFactory.builder().loaderOptions(loaderOptions).build()
        val yamlMapper = YAMLMapper(yamlFactory).registerKotlinModule().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        val rsmodItems: List<RsmodItemYml> = yamlMapper.readValue(itemsYml)

        val unnotedByName = HashMap<String, Int>(20_000)
        val notedByName = HashMap<String, Int>(20_000)
        for (def in ItemDefinitions.definitions) {
            if (def.stringId.isEmpty() || def.name == "null") continue
            val key = normalize(def.name)
            if (key.isEmpty()) continue
            val bucket = if (def.notedTemplateId != -1) notedByName else unnotedByName
            val existing = bucket[key]
            if (existing == null || def.id < existing) {
                bucket[key] = def.id
            }
        }

        val result = Int2ObjectOpenHashMap<String>(rsmodItems.size)
        var matchedById = 0
        var matchedExactBucket = 0
        var matchedFallbackBucket = 0
        var unmatched = 0
        val voidDefs = ItemDefinitions.definitions
        for (item in rsmodItems) {
            val key = normalize(item.name)
            if (key.isEmpty()) {
                unmatched++
                continue
            }
            val directDef = voidDefs.getOrNull(item.id)
            if (directDef != null && directDef.stringId.isNotEmpty() && normalize(directDef.name) == key) {
                result[item.id] = directDef.stringId
                matchedById++
                continue
            }
            val noted = item.isNoted()
            val primary = if (noted) notedByName[key] else unnotedByName[key]
            val fallback = if (noted) unnotedByName[key] else notedByName[key]
            val voidId = primary ?: fallback
            if (voidId == null) {
                unmatched++
                continue
            }
            val stringId = voidDefs[voidId].stringId
            if (stringId.isEmpty()) {
                unmatched++
                continue
            }
            result[item.id] = stringId
            if (primary != null) matchedExactBucket++ else matchedFallbackBucket++
        }
        println("item map: rsmod_entries=${rsmodItems.size} by_id=$matchedById exact_bucket=$matchedExactBucket fallback_bucket=$matchedFallbackBucket unmatched=$unmatched")
        return result
    }

    private fun normalize(name: String): String =
        name.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')

    private fun RsmodSave.toPlayerSave(
        itemMap: Int2ObjectOpenHashMap<String>,
        dropped: Object2IntOpenHashMap<Int>,
    ): PlayerSave {
        val xp = IntArray(25)
        val lvls = IntArray(25)
        for (s in skills) {
            if (s.skill in 0..24) {
                xp[s.skill] = (s.xp * 10.0).toInt().coerceAtLeast(0)
                lvls[s.skill] = s.lvl.coerceAtLeast(1)
            }
        }
        for (i in lvls.indices) {
            if (lvls[i] == 0) lvls[i] = if (i == 3) 10 else 1
        }

        val looksArr = IntArray(7) { i -> appearance.looks.getOrNull(i) ?: 0 }
        val coloursArr = IntArray(5) { i -> appearance.colors.getOrNull(i) ?: 0 }

        val inventories = HashMap<String, Array<Item>>(3)
        for (container in itemContainers) {
            val voidName = when (container.name) {
                "inventory" -> "inventory"
                "equipment" -> "worn_equipment"
                "bank" -> "bank"
                else -> continue
            }
            inventories[voidName] = buildContainer(voidName, container, itemMap, dropped)
        }

        val vars = HashMap<String, Any>(2)
        if (displayName.isNotBlank() && displayName != username) {
            vars["display_name"] = displayName
        }

        return PlayerSave(
            name = username,
            password = passwordHash,
            tile = Tile(x, z, height),
            experience = xp,
            blocked = emptyList(),
            levels = lvls,
            male = appearance.gender == 0,
            looks = looksArr,
            colours = coloursArr,
            variables = vars,
            inventories = inventories,
            friends = emptyMap(),
            ignores = emptyList(),
            offers = Array(6) { ExchangeOffer.EMPTY },
            history = emptyList(),
        )
    }

    private fun buildContainer(
        voidName: String,
        container: RsmodContainer,
        itemMap: Int2ObjectOpenHashMap<String>,
        dropped: Object2IntOpenHashMap<Int>,
    ): Array<Item> {
        val def = InventoryDefinitions.get(voidName)
        val maxRsmodSlot = container.items.keys.mapNotNull { it.toIntOrNull() }.maxOrNull() ?: -1
        val capacity = maxOf(def.length, maxRsmodSlot + 1, 1)
        val arr = Array<Item>(capacity) { Item.EMPTY }
        // The bank must stay contiguous - the client's tab counts assume no gaps, so
        // dropped items would hide everything after them until a deposit re-shifts.
        // Inventory/equipment keep their original slots.
        val compact = voidName == "bank"
        var nextSlot = 0
        for ((slotStr, item) in container.items.toSortedMap(compareBy { it.toIntOrNull() ?: Int.MAX_VALUE })) {
            val slot = slotStr.toIntOrNull() ?: continue
            if (slot < 0 || slot >= capacity) continue
            val stringId = itemMap.get(item.id)
            if (stringId == null) {
                dropped.addTo(item.id, 1)
                continue
            }
            arr[if (compact) nextSlot++ else slot] = Item(stringId, item.amount)
        }
        return arr
    }

    private fun Array<String>.argInt(prefix: String): Int? =
        firstOrNull { it.startsWith("$prefix=") }?.substringAfter("=")?.toIntOrNull()

    private fun Array<String>.argString(prefix: String): String? =
        firstOrNull { it.startsWith("$prefix=") }?.substringAfter("=")

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class RsmodSave(
        val username: String = "",
        val displayName: String = "",
        val passwordHash: String = "",
        val x: Int = 3200,
        val z: Int = 3200,
        val height: Int = 0,
        val appearance: RsmodAppearance = RsmodAppearance(),
        val skills: List<RsmodSkill> = emptyList(),
        val itemContainers: List<RsmodContainer> = emptyList(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class RsmodAppearance(
        val gender: Int = 0,
        val looks: List<Int> = emptyList(),
        val colors: List<Int> = emptyList(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class RsmodSkill(
        val skill: Int = 0,
        val xp: Double = 0.0,
        val lvl: Int = 1,
        val lastLvl: Int = 1,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class RsmodContainer(
        val name: String = "",
        val items: Map<String, RsmodItem> = emptyMap(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class RsmodItem(
        val id: Int = -1,
        val amount: Int = 1,
        val attr: Map<String, Int> = emptyMap(),
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class RsmodItemYml(
        val id: Int = -1,
        val name: String = "",
        val examine: String = "",
    ) {
        fun isNoted(): Boolean = examine.contains("this note at any bank", ignoreCase = true)
    }
}
