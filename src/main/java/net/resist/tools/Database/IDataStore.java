package net.resist.tools.Database;

import java.util.List;

public interface IDataStore {

    public abstract String getDatabaseName();

    public abstract boolean load();

    public abstract List<String> getAccepted();

    public abstract boolean addPlayer(String uuid);

    public abstract boolean removePlayer(String uuid);

    public abstract boolean clearList();



}
