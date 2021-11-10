package com.github.unldenis.obj.honeycomb;


import com.github.unldenis.gamelogic.GamePlayer;
import com.github.unldenis.util.SpigotUtils;
import com.github.unldenis.util.StuffUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import java.util.ArrayList;

@Getter
@Setter
public class Shape {

    private final String name;
    private final Material material;
    private Location spawn;
    private Location pos1;
    private Location pos2;
    private boolean[][] structure;

    private ArrayList<GamePlayer> gamePlayers = new ArrayList<>();
    private boolean right = false;


    /**
     * Constructor of a shape
     * @param name name of the figure
     * @param material material of blocks
     */
    public Shape(String name, Material material) {
        this.name = name;
        this.material = material;
    }


    /**
     * Method that checks if shape is right
     * @return true if this saved shaped is equals to current
     */
    public boolean check() {
        return StuffUtils.equal(structure, SpigotUtils.structBetweenTwoLocations(pos1, pos2, material));
    }


    /**
     * Method that save the shape
     */
    public void load() {
        structure = SpigotUtils.structBetweenTwoLocations(pos1, pos2, material);
    }

}
