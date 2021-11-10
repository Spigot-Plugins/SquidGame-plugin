package com.github.unldenis;

import com.github.unldenis.api.SquidGameApi;
import com.github.unldenis.command.MainCommand;
import com.github.unldenis.data.DataManager;
import com.github.unldenis.helper.gui.Menu;
import com.github.unldenis.listener.GameEvents;
import com.github.unldenis.listener.GreenRedLightEvents;
import com.github.unldenis.listener.HoneycombEvents;
import com.github.unldenis.manager.GameManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.function.Consumer;

public class SquidGame extends JavaPlugin {

    private HashSet<SquidGameApi> apiCalls = new HashSet<>();
    private DataManager messages;
    private GameManager gameManager;

    @Override
    public void onEnable() {
        /**
         * Loading messages
         */
        messages = new DataManager(this, "messages.yml");

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
     * Method to use to register your SquidGameApi implementation
     * @param apiImplementation your class implementing SquidGameApi
     */
    public void register(SquidGameApi apiImplementation) {
        apiCalls.add(apiImplementation);
    }

    /**
     * Method used to call the API
     * @param consumer to apply to all registered classes
     */
    public void execute(Consumer<SquidGameApi> consumer) {
        for(SquidGameApi api: apiCalls)
            consumer.accept(api);
    }

    /**
     * Getter of gameManager
     * @return manager of games
     */
    public GameManager getGameManager() {
        return gameManager;
    }

    /**
     * Getter of messages yml
     * @return messages of plugin
     */
    public FileConfiguration getMessages() {
        return messages.getConfig();
    }



}
