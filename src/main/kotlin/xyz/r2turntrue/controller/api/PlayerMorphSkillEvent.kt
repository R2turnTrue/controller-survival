package xyz.r2turntrue.controller.api

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerMorphSkillEvent(val player: Player, val morph: LivingEntity, private var cancelled: Boolean = false): Event(), Cancellable {

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

    override fun isCancelled(): Boolean = this.isCancelled

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

}