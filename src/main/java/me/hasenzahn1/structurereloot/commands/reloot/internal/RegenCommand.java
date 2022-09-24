package me.hasenzahn1.structurereloot.commands.reloot.internal;

import me.hasenzahn1.structurereloot.StructureReloot;
import me.hasenzahn1.structurereloot.commands.reloot.RelootListLootablesCommand;
import me.hasenzahn1.structurereloot.commandsystem.BaseCommand;
import me.hasenzahn1.structurereloot.commandsystem.SubCommand;
import me.hasenzahn1.structurereloot.database.LootBlockValue;
import me.hasenzahn1.structurereloot.database.LootEntityValue;
import me.hasenzahn1.structurereloot.database.WorldDatabase;
import me.hasenzahn1.structurereloot.reloot.RelootHelper;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class RegenCommand extends SubCommand {

    public RegenCommand(BaseCommand parent) {
        super(parent, "regen", "reloot.commands.regen");
    }

    @Override
    public boolean performCommand(CommandSender sender, String[] args) {
        if(args.length != 3){
            return true;
        }

        World world = Bukkit.getWorld(args[0]);
        if(world == null){
            return true;
        }

        if(!args[1].equalsIgnoreCase("block") && !args[1].equalsIgnoreCase("entity")){
            return true;
        }

        WorldDatabase database = StructureReloot.getInstance().getDatabase(world);
        int page = 0;
        if(args[1].equalsIgnoreCase("block")){
            LootBlockValue value = database.getBlock(LootBlockValue.getLocFromString(world, args[2]));
            if(value == null) return true;
            List<LootBlockValue> values = database.getAllBlocks();
            page = values.indexOf(value) / 10;
            RelootHelper.relootOneBlock(value);
            database.removeBlock(value);
        }else{
            LootEntityValue value = database.getEntity(LootBlockValue.getLocFromString(world, args[2]));
            if(value == null) return true;
            List<LootEntityValue> values = database.getAllEntities();
            page = values.indexOf(value) / 10;
            RelootHelper.relootOneEntity(value);
            database.removeEntity(value);
        }
        database.close();
        RelootListLootablesCommand.listAllElements(sender,
                world,
                page,
                args[1].equalsIgnoreCase("block") ? database.getAllBlocks() : new ArrayList<>(),
                !args[1].equalsIgnoreCase("block") ? database.getAllEntities() : new ArrayList<>(),
                args[0].equalsIgnoreCase("block") ? "blocks" : "entities");
        return true;
    }
}
