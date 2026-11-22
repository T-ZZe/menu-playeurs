package fr.enchantlimit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Combat tag (reprend le comportement de CombatTagPlugin) avec une duree reglable
 * et des objets autorises / interdits pendant le combat.
 */
public class CombatManager implements Listener {

    private final LimitManager limits;
    private final Map<UUID, Long> end = new HashMap<>();

    public CombatManager(LimitManager limits) {
        this.limits = limits;
    }

    public int seconds() {
        return limits.getSetting("combat_seconds", 45);
    }

    public boolean punishQuit() {
        return limits.getSetting("combat_punish", 1) == 1;
    }

    public boolean isInCombat(Player p) {
        Long fin = end.get(p.getUniqueId());
        return fin != null && fin > System.currentTimeMillis();
    }

    private boolean bypass(Player p) {
        return p.hasPermission("enchantlimit.bypass");
    }

    private void tag(Player p) {
        if (bypass(p)) return;
        boolean already = isInCombat(p);
        end.put(p.getUniqueId(), System.currentTimeMillis() + seconds() * 1000L);

        boolean elytraAllowed = limits.isAllowedInCombat(PvpItem.ELYTRA);
        if (!elytraAllowed && p.isGliding()) p.setGliding(false);

        if (!already) {
            String msg = "Tu es en combat pendant " + seconds() + "s.";
            if (!elytraAllowed) msg += " Pas d'élytra.";
            if (punishQuit()) msg += " Te déconnecter te tue.";
            p.sendMessage(Component.text(msg, NamedTextColor.RED));
        }
    }

    // ------------------------------------------------------------ evenements

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        Player attacker = null;
        if (e.getDamager() instanceof Player direct) {
            attacker = direct;
        } else if (e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }
        if (attacker == null || attacker.equals(victim)) return;
        tag(victim);
        tag(attacker);
    }

    @EventHandler
    public void onGlide(EntityToggleGlideEvent e) {
        if (!(e.getEntity() instanceof Player p) || !e.isGliding()) return;
        if (isInCombat(p) && !limits.isAllowedInCombat(PvpItem.ELYTRA)) {
            e.setCancelled(true);
            p.sendActionBar(Component.text("Élytres interdites en combat !", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        boolean punish = isInCombat(p) && punishQuit() && !Bukkit.isStopping();
        end.remove(p.getUniqueId());
        if (punish) p.setHealth(0.0);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        end.remove(e.getEntity().getUniqueId());
    }

    /** Appele chaque seconde : compte a rebours et fin de combat. */
    public void tick() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<UUID, Long>> it = end.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> en = it.next();
            Player p = Bukkit.getPlayer(en.getKey());
            if (p == null) {
                it.remove();
                continue;
            }
            long rest = (en.getValue() - now + 999) / 1000;
            if (rest <= 0) {
                it.remove();
                p.sendMessage(Component.text("Tu n'es plus en combat.", NamedTextColor.GREEN));
            } else {
                p.sendActionBar(Component.text("En combat : " + rest + "s", NamedTextColor.RED));
            }
        }
    }
}
