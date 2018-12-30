package net.resist.tools.Database;
import java.util.List;
public interface IDataStore {
    public abstract String getDatabaseName();
    public abstract boolean load();
    public abstract List<String> getAccepted();
    public abstract List<String> getLoggedIn();
    public abstract boolean addPlayer(String username);
    public abstract boolean removePlayer(String username);
    public abstract boolean clearList();
	public abstract String getWordpressID(String username);
}