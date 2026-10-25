# Modules

Dependency direction: `game` → `engine`, `cache`, `network`, `config`, `types`; `engine` → `buffer`, `cache`, `network`, `config`, `types`; `cache`/`network` → `buffer`. `config`, `types` and `buffer` depend on nothing. `database` depends on `engine` (never the reverse — the server calls into it only when built with `-PincludeDb`).

|        Module         |                                                                 Responsibility                                                                  |
|-----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| **engine**            | Game loop, entity model (Player, NPC, GameObject, FloorItem), event system, inventory transactions, pathfinding, all `Script` mix-in interfaces |
| **game**              | All content (`content/`), `Main.kt`, `GameTick.kt`, `GameModules.kt`, `ContentLoader.kt`, `WorldTest.kt`                                        |
| **cache**             | Decoders and encoders of binary RS cache files                                                                                                  |
| **config**            | Groml reader — the simplified toml dialect used by every file under `data/` (string IDs, shops, drop tables, etc.)                              |
| **network**           | Ktor server for connections, login, packet encoding/decoding                                                                                    |
| **buffer**            | Binary read/write utilities                                                                                                                     |
| **types**             | Shared value types (`Tile`, `Item`, `Direction`, etc.)                                                                                          |
| **database**          | Optional PostgreSQL via Exposed ORM (`-PincludeDb` only)                                                                                        |
| **tools / tools:app** | Dev utilities and Compose cache viewer — not part of the server                                                                                 |

