package fr.enchantlimit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class EnchantLimitPlugin extends JavaPlugin {

    private LimitManager limits;
    private GuiManager gui;

    @Override
    public void onEnable() {
        limits = new LimitManager(this);
        limits.load();
        gui = new GuiManager(limits);

        LimitListener listener = new LimitListener(this, limits);
        CombatManager combat = new CombatManager(limits);
        CooldownListener cooldowns = new CooldownListener(limits, combat);
        ItemLimitListener itemLimits = new ItemLimitListener(limits);
        RulesListener rules = new RulesListener(limits);

        var pm = Bukkit.getPluginManager();
        pm.registerEvents(gui, this);
        pm.registerEvents(listener, this);
        pm.registerEvents(combat, this);
        pm.registerEvents(cooldowns, this);
        pm.registerEvents(itemLimits, this);
        pm.registerEvents(rules, this);

        // Le combat tag est maintenant integre : on coupe l'ancien plugin pour eviter les doublons.
        Plugin old = pm.getPlugin("CombatTagPlugin");
        if (old != null && old.isEnabled()) {
            getLogger().warning("CombatTagPlugin detecte : le combat tag est inclus dans EnchantLimit, "
                    + "l'ancien plugin est desactive. Tu peux supprimer CombatTagPlugin.jar.");
            pm.disablePlugin(old);
        }

        var cmd = getCommand("menu");
        if (cmd != null) cmd.setExecutor(new EnchantCommand(gui));

        // Pieces d'armure portees : controle toutes les 5 ticks
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) listener.enforce(p);
        }, 5L, 5L);

        // Quantite d'objets portes + compte a rebours du combat : toutes les secondes
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) itemLimits.enforce(p);
            combat.tick();
        }, 20L, 20L);

        // Niveaux d'enchantement : controle des inventaires toutes les 2 secondes
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Inventory inv = p.getInventory();
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack it = inv.getItem(i);
                    if (it != null && limits.clamp(it)) inv.setItem(i, it);
                }
            }
        }, 40L, 40L);
    }

    @Override
    public void onDisable() {
        if (limits != null) limits.save();
    }
}
