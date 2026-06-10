package world.gregs.voidps.network.client

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoginAttemptTrackerTest {

    private var now = 0L
    private lateinit var tracker: LoginAttemptTracker

    @BeforeEach
    fun setup() {
        now = 0L
        tracker = LoginAttemptTracker(3, 1000) { now }
    }

    @Test
    fun `Under threshold isn't blocked`() {
        val address = "123.456.789"
        tracker.failure(address)
        tracker.failure(address)

        assertFalse(tracker.blocked(address))
    }

    @Test
    fun `Threshold blocks address`() {
        val address = "123.456.789"
        repeat(3) {
            tracker.failure(address)
        }

        assertTrue(tracker.blocked(address))
    }

    @Test
    fun `Block expires after timeout`() {
        val address = "123.456.789"
        repeat(3) {
            tracker.failure(address)
        }
        assertTrue(tracker.blocked(address))

        now = 1000

        assertFalse(tracker.blocked(address))
    }

    @Test
    fun `Failures during block don't extend it`() {
        val address = "123.456.789"
        repeat(3) {
            tracker.failure(address)
        }
        now = 500
        tracker.failure(address)
        tracker.failure(address)

        now = 1000

        assertFalse(tracker.blocked(address))
    }

    @Test
    fun `Success resets failed attempts`() {
        val address = "123.456.789"
        tracker.failure(address)
        tracker.failure(address)
        tracker.success(address)
        tracker.failure(address)
        tracker.failure(address)

        assertFalse(tracker.blocked(address))

        tracker.failure(address)

        assertTrue(tracker.blocked(address))
    }

    @Test
    fun `Stale failures decay`() {
        val address = "123.456.789"
        tracker.failure(address)
        tracker.failure(address)

        now = 1000
        tracker.failure(address)
        tracker.failure(address)

        assertFalse(tracker.blocked(address))
    }

    @Test
    fun `Disabled tracker never blocks`() {
        for (max in intArrayOf(0, -1)) {
            val disabled = LoginAttemptTracker(max, 1000) { now }
            repeat(10) {
                disabled.failure("123.456.789")
            }
            assertFalse(disabled.blocked("123.456.789"))
        }
    }

    @Test
    fun `Addresses tracked independently`() {
        repeat(3) {
            tracker.failure("123.456.789")
        }

        assertTrue(tracker.blocked("123.456.789"))
        assertFalse(tracker.blocked("192.168.1.1"))
    }

    @Test
    fun `Clearing removes all attempts`() {
        repeat(3) {
            tracker.failure("123.456.789")
        }

        tracker.clear()

        assertFalse(tracker.blocked("123.456.789"))
    }
}
