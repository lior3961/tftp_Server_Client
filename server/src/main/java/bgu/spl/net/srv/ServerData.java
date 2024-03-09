package bgu.spl.net.srv;
import java.io.FileInputStream;
import java.util.concurrent.ConcurrentHashMap;

public class ServerData {

    private ConcurrentHashMap<String, Boolean> users; // mapping each username to his login stae
    private ConcurrentHashMap<String, FileInputStream> files; // mapping each file name to his file path
    private ConcurrentHashMap<Integer, String> idToUserName; // mapping each logged connectionid to his username 
    private Connections<byte[]> connections;
    
    public ServerData()
    {
        this.users = new ConcurrentHashMap<String, Boolean>();
        this.files = new ConcurrentHashMap<String, FileInputStream>();
        this.connections = new ConnectionsImpl<byte[]>();
        idToUserName = new ConcurrentHashMap<Integer, String>();
    }

    public void connect(int connectionId, ConnectionHandler<byte[]> ch)
    {
        this.connections.connect(connectionId, ch);
    }

    public void disconnect(int connectionId)
    {
        String userName = this.idToUserName.get(connectionId);
        this.users.put(userName, false);
    }

    public boolean logInOrRegister(String userName, int connectionId) //true if logged in, false if ERROR
    {
        if(this.users.containsKey(userName))
        {
            if(this.users.get(userName))
                //*
                // need to check if its firs action
                // */
            {
                return false; //already logged in - ERROR
            }
        }
        this.users.put(userName, true); //log in or register
        this.idToUserName.put(connectionId, userName);
        return true;
    }
    
    public boolean deleteFileFromServer(String fileName)
    {
        if(!this.files.containsKey(fileName)) 
        //*
        //*
        //check if the client is logged in
        {
            return false; //file doesn't exist
        }
        this.files.remove(fileName);
        return true;  // file has deleted
    }

    public boolean addFileToServer(String fileName)
    {
        //implement
        //
        //check if client is logged in
        return true;
    }

    public Connections<byte[]> getConnections()
    {
        return this.connections;
    }

    public boolean hadDisconnected(int connectionId) //false if logged in or hasn't logged in yet, true if disconnected
    {
        String userName = this.idToUserName.get(connectionId); //check if userName logged at least once
        if(userName != null)
        {
            if(!this.users.get(userName))
            {
                this.connections.disconnect(connectionId);
                this.idToUserName.remove(connectionId);
                return true;
            }
        }
        return false;
    }
    
}
