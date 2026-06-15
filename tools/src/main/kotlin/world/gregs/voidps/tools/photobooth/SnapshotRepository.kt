package world.gregs.voidps.tools.photobooth

import world.gregs.voidps.engine.data.Storage

/**
 * Reads photo booth snapshots from persisted player data (the `photo_booth_*` variables written by
 * Iconis.saveSnapshot).
 */
class SnapshotRepository(private val storage: Storage) {

    /** All account names known to storage (lowercase keys). */
    fun names(): Set<String> = storage.names().keys

    /** Loads a player's snapshot, or null if they have never used the booth. */
    fun load(accountName: String): PhotoSnapshot? {
        val variables = storage.load(accountName)?.variables ?: return null
        return fromVariables(variables)
    }

    /** Loads a snapshot only if the player is flagged dirty (needs a fresh render). */
    fun loadIfDirty(accountName: String): PhotoSnapshot? {
        val variables = storage.load(accountName)?.variables ?: return null
        if (variables["photo_booth_dirty"] != true) return null
        return fromVariables(variables)
    }

    /** Clears the dirty flag after a fresh render so the next sweep skips this account. */
    fun clearDirty(accountName: String) {
        val save = storage.load(accountName) ?: return
        if (save.variables["photo_booth_dirty"] != true) return
        val variables = save.variables.toMutableMap()
        variables.remove("photo_booth_dirty")
        storage.save(listOf(save.copy(variables = variables)))
    }

    private fun fromVariables(variables: Map<String, Any>): PhotoSnapshot? {
        val male = variables["photo_booth_male"] as? Boolean ?: return null
        val looks = variables["photo_booth_looks"] as? String ?: return null
        val colours = variables["photo_booth_colours"] as? String ?: return null
        val equipment = variables["photo_booth_equipment"] as? String ?: ""
        val time = (variables["photo_booth_time"] as? Number)?.toLong() ?: 0L
        return PhotoSnapshot.parse(male, looks, colours, equipment, time)
    }
}
