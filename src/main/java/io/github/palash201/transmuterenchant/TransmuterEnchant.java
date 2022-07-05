package io.github.palash201.transmuterenchant;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of our plugin. Before creating a plugin, please make sure you have UltraPrisonCore and spigot in your classpath!
 */
public final class TransmuterEnchant extends JavaPlugin {

    @Override
    public void onEnable() {

        //Firstly check if we have UltraPrisonCore loaded. Make sure to add depend: [UltraPrisonCore] into plugin.yml!

        if (Bukkit.getPluginManager().getPlugin("UltraPrisonCore") == null) {
            this.getLogger().warning("Unable to hook into UltraPrisonCore! Disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        //Create our custom enchant
        Transmuter enchant = new Transmuter();

        //Register it
        enchant.register();

        //You are done. Have fun with your custom enchants! :)

    }

    @Override
    public void onDisable() {
        //You do not have to unregister the enchant. UltraPrisonCore will handle that for you :)
    }
}