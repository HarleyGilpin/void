package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotHealItems
import content.bot.behaviour.condition.BotHasClock
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.client.instruction.InteractInterface

class BotReactiveEatTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        player = Player()
        bot = Bot(player)
        player.experience.player = player
        player.levels.link(player, PlayerLevels(player.experience))
        player.inventories.validItemRule = ValidItemRestriction()
        player.inventories.player = player
        player.inventories.normalStack = ItemDependentStack
        player.inventories.inventory(InventoryDefinition(stringId = "inventory", length = 4))
    }

    @AfterEach
    fun teardown() {
        ItemDefinitions.clear()
        BotHealItems.reset()
    }

    @Test
    fun `Eats matching food when condition passes`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, stringId = "shark", options = arrayOf("Eat"))),
            mapOf("shark" to 0),
        )
        BotHealItems.setForTest(listOf(BotHealItems.Entry("shark", "Eat", 200)))
        player.inventory.add("shark")
        player.start("low_hp", 10)

        var captured: Any? = null
        val world = FakeWorld(execute = { _, instruction ->
            captured = instruction
            true
        })

        val state = BotReactiveEat(BotHasClock("low_hp")).update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(captured is InteractInterface)
        assertEquals(BehaviourState.Wait(1, BehaviourState.Running), state)
        assertTrue(player.hasClock("just_ate_food"))
    }

    @Test
    fun `Drinks heal potion using Drink option`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 200, stringId = "saradomin_brew_4", options = arrayOf("Drink"))),
            mapOf("saradomin_brew_4" to 0),
        )
        BotHealItems.setForTest(listOf(BotHealItems.Entry("saradomin_brew_*", "Drink", 160)))
        player.inventory.add("saradomin_brew_4")

        var captured: Any? = null
        val world = FakeWorld(execute = { _, instruction ->
            captured = instruction
            true
        })

        val state = BotReactiveEat().update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(captured is InteractInterface)
        assertEquals(BehaviourState.Wait(1, BehaviourState.Running), state)
        // Drink path must NOT set just_ate_food (that gates the brew→food chain).
        assertTrue(!player.hasClock("just_ate_food"))
    }

    @Test
    fun `Skips when condition fails`() {
        BotHealItems.setForTest(listOf(BotHealItems.Entry("shark", "Eat", 200)))
        val state = BotReactiveEat(BotHasClock("missing")).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))
        assertNull(state)
    }

    @Test
    fun `Returns null when no heal items configured`() {
        BotHealItems.setForTest(emptyList())
        val state = BotReactiveEat().update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))
        assertNull(state)
    }
}
