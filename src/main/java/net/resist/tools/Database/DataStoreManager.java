package net.resist.tools.Database;

import net.resist.tools.Config;
import net.resist.tools.Main;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class DataStoreManager {

    private final Main plugin;

    private final Map<String, Class<? extends IDataStore>> dataStores = new HashMap<>();
    private IDataStore dataStore;

    public DataStoreManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean load() {
        if (getDataStore() != null) {
            clearDataStores();
        }
        registerDataStore("MYSQL", MYSQLDataStore.class);
		setDataStoreInstance("MYSQL");
		plugin.getLogger().info("Loading MySQL data...");
		return getDataStore().load();
    }

    /**
     * Register a new Data Store. This should be run at onLoad()<br>
     *
     * @param dataStoreId ID that identifies this data store <br>
     * @param dataStoreClass a class that implements IDataStore
     */
    public void registerDataStore(String dataStoreId, Class<? extends IDataStore> dataStoreClass) {
        dataStores.put(dataStoreId, dataStoreClass);
    }

    /**
     * Unregisters the data store with the provided id
     *
     * @param dataStoreId
     */
    public void unregisterDataStore(String dataStoreId) {
        dataStores.remove(dataStoreId);
    }

    /**
     * Unregisters all data stores
     */
    public void clearDataStores() {
        dataStores.clear();
    }

    /**
     * List of registered data stores id
     *
     * @return
     */
    public List<String> getAvailableDataStores() {
        List<String> list = new ArrayList<>();
        list.addAll(dataStores.keySet());
        return Collections.unmodifiableList(list);
    }

    /**
     * Sets and instantiate the data store
     *
     * @param dataStoreId
     */
    private void setDataStoreInstance(String dataStoreId) {
        try {
            dataStore = dataStores.get(dataStoreId).getConstructor(Main.class).newInstance(this.plugin);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException | SecurityException e) {
            throw new RuntimeException("Couldn't instantiate data store " + dataStoreId + " " + e);
        }
    }

    /**
     * Gets current data store. Returns null if there isn't an instantiated data
     * store
     *
     * @return
     */
    public IDataStore getDataStore() {
        return dataStore;
    }

}
