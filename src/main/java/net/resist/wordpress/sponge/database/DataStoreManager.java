package net.resist.wordpress.sponge.database;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import net.resist.wordpress.sponge.Main;
public final class DataStoreManager{
    private final Main plugin;
    private final Map<String,Class<? extends IDataStore>> dataStores=new HashMap<>();
    private IDataStore dataStore;
    public DataStoreManager(Main plugin){
        this.plugin=plugin;
    }
    public boolean load(){
        if(getDataStore()!=null){
            clearDataStores();
        }
        registerDataStore("MYSQL",MYSQLDataStore.class);
        setDataStoreInstance("MYSQL");
        plugin.getLogger().info("Loading MySQL data...");
        return getDataStore().load();
    }
    public void registerDataStore(String dataStoreId,Class<? extends IDataStore> dataStoreClass){
        dataStores.put(dataStoreId,dataStoreClass);
    }
    public void unregisterDataStore(String dataStoreId){
        dataStores.remove(dataStoreId);
    }
    public void clearDataStores(){
        dataStores.clear();
    }
    public List<String> getAvailableDataStores(){
        List<String> list=new ArrayList<>();
        list.addAll(dataStores.keySet());
        return Collections.unmodifiableList(list);
    }
    private void setDataStoreInstance(String dataStoreId){
        try{
            dataStore=dataStores.get(dataStoreId).getConstructor(Main.class).newInstance(this.plugin);
        }catch(InstantiationException|IllegalAccessException|IllegalArgumentException|NoSuchMethodException
            |InvocationTargetException|SecurityException e){
            throw new RuntimeException("Couldn't instantiate data store "+dataStoreId+" "+e);
        }
    }
    public IDataStore getDataStore(){
        return dataStore;
    }
}