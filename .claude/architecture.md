# Architecture

## Content Scripts

Kotlin classes implementing `Script` (`engine/src/main/kotlin/world/gregs/voidps/engine/Script.kt`). The interface composes many mix-ins (`VariableApi`, `InventoryApi`, `CombatApi`, `Dialogues`, `Operation`, `Approachable`, `TimerApi`, …) so content calls engine APIs directly. Handlers register in `init {}` — `Script.checkLoading()` rejects any registered after startup. `ScriptMetadataTask` (see `game/build.gradle.kts`) generates `scripts.txt` into resources at build time; `ContentLoader` instantiates every listed script via reflection at startup.

Content layout: `content/{achievement,activity,area,bot,entity,minigame,quest,skill,social}/`

## Events

Network decodes clicks into `Instruction`s (`network/.../client/instruction/`); `InstructionTask` dispatches them through `InstructionHandlers` to the handlers scripts registered. Handler IDs support wildcards and comma-separated lists (`npcApproach("Spy-on", "*_penguin,*_turkey")`); `Wildcards` expands those patterns against the loaded definitions and caches the result in `data/.temp/wildcards.txt` (path from `storage.wildcards`).

## Dependency Injection (Koin)

Singletons are registered in `EngineModules.kt` (engine), `GameModules.kt` and `Main.kt` (definitions decoded from the cache). Scripts take them as constructor parameters; elsewhere use `get<T>()`. Key singletons: `ItemDefinitions`, `NPCDefinitions`, `ObjectDefinitions`, `InterfaceDefinitions`, `Tables`, `Players`, `NPCs`, `GameObjects`, `FloorItems`.

## Game Loop

`GameLoop` ticks every 600 ms through an ordered `List<Runnable>` built by `getTickStages()` in `game/src/main/kotlin/GameTick.kt`. Order matters and is roughly: player/NPC flag resets → bots, hunting, Grand Exchange → connection queue → NPC and floor-item spawns → client instructions → `World` → NPC/player modes → floor item and object timers → zone and character update encoding → account saving. A stage taking over 100 ms logs a warning.

## Inventory Transactions

All mutations go through `engine/.../inv/transact/`. Operations are staged and committed atomically; failures roll back and report a `TransactionError`.

## Settings & Data

`game.properties` (root) holds every runtime setting, read with `Settings["key"]`; environment variables override it. Game data lives under `data/` as Groml configs mirroring the content packages, plus `data/cache/` (RS cache, required for tests) and `data/saves/`.
