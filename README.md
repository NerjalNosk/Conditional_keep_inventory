## Conditional Keep Inventory

_Note: this mod requires the Fabric API. Check out the **Links** section for more._

### Table of Contents
  * [Table of Contents](#table-of-contents)
  * [Introduction](#introduction)
  * [What it does](#what-it-does)
  * [How it works](#how-it-works)
  * [Config structure](#config-structure)
  * [Command usage](#command-usage)
  * [Sources examples](#sources-examples)
  * [Links](#links)


### Introduction

  This mod intends to let you set a configuration of possible deaths which, if validated, would either trigger the KeepInventory even though it is supposedly off, or not trigger it when it is in fact on.
  The idea was given to me by some people who wanted a mod to prevent loosing stuff in specific situations, and I pushed the idea a bit further, adding the possibility to fully customize the execution of the KeepInventory depending on your death.
  Therefore, I here introduce to you, the Conditional Keep Inventory mod. (or CDI)
  
### What it does

  The mod adds a config file to the game, named `conditionalKeepInventory.json`, which includes both a whitelist and a blacklist.
  It also includes rows for two gamerules added to the game. The first one applies at all time: it triggers about the whole mod: `conditionalKeepInventory`. The second one comes in action a bit later.
  When any player dies, if the `keepInventory` vanilla gamerule is set to _true_, and the death matches any condition set in the blacklist, the player's inventory drops anyway. If set to _false_, the mod searches for matching death into the whitelist, and if it finds any, the player gets to keep its inventory. But here comes the second gamerule, `conditionalDoVanishing`, which makes it so that, when the player gets to keep its inventory despite the vanilla `keepInventoory`, and this gamerule is set to _true_, all the items with the _Curse of Vanishing_ in their inventory still get to disappear in thin air.
  Finally, when the server stops, the mod saves the config to the config file to make sure it matches the gamerules.

### How it works

  When the server starts, the mod tries to load the config file, or (re)creates it, in order to save on cache the corresponding data.
  Also, it checks if it is set to _true_ the `doBackupOnStartup` option, in which case it automatically creates a backup, in the `config/backups/conditionalKeepInventory/` folder (which gets created if need be). Of course, if the mod cannot properly parse the config file even though it exists, it first creates a backup of the wrong one and create a new one, so that all isn't lost.
  Then, the mod listens to the damages taken by any player. That is, in order to compare it to the config data.
  Last but not least, comparing each vanilla and this mod's keepInventory gamerules, it either disables the drop, make it happen, or let the game do its default.
  In the end, as the server stops, the mod saved the config to the config file, disregarding any changes brought to any, but to ensure that the gamerules edits get saved in the file as well, so that they can be edited off-game.
  
### Config structure

  As said earlier, the config contains multiple information:
  * **enabled** _boolean_: toggles the mod acting upon player death (also linked with the `conditionalKeepInventory` gamerule)
  * **doVanish** _boolean_: toggles the _Curse of Vanishing_ on death when the player keeps its inventory due to the whitelist (also linked with the `conditionalDoVanishing` gamerule)
  * **doBackupOnStartup** _boolean_: sets whether the mod shall make a backup of the current config file any time it loads
  * **whitelist** _list_: list of all the different conditions in which the whitelist event shall be validated
  * **blacklist** _list_: list of all the different conditions in which the blacklist event shall be validated\
    Structure of a whitelist/blacklist object:
    * **id** _integer_: id of the condition, must be unique for its list. _required_
    * **toggle** _boolean_: allows turning off a condition without removing it, considered as _true_ if missing
    * **killer_entity** _string_: the identifier of the entity which killed the player, in the format `"<namespace>:<entity_type>"` (if the namespace is missing, it will be considered as being `minecraft`). E.g. `"minecraft:zombie"`, `"horse"` (even though I don't know in which situation would a horse kill you). Warning! If left empty (`""`), the death shall not be caused by another entity!
    * **source** _string_: the source of death. Although a full list cannot be made, I still can provide a few examples [here](#sources-examples). Warning! As a death cause cannot have no source, leaving it empty would result in the same as setting the condition's `toggle` property to _false_!
    * **projectile** _string_: the identifier of the projectile entity which caused the death, written the same way as the `killer_entity` property. E.g. `"minecraft:arrow"`, `"trident"`, or `"small_fireball"` for the blazes' fireballs.
    * **held_item** _string_: the identifier of the item held by the killer entity (main hand). Can be used to get the killing weapon/item, although with no more details now.\
      **Warning!** You can let any of the `killer_entity`, `source`, `projectile` or `held_item` missing from a condition, but a condition with none of them will be skipped, even in the config data parsing, and would therefore not be reachable in-game.
    **Other warning!** You may add whatever else you want into the file, the mod won't take it in consideration. But as soon as the server shuts down, it will all get deleted as the mod would overwrite the file.

  Example of config file:\
       *the "_" properties only are here used as comments*
```json
{
  "enabled":true,
  "doVanishingCurse":true, 
  "_":"makes the Curse of Vanishing enchant take effect if a whitelist death event gets triggered",
  "whitelist":[
    {
      "id":1,
      "toggle":true,
      "source":"drown",
      "_":"applies when the player drowns, regardless of all other elements"
    },
    {
      "id":2,
      "toggle":false,
      "_":"won't trigger no matter what",
      "killer_entity": "minecraft:skeleton",
      "projectile": "arrow",
      "held_item": "bow",
      "_":"would have triggered only when the player gets shot by an arrow fired by a skeleton holding a bow it its main hand, if the toggle wasn't false"
    }
  ],
  "blacklist":[
    {
      "id":1,
      "toggle":true,
      "source":"anvil",
      "_":"applies when the player is squashed by an anvil, regardless of all other elements"
    }
  ]
}
```

### Command usage

  _(Still in development, coming sooner or later)_
  
### Sources examples
  
  * _"inFire"_ : when dying from fire.
  * _"lightningBolt"_ : when dying from a lightning bolt.
  * _"onFire"_ : when suffering from a too hot environment (too hot biomes, doesn't exist in vanilla without datapacks).
  * _"lava"_ : when the sauna's water seems a little too bright, but you still dive in.
  * _"hotFloor"_ : when dying from being on a magma block.
  * _"inWall"_ : dying of suffocation.
  * _"cramming"_ : being squished between too many entities.
  * _"drown"_ : when air lacks in your lungs.
  * _"starve"_ : you really should have spared that last piece of bread.
  * _"cactus"_ : now that's just lame... Yea, you really hugged that cactus to death... But yours.
  * _"fall"_ : you better not forget to hold that sneak button next time.
  * _"flyIntoWall"_ : why did you think humans don't have wings? You'd better have kept those elytras for yourself.
  * _"outOfWorld"_ : oh well... that one you better have on whitelist because otherwise the stuff would be kinda ruined.
  * _"magic"_ : we might guess playing with alchemy is even more dreadful than toying with food now.
  * _"wither"_ : yup, that tall skeleton really got you there... Or was it that puny little flower?
  * _"anvil"_ : someone might have done it on purpose. Anvils aren't supposed to fall from the sky!
  * _"fallingBlock"_ : when dying from a fallingBlockEntity. That works with some falling_block's nbt that isn't used by default.
  * _"dragonBreath"_ : No, you're not that Donkey, dragons don't blow you sweet words.
  * _"dryout"_ : I don't know how you could die from that, at least it wouldn't be vanilla, because that by default is for axolotls.
  * _"sweetBerryBush"_ : Those damn bushes. Again. They really are annoying, aren't they?
  * _"freeze"_ : Now you've gotten into history! ... The future one's, when they'll find you frozen in ice.
  * _"fallingStalactite"_ : so those caverns wouldn't be as safe as they might seem, apparently. Death can literally _fall_ upon you anytime.
  * _"stalagmite"_ : But it was looking so cute! Who could have guessed it would be so sharp!
  * _"mob"_ : Something just killed you, no matter what it was. But it was alive.
  * _"sting"_ : Those bees can really be deadly when you annoy them.
  * _"player"_ : Murderer! Assassin! He just killed someone!
  * _"fireworks"_ : It looks pretty, but better stay at a reasonable distance.
  * _"thorns"_ : And say you wanted to kill it... It really is worse than a cactus.

### Links

 * [CurseForge]()
 * Modrinth (yet to come)
 * [Fabric API (modrinth)](https://modrinth.com/mod/fabric-api)
 * [Fabric API (curseforge)](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
 * [Fabric Mod Loader](https://fabricmc.net/use/)
