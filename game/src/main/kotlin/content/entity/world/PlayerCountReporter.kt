package content.entity.world

import com.github.michaelbull.logging.InlineLogger
import content.social.friend.world
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Stores the online (non-bot) player count every minute for external consumers e.g. the website.
 */
class PlayerCountReporter(val storage: Storage) : Script {
    val logger = InlineLogger()
    val scope = CoroutineScope(Dispatchers.IO)

    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Error saving player count!" }
    }

    init {
        worldSpawn {
            World.timers.start("player_count_report")
        }

        worldTimerStart("player_count_report") { TimeUnit.SECONDS.toTicks(60) }
        worldTimerTick("player_count_report") {
            val count = Players.count { !it.contains("bot") }
            scope.launch(handler) {
                storage.savePlayerCount(Settings.world, count)
            }
            Timer.CONTINUE
        }
    }
}
