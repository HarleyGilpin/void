package content.skill.prayer.bone

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.statement
import content.skill.prayer.PrayerConfigs.PRAYERS
import content.skill.prayer.isCurses
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject

class AltarOfZaros : Script {

    init {
        objectOperate("Pray", "prayer_altar_of_zaros") { (target) ->
            prayAtZaros(target)
        }
    }

    private suspend fun Player.prayAtZaros(target: GameObject) {
        walkToDelay(target.nearestTo(tile))
        arriveDelay()
        face(target)
        anim("altar_pray_zaros", override = true)
        if (levels.getOffset(Skill.Prayer) < 0) {
            levels.set(Skill.Prayer, (levels.getMax(Skill.Prayer) * 1.15).toInt())
            message("You recharge your Prayer points.")
            set("prayer_point_power_task", true)
        } else {
            message("You already have full Prayer points.")
        }
        val curses = isCurses()
        choice("Change from ${if (curses) "curses" else "prayers"}?") {
            option("Yes, replace my prayer book.") {
                if (curses) {
                    statement("The altar eases its grip on your mind. The curses slip from your memory and you recall the prayers you used to know.")
                    this[PRAYERS] = "normal"
                } else {
                    statement("The altar fills your head with dark thoughts, purging the prayers from your memory and leaving only curses in their place.")
                    this[PRAYERS] = "curses"
                }
            }
            option("Nevermind.")
        }
    }
}
