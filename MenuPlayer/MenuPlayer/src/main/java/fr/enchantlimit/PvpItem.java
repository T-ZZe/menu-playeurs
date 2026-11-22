package fr.enchantlimit;

import org.bukkit.Material;

/** Objets dont on peut autoriser ou interdire l'usage pendant un combat. */
public enum PvpItem {
    ELYTRA("Élytres", "ELYTRA", null, false),
    MACE("Masse", "MACE", CooldownType.MACE, true),
    SPEAR("Lance", "DIAMOND_SPEAR", CooldownType.SPEAR, true),
    PEARL("Perle de l'Ender", "ENDER_PEARL", CooldownType.PEARL, true),
    WIND_CHARGE("Wind Charge", "WIND_CHARGE", CooldownType.WIND_CHARGE, true),
    TRIDENT("Trident", "TRIDENT", CooldownType.TRIDENT, true),
    FIREWORK("Feu d'artifice", "FIREWORK_ROCKET", CooldownType.FIREWORK, true),
    END_CRYSTAL("Cristal de l'End", "END_CRYSTAL", CooldownType.END_CRYSTAL, true),
    RESPAWN_ANCHOR("Respawn Anchor", "RESPAWN_ANCHOR", CooldownType.RESPAWN_ANCHOR, true),
    TNT_MINECART("Minecart TNT", "TNT_MINECART", CooldownType.TNT_MINECART, true),
    GAPPLE("Pomme d'or enchantée", "ENCHANTED_GOLDEN_APPLE", CooldownType.GAPPLE, true);

    public final String label;
    public final Material icon;
    public final CooldownType link; // action de cooldown correspondante (null pour les elytres)
    public final boolean defaultAllowed;

    PvpItem(String label, String iconName, CooldownType link, boolean defaultAllowed) {
        this.label = label;
        Material m = Material.matchMaterial(iconName);
        this.icon = m == null ? Material.STICK : m;
        this.link = link;
        this.defaultAllowed = defaultAllowed;
    }

    public String key() {
        return "combat_allow_" + name();
    }

    public static PvpItem of(CooldownType t) {
        for (PvpItem i : values()) {
            if (i.link == t) return i;
        }
        return null;
    }
}
