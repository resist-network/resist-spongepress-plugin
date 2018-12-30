package net.resist.wordpress.sponge.database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.zaxxer.hikari.HikariDataSource;
import net.resist.wordpress.sponge.Config;
import net.resist.wordpress.sponge.Main;
public final class MYSQLDataStore implements IDataStore{
    private final Main plugin;
    private final Optional<HikariDataSource> dataSource;
    private static final String mysqlError="MySQL: Couldn't read from MySQL database.";
    public MYSQLDataStore(Main plugin){
        this.plugin=plugin;
        this.dataSource=getDataSource();
    }
    @Override
    public String getDatabaseName(){
        return "MySQL";
    }
    @Override
    public boolean load(){
        if(!dataSource.isPresent()){
            return false;
        }
        try(Connection connection=getConnection()){
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS "+Config.mysqlPrefix+"players ("
                +" username VARCHAR(60) NOT NULL PRIMARY KEY, lastIP VARCHAR(60), locX INT(10), locY INT(10), locZ INT(10));");
            getConnection().commit();
        }catch(SQLException ex){
            return false;
        }
        return true;
    }
    @Override
    public List<String> getAccepted(){
        List<String> uuidList=new ArrayList<>();
        try(Connection connection=getConnection()){
            ResultSet rs=connection.createStatement().executeQuery("SELECT user_login FROM "+Config.mysqlPrefix
                +"users");
            while(rs.next()){
                uuidList.add(rs.getString("user_login"));
            }
            return uuidList;
        }catch(SQLException ex){
            plugin.getLogger().info(mysqlError,ex);
            return new ArrayList<>();
        }
    }
    @Override
    public String getWordpressID(String username){
        List<String> uuidList=new ArrayList<>();
        String wordpressID=null;
        try(Connection connection=getConnection()){
            ResultSet rs=connection.createStatement().executeQuery("SELECT ID FROM "+Config.mysqlPrefix
                +"users WHERE user_login = '"+username+"'");
            while(rs.next()){
                uuidList.add(rs.getString("ID"));
            }
            wordpressID=uuidList.toString().replace("[","").replace("]","");
        }catch(SQLException ex){
            plugin.getLogger().info(mysqlError,ex);
        }
        return wordpressID;
    }
    @Override
    public List<String> getLoggedIn(){
        List<String> uuidList=new ArrayList<>();
        try(Connection connection=getConnection()){
            ResultSet rs=connection.createStatement().executeQuery("SELECT username FROM "+Config.mysqlPrefix
                +"players");
            while(rs.next()){
                uuidList.add(rs.getString("username"));
            }
            return uuidList;
        }catch(SQLException ex){
            plugin.getLogger().info(mysqlError,ex);
            return new ArrayList<>();
        }
    }
    @Override
    public boolean addPlayer(String username){
        try(Connection connection=getConnection()){
            PreparedStatement statement=connection.prepareStatement("INSERT INTO "+Config.mysqlPrefix
                +"players VALUES ('"+username+"','',0,0,0);");
            plugin.getLogger().info("SQL Query: "+statement.toString());
            return statement.executeUpdate()>0;
        }catch(SQLException ex){}
        return false;
    }
    @Override
    public boolean removePlayer(String username){
        try(Connection connection=getConnection()){
            PreparedStatement statement=connection.prepareStatement("DELETE FROM "+Config.mysqlPrefix
                +"players WHERE username = '"+username+"';");
            return statement.executeUpdate()>0;
        }catch(SQLException ex){}
        return false;
    }
    @Override
    public boolean clearList(){
        try(Connection connection=getConnection()){
            PreparedStatement statement=connection.prepareStatement("TRUNCATE TABLE "+Config.mysqlPrefix+"players;");
            return statement.executeUpdate()>0;
        }catch(SQLException ex){
            plugin.getLogger().error("MySQL: Error removing playerdata",ex);
        }
        return false;
    }
    public boolean hasColumn(String tableName,String columnName){
        try(Connection connection=getConnection()){
            DatabaseMetaData md=connection.getMetaData();
            ResultSet rs=md.getColumns(null,null,tableName,columnName);
            return rs.next();
        }catch(SQLException ex){
            plugin.getLogger().error("MySQL: Error checking if column exists.",ex);
        }
        return false;
    }
    public Optional<HikariDataSource> getDataSource(){
        HikariDataSource ds=new HikariDataSource();
        try{
            ds.setDriverClassName("org.mariadb.jdbc.Driver");
            ds.setJdbcUrl("jdbc:mariadb://"+Config.mysqlHost+":"+Config.mysqlPort+"/"+Config.mysqlDatabase);
            ds.addDataSourceProperty("user",Config.mysqlUser);
            ds.addDataSourceProperty("password",Config.mysqlPass);
            ds.setConnectionTimeout(1000);
            ds.setLoginTimeout(5);
            ds.setAutoCommit(true);
            return Optional.ofNullable(ds);
        }catch(SQLException ex){
            plugin.getLogger().error("MySQL: Failed to get datastore.",ex);
            return Optional.empty();
        }
    }
    public Connection getConnection() throws SQLException{
        return dataSource.get().getConnection();
    }
}