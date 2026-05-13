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
        if (username.length > 12) {
            return Response.LOGIN_SERVER_REJECTED_SESSION
        }
        val passwordHash = account.password(username)
        if (passwordHash == null && account.used(username)) {
            // Username already in use as a display name
            return Response.INVALID_CREDENTIALS
        }
        if (!account.exists(username)) {
            if (passwordHash != null) {
                // Failed to find account file despite AccountDefinition exists in memory (aka existed on startup)
                return Response.ACCOUNT_DISABLED
            }
            return Response.SUCCESS
        }
        try {
            if (passwordHash == null) {
                // Failed to find accounts password despite account file existing (created since startup)
                return Response.ACCOUNT_DISABLED
            }
            val matches = if (passwordHash.startsWith("\$argon2")) {
                argon2.verify(passwordHash, password.toCharArray())
            } else {
                BCrypt.checkpw(password, passwordHash)
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

    fun encrypt(username: String, password: String): String {
        val passwordHash = account.password(username)
        if (passwordHash != null && !passwordHash.startsWith("\$argon2")) {
            return passwordHash
        }
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
}
