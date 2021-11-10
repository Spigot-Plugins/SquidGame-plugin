package com.github.unldenis;

import com.github.unldenis.command.MainCommand;
import com.github.unldenis.helper.gui.Menu;
import com.github.unldenis.listener.GameEvents;
import com.github.unldenis.listener.GreenRedLightEvents;
import com.github.unldenis.listener.HoneycombEvents;
import com.github.unldenis.manager.GameManager;
import org.bukkit.plugin.java.JavaPlugin;

public class SquidGame extends JavaPlugin {

    private GameManager gameManager;

    @Override
    public void onEnable() {
        /**
         * Loading gameManager
         */
        gameManager = new GameManager(this);


        /**
         * Loading events
         */
        Menu.register(this);
        new GameEvents(this);
        new GreenRedLightEvents(this);
        new HoneycombEvents(this);

        /**
         * Loading commands
         */
        new MainCommand(this);
    }

    /**
     * Getter of gameManager
     * @return manager of games
     */
    public GameManager getGameManager() {
        return gameManager;
    }
}
