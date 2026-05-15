package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import content.entity.effect.frozen
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractFloorItem

/**
 * Reactive scavenger: scans floor items within [radius] tiles and grabs the first one
 * eligible under [strategy] (typically [BotLootStrategy.SURVIVAL] for dropped consumables).
 *
 * Skips while the bot is frozen — pathing to a tile we can't move to just makes the bot
 * look stuck. Skips while a consume window is active or the inventory is full.
 */
data class BotReactiveLoot(
    val radius: Int = 1,
    val strategy: BotLootStrategy = BotLootStrategy.SURVIVAL,
    val condition: Condition? = null,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState? {
        val player = bot.player
        if (player.frozen) return null
        if (condition != null && !condition.check(player)) return null
        val inv = player.inventory
        if (inv.spaces <= 0) return null
        for (tile in Spiral.spiral(player.tile, radius)) {
            for (item in FloorItems.at(tile)) {
                if (!eligible(player.accountName, item)) continue
                if (!strategy.accepts(item)) continue
                val index = item.def.floorOptions.indexOf("Take")
                if (index == -1) continue
                val canStack = item.def.stackable == 1 && inv.indexOf(item.id) >= 0
                if (inv.spaces <= 0 && !canStack) continue
                val valid = world.execute(player, InteractFloorItem(item.def.id, item.tile.x, item.tile.y, index))
                if (!valid) {
                    return BehaviourState.Failed(Reason.Invalid("Invalid loot interaction: $item $index"))
                }
                return BehaviourState.Wait(1, BehaviourState.Running)
            }
        }
        return null
    }

    private fun eligible(accountName: String, item: FloorItem): Boolean {
        val owner = item.owner
        if (owner != null && owner != accountName) return false
        if (item.def.cost <= 0) return false
        return true
    }
}
