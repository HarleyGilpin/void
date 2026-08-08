package world.gregs.voidps.engine.data.definition

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.previousName
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.timedLoad
import java.util.concurrent.ConcurrentHashMap

/**
 * Stores data about player accounts whether they're online or offline
 *
 * Account names are the identity here: they're unique and never change, so [definitions] and
 * [clans] are keyed by them. Display names are mutable and nothing in storage stops two accounts
 * claiming the same one, so they're resolved through the [byDisplayName] index instead of being
 * used as a key - keying by display name lets one account's password hash overwrite another's.
 */
class AccountDefinitions(
    private val definitions: MutableMap<String, AccountDefinition> = ConcurrentHashMap(),
    val displayNames: MutableMap<String, String> = ConcurrentHashMap(),
    val clans: MutableMap<String, Clan> = ConcurrentHashMap(),
) {

    private val logger = InlineLogger()

    /**
     * Display name to account lookup, derived from [definitions] rather than stored separately.
     * When two accounts claim one display name the first indexed keeps it; the loser still has
     * its own [definitions] entry, so a clash costs an ambiguous name lookup and never a login.
     */
    private val byDisplayName: MutableMap<String, AccountDefinition> = ConcurrentHashMap()

    init {
        for (definition in definitions.values) {
            index(definition)
        }
    }

    fun add(player: Player) {
        val account = player.accountName.lowercase()
        val definition = AccountDefinition(player.accountName, player.name, player.previousName, player.passwordHash)
        definitions[account] = definition
        displayNames[account] = player.name
        index(definition)
        clans[account] = Clan(
            owner = player.accountName,
            ownerDisplayName = player.name,
            name = player["clan_name", ""],
            friends = player.friends,
            ignores = player.ignores,
            joinRank = ClanRank.valueOf(player["clan_join_rank", "Anyone"]),
            talkRank = ClanRank.valueOf(player["clan_talk_rank", "Anyone"]),
            kickRank = ClanRank.valueOf(player["clan_kick_rank", "Corporeal"]),
            lootRank = ClanRank.valueOf(player["clan_loot_rank", "None"]),
            coinShare = player["coin_share_setting", false],
        )
    }

    fun update(accountName: String, newName: String, previousDisplayName: String) {
        val account = accountName.lowercase()
        val definition = definitions[account] ?: return
        byDisplayName.remove(previousDisplayName.lowercase(), definition)
        definition.displayName = newName
        definition.previousName = previousDisplayName
        displayNames[account] = newName
        index(definition)
    }

    /**
     * Clans are keyed by owner account name to match storage, but they're joined by the owner's
     * display name, so resolve that first and fall back to treating [displayName] as an account.
     */
    fun clan(displayName: String): Clan? {
        val account = byDisplayName[displayName.lowercase()]?.accountName ?: displayName
        return clans[account.lowercase()]
    }

    fun get(displayName: String) = byDisplayName[displayName.lowercase()]

    fun getValue(displayName: String) = byDisplayName.getValue(displayName.lowercase())

    fun getByAccount(accountName: String) = definitions[accountName.lowercase()]

    fun load(storage: Storage = get()): AccountDefinitions {
        timedLoad("account") {
            for ((_, definition) in storage.names()) {
                definitions[definition.accountName.lowercase()] = definition
                displayNames[definition.accountName.lowercase()] = definition.displayName
                index(definition)
            }
            for ((name, definition) in storage.clans()) {
                clans[name.lowercase()] = definition
            }
            definitions.size
        }
        return this
    }

    /**
     * Merges freshly loaded storage data into the in-memory cache so external
     * changes (website password resets, imported accounts) apply without a restart.
     * Entries whose account matches [skip] (online or mid-save) are left untouched
     * as their in-memory state may be newer than storage.
     * Add/update only; never removes entries. Must be called on the game thread.
     * @return number of definitions added or updated
     */
    fun merge(
        names: Map<String, AccountDefinition>,
        clanUpdates: Map<String, Clan>,
        skip: (accountName: String) -> Boolean,
    ): Int {
        var count = 0
        for ((_, definition) in names) {
            if (skip(definition.accountName)) {
                continue
            }
            val account = definition.accountName.lowercase()
            val existing = definitions[account]
            if (existing == null) {
                definitions[account] = definition
                index(definition)
            } else if (existing == definition) {
                // Matched on account name, so every field can be brought into line below and
                // repeat merges of unchanged storage settle here instead of looping forever.
                continue
            } else {
                if (!existing.displayName.equals(definition.displayName, ignoreCase = true)) {
                    byDisplayName.remove(existing.displayName.lowercase(), existing)
                    existing.displayName = definition.displayName
                    index(existing)
                } else {
                    existing.displayName = definition.displayName
                }
                existing.previousName = definition.previousName
                existing.passwordHash = definition.passwordHash
            }
            displayNames[account] = definition.displayName
            count++
        }
        for ((name, clan) in clanUpdates) {
            if (skip(clan.owner)) {
                continue
            }
            val existing = clans[name.lowercase()]
            if (existing == null) {
                clans[name.lowercase()] = clan
            } else {
                existing.ownerDisplayName = clan.ownerDisplayName
                existing.name = clan.name
                existing.friends = clan.friends
                existing.ignores = clan.ignores
                existing.joinRank = clan.joinRank
                existing.talkRank = clan.talkRank
                existing.kickRank = clan.kickRank
                existing.lootRank = clan.lootRank
                existing.coinShare = clan.coinShare
            }
        }
        return count
    }

    private fun index(definition: AccountDefinition) {
        val existing = byDisplayName.putIfAbsent(definition.displayName.lowercase(), definition)
        if (existing != null && !existing.accountName.equals(definition.accountName, ignoreCase = true)) {
            logger.warn {
                "Display name '${definition.displayName}' is claimed by both '${existing.accountName}' and " +
                    "'${definition.accountName}'; name lookups will resolve to '${existing.accountName}'."
            }
        }
    }
}
