package xyz.r2turntrue.controller

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent
import io.papermc.paper.event.player.PlayerArmSwingEvent
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityTargetEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.r2turntrue.controller.api.Controller.exitMorph
import xyz.r2turntrue.controller.api.MorphedPlayerAttackEvent
import xyz.r2turntrue.controller.api.PlayerMorphSkillEvent
import xyz.r2turntrue.controller.api.PlayerMorphedEvent
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object EventListener : Listener {

    val morphed = HashMap<UUID, LivingEntity>()
    val cooltimes = ArrayList<UUID>()

    val pm = Bukkit.getPluginManager()

    @EventHandler
    fun useSkill(event: PlayerInteractEvent) {
        val morph = morphed[event.player.uniqueId]

        if((event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) && morph != null) {
            if(!cooltimes.contains(morph.uniqueId)) {

                fun cool(tick: Long) {
                    cooltimes.add(morph.uniqueId)

                    Bukkit.getScheduler().scheduleSyncDelayedTask(ControllerPlugin.instance, {
                        cooltimes.remove(morph.uniqueId)
                        if(!morph.isDead)
                            morph.isCollidable = true
                    }, tick)
                }

                val evt = PlayerMorphSkillEvent(event.player, morph)
                pm.callEvent(evt)

                if(evt.isCancelled)
                    return

                if(morph is Creeper) {
                    morph.ignite()
                    exitMorph(event.player)
                    cool(200)
                } else if(morph is Wither) {
                    val ws = event.player.launchProjectile(WitherSkull::class.java)
                    ws.velocity = event.player.eyeLocation.direction.multiply(5)
                    ws.isCharged = true
                    morph.isCollidable = false
                    cool(20)
                } else if(morph is Ghast) {
                    val ws = event.player.launchProjectile(Fireball::class.java)
                    ws.velocity = event.player.eyeLocation.direction.multiply(5)
                    morph.isCollidable = false
                    cool(20)
                }
            }
        }
    }

    @EventHandler
    fun spectate(event: PlayerStartSpectatingEntityEvent) {
        val st = event.newSpectatorTarget
        event.isCancelled = true
        if(st !is LivingEntity)
            return
        if(st is Player)
            return
        if(morphed.entries.find { e -> e.value.uniqueId == st.uniqueId } != null)
            return
        if(cooltimes.contains(st.uniqueId))
            return

        val evt = PlayerMorphedEvent(event.player, st)
        pm.callEvent(evt)

        if(evt.isCancelled)
            return

        event.player.gameMode = GameMode.SURVIVAL
        event.player.allowFlight = st.type == EntityType.BEE || st.type == EntityType.BAT || st.type == EntityType.PARROT || st.type == EntityType.PHANTOM || st.type == EntityType.ENDER_DRAGON || st.type == EntityType.WITHER || st.type == EntityType.GHAST
        event.player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = st.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue
        event.player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = st.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue ?: 1.0
        event.player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = st.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue ?: 4.0

        if(st is Slime) {
            event.player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 3, false, false))
        }

        //event.player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = st.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue ?: 0.10000000149011612
        event.player.health = st.health
        event.player.foodLevel = 20

        morphed[event.player.uniqueId] = st
        //st.setAI(false)

        val packet = PacketPlayOutEntityDestroy(st.entityId)
        (event.player as CraftPlayer).handle.b.a(packet)

        for (plr in Bukkit.getOnlinePlayers().filter { b -> b.uniqueId != event.player.uniqueId }) {
            plr.hidePlayer(ControllerPlugin.instance, event.player)
        }
    }

    @EventHandler
    fun damage(event: EntityDamageEvent) {
        if(morphed.contains(event.entity.uniqueId))
            event.isCancelled = true

        if(event.cause == EntityDamageEvent.DamageCause.SUFFOCATION && morphed.entries.find { e -> e.value.uniqueId == event.entity.uniqueId } != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun damageByDamage(event: EntityDamageByEntityEvent) {
        if(event.damager !is Player)
            return

        val evt = MorphedPlayerAttackEvent(event.damager as Player)
        pm.callEvent(evt)

        if(evt.damage)
            return

        if(morphed.contains(event.damager.uniqueId))
            event.isCancelled = true
    }

    /*
    @EventHandler
    fun playerMove(event: PlayerMoveEvent) {
        val morph = morphed[event.player.uniqueId] ?: return

        morph.teleport(event.player.location)
    }
     */

    @EventHandler
    fun hunger(event: FoodLevelChangeEvent) {
        if(morphed.contains(event.entity.uniqueId))
            event.isCancelled = true
    }

    @EventHandler
    fun playerSwingHand(event: PlayerArmSwingEvent) {
        val morph = morphed[event.player.uniqueId] ?: return

        morph.swingMainHand()
    }

    @EventHandler
    fun onSneak(event: PlayerSwapHandItemsEvent) {
        if(event.player.isSneaking)
            exitMorph(event.player)
    }

    /*
    @EventHandler
    fun respawn(event: PlayerRespawnEvent) {
        event.player.gameMode = GameMode.SPECTATOR
    }
     */

    @EventHandler
    fun notTarget(event: EntityTargetEvent) {
        if(morphed.contains(event.target?.uniqueId))
            event.isCancelled = true
    }

    @EventHandler
    fun join(event: PlayerJoinEvent) {
        for (entry in morphed) {
            val player = Bukkit.getPlayer(entry.key)

            if(player != null && player.isOnline) {
                event.player.hidePlayer(ControllerPlugin.instance, player)
            }
        }
    }

    @EventHandler
    fun quit(event: PlayerQuitEvent) {
        exitMorph(event.player)
    }

}
