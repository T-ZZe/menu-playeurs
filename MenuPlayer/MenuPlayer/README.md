# MenuPlayer (EnchantLimit)

Plugin Paper **1.21.11** (Java 21) : `/menu` (OP, aliases `/enchantement`, `/enchlimit`).

## Onglets

- **Enchantement** : niveaux d'enchantement par equipement (Items / Armures), limites de pieces portees,
  limites d'objets (Wind Charge jusqu'a 2 stacks, perles, gapples, totems, Force II), cooldowns.
- **PvP** : combat tag (duree 45 / 30 / 20 / 15 s), objets autorises ou interdits en combat
  (elytres, masse, lance, perles...), mort si deconnexion en combat.
  Le combat tag remplace CombatTagPlugin (desactive automatiquement s'il est present : supprime son jar).
- **Equilibrage** : degats de la masse, de la lance, du trident, des cristaux, des Respawn Anchors,
  de la TNT et des minecarts TNT (100 / 75 / 50 / 25 %).
- **Monde** : PvP global, Nether ouvert/ferme, End ouvert/ferme (perles et entites comprises).

Permission `enchantlimit.bypass` : ignore limites, cooldowns, combat tag et regles du monde (aucun joueur par defaut).

Compilation : GitHub Actions -> artefact `MenuPlayer` (`MenuPlayer.jar`).
