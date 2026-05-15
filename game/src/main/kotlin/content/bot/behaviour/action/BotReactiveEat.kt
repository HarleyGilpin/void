package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotHealItems
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterface

/**
 * Reactive heal: consumes the first inventory item that matches a row in the
 * `bot_heal_items` cache table. Gated by an optional [condition] (typically a
 * `skill_percent` check against `constitution`).
 *
 * Eating sets the `just_ate_food` clock so the existing brew-chain reactives
 * can stack an overheal on the next tick.
 */
data class BotReactiveEat(val condition: Condition? = null) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState? {
        val player = bot.player
        // Server already serialises eat/drink via these clocks; respect them so we don't queue
        // throw-away InteractInterfaces that extend action_delay every tick during a fight
        // (Eating.consume: actionRemaining + attackTicks). Without this, reactive eat lands on
        // top of the main combat action and the bot keeps re-pausing its own attack window.
        if (player.hasClock("food_delay") || player.hasClock("drink_delay") || player.hasClock("combo_delay")) return null
        if (condition != null && !condition.check(player)) return null
        val entries = BotHealItems.entries()
        if (entries.isEmpty()) return null
        val inv = player.inventory
        for (index in inv.indices) {
            val item = inv[index]
            if (item.isEmpty()) continue
            val entry = entries.firstOrNull { wildcardEquals(it.pattern, item.id) } ?: continue
            val option = item.def.options.indexOf(entry.option)
            if (option == -1) continue
            val valid = world.execute(player, InteractInterface(149, 0, item.def.id, index, option))
            if (!valid) {
                return BehaviourState.Failed(Reason.Invalid("Invalid heal interaction: ${item.id} $index $option"))
            }
            if (entry.option == "Eat") player.start("just_ate_food", 2)
            return BehaviourState.Wait(1, BehaviourState.Running)
        }
        return null
    }
}
