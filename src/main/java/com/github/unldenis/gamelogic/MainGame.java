package com.github.unldenis.gamelogic;

import com.github.unldenis.SquidGame;
import com.github.unldenis.data.DataManager;
import com.github.unldenis.gamelogic.game.HoneycombGame;
import com.github.unldenis.helper.util.ReflectionUtil;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Optional;


/**
 * Class representing a match
 * You can see this class as the organizer / manager of multiple games.
 * @author unldenis
 */
public class MainGame {

    //static
    private final SquidGame plugin;
    @Getter private final String name;
    @Getter private ArrayList<Game> gameList = new ArrayList<>();
    private Integer maxPlayers = 20;
    private Integer minPlayers = 2;
    private Location lobbyLoc;

    //dynamic
    private int round = 0;
    private ArrayList<GamePlayer> players = new ArrayList<>();
    @Getter private GameStatus gameStatus = GameStatus.WAITING;
    private BukkitTask currentTask = null;
    private ArrayList<GamePlayer> spectators = new ArrayList<>();


    /**
     * Constructor of Game class
     * @param plugin squidgame object
     * @param name name of the game
     */
    public MainGame(@NonNull SquidGame plugin, @NonNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Method for joining a player in squidgame.
     * @param player player that requested to join
     */
    public void join(@NonNull Player player) {
        Optional<MainGame> alreadyPly = plugin.getGameManager().find(player);
        if(alreadyPly.isPresent()) {
            player.sendMessage(ChatColor.RED+"You're already in game");
            return;
        }
        if(round>0) {
            player.sendMessage(ChatColor.RED+"This game is already in second round");
            return;
        }
        if(players.size()==maxPlayers) {
            player.sendMessage(ChatColor.RED+"This game is full");
            return;
        }
        GamePlayer gamePlayer = new GamePlayer(player);
        gamePlayer.resetPlayer();
        gamePlayer.teleport(lobbyLoc);
        players.add(gamePlayer);

        for(int j=0; j<100; j++)
            player.sendMessage("");
        sendMessage("&a"+player.getName()+" &7joined in SquidGame (&a"+players.size()+"&7/"+maxPlayers+")");

        //add api reference
        plugin.execute(api -> api.onPlayerJoin(player, this));

        if(gameStatus.equals(GameStatus.WAITING) && players.size()==minPlayers)  {
            gameStatus = GameStatus.STARTING;
            sendTitle("&7First game is...", "");
            currentTask = new BukkitRunnable() {
                int timer = 20;
                Game game = getCurrentGame();
                @Override
                public void run() {
                    if(timer==20) {
                        sendTitle("&4" + game.getName(), "");
                        playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
                    }
                    if(timer==0) {
                        cancel();
                        sendMessage(game.getDescription());
                        game.teleportSpectators();
                        game.start();
                        return;
                    }
                    if(timer==20 || timer == 10 || timer<=5)
                        sendActionBar("Game starting in &a" + timer+"&fs");
                    timer--;
                }
            }.runTaskTimer(plugin, 30L, 20L);
        }

    }

    /**
     * Method used for when a player leaves the game by command or quits
     * @param player player that left
     */
    public void leave(Player player) {
        sendMessage("&c"+player.getName()+" &7left (&c"+players.size()+"&7/"+maxPlayers+")");

        GamePlayer gamePlayer = find(player);
        players.remove(gamePlayer);
        gamePlayer.resetPlayer();
        gamePlayer.teleport(lobbyLoc);
        gamePlayer.rollback();

        //add api reference
        plugin.execute(api -> api.onPlayerQuit(player, this));


        //check if game is starting
        if(currentTask!=null && !currentTask.isCancelled() && gameStatus.equals(GameStatus.STARTING) &&  players.size()< minPlayers) {
            currentTask.cancel();
            setGameStatus(GameStatus.WAITING);
            sendActionBar("&cGame starting stopped");
            return;
        }

        //game already started
        if(gameStatus.equals(GameStatus.PLAYING)) {
            if(players.size()==1)
                endGame();
            else
                getCurrentGame().onQuit(gamePlayer);//added for eventually minigame settings

        }



    }

    /**
     * Method used for when a spectator leaves the game by command or quits
     * @param player player that left
     */
    public void leaveSpec(Player player) {
        GamePlayer gamePlayer = findSpec(player);
        spectators.remove(gamePlayer);
        gamePlayer.getPlayer().setGameMode(GameMode.ADVENTURE);
        gamePlayer.teleport(lobbyLoc);
        gamePlayer.rollback();
    }

    /**
     * Method that is called when a minigame is finished and you move on to the one after it
     */
    public void nextGame() {
        if(round+1==gameList.size()) {
            endGame();
            return;
        }
        round++;

        gameStatus = GameStatus.STARTING;
        sendTitle("&7Next game is...", "");
        currentTask = new BukkitRunnable() {
            int timer = 20;
            Game game = getCurrentGame();
            @Override
            public void run() {
                if(timer==20) {
                    sendTitle("&4" + game.getName(), "");
                    playSound(Sound.UI_TOAST_CHALLENGE_COMPLETE);
                }
                if(timer==0) {
                    cancel();
                    sendMessage(game.getDescription());
                    game.teleportSpectators();
                    game.start();
                    return;
                }
                if(timer==20 || timer == 10 || timer<=5)
                    sendActionBar("Game starting in &a" + timer+"&fs");
                timer--;
            }
        }.runTaskTimer(plugin, 30L, 20L);
    }


    /**
     * Method that eliminates a player from the game
     * @param player player to eliminate
     */
    public void eliminate(Player player ) {
        eliminate(find(player));
    }

    /**
     * Method that eliminates a gamePlayer from the game
     * @param gamePlayer gamePlayer to eliminate
     */
    public void eliminate(GamePlayer gamePlayer) {
        sendMessage("&c"+gamePlayer.getPlayer().getName()+" &7eliminated");

        players.remove(gamePlayer);
        gamePlayer.resetPlayer();

        //set to spectator
        spectators.add(gamePlayer);


        //add api reference
        plugin.execute(api -> api.onPlayerEliminated(gamePlayer.getPlayer(), this));


        if(players.size()==1) {
            if(!(getCurrentGame() instanceof HoneycombGame)) getCurrentGame().reset();
            new BukkitRunnable() {
                @Override
                public void run() {
                    endGame();
                }
            }
            .runTaskLater(plugin, 60L);
        }
    }

    /**
     * Method called when game is end
     */
    private void endGame() {

        for(GamePlayer gP: players) {
            gP.sendTitle("&eYou won", "");
            gP.resetPlayer();
            gP.teleport(lobbyLoc);
            gP.rollback();
        }
        for(GamePlayer spec: spectators ) {
            spec.getPlayer().setGameMode(GameMode.ADVENTURE);
            spec.teleport(lobbyLoc);
            spec.rollback();
        }

        reset();

    }


    /**
     * Method that saves the game in the config.yml
     */
    public void save(DataManager gamesYml) {
        FileConfiguration cfg = gamesYml.getConfig();
        String prefix = "squidgames."+name+".";
        cfg.set(prefix+"maxPlayers", maxPlayers);
        cfg.set(prefix+"minPlayers", minPlayers);
        cfg.set(prefix+"lobbyLoc", lobbyLoc);
        prefix+="games.";
        for(int j=0; j<gameList.size(); j++) {
            Game game = gameList.get(j);
            cfg.set(prefix+j+".name", game.getName());
            cfg.set(prefix+j+".description", game.getDescription());
            cfg.set(prefix+j+".spawns", game.getSpawns());
            game.save(cfg, prefix+j+".");
        }
        gamesYml.saveConfig();
    }


    /**
     * Method that load the game from config.yml
     */
    public void load(FileConfiguration cfg) {
        String prefix = "squidgames."+name+".";
        maxPlayers = cfg.getInt(prefix+"maxPlayers");
        minPlayers = cfg.getInt(prefix+"minPlayers");
        lobbyLoc = cfg.getLocation(prefix+"lobbyLoc");
        prefix+="games";
        var list =  cfg.getConfigurationSection(prefix);
        if(list!=null) {
            for(String s: list.getKeys(false)) {
                Class<?> clazz = null;
                try {
                    clazz = Class.forName("com.github.unldenis.gamelogic.game."+cfg.getString(prefix+"."+s+".name")+"Game");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    continue;
                }
                try {
                    Game game = ReflectionUtil.instantiate(clazz, this);
                    game.setSpawns((ArrayList<Location>) cfg.getList(prefix+"."+s+".spawns"));
                    game.load(cfg, prefix+"."+s+".");
                    gameList.add(game);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    /**
     * Method that return the actual game
     * @return the game object
     */
    public Game getCurrentGame() {
        return gameList.get(round);
    }

    /**
     * Method that send a title to all players
     * @param title title to send
     * @param subTitle subTitle to send
     */
    public void sendTitle(@NonNull String title, String subTitle) {
        for(GamePlayer gamePlayer: players)
            gamePlayer.sendTitle(title, subTitle);
    }

    /**
     * Method that send actionBar message to all players
     * @param m message to send
     */
    public void sendActionBar(@NonNull String m) {
        for(GamePlayer gamePlayer: players)
            gamePlayer.sendActionBar(m);
    }

    /**
     * Method that send a message to all players
     * @param m message to send
     */
    public void sendMessage(@NonNull String m) {
        for(GamePlayer gamePlayer: players)
            gamePlayer.sendMessage(m);
    }


    /**
     * Method that plays a sound to all players
     * @param s sound to play
     */
    public void playSound(@NonNull Sound s) {
        for(GamePlayer gamePlayer: players)
            gamePlayer.playSound(s);
    }

    /**
     * Method that find a player in maingame
     * @param player player to find
     * @return gameplayer object of the player parameter
     */
    public GamePlayer find(@NonNull Player player) {
        for(GamePlayer gamePlayer: players)
            if(gamePlayer.getPlayer().getEntityId()==player.getEntityId())
                return gamePlayer;
        return null;
    }

    /**
     * Method that find a spectator in maingame
     * @param player player to find
     * @return gameplayer object of the player parameter
     */
    public GamePlayer findSpec(@NonNull Player player) {
        for(GamePlayer gamePlayer: spectators)
            if(gamePlayer.getPlayer().getEntityId()==player.getEntityId())
                return gamePlayer;
        return null;
    }

    /**
     * Method that teleports all players
     * @param loc location where to teleport
     */
    private void teleport(@NonNull Location loc) {
        for (GamePlayer gamePlayer : players)
            gamePlayer.teleport(loc);
    }

    /**
     * Reset to default the maingame
     */
    private void reset() {
        round = 0;
        players.clear();
        gameStatus = GameStatus.WAITING;
        currentTask = null;
        spectators.clear();
    }


    /**
     * Method that returns gameplayers
     * @return list of gameplayers
     */
    public @NonNull ArrayList<GamePlayer> getPlayers() {
        return players;
    }


    /**
     * Method that returns spectators
     * @return list of gameplayers
     */
    public @NonNull ArrayList<GamePlayer> getSpectators() {
        return spectators;
    }


    /**
     * Method that returns SquidGame instance
     * @return main plugin
     */
    public @NonNull SquidGame getPlugin() {
        return plugin;
    }

    /**
     * Method that returns lobby location
     * @return lobby location
     */
    public Location getLobbyLoc() {
        return lobbyLoc;
    }

    /**
     * Setter of gamestatus
     * @param gameStatus new gamestatus to set
     */
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }
}
