package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connections;

    public ConnectionsImpl()
    {
        connections = new ConcurrentHashMap<Integer, ConnectionHandler<T>>();
    }

    @Override
    public void connect(int connectionId, ConnectionHandler<T> handler)
    {
        this.connections.put(connectionId, handler);
    }

    @Override
    public boolean send(int connectionId, T msg)
    {
        ConnectionHandler<T> ch = this.connections.get(connectionId);
        if(ch != null)
        {
            ch.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId)
    {
        this.connections.remove(connectionId);
    }

    
}
