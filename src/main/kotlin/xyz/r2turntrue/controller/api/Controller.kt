package xyz.r2turntrue.controller.api

import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Slime
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import xyz.r2turntrue.controller.ControllerPlugin
import xyz.r2turntrue.controller.EventListener

object Controller {

    fun morph(player: Player, st: LivingEntity) {
        if(st is Player)
            return
        if(EventListener.morphed.entries.find { e -> e.value.uniqueId == st.uniqueId } != null)
            return
        if(EventListener.cooltimes.contains(st.uniqueId))
            return

        player.gameMode = GameMode.SURVIVAL
        player.allowFlight = st.type == EntityType.BEE || st.type == EntityType.BAT || st.type == EntityType.PARROT || st.type == EntityType.PHANTOM || st.type == EntityType.ENDER_DRAGON || st.type == EntityType.WITHER || st.type == EntityType.GHAST
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = st.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.baseValue
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = st.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue ?: 1.0
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = st.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue ?: 4.0

        if(st is Slime) {
            player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 3, false, false))
        }

        //player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = st.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue ?: 0.10000000149011612
        player.health = st.health
        player.foodLevel = 20

        EventListener.morphed[player.uniqueId] = st
        //st.setAI(false)

        val packet = PacketPlayOutEntityDestroy(st.entityId)
        (player as CraftPlayer).handle.b.a(packet)

        for (plr in Bukkit.getOnlinePlayers().filter { b -> b.uniqueId != player.uniqueId }) {
            plr.hidePlayer(ControllerPlugin.instance, player)
        }
    }

    fun exitMorph(player: Player) {
        val morph = EventListener.morphed[player.uniqueId] ?: return

        morph.setAI(true)

        player.gameMode = GameMode.SPECTATOR

        val morphNms = (morph as CraftLivingEntity).handle

        val packet = PacketPlayOutSpawnEntityLiving(morphNms)
        (player as CraftPlayer).handle.b.a(packet)

        val packet2 = PacketPlayOutEntityMetadata(morph.entityId, morphNms.ai(), true)
        player.handle.b.a(packet2)

        for (plr in Bukkit.getOnlinePlayers()) {
            plr.showPlayer(ControllerPlugin.instance, player)
        }

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.baseValue = 20.0
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.baseValue = 1.0
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)?.baseValue = 4.0
        //player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)?.baseValue = 0.10000000149011612
        player.health = 20.0
        player.foodLevel = 20
        player.removePotionEffect(PotionEffectType.JUMP)

        EventListener.morphed.remove(player.uniqueId)
    }

}