package content.entity.player

import content.bot.isBot
import content.entity.player.bank.bank
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue

class Introduction : Script {

    fun welcome(player: Player) {
        player.message("Welcome to ${Settings["server.name"]}.", ChatType.Welcome)
        if (player.contains("creation")) {
            return
        }
        if (Settings["world.start.creation", true] && !player.isBot) {
            player["delay"] = -1
            World.queue("welcome_${player.name}", 1) {
                player.open("character_creation")
            }
        } else {
            player.flagAppearance()
            setup(player)
        }
    }

    init {
        playerSpawn(::welcome)

        interfaceClosed("character_creation") {
            flagAppearance()
            setup(this)
        }
    }

    fun setup(player: Player) {
        player.queue("welcome") {
            player.statement("Welcome to the Void Combat Demo! You've been outfitted with end-game tier equipment and weapons, all stored in the bank chest on the East wall. When you're ready, step through the South Portal for quick teleports to popular PVM and PVP locations.")        }
        player.stop("delay")
        player["creation"] = System.currentTimeMillis()

        if (!Settings["world.setup.gear", true]) {
            return
        }
        player.bank.apply {
            // Tab 1 - Melee weapons (20 items)
            add("abyssal_whip", 999)
            add("dragon_scimitar", 999)
            add("dragon_dagger_p++", 999)
            add("granite_maul", 999)
            add("dragon_claws", 1000)
            add("rune_2h_sword", 999)
            add("korasis_sword", 999)
            add("armadyl_godsword", 999)
            add("bandos_godsword", 999)
            add("saradomin_godsword", 999)
            add("zamorak_godsword", 999)
            add("zamorakian_spear", 999)
            add("chaotic_rapier", 999)
            add("chaotic_longsword", 999)
            add("chaotic_maul", 999)
            add("vestas_longsword", 999)
            add("vestas_spear", 999)
            add("statiuss_warhammer", 999)
            add("morrigans_javelin", 10000000)
            add("morrigans_throwing_axe", 10000000)

            // Tab 2 - Melee armour (63 items)
            // Helmets
            add("fighter_hat", 999)
            add("helm_of_neitiznot", 999)
            add("berserker_helm", 999)
            add("warrior_helm", 999)
            add("torva_full_helm", 999)
            add("statiuss_full_helm", 999)
            add("dragon_full_helm", 999)
            add("rune_full_helm", 999)
            add("dharoks_helm", 999)
            add("torags_helm", 999)
            add("veracs_helm", 999)
            add("initiate_sallet", 999)
            add("proselyte_sallet", 999)
            add("third_age_full_helmet", 999)
            // Void knight gear
            add("void_melee_helm", 999)
            add("void_ranger_helm", 999)
            add("void_mage_helm", 999)
            // Bodies
            add("fighter_torso", 999)
            add("torva_platebody", 999)
            add("bandos_chestplate", 999)
            add("statiuss_platebody", 999)
            add("vestas_chainbody", 999)
            add("dragon_platebody", 999)
            add("rune_platebody", 999)
            add("dharoks_platebody", 999)
            add("torags_platebody", 999)
            add("veracs_brassard", 999)
            add("initiate_hauberk", 999)
            add("proselyte_hauberk", 999)
            add("third_age_platebody", 999)
            add("void_knight_top", 999)
            // Legs
            add("torva_platelegs", 999)
            add("bandos_tassets", 999)
            add("statiuss_platelegs", 999)
            add("vestas_plateskirt", 999)
            add("dragon_platelegs", 999)
            add("rune_platelegs", 999)
            add("dharoks_platelegs", 999)
            add("torags_platelegs", 999)
            add("veracs_plateskirt", 999)
            add("initiate_cuisse", 999)
            add("proselyte_cuisse", 999)
            add("third_age_platelegs", 999)
            add("void_knight_robe", 999)
            // Boots
            add("bandos_boots", 999)
            add("dragon_boots", 999)
            add("rune_boots", 999)
            add("rock_climbing_boots", 999)
            // Gloves
            add("void_knight_gloves", 999)
            // Melee weapons (non-tab-1)
            add("dharoks_greataxe", 999)
            add("torags_hammers", 999)
            add("veracs_flail", 999)
            // Shields
            add("dragon_defender", 999)
            add("chaotic_kiteshield", 999)
            add("rune_kiteshield", 999)
            add("third_age_kiteshield", 999)
            add("dragonfire_shield_charged", 999)
            add("spirit_shield", 999)
            add("blessed_spirit_shield", 999)
            add("arcane_spirit_shield", 999)
            add("divine_spirit_shield", 999)
            add("elysian_spirit_shield", 999)
            add("spectral_spirit_shield", 999)

            // Tab 3 - Ranged (45 items)
            // Ranged weapons
            add("magic_shortbow", 999)
            add("dark_bow", 999)
            add("rune_crossbow", 999)
            add("zaryte_bow", 999)
            add("crystal_bow_full", 999)
            add("chaotic_crossbow", 999)
            add("karils_crossbow", 999)
            // Ammo
            add("rune_arrow", 10000000)
            add("dragon_arrow", 10000000)
            add("red_chinchompa", 10000000)
            add("bronze_bolts", 10000000)
            add("iron_bolts", 10000000)
            add("steel_bolts", 10000000)
            add("mithril_bolts", 10000000)
            add("adamant_bolts", 10000000)
            add("runite_bolts", 10000000)
            add("black_bolts", 10000000)
            add("silver_bolts", 10000000)
            add("ruby_bolts_e", 10000000)
            add("diamond_bolts_e", 10000000)
            add("dragon_bolts_e", 10000000)
            add("onyx_bolts_e", 10000000)
            // Ranged armour - Helmets
            add("pernix_cowl", 999)
            add("morrigans_coif", 999)
            add("karils_coif", 999)
            add("armadyl_helmet", 999)
            add("robin_hood_hat", 999)
            add("archer_helm", 999)
            add("third_age_ranger_coif", 999)
            // Ranged armour - Bodies
            add("pernix_body", 999)
            add("morrigans_leather_body", 999)
            add("karils_top", 999)
            add("armadyl_chestplate", 999)
            add("black_dragonhide_body", 999)
            add("third_age_ranger_body", 999)
            // Ranged armour - Legs
            add("pernix_chaps", 999)
            add("morrigans_leather_chaps", 999)
            add("karils_skirt", 999)
            add("armadyl_chainskirt", 999)
            add("black_dragonhide_chaps", 999)
            add("third_age_ranger_chaps", 999)
            // Ranged accessories
            add("ranger_boots", 999)
            add("avas_accumulator", 999)
            add("third_age_vambraces", 999)
            add("black_dragonhide_vambraces", 999)

            // Tab 4 - Magic (36 items)
            // Magic weapons
            add("ancient_staff", 999)
            add("chaotic_staff", 999)
            add("zuriels_staff", 999)
            add("ahrims_staff", 999)
            // Magic armour - Helmets
            add("virtus_mask", 999)
            add("zuriels_hood", 999)
            add("ahrims_hood", 999)
            add("farseer_helm", 999)
            add("mystic_hat_blue", 999)
            add("splitbark_helm", 999)
            add("third_age_mage_hat", 999)
            // Magic armour - Bodies
            add("virtus_robe_top", 999)
            add("zuriels_robe_top", 999)
            add("ahrims_robe_top", 999)
            add("mystic_robe_top_blue", 999)
            add("splitbark_body", 999)
            add("third_age_robe_top", 999)
            // Magic armour - Legs
            add("virtus_robe_legs", 999)
            add("zuriels_robe_bottom", 999)
            add("ahrims_robe_skirt", 999)
            add("mystic_robe_bottom_blue", 999)
            add("splitbark_legs", 999)
            add("third_age_robe", 999)
            // Magic accessories
            add("mystic_gloves_blue", 999)
            add("mystic_boots_blue", 999)
            add("splitbark_gauntlets", 999)
            add("splitbark_boots", 999)
            // Runes
            add("blood_rune", 10000000)
            add("death_rune", 10000000)
            add("fire_rune", 10000000)
            add("chaos_rune", 10000005)
            add("nature_rune", 10000000)
            add("law_rune", 10000000)
            add("air_rune", 10000000)
            add("water_rune", 10000000)
            add("earth_rune", 10000000)

            // Tab 5 -  accessories & misc gear (21 items)
            // Amulets
            add("amulet_of_fury", 999)
            add("amulet_of_glory_4", 999)
            add("berserker_necklace", 999)
            add("phoenix_necklace", 999)
            add("games_necklace_8", 999)
            add("skills_necklace_4", 999)
            add("arcane_pulse_necklace", 999)
            add("arcane_blast_necklace", 999)
            add("arcane_stream_necklace", 999)
            add("third_age_amulet", 999)
            // Rings
            add("ring_of_duelling_8", 999)
            add("ring_of_wealth", 999)
            add("berserker_ring", 999)
            add("warrior_ring", 999)
            add("archers_ring", 999)
            add("seers_ring", 999)
            add("ring_of_vigour", 999)
            add("ring_of_life", 999)
            add("ring_of_recoil", 999)

            // Gloves
            add("culinaromancers_gloves_10", 999)
            add("combat_bracelet_4", 999)

            // Tab 6 - Capes (8 items)
            add("attack_cape_t", 999)
            add("strength_cape_t", 999)
            add("defence_cape_t", 999)
            add("ranged_cape_t", 999)
            add("magic_cape_t", 999)
            add("prayer_cape_t", 999)
            add("constitution_cape_t", 999)
            add("fire_cape", 999)

            // Tab 7 - Potions & food (11 items)
            add("overload_4", 10000000)
            add("super_prayer_potion_4", 10000000)
            add("super_restore_4", 10000000)
            add("saradomin_brew_4", 10000000)
            add("super_attack_4", 10000000)
            add("super_strength_4", 10000000)
            add("super_defence_4", 10000000)
            add("shark", 10000000)
            add("manta_ray", 10000000)
            add("rocktail", 10000000)
            // Misc
            add("dwarven_rock_cake", 10000000)

            // Main tab (tab 0) - Coins & teleport supplies (8 items)
            add("coins", 10000000)
            add("varrock_teleport", 10000000)
            add("lumbridge_teleport", 10000000)
            add("falador_teleport", 10000000)
            add("camelot_teleport", 10000000)
            add("ardougne_teleport", 10000000)
            add("watchtower_teleport", 10000000)
            add("teleport_to_house", 10000000)
        }
        // Set bank tab item counts
        player["bank_tab_0"] = 13  // Main tab - coins, teleports & jewelry
        player["bank_tab_1"] = 20  // Melee weapons
        player["bank_tab_2"] = 63  // Melee armour
        player["bank_tab_3"] = 45  // Ranged
        player["bank_tab_4"] = 36  // Magic & runes
        player["bank_tab_5"] = 21  // accessories & misc gear
        player["bank_tab_6"] = 8   // Skill capes
        player["bank_tab_7"] = 10  // Potions & food
        player.inventory.apply {
            add("bronze_hatchet")
            add("tinderbox")
            add("small_fishing_net")
            add("shrimp")
            add("bucket")
            add("empty_pot")
            add("bread")
            add("bronze_pickaxe")
            add("bronze_dagger")
            add("bronze_sword")
            add("wooden_shield")
            add("shortbow")
            add("bronze_arrow", 25)
            add("air_rune", 25)
            add("mind_rune", 15)
            add("water_rune", 6)
            add("earth_rune", 4)
            add("body_rune", 2)
        }
    }
}
