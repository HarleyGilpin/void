package world.gregs.voidps.tools.photobooth

import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.storage.DatabaseStorage

/**
 * Reads photo booth snapshots from persisted player data (the `photo_booth_*` variables written by
 * Iconis.saveSnapshot).
 */
class SnapshotRepository(private val storage: Storage) {

    /** All account names known to storage (lowercase keys). */
    fun names(): Set<String> = storage.names().keys

    /**
     * Account names flagged for a re-render.
     *
     * The database answers this in one query. File storage has no equivalent, so it falls back to
     * reading each save; that only costs a local file read per account, and it keeps the tool usable
     * on a `storage.type=files` install.
     */
    fun dirtyNames(): List<String> = when (storage) {
        is DatabaseStorage -> storage.flagged(DIRTY)
        else -> names().filter { storage.load(it)?.variables?.get(DIRTY) == true }
    }

    /**
     * The account's database id, used to name the output PNGs. Null on file storage, where
     * accounts have no id; callers fall back to a name-derived file name there.
     */
    fun accountId(accountName: String): Int? = (storage as? DatabaseStorage)?.accountId(accountName)

    /** Loads a player's snapshot, or null if they have never used the booth. */
    fun load(accountName: String): PhotoSnapshot? {
        val variables = storage.load(accountName)?.variables ?: return null
        return fromVariables(variables)
    }

    private fun fromVariables(variables: Map<String, Any>): PhotoSnapshot? {
        val male = variables["photo_booth_male"] as? Boolean ?: return null
        val looks = variables["photo_booth_looks"] as? String ?: return null
        val colours = variables["photo_booth_colours"] as? String ?: return null
        val equipment = variables["photo_booth_equipment"] as? String ?: ""
        val time = (variables["photo_booth_time"] as? Number)?.toLong() ?: 0L
        return PhotoSnapshot.parse(male, looks, colours, equipment, time)
    }

    companion object {
        private const val DIRTY = "photo_booth_dirty"
    }
}
