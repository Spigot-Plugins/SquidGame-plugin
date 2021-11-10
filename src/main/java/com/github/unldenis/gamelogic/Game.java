package com.github.unldenis.gamelogic;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Random;

@Getter
public abstract class Game {

    protected final MainGame mainGame;
    private final String name;
    private final String description;
    @Setter private ArrayList<Location> spawns = new ArrayList<>();
    /**
     * Constructor of generic Game
     * @param mainGame maingame of this abstract game
     * @param name name of the game
     * @param description description of the game
     */
    protected Game(@NonNull MainGame mainGame, @NonNull String name, @NonNull String description) {
        this.mainGame = mainGame;
        this.name = name;
        this.description = description;
    }

    /**
     * Method called after lobby countdown in maingame
     */
    public abstract void start();


    /**
     * Method called to reset a game
     */
    public abstract void reset();


    /**
     * Method that teleports spectators in game
     */
    public abstract void teleportSpectators();


    /**
     * A method that eventually does something specific in the minigame when a player exits
     * @param gamePlayer gamePlayer that quits
     */
    public abstract void onQuit(GamePlayer gamePlayer);


    /**
     * Method that saves the game in the games.yml
     */
    public abstract void save(FileConfiguration cfg, String prefix);

    /**
     * Method that loades the game from games.yml
     */
    public abstract void load(FileConfiguration cfg, String prefix);

    /**
     * Method that return a random spawn
     * @return a spawn if created
     */
    public @NonNull Location getRandomSpawn(ArrayList<Location> list) {
        return list.get(new Random().nextInt(list.size()));
    }


}
