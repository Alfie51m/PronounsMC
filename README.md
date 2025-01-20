# PronounsPlugin

**PronounsPlugin** is a Minecraft Spigot plugin that allows players to set and display their pronouns. It supports multiple database types (**MySQL** and **SQLite**) and provides customizable messages via language files.

## Notes

- PlaceholderAPI and our expansion is required for use in other plugins like a chat plugin.
- We reccomend using a plugin like [ChatInjector](https://www.spigotmc.org/resources/chatinjector-1-13.81201/) for use with plugins that **don't** support PlaceholderAPI like EssentialsX Chat.

## Features

- Players can set and display their pronouns using simple commands.
- Pronouns are color-coded and configurable via `config.yml`.
- Supports **MySQL** or **SQLite** for data storage.
- Fully customizable messages via language files (e.g., `lang/en_US.yml`).
- Reload settings and messages without restarting the server.

---

## Installation

1. **Download** the plugin jar file and place it in your server's `plugins/` folder.
2. **Download** the PlaceholderAPI expansion and place in `plugins/PlaceholderAPI/expansions/` (Optional)
2. **Start your server** to generate the default configuration files.
3. **Edit `config.yml`** to configure:
    - **Database type**: Choose between `mysql` or `sqlite`.
    - **Available pronouns**: Customize the list of pronouns and their color-coded formats.
    - **Language file**: Specify the language file (default: `en_US`).
4. **Restart your server** or use `/pronouns reload` to apply changes.

---

## Configuration

### `config.yml`

Colour formatting can be used in pronouns as shown below, this is optional.

```yaml
langFile: "en_US"

database:
  type: sqlite # Use either 'mysql' or 'sqlite'
  host: localhost
  port: 3306
  name: minecraft
  user: root
  password: password

availablePronouns:
  he/him: "&3(&bHe&3/&bHim&3)&r"
  she/her: "&5(&dShe&5/&dHer&5)&r"
  they/them: "&2(&aThey&2/&aThem&2)&r"
  it/its: "&8(&7It&8/&7Its&8)&r"
  he/they: "&3(&bHe&3/&bThey&3)&r"
  she/they: "&4(&cShe&4/&cThey&4)&r"
  xe/xem: "&8(&7Xe&8/&7Xem&8)&r"
  ze/zir: "&8(&7Ze&8/&7Zir&8)&r"
```

### `lang/en_US.yml`

All user-facing messages are customizable in the language file. Example:

```yaml
messages:
  usageMain: "&cUsage: /pronouns <command>"
  usageGet: "&cUsage: /pronouns get <username>"
  noPermission: "&cYou don't have permission for that."
  playerNotFound: "&cPlayer not found!"
  invalidPronoun: "&cInvalid pronoun. Use /pronouns list to see available options."
  noPronounsConfigured: "&cNo pronouns configured."
  pluginReloaded: "&aPronounsPlugin config reloaded."
  pronounSet: "&aYour pronouns have been set to: &r{pronouns}"
  availablePronounsHeader: "&aAvailable pronouns:"
  playerPronounFormat: "&a{player}'s pronouns: &r{pronouns}"
  onlyPlayers: "&cOnly players can set pronouns."
```

---

## Commands

| Command                | Description                                | Permission          |
|------------------------|--------------------------------------------|---------------------|
| `/pronouns <pronoun>`  | Sets your pronouns to the specified key.   | None                |
| `/pronouns get <name>` | Displays the pronouns of another player.   | `pronouns.get`      |
| `/pronouns list`       | Lists all available pronouns.              | None                |
| `/pronouns reload`     | Reloads the configuration and language.    | `pronouns.reload`   |

---

## Permissions

| Permission          | Description                              | Default |
|---------------------|------------------------------------------|---------|
| `pronouns.get`      | Allows retrieving another player's pronouns. | OP      |
| `pronouns.reload`   | Allows reloading the configuration and language. | OP      |

---

## Database Support

### MySQL
To use MySQL:
1. Set `database.type: mysql` in `config.yml`.
2. Configure `host`, `port`, `name`, `user`, and `password`.

### SQLite
To use SQLite:
1. Set `database.type: sqlite` in `config.yml`.
2. The plugin will create a `pronouns.db` file in the plugin folder.

---

## License
This project is licensed under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).

---

## Contribution

Contributions are welcome! To contribute:
1. Fork the repository.
2. Create a new feature branch.
3. Submit a pull request explaining your changes.

---

## Thanks for using PronounsPlugin
You can also find this plugin on... Coming Soon
