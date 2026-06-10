package world.gregs.voidps.network.client

import java.util.concurrent.ConcurrentHashMap

/**
 * Temporarily blocks login attempts per ip address after too many failed password attempts
 * Tracking is per ip rather than per account so failed attempts can't be used to lock other players out of their accounts
 * @param maxAttempts number of failed attempts before an address is blocked, 0 or less to disable
 * @param timeoutMillis how long an address is blocked for; also how long failed attempts are remembered
 */
class LoginAttemptTracker(
    private val maxAttempts: Int,
    private val timeoutMillis: Long,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private data class Entry(val count: Int, val expires: Long)

    private val attempts = ConcurrentHashMap<String, Entry>()

    fun blocked(address: String): Boolean {
        if (maxAttempts <= 0) {
            return false
        }
        val entry = attempts.computeIfPresent(address) { _, current ->
            if (clock() >= current.expires) null else current
        } ?: return false
        return entry.count >= maxAttempts
    }

    fun failure(address: String) {
        if (maxAttempts <= 0) {
            return
        }
        val now = clock()
        attempts.compute(address) { _, entry ->
            when {
                entry == null || now >= entry.expires -> Entry(1, now + timeoutMillis)
                entry.count >= maxAttempts -> entry // Blocked; fixed expiry, don't extend
                else -> Entry(entry.count + 1, now + timeoutMillis)
            }
        }
        if (attempts.size > CLEANUP_THRESHOLD) {
            attempts.values.removeIf { now >= it.expires }
        }
    }

    fun success(address: String) {
        if (maxAttempts > 0) {
            attempts.remove(address)
        }
    }

    fun clear() {
        attempts.clear()
    }

    companion object {
        private const val CLEANUP_THRESHOLD = 1000
    }
}
