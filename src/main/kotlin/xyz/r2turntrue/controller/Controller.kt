package xyz.r2turntrue.controller

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class Controller: JavaPlugin() {

    companion object {
        lateinit var instance: Controller
    }

    override fun onEnable() {
        instance = this
        server.pluginManager.registerEvents(EventListener, this)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, Task, 0, 1)
    }

}