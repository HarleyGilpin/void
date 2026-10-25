# CLAUDE.md

**Void** is a 2011 RuneScape private server emulator (revision 634) written in Kotlin. JVM 21+, Gradle 9. Main artefact: `void-server.jar`.

Entry point `game/src/main/kotlin/Main.kt`; content in `game/src/main/kotlin/content/`; game data in `data/`; runtime settings in `game.properties`.

## Always

- Run `./gradlew spotlessApply` before committing.
- Register event handlers only inside a `Script`'s `init {}` — anything later throws at startup.
- Put game data in Groml configs under `data/`, not in Kotlin.
- Keep the module dependency direction intact (see [Modules](modules.md)); content depends on engine, never the reverse.

## Further Reading

- [Build](build.md) — build and tests
- [Modules](modules.md) — module layout and dependency direction
- [Architecture](architecture.md) — content scripts, events, DI, game loop, inventory
- [Testing](testing.md) — WorldTest, integration vs unit patterns
- [Code Style](code-style.md) — ktlint, structural patterns, naming conventions
- [Groml Config](groml.md) — config format spec and examples
