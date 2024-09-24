package org.terraform.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.terraform.command.contants.TerraCommand;
import org.terraform.coregen.populatordata.PopulatorDataPostGen;
import org.terraform.data.SimpleBlock;
import org.terraform.main.TerraformGeneratorPlugin;
import org.terraform.utils.MazeSpawner;

import java.util.Random;
import java.util.Stack;

public class MazeCommand extends TerraCommand {

    public MazeCommand(TerraformGeneratorPlugin plugin, String... aliases) {
        super(plugin, aliases);
    }

    @Override
    public @NotNull String getDefaultDescription() {
        return "Spawns a randomised Maze of width and length of 30 and height of 3";
    }

    @Override
    public boolean canConsoleExec() {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender) {

        return sender.isOp();
    }

    @Override
    public void execute(CommandSender sender, Stack<String> args) {

        Player p = (Player) sender;
        PopulatorDataPostGen data = new PopulatorDataPostGen(p.getLocation().getChunk());
        int x = p.getLocation().getBlockX();
        int y = p.getLocation().getBlockY();
        int z = p.getLocation().getBlockZ();
        //		TreeDB.spawnCoconutTree(new Random(), data, x,y,z);
        MazeSpawner spawner = new MazeSpawner(new Random(), new SimpleBlock(data, x, y, z), 30, 30);
        spawner.setMazePeriod(5);
        spawner.prepareMaze();
        spawner.carveMaze(false, Material.WHITE_WOOL);
        p.sendMessage("Complete");
    }
}
