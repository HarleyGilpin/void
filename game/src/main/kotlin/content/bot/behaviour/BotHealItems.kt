package content.bot.behaviour

import world.gregs.voidps.engine.data.definition.Tables

/**
 * Cached view over the `bot_heal_items` cache table. Used by `BotReactiveEat`
 * to pick which inventory item to consume, and by perception (e.g. healer
 * role detection) to count restocking capacity.
 */
object BotHealItems {
    data class Entry(val pattern: String, val option: String, val heal: Int)

    private var cache: List<Entry>? = null

    fun entries(): List<Entry> {
        cache?.let { return it }
        val table = Tables.getOrNull("bot_heal_items") ?: return emptyList()
        val list = table.rows().map { row ->
            Entry(row.string("item"), row.string("option"), row.int("heal"))
        }
        cache = list
        return list
    }

    fun reset() {
        cache = null
    }

    fun setForTest(entries: List<Entry>) {
        cache = entries
    }
}
