package me.hasenzahn1.structurereloot.database.tables;

import me.hasenzahn1.structurereloot.database.LootBlockValue;
import me.hasenzahn1.structurereloot.database.LootEntityValue;
import me.hasenzahn1.structurereloot.databasesystem.Database;
import me.hasenzahn1.structurereloot.databasesystem.Table;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static me.hasenzahn1.structurereloot.database.tables.BlockTable.getNamespacedKey;

public class EntityTable extends Table {

    private final World world;

    private boolean cacheRemove;
    private final ArrayList<LootEntityValue> cachedLootEntityValues;

    public EntityTable(Database database, World world) {
        super("entities", database);
        this.world = world;
        cacheRemove = false;
        cachedLootEntityValues = new ArrayList<>();
    }

    //Creation
    @Override
    public String getCreationString() {
        return "CREATE TABLE IF NOT EXISTS " + getTableName() + " (" +
                "location varchar(27) PRIMARY KEY," +
                "lootTable varchar(57)," +
                "uuid varchar(36)," +
                "entityType varchar(20) NOT NULL" +
                ");";
    }

    //Get Values
    public LootEntityValue getEntity(Location loc){
        Connection con = getConnection();
        try(PreparedStatement statement = con.prepareStatement(
                "SELECT * FROM " + getTableName() + " WHERE location='" + LootBlockValue.locationToLocationString(loc) + "'"
        )){
            ResultSet set = statement.executeQuery();
            if(set.next()){
                return new LootEntityValue(world,
                        set.getString("entityType"),
                        set.getString("location"),
                        getNamespacedKey(set.getString("lootTable")),
                        set.getString("uuid"));
            }else{
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<LootEntityValue> getAllEntities(){
        Connection con = getConnection();
        try(PreparedStatement statement = con.prepareStatement(
                "SELECT * FROM " + getTableName()
        )) {
            ResultSet set = statement.executeQuery();
            ArrayList<LootEntityValue> values = new ArrayList<>();
            while(set.next()){
                values.add(new LootEntityValue(world,
                        set.getString("entityType"),
                        set.getString("location"),
                        getNamespacedKey(set.getString("lootTable")),
                        set.getString("uuid")));
            }
            return values;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    //Add Values
    public void addEntity(LootEntityValue value){
        Connection con = getConnection();
        try(PreparedStatement statement = con.prepareStatement(
                "INSERT OR REPLACE INTO " + getTableName() + " (location, lootTable, uuid, entityType) VALUES(?,?,?,?)"
        )){
            statement.setString(1, value.getLocationString());
            statement.setString(2, value.getLootTableString());
            statement.setString(3, value.getUUIDString());
            statement.setString(4, value.getEntityString());
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addMultipleEntities(List<LootEntityValue> values){
        if(values.size() == 0) return;
        Connection con = getConnection();
        StringBuilder sqlString = new StringBuilder("INSERT OR REPLACE INTO " + getTableName() + " (location, lootTable, uuid, entityType) VALUES ");
        for(LootEntityValue val : values){
            sqlString.append("('");
            sqlString.append(val.getLocationString()).append("','");
            sqlString.append(val.getLootTableString()).append("','");
            sqlString.append(val.getUUIDString()).append("','");
            sqlString.append(val.getEntityString()).append("'),");
        }
        sqlString.deleteCharAt(sqlString.length()-1);
        //Bukkit.broadcastMessage(sqlString.toString());
        try(PreparedStatement statement = con.prepareStatement(
                sqlString.toString()
        )){
            statement.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    //Remove Values
    public void removeEntity(LootEntityValue value){
        if(cacheRemove){
            cachedLootEntityValues.add(value);
            return;
        }
        Connection con = getConnection();
        try(PreparedStatement statement = con.prepareStatement(
                "DELETE FROM " + getTableName() + " WHERE location='" + value.getLocationString() + "'"
        )){
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeMultipleEntities(List<LootEntityValue> values){
        if(values.size() == 0) return;
        Connection con = getConnection();
        StringBuilder sqlString = new StringBuilder("DELETE FROM ").append(getTableName()).append(" WHERE location IN ('").append(values.get(0).getLocationString()).append("'");

        for(int i = 1; i <  values.size(); i++){
            sqlString.append(", '").append(values.get(i).getLocationString()).append("'");
        }
        sqlString.append(")");

        try(PreparedStatement statement = con.prepareStatement(sqlString.toString())){
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Caching
    public void setCacheRemove(boolean value){
        this.cacheRemove = value;
        if(!value) {
            removeMultipleEntities(cachedLootEntityValues);
            cachedLootEntityValues.clear();
        }
    }

}
