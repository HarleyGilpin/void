package content.bot.behaviour.action

import content.bot.Bot
import content.bot.FakeBehaviour
import content.bot.FakeWorld
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.condition.BotHasClock
import content.entity.effect.movementDelay
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.config.data.InventoryDefinition
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.restrict.ValidItemRestriction
import world.gregs.voidps.engine.inv.stack.ItemDependentStack
import world.gregs.voidps.network.client.instruction.InteractFloorItem

class BotReactiveLootTest {

    private lateinit var bot: Bot
    private lateinit var player: Player

    @BeforeEach
    fun setup() {
        FloorItems.clear()
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
    }

    @Test
    fun `Picks up survival-eligible drop`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, cost = 10, options = arrayOf("Eat"), floorOptions = arrayOf("Take"), stackable = 0)),
            mapOf("shark" to 0),
        )
        FloorItems.add(player.tile, "shark", 1, owner = player.accountName)
        FloorItems.run()

        var captured: Any? = null
        val world = FakeWorld(execute = { _, instruction ->
            captured = instruction
            true
        })

        val state = BotReactiveLoot().update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertTrue(captured is InteractFloorItem)
        assertEquals(BehaviourState.Wait(1, BehaviourState.Running), state)
    }

    @Test
    fun `Skips items owned by another player`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, cost = 10, options = arrayOf("Eat"), floorOptions = arrayOf("Take"), stackable = 0)),
            mapOf("shark" to 0),
        )
        FloorItems.add(player.tile, "shark", 1, owner = "someone_else")
        FloorItems.run()

        var called = false
        val world = FakeWorld(execute = { _, instruction ->
            if (instruction is InteractFloorItem) called = true
            true
        })

        val state = BotReactiveLoot().update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertFalse(called)
        assertNull(state)
    }

    @Test
    fun `Frozen bot does not loot`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, cost = 10, options = arrayOf("Eat"), floorOptions = arrayOf("Take"), stackable = 0)),
            mapOf("shark" to 0),
        )
        FloorItems.add(player.tile, "shark", 1, owner = player.accountName)
        FloorItems.run()
        player.movementDelay = 5

        var called = false
        val world = FakeWorld(execute = { _, instruction ->
            if (instruction is InteractFloorItem) called = true
            true
        })

        val state = BotReactiveLoot().update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertFalse(called)
        assertNull(state)
    }

    @Test
    fun `Condition gate fails closed`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, cost = 10, options = arrayOf("Eat"), floorOptions = arrayOf("Take"), stackable = 0)),
            mapOf("shark" to 0),
        )
        FloorItems.add(player.tile, "shark", 1, owner = player.accountName)
        FloorItems.run()

        val state = BotReactiveLoot(condition = BotHasClock("missing")).update(bot, FakeWorld(), BehaviourFrame(FakeBehaviour()))

        assertNull(state)
    }

    @Test
    fun `Skips when inventory has no space`() {
        ItemDefinitions.set(
            arrayOf(
                ItemDefinition(id = 100, cost = 10, options = arrayOf("Eat"), floorOptions = arrayOf("Take"), stackable = 0),
                ItemDefinition(id = 101, cost = 1, stackable = 0),
            ),
            mapOf("shark" to 0, "filler" to 1),
        )
        FloorItems.add(player.tile, "shark", 1, owner = player.accountName)
        FloorItems.run()
        // Fill inventory (length 4 from setup).
        repeat(4) { player.inventory.add("filler") }

        var called = false
        val world = FakeWorld(execute = { _, instruction ->
            if (instruction is InteractFloorItem) called = true
            true
        })

        val state = BotReactiveLoot().update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertFalse(called)
        assertNull(state)
    }

    @Test
    fun `Skips items below cost threshold`() {
        ItemDefinitions.set(
            arrayOf(ItemDefinition(id = 100, cost = 0, options = arrayOf("Eat"), floorOptions = arrayOf("Take"), stackable = 0)),
            mapOf("worthless" to 0),
        )
        FloorItems.add(player.tile, "worthless", 1, owner = player.accountName)
        FloorItems.run()

        var called = false
        val world = FakeWorld(execute = { _, instruction ->
            if (instruction is InteractFloorItem) called = true
            true
        })

        val state = BotReactiveLoot().update(bot, world, BehaviourFrame(FakeBehaviour()))

        assertFalse(called)
        assertNull(state)
    }
}
