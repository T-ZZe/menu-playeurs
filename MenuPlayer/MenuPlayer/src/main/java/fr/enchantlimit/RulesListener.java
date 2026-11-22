package fr.enchantlimit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Trident;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Regles du monde (PvP, dimensions) et equilibrage des degats. */
public class RulesListener implements Listener {

    private final LimitManager limits;

    public RulesListener(LimitManager limits) {
        this.limits = limits;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        Entity damager = e.getDamager();

        Player attacker = null;
        if (damager instanceof Player direct) {
            attacker = direct;
        } else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        // PvP global
        if (attacker != null && !attacker.equals(victim)
                && limits.getSetting("pvp_enabled", 1) == 0
                && !attacker.hasPermission("enchantlimit.bypass")) {
            e.setCancelled(true);
            attacker.sendActionBar(Component.text("Le PvP est désactivé.", NamedTextColor.RED));
            return;
        }

        // Equilibrage des degats
        int pct = 100;
        if (damager instanceof EnderCrystal) {
            pct = limits.getSetting("dmg_crystal", 100);
        } else if (damager instanceof ExplosiveMinecart) {
            pct = limits.getSetting("dmg_minecart", 100);
        } else if (damager instanceof TNTPrimed) {
            pct = limits.getSetting("dmg_tnt", 100);
        } else if (damager instanceof Trident) {
            pct = limits.getSetting("dmg_trident", 100);
        } else if (damager instanceof Player melee) {
            Material m = melee.getInventory().getItemInMainHand().getType();
            if (m == Material.MACE) pct = limits.getSetting("dmg_mace", 100);
            else if (m.name().endsWith("_SPEAR")) pct = limits.getSetting("dmg_spear", 100);
            else if (m == Material.TRIDENT) pct = limits.getSetting("dmg_trident", 100);
        }
        if (pct != 100) e.setDamage(e.getDamage() * pct / 100.0);
    }

    /** Explosion d'une Respawn Anchor (type de degats "bad respawn point"). */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAnchorDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (e.getDamageSource().getDamageType() != DamageType.BAD_RESPAWN_POINT) return;
        int pct = limits.getSetting("dmg_anchor", 100);
        if (pct != 100) e.setDamage(e.getDamage() * pct / 100.0);
    }

    /** Nom de la dimension de destination si elle est fermee, sinon null. */
    private String closedName(Location to) {
        if (to == null || to.getWorld() == null) return null;
        World.Environment env = to.getWorld().getEnvironment();
        if (env == World.Environment.NETHER && limits.getSetting("nether_open", 1) == 0) return "Le Nether";
        if (env == World.Environment.THE_END && limits.getSetting("end_open", 1) == 0) return "L'End";
        return null;
    }

    /** Joueur qui passe un portail. */
    @EventHandler(ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent e) {
        String name = closedName(e.getTo());
        if (name == null || e.getPlayer().hasPermission("enchantlimit.bypass")) return;
        e.setCancelled(true);
        e.getPlayer().sendMessage(Component.text(name + " est fermé pour le moment.", NamedTextColor.RED));
    }

    /** Autres entites (perles, objets, mobs, minecarts...) qui passent un portail. */
    @EventHandler(ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent e) {
        if (closedName(e.getTo()) == null) return;
        if (e.getEntity() instanceof Projectile proj && proj.getShooter() instanceof Player shooter
                && shooter.hasPermission("enchantlimit.bypass")) {
            return;
        }
        e.setCancelled(true);
    }

    /** Securite : une perle ne peut pas t'envoyer dans une dimension fermee. */
    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        Location to = e.getTo();
        if (to == null || to.getWorld() == null || to.getWorld().equals(e.getFrom().getWorld())) return;
        String name = closedName(to);
        if (name == null || e.getPlayer().hasPermission("enchantlimit.bypass")) return;
        e.setCancelled(true);
        e.getPlayer().sendMessage(Component.text(name + " est fermé pour le moment.", NamedTextColor.RED));
    }
}
