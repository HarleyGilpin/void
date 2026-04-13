# Void Server Commands

Commands are entered in-game prefixed with `::`. Required arguments use `(parens)`, optional arguments use `[brackets]`.

Use `::commands [filter]` to browse in-game, or `::help <command>` for details on a specific command.

---

## All Players

| Command | Syntax | Description |
|---|---|---|
| `commands` | `::commands [filter]` | Display a list of available commands |
| `help` | `::help (command-name)` | Find more information about a specific command |
| `players` | `::players` | Get the total and local player counts |

---

## Mod (mod or admin)

### Search & Lookup
| Command | Aliases | Syntax | Description |
|---|---|---|---|
| `find` | `search` | `::find (content-name)` | Search all content (items, objects, npcs, commands, players, clans, variables) |
| `items` | — | `::items (name)` | Search all items by name or id |
| `objects` | — | `::objects (name)` | Search all game objects by name or id |
| `npcs` | — | `::npcs [name]` | Search all NPCs by name or id (no args = total count) |
| `accounts` | — | `::accounts [name]` | Search all players |
| `clans` | — | `::clans (name)` | Search all clans |

### Logs
| Command | Syntax | Description |
|---|---|---|
| `logs` | `::logs [term] [term2] ...` | Search audit logs for a string |
| `log_limit` | `::log_limit [past-hours]` | Set how many hours back log searches look (admins default 3, mods default 1) |

### Inventory & Drop Tables
| Command | Syntax | Description |
|---|---|---|
| `clear` | `::clear` | Delete all items in your inventory |
| `vars` | `::vars [variable-name] [player-name]` | Search a player's variables |
| `chance` | `::chance (drop-table-id)` | Display drop chances for every item in a drop table |
| `sim` | `::sim (drop-table-id) [drop-count]` | Simulate drops from a drop table (supports SI suffixes: 10k, 5m) |

### Server
| Command | Syntax | Description |
|---|---|---|
| `save` | `::save` | Save all players and exchange data |

### Camera
| Command | Aliases | Syntax | Description |
|---|---|---|---|
| `camera_reset` | `cam_reset`, `clear_cam`, `cam_clear`, `clear_camera`, `camera_clear` | `::camera_reset` | Reset camera to normal |

### Farming
| Command | Syntax | Description |
|---|---|---|
| `patches` | `::patches [player-name]` | List farming patches for a player |
| `growth` | `::growth [player-name]` | Check next farming growth tick |
| `rot` | `::rot [player-name]` | Complete all rotting compost bins |

---

## Admin

### Items
| Command | Syntax | Description |
|---|---|---|
| `item` | `::item (item-id) [amount]` | Spawn an item into your inventory (supports 10k, 5m etc.) |
| `give` | `::give (player-name) (item-id) [amount]` | Spawn an item into another player's inventory |
| `items` | `::items (item-id) [id] [id] [id] [id]` | Spawn multiple items at once |
| `food` | `::food` | Fill inventory with rocktail |
| `pots` | `::pots` | Fill inventory with overload + super restore potions |

### Teleport
| Command | Aliases | Syntax | Description |
|---|---|---|---|
| `tele` | `tp` | `::tele (x) (y) [level]` | Teleport to coordinates |
| `tele` | `tp` | `::tele (area-name)` | Teleport to a named area (e.g. `varrock`, `lumbridge`, `grand_exchange`) |
| `tele` | `tp` | `::tele (region-id)` | Teleport to a region by ID |
| `tele_to` | — | `::tele_to (player-name)` | Teleport to another player |
| `tele_to_me` | — | `::tele_to_me (player-name)` | Teleport another player to you |

### Player Stats & State
| Command | Aliases | Syntax | Description |
|---|---|---|---|
| `master` | — | `::master [player-name]` | Set all skills to level 99 |
| `level` | `set_level` | `::level (skill-name) (level) [player-name]` | Set a skill to a specific level |
| `reset` | — | `::reset [player-name]` | Reset all skills to level 1 |
| `god` | — | `::god [true\|false]` | Toggle god mode (optional: enable insta-kill) |
| `boost` | — | `::boost [amount]` | Boost all stats by amount (default 25) |
| `rest` | — | `::rest [player-name]` | Restore run energy to full |
| `spec` | — | `::spec [player-name]` | Restore special attack to full |
| `pray` | — | `::pray [player-name]` | Restore full prayer points |
| `restore` | — | `::restore [player-name]` | Restore all skill levels |
| `respawn` | — | `::respawn [player-name]` | Teleport to last death location |

### Player Management
| Command | Aliases | Syntax | Description |
|---|---|---|---|
| `skull` | — | `::skull [player-name]` | Apply a skull to a player |
| `unskull` | — | `::unskull [player-name]` | Remove skull from a player |
| `hide` | — | `::hide [player-name]` | Toggle player invisibility |
| `pos` | `mypos`, `tile` | `::pos [player-name]` | Show player's current tile position |
| `chat` | — | `::chat (message) [player-name]` | Force a chat message over a player's head |
| `hit` | — | `::hit [amount] [player-name]` | Damage a player by an amount |
| `watch` | — | `::watch [player-name]` | Look at another player |
| `debug` | — | `::debug [player-name]` | Toggle debug mode for a player |
| `rights` | `promote` | `::rights (player-name) (none\|mod\|admin)` | Set another player's rights |
| `inv` | — | `::inv [player-name]` | Open a player's inventory |

### Player Customisation
| Command | Syntax | Description |
|---|---|---|
| `spellbook` | `::spellbook [ancient\|lunar\|modern\|dungeoneering] [player-name]` | Switch spellbook |
| `prayers` | `::prayers [normal\|curses] [player-name]` | Switch prayer book |
| `unlock` | `::unlock (all\|music\|tasks\|emotes\|quests) [player-name]` | Unlock all content for an activity |

### Player Updating (Visual)
| Command | Aliases | Syntax | Description |
|---|---|---|---|
| `anim` | — | `::anim (anim-id)` | Perform an animation (-1 to clear) |
| `emote` | — | `::emote (emote-id)` | Perform a render emote (-1 to clear) |
| `gfx` | — | `::gfx (gfx-id)` | Perform a graphic effect (-1 to clear) |
| `proj` | `shoot` | `::proj (gfx-id)` | Shoot a projectile |
| `overlay` | — | `::overlay` | Apply colour overlay to player |
| `exact_move` | `move` | `::exact_move [start-x] [start-y] [end-x] [end-y]` | Trigger exact movement |
| `face` | — | `::face (delta-x) (delta-y)` | Turn player to face a direction |
| `time` | — | `::time` | Set player time bar |
| `skill_level` | — | `::skill_level (level)` | Set displayed skill level |
| `combat_level` | — | `::combat_level (level)` | Set displayed combat level |
| `summoning_level` | — | `::summoning_level (level)` | Set summoning combat level |
| `toggle_skill_level` | — | `::toggle_skill_level` | Toggle skill level display |
| `tfm` | — | `::tfm (npc-id) [player-name]` | Transform into an NPC (-1 to clear) |

### Effects
| Command | Syntax | Description |
|---|---|---|
| `disease` | `::disease [damage] [player-name]` | Disease a player |
| `poison` | `::poison [damage] [player-name]` | Poison a player |
| `freeze` | `::freeze [ticks] [player-name]` | Freeze a player |
| `cure` | `::cure [player-name]` | Cure a player |

### NPC
| Command | Syntax | Description |
|---|---|---|
| `npc` | `::npc (npc-id)` | Spawn an NPC at your position |

### NPC Updating (Visual — targets NPC 1 tile north)
| Command | Syntax | Description |
|---|---|---|
| `npc_tfm` | `::npc_tfm (transform-id)` | Transform NPC |
| `npc_turn` | `::npc_turn (delta-x) (delta-y)` | Turn NPC to face direction |
| `npc_anim` | `::npc_anim (anim-id)` | Play animation on NPC |
| `npc_gfx` | `::npc_gfx (gfx-id)` | Play graphic on NPC |
| `npc_chat` | `::npc_chat [message]` | Make NPC say a message |
| `npc_hit` | `::npc_hit` | Apply a heal hitsplat to NPC |
| `npc_overlay` | `::npc_overlay` | Apply colour overlay to NPC |
| `npc_time` | `::npc_time` | Set NPC time bar |
| `npc_watch` | `::npc_watch` | Make NPC watch you |
| `npc_run` | `::npc_run` | Enable running for NPC |
| `npc_crawl` | `::npc_crawl` | (stub) |

### Object
| Command | Syntax | Description |
|---|---|---|
| `obj` | `::obj (id) [shape] [rotation] [ticks]` | Spawn a game object at your position |

### Interface
| Command | Aliases | Syntax | Description |
|---|---|---|---|
| `inter` | `iface` | `::inter (interface-id)` | Open an interface by int or string id |
| `show` | — | `::show (interface-id) (component-id) (visible)` | Toggle visibility of an interface component |
| `colour` | — | `::colour (iface-id) (comp-id) (red) (green) (blue)` | Set colour of an interface component |
| `send_text` | — | `::send_text (interface-id) (component-id) (text)` | Set text of an interface component |
| `setting` | — | `::setting (id) (comp) (from) (to) [setting] [s2] [s3]` | Send settings to an interface component |
| `script` | — | `::script (id) [p1] [p2] [p3] [p4] [p5]` | Run a client script |
| `send_items` | — | `::send_items (iface-id) (comp-id) (item) [amount]` | Send an item to an interface component |
| `shop` | — | `::shop (shop-id)` | Open a shop by id |
| `expr` | — | `::expr (expression-id) [npc-id]` | Display dialogue head with animation expression |

### Sound
| Command | Aliases | Syntax | Description |
|---|---|---|---|
| `sound` | — | `::sound (sound-id)` | Play a sound by int or string id |
| `midi` | — | `::midi (midi-id)` | Play a midi by int or string id |
| `jingle` | — | `::jingle (jingle-id)` | Play a jingle by int or string id |
| `song` | `track` | `::song (song-id)` | Play a music track by int or string id |

### Variables
| Command | Syntax | Description |
|---|---|---|
| `var` | `::var (variable-name) (value)` | Set a player variable |
| `varp` | `::varp (id) (value)` | Send player-variable to client |
| `varbit` | `::varbit (id) (value)` | Send variable-bit to client |
| `varc` | `::varc (id) (value)` | Send client-variable to client |
| `varcstr` | `::varcstr (id) (value)` | Send variable-client-string to client |
| `variables` | — | `::variables [var-name] [player-name]` | List a player's set variables |
| `timers` | — | `::timers [timer-name] [player-name]` | List a player's active timers |
| `under` | — | `::under [all\|objects\|players\|npcs\|items\|collisions] [player-name]` | List entities under a player |

### Camera
| Command | Syntax | Description |
|---|---|---|
| `move_to` | `::move_to (x) (y) (height) (c-speed) (v-speed)` | Move camera to coordinates |
| `look_at` | `::look_at (x) (y) (height) (c-speed) (v-speed)` | Turn camera to look at coordinates |
| `shake` | `::shake (intensity) (type) (cycle) (movement) (speed)` | Shake the camera |

### Path & Collision
| Command | Syntax | Description |
|---|---|---|
| `patrol` | `::patrol (patrol-id)` | Walk along a patrol route |
| `show_patrol` | `::show_patrol (patrol-id)` | Display a patrol route with graphics |
| `path` | `::path` | Toggle showing calculated walk paths |
| `col` | `::col` | Print collision flags at your tile to console |
| `show_col` | `::show_col` | Show nearby collision visually |
| `pf_bench` | `::pf_bench` | Run pathfinder benchmark |

### Zone
| Command | Syntax | Description |
|---|---|---|
| `rotate_zone` | `::rotate_zone [rotation]` | Rotate the current zone |
| `clear_zone` | `::clear_zone` | Reset current zone back to static |
| `copy_zone` | `::copy_zone (from) [to] [rotation]` | Create a dynamic zone copy |

### Bots
| Command | Syntax | Description |
|---|---|---|
| `bots` | `::bots (count)` | Spawn N bots |
| `clear_bots` | `::clear_bots [count]` | Clear all or N bots |
| `bot` | `::bot [task]` | Toggle yourself as a bot player |
| `bot_info` | `::bot_info [name]` | Print bot info |
| `kill` | `::kill` | Hard-remove all bot players from the server immediately |
| `go_to` | `::go_to [area-id]` | Enables bot mode on yourself and navigates to a named area using the bot navigation graph |
| `walk_to_bank` | `::walk_to_bank` | Prints the bot navigation graph path to the nearest bank to console |

### Server
| Command | Syntax | Description |
|---|---|---|
| `update` | `::update [time]` | Start a system shutdown (e.g. `1h 30m`, `100` ticks) |
| `reload` | `::reload (config-type)` | Reload config files (e.g. `npcs`, `items`, `settings`, `drops`, `areas`) |
