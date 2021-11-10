package com.github.unldenis.manager;

import com.github.unldenis.SquidGame;
import com.github.unldenis.data.DataManager;
import com.github.unldenis.gamelogic.MainGame;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GameManager {

    private final SquidGame plugin;
    private final DataManager gamesYml;

    private Set<MainGame> mainGames = new HashSet<>();

    /**
     * Costrunctor of game manager
     * @param plugin main plugin
     */
    public GameManager(@NonNull SquidGame plugin) {
        this.plugin = plugin;
        this.gamesYml = new DataManager(plugin, "games.yml");
        loadAll();

    }

    /**
     * Method used to find a maingame from a player
     * @param player player to find
     * @return maingame where player is playing, if present
     */
    public Optional<MainGame> find(Player player) {
        return mainGames.stream()
                .filter(m -> m.find(player) != null)
                .findFirst();
    }

    /**
     * Method used to find a maingame from a spectator
     * @param player player to find
     * @return maingame where player is spectating, if present
     */
    public Optional<MainGame> findSpec(Player player) {
        return mainGames.stream()
                .filter(m -> m.findSpec(player) != null)
                .findFirst();
    }

    /**
     * Method used to find a maingame from a string
     * @param name name of game to find
     * @return maingame if string name is present
     */
    public Optional<MainGame> find(String name) {
        return mainGames.stream()
                .filter(m -> m.getName().equals(name))
                .findFirst();
    }


    /**
     * Method used to save in config
     * @param mainGame maingame to save
     */
    public void save(MainGame mainGame) {
        mainGame.save(gamesYml);
    }


    /**
     * Method used to load all games from games.yml
     */
    public void loadAll() {
        FileConfiguration cfg = gamesYml.getConfig();
        var list = cfg.getConfigurationSection("squidgames.");
        if(list!=null) {
            for (String s : list.getKeys(false)) {
                MainGame mainGame = new MainGame(plugin, s);
                mainGame.load(cfg);
                mainGames.add(mainGame);
            }
        }
        plugin.getLogger().info("Loaded " + mainGames.size()+ " games");

    }

    /**
     * Getter of games
     * @return set of maingames
     */
    public Set<MainGame> getMainGames() {
        return mainGames;
    }
}
