package world.gregs.voidps.network.login

import de.mkammerer.argon2.Argon2Factory
import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.network.Response

/**
 * Checks account credentials are valid
 */
class PasswordManager(private val account: AccountLoader) {

    private val argon2 by lazy { Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2i) }

    fun validate(username: String, password: String): Int {
        if (!AccountNames.valid(username)) {
            // The client shows 11 as a weak password warning, 3 is "Invalid username or password"
            return Response.INVALID_CREDENTIALS
        }
        val passwordHash = account.password(username)
        if (passwordHash == null) {
            if (account.used(username)) {
                // Username already in use as a display name
                return Response.INVALID_CREDENTIALS
            }
            if (account.exists(username)) {
                // Failed to find accounts password despite account file existing (created since startup)
                return Response.ACCOUNT_DISABLED
            }
            return Response.SUCCESS
        }
        // Accounts created since startup are only in memory until their first save, so the hash is checked whether or not a file exists yet
        try {
            val matches = if (passwordHash.startsWith("\$argon2")) {
                argon2.verify(passwordHash, password.toCharArray())
            } else {
                BCrypt.checkpw(password, normaliseBCryptRevision(passwordHash))
            }
            if (matches) {
                return Response.SUCCESS
            }
        } catch (e: IllegalArgumentException) {
            return Response.COULD_NOT_COMPLETE_LOGIN
        } catch (e: Exception) {
            return Response.COULD_NOT_COMPLETE_LOGIN
        }
        return Response.INVALID_CREDENTIALS
    }

    /**
     * jBCrypt only accepts the `$2$` and `$2a$` salt revisions, but the website hashes passwords
     * with PHP which emits `$2y$` (and other libraries `$2b$`). All three are the same algorithm,
     * so rewrite the revision to `$2a$` rather than rejecting the login.
     */
    private fun normaliseBCryptRevision(passwordHash: String): String = when {
        passwordHash.startsWith("\$2y\$") || passwordHash.startsWith("\$2b\$") -> "\$2a\$${passwordHash.substring(4)}"
        else -> passwordHash
    }

    fun encrypt(username: String, password: String): String {
        val passwordHash = account.password(username)
        if (passwordHash != null && !passwordHash.startsWith("\$argon2")) {
            return passwordHash
        }
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}
