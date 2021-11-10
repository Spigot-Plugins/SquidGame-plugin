package com.github.unldenis.command;

import com.github.unldenis.SquidGame;
import com.github.unldenis.gamelogic.Game;
import com.github.unldenis.gamelogic.MainGame;
import com.github.unldenis.helper.Commands;
import com.github.unldenis.helper.util.ChatUtil;
import com.github.unldenis.helper.util.ReflectionUtil;
import com.github.unldenis.menusystem.objectviewer.MainGameViewer;
import lombok.NonNull;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * Class that register the main command.
 * My library is used to create this command.
 * @see <a href="https://github.com/unldenis/UnldenisHelper">https://github.com/unldenis/UnldenisHelper</a>
 */
public final class MainCommand  {
    private final SquidGame plugin;

    public MainCommand(SquidGame plugin) {
        this.plugin = plugin;
        register();
    }

    private void register() {
        Commands.create("squidgame").handler(((sender, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only playes can run this command");
                return;
            }
            Player player = (Player) sender;
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("help") && args.length == 1) {
                    sendMessage(player, """
                        &4Admin help
                        &7/squidgame create <maingame>
                        &7/squidgame edit <maingame> 
                        &7/squidgame addgame <maingame> <game> 
                        &7/squidgame save <maingame> 
                        &7/squidgame join <maingame>               
                        &7/squidgame quit              
                        """);
                    return;
                } else if (args[0].equalsIgnoreCase("create") && args.length == 2) {
                    Optional<MainGame> game = plugin.getGameManager().find(args[1]);
                    if (game.isPresent()) {
                        sendMessage(player, "&cThis game already exist");
                        return;
                    }

                    MainGame mainGame = new MainGame(plugin, args[1]);
                    plugin.getGameManager().getMainGames().add(mainGame);
                    sendMessage(player, "&7Game &a" + mainGame.getName() + " &7created");
                    return;
                } else if (args[0].equalsIgnoreCase("edit") && args.length == 2) {
                    Optional<MainGame> game = plugin.getGameManager().find(args[1]);
                    if (game.isEmpty()) {
                        sendMessage(player, "&cThis game doesn't exist");
                        return;
                    }
                    new MainGameViewer(player, game.get()).open();
                    return;
                } else if (args[0].equalsIgnoreCase("addgame") && args.length == 3) {
                    Optional<MainGame> optionalMainGame = plugin.getGameManager().find(args[1]);
                    if (optionalMainGame.isEmpty()) {
                        sendMessage(player, "&cThis game doesn't exist");
                        return;
                    }
                    MainGame mainGame = optionalMainGame.get();

                    Class<?> clazz;
                    try {
                        clazz = Class.forName("com.github.unldenis.gamelogic.game."+args[2]+"Game");
                    } catch (ClassNotFoundException e) {
                        sendMessage(player, "&cGame not found, try again");
                        return;
                    }
                    Game game = null;
                    try {
                        game = ReflectionUtil.instantiate(clazz, mainGame);
                        mainGame.getGameList().add(game);
                        sendMessage(player, "&aGame added");
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                } else if (args[0].equalsIgnoreCase("join") && args.length == 2) {
                    Optional<MainGame> game = plugin.getGameManager().find(args[1]);
                    if (game.isEmpty()) {
                        sendMessage(player, "&cThis game doesn't exist");
                        return;
                    }
                    game.get().join(player);
                    return;
                } else if (args[0].equalsIgnoreCase("save") && args.length == 2) {
                    Optional<MainGame> game = plugin.getGameManager().find(args[1]);
                    if (game.isEmpty()) {
                        sendMessage(player, "&cThis game doesn't exist");
                        return;
                    }
                    plugin.getGameManager().save(game.get());
                    sendMessage(player, "&aGame saved");
                    return;
                } else if (args[0].equalsIgnoreCase("quit") && args.length == 1) {
                    Optional<MainGame> game = plugin.getGameManager().find(player);

                    //not playing
                    if (game.isEmpty()) {
                        game = plugin.getGameManager().findSpec(player);

                        //not spectating
                        if(game.isEmpty())
                            sendMessage(player, "&cYou aren't playing");
                            //else spectating
                        else
                            game.get().leaveSpec(player);

                        return;
                    }
                    game.get().leave(player);
                    return;
                }
            }
            player.sendMessage(ChatColor.RED + "Invalid format. Usage: /squidgame help");
        })).bindWith(plugin);
    }



    private void sendMessage(@NonNull Player player, @NonNull String message) {
        player.sendMessage(ChatUtil.color(message));
    }


}
