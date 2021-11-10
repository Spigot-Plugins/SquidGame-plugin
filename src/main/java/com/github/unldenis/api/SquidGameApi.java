package com.github.unldenis.api;


import com.github.unldenis.SquidGame;
import com.github.unldenis.gamelogic.MainGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public abstract class SquidGameApi {

    /**
     * Constructor that automatically adds your implementation to the plugin
     */
    public SquidGameApi() {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("SquidGame")) {
            SquidGame squidGame = (SquidGame) Bukkit.getServer().getPluginManager().getPlugin("SquidGame");
            squidGame.register(this);
        }
    }


    /**
     * Method which is called when a player enters a game
     * @param player player who entered
     * @param mainGame player game
     */
    public abstract void onPlayerJoin(Player player, MainGame mainGame);

    /**
     * Method which is called when a player quit a game
     * @param player player who quit
     * @param mainGame player game
     */
    public abstract void onPlayerQuit(Player player, MainGame mainGame);


    /**
     * Method which is called when a player is eliminated
     * @param player player who is eliminated
     * @param mainGame player game
     */
    public abstract void onPlayerEliminated(Player player, MainGame mainGame);


}
