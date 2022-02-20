package xyz.r2turntrue.controller.api

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class MorphedPlayerAttackEvent(val player: Player, var damage: Boolean = false): Event() {

    override fun getHandlers(): HandlerList {
        return HANDLERS;
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

}