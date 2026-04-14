package content.minigame.pvp_demo

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class PvpDemoPortal : Script {

    companion object {
        private const val MAIN = 0
        private const val PVP = 1
        private const val PVM = 2
        private const val SKILLING = 3
        private const val MINIGAMES = 4
    }

    init {
        objectOperate("Enter", "clan_wars_challenge_portal") {
            openMenu(MAIN)
        }
    }

    private suspend fun Player.openMenu(page: Int) {
        when (page) {
            MAIN -> openMainMenu()
            PVP -> openPvp()
            PVM -> openPvm()
            SKILLING -> openSkilling()
            MINIGAMES -> openMinigames()
        }
    }

    private suspend fun Player.openMainMenu() = choice("Quick Teleports") {
        option("PvP") { openMenu(PVP) }
        option("PvM (Boss Lairs)") { openMenu(PVM) }
        option("Skilling") { openMenu(SKILLING) }
        option("Minigames") { openMenu(MINIGAMES) }
        option("Cancel")
    }

    private suspend fun Player.openPvp() = choice("Quick Teleports - PvP") {
        option("Back") { openMenu(MAIN) }
        option("Mage Bank") { teleportTo(3099, 3960, 0, "Mage Bank.") }
        option("Edgeville Ditch") { teleportTo(3092, 3519, 0, "Edgeville ditch.") }
        option("1v1 Volcano") { teleportTo(3366, 3936, 0, "1v1 Volcano.") }
        option("Duel Arena") { teleportTo(3351, 3254, 0, "Duel Arena.") }
    }

    private suspend fun Player.openPvm() = choice("Quick Teleports - PvM") {
        option("Back") { openMenu(MAIN) }
        option("Godwars Dungeon") { teleportTo(2915, 3747, 0, "Godwars Dungeon.") }
        option("Corporeal Beast Lair") { teleportTo(3209, 3780, 0, "Corporeal Beast Lair.") }
        option("Barrows") { teleportTo(3565, 3314, 0, "Barrows.") }
        option("Nex Chamber") { teleportTo(2913, 5217, 0, "Nex Chamber.") }
    }

    private suspend fun Player.openSkilling() = choice("Quick Teleports - Skilling") {
        option("Back") { openMenu(MAIN) }
        option("Fishing Guild") { teleportTo(2581, 3403, 0, "Fishing Guild.") }
        option("Mining Guild") { teleportTo(3026, 9344, 0, "Mining Guild.") }
        option("Crafting Guild") { teleportTo(2883, 3370, 0, "Crafting Guild.") }
        option("Gnome Agility Course") { teleportTo(2480, 3422, 0, "Gnome Agility Course.") }
    }

    private suspend fun Player.openMinigames() = choice("Quick Teleports - Minigames") {
        option("Back") { openMenu(MAIN) }
        option("Fight Caves") { teleportTo(2440, 5171, 0, "Fight Caves.") }
        option("Castle Wars") { teleportTo(2439, 3091, 0, "Castle Wars.") }
        option("Sorceress Garden") { teleportTo(3575, 3793, 0, "Sorceress Garden.") }
        option("Barbarian Assault") { teleportTo(2538, 9968, 0, "Barbarian Assault.") }
    }

    private fun Player.teleportTo(x: Int, y: Int, z: Int, locationName: String) {
        tele(Tile(x, y, z))
        message("Teleported to $locationName")
    }
}
