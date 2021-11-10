package com.github.unldenis.util;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class SpigotUtils {


    public static Location getTargetBlock(@NonNull Player player, double dist) {
        Vector dirToDestination = player.getEyeLocation().getDirection().normalize();
        RayTraceResult rts = player.getWorld().rayTraceBlocks(player.getEyeLocation(), dirToDestination, dist);
        if (rts != null) {
            if (rts.getHitBlock() != null) {
                return rts.getHitBlock().getLocation();
            }
        }
        return null;
    }


    public static boolean[][] structBetweenTwoLocations(Location pos1, Location pos2, Material mat) {
        //We will name each coordinate
        int y = pos1.getBlockY();
        int x1 = pos1.getBlockX();
        int z1 = pos1.getBlockZ();
        int x2 = pos2.getBlockX();
        int z2 = pos2.getBlockZ();
        //Then we create the following integers
        int xMin, zMin;
        int xMax, zMax;
        //Now we need to make sure xMin is always lower then xMax
        if(x1 > x2){ //If x1 is a higher number then x2
            xMin = x2;
            xMax = x1;
        }else{
            xMin = x1;
            xMax = x2;
        }
        //And Z
        if(z1 > z2){
            zMin = z2;
            zMax = z1;
        }else{
            zMin = z1;
            zMax = z2;
        }
        //We create the bidimensional array
        boolean[][] structure = new boolean[xMax-xMin+1][zMax-zMin+1];
        //Now it's time for the loop
        for(int x = xMin; x <= xMax; x ++){
            for(int z = zMin; z <= zMax; z ++){
                structure[x-xMin][z-zMin] = new Location(pos1.getWorld(), x, y, z).getBlock().getType().equals(mat);
            }
        }
        return structure;
    }



}
