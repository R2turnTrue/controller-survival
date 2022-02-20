package xyz.r2turntrue.controller

import org.bukkit.Bukkit

object Task: Runnable {

    override fun run() {
        for (entry in EventListener.morphed) {
            val player = Bukkit.getPlayer(entry.key) ?: continue

            if(entry.value.health <= 0) {
                EventListener.exitMorph(player)
                continue
            }

            entry.value.teleport(player.location)
            player.health = entry.value.health
        }
    }

}