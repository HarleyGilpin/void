package world.gregs.voidps.tools.photobooth

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.storage.DatabaseStorage
import world.gregs.voidps.tools.avatar.PhotoBooth
import world.gregs.voidps.tools.avatar.PhotoSnapshot
import world.gregs.voidps.tools.avatar.SnapshotRepository
import java.awt.image.BufferedImage
import java.io.File

/**
 * Batch-renders photo booth avatars against the live player store. The rendering itself belongs to
 * [PhotoBooth] in `:tools:avatar`; this is only the CLI that picks a [Storage] for it, because
 * `:tools:avatar` is deliberately database-free (`:web:server` and so `:game` depend on it, and the
 * server only links `:database` under `-PincludeDb`).
 *
 * For each player it writes two transparent PNGs, mirroring Jagex's avatar service:
 * `<key>_full.png` (full body) and `<key>_chat.png` (chathead).
 *
 * `<key>` is the account's database id (e.g. `4242_chat.png`). Names are not safe as file keys:
 * "a b", "a_b" and "A-B" are three different accounts that all sanitise to `a_b`, so one player's
 * avatar could be pushed as another's. On file storage (no ids) the sanitised name is still used.
 *
 * Modes (one required):
 *  --player=<accountName>                           render one player's stored snapshot
 *  --players=<name,name,...>                        render several players
 *  --all-dirty                                      render every player flagged photo_booth_dirty
 *
 * Options: --out=<dir> (default ./data/avatars), --size=<px> (default 192), --yaw=<deg>/--pitch=<deg>
 *  (chathead angle), --chat-anim=<id>/--chat-frame=<n> (chathead expression, default 9807 frame 0).
 * Database modes require storage.database.* in game.properties.
 * For a database-free render of a literal snapshot use :tools:avatar:renderPhotoBooth --snapshot=.
 *
 * Gradle: ./gradlew :tools:renderPhotoBooth -Pargs="--player=name --out=/tmp/avatar"
 */
object PhotoBoothRenderer {

    @JvmStatic
    fun main(args: Array<String>) {
        val outDir = File(args.value("--out") ?: "./data/avatars").apply { mkdirs() }
        val size = args.value("--size")?.toIntOrNull() ?: 192

        Settings.load()
        val cache = CacheDelegate(Settings["storage.cache.path"])
        // ItemDecoder (not Full) drives ItemDefinitions and assigns the same sequential equipIndex the
        // game uses, so the equipIndex->itemId map is guaranteed to match captured snapshots.
        ItemDefinitions.init(ItemDecoder().load(cache)).load(configFiles().list(Settings["definitions.items"]))
        val booth = PhotoBooth(
            cache,
            chatAnimation = args.value("--chat-anim")?.toIntOrNull() ?: PhotoBooth.DEFAULT_CHAT_ANIMATION,
            chatFrame = args.value("--chat-frame")?.toIntOrNull() ?: 0,
            chatYaw = args.value("--yaw")?.toDoubleOrNull() ?: -25.0,
            chatPitch = args.value("--pitch")?.toDoubleOrNull() ?: 8.0,
        )

        fun writePng(image: BufferedImage?, type: String, name: String, key: String) {
            if (image == null) return
            val file = File(outDir, "${key}_$type.png")
            PhotoBooth.write(image, file)
            println("  ✓ $name [$type] -> ${file.path}")
        }

        fun render(snapshot: PhotoSnapshot, name: String, key: String) {
            val avatar = booth.render(snapshot, size)
            if (avatar == null) {
                println("  ! $name: no renderable model (empty snapshot?)")
                return
            }
            writePng(avatar.full, PhotoBooth.FULL, name, key)
            writePng(avatar.chat, PhotoBooth.CHAT, name, key)
        }

        when {
            args.value("--player") != null -> withStorage { repo ->
                // '+' stands in for spaces so account names survive -Pargs space-splitting.
                val name = args.value("--player")!!.replace('+', ' ')
                val snapshot = repo.load(name)
                if (snapshot == null) println("No snapshot for $name") else render(snapshot, name, fileKey(repo, name))
            }
            args.value("--players") != null -> withStorage { repo ->
                args.value("--players")!!.split(",").map { it.trim().replace('+', ' ') }.filter { it.isNotEmpty() }.forEach { name ->
                    val snapshot = repo.load(name)
                    if (snapshot == null) println("  - $name: no snapshot") else render(snapshot, name, fileKey(repo, name))
                }
            }
            args.contains("--all-dirty") -> withStorage { repo ->
                // Ask storage which accounts are flagged rather than loading every player to check
                // one variable - the full scan cost minutes against a live world.
                val names = repo.dirtyNames()
                println("${names.size} account(s) flagged photo_booth_dirty")
                var rendered = 0
                for (name in names) {
                    val snapshot = repo.load(name) ?: continue
                    render(snapshot, name, fileKey(repo, name))
                    rendered++
                }
                println("Rendered $rendered dirty avatar(s)")
            }
            else -> println("No mode given. Use --player=, --players= or --all-dirty. See file header.")
        }
    }

    private inline fun withStorage(block: (SnapshotRepository) -> Unit) {
        val storage: Storage = if (Settings["storage.type", "files"] == "database") {
            // Reads only, against a database the server owns - see connectWithoutSchema.
            DatabaseStorage.connectWithoutSchema(
                Settings["storage.database.username"],
                Settings["storage.database.password"],
                Settings["storage.database.driver"],
                Settings["storage.database.jdbcUrl"],
                Settings["storage.database.poolSize", 2],
            )
            DatabaseStorage()
        } else {
            FileStorage(File(Settings["storage.players.path"]))
        }
        block(SnapshotRepository(storage))
    }

    private fun sanitize(name: String): String = name.lowercase().replace(Regex("[^a-z0-9_-]"), "_")

    /** Account id when storage knows one (database), else the legacy sanitised name (file storage). */
    private fun fileKey(repo: SnapshotRepository, name: String): String = repo.accountId(name)?.toString() ?: sanitize(name)

    private fun Array<String>.value(prefix: String): String? = firstOrNull { it.startsWith("$prefix=") }?.substringAfter("=")
}
