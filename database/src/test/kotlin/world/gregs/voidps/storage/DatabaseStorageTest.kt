package world.gregs.voidps.storage

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import world.gregs.voidps.engine.data.AbuseReport
import kotlin.test.assertEquals

class DatabaseStorageTest : StorageTest(), DatabaseTest {

    override val storage = DatabaseStorage()

    @Test
    fun `Store an abuse report`() {
        val report = AbuseReport(
            reporter = "mod_steve",
            reported = "Durial321",
            rule = 6,
            ruleName = "Macroing",
            mute = true,
            suggestion = "extra info",
            time = 1234567890,
            evidence = listOf("[00:00:01] public: free armour trimming", "[00:00:02] public: selling gf"),
        )

        storage.saveReport(report)

        transaction {
            val row = ReportsTable.selectAll().single()
            assertEquals(report.reporter, row[ReportsTable.reporter])
            assertEquals(report.reported, row[ReportsTable.reported])
            assertEquals(report.rule, row[ReportsTable.rule])
            assertEquals(report.ruleName, row[ReportsTable.ruleName])
            assertEquals(report.mute, row[ReportsTable.mute])
            assertEquals(report.suggestion, row[ReportsTable.suggestion])
            assertEquals(report.time, row[ReportsTable.time])
            assertEquals(report.evidence, row[ReportsTable.evidence])
        }
    }

    @Test
    fun `Saving variable with invalid format throws exception`() {
        assertThrows<IllegalArgumentException> {
            storage.save(listOf(save.copy(variables = mapOf("invalid_float" to 0.2f))))
        }
    }

    @Test
    fun `Load variable with invalid format throws exception`() {
        storage.save(listOf(save))
        transaction {
            val id = AccountsTable.selectAll().where { AccountsTable.name eq save.name }.first()[AccountsTable.id]
            VariablesTable.insert {
                it[playerId] = id
                it[name] = "invalid"
                it[type] = -1
            }
        }
        assertThrows<IllegalArgumentException> {
            storage.load(save.name)
        }
    }

    @Test
    fun `Save player count overwrites previous value`() {
        storage.savePlayerCount(1, 5)
        storage.savePlayerCount(1, 3)
        transaction {
            val rows = PlayerCountTable.selectAll().toList()
            assertEquals(1, rows.size)
            assertEquals(1, rows.first()[PlayerCountTable.world])
            assertEquals(3, rows.first()[PlayerCountTable.count])
        }
    }

    @Test
    fun `Load variable with missing value throws null pointer`() {
        storage.save(listOf(save))
        transaction {
            val id = AccountsTable.selectAll().where { AccountsTable.name eq save.name }.first()[AccountsTable.id]
            VariablesTable.insert {
                it[playerId] = id
                it[name] = "invalid"
                it[type] = 1
            }
        }
        assertThrows<NullPointerException> {
            storage.load(save.name)
        }
    }

    @Test
    fun `Flagged returns accounts with the variable set to true`() {
        // Offer ids are unique across the whole table, so the fixture's offers can only belong to
        // one of these accounts; none of them need offers to answer the question being tested.
        fun account(name: String, variables: Map<String, Any>) = save.copy(
            name = name,
            variables = variables,
            offers = emptyArray(),
            history = emptyList(),
        )

        storage.save(
            listOf(
                account("dirty", mapOf("photo_booth_dirty" to true)),
                account("clean", mapOf("photo_booth_dirty" to false)),
                account("unset", mapOf("in_pvp" to true)),
            ),
        )

        assertEquals(listOf("dirty"), storage.flagged("photo_booth_dirty"))
    }

    @Test
    fun `Flagged returns nothing when no account has the variable`() {
        storage.save(listOf(save))

        assertEquals(emptyList<String>(), storage.flagged("photo_booth_dirty"))
    }
}
