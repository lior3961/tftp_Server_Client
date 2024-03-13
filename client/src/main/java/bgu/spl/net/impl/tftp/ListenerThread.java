package bgu.spl.net.impl.tftp;
import java.io.BufferedInputStream;
import java.net.Socket;
import java.io.IOException;

public class ListenerThread implements Runnable
{
    private BufferedInputStream in;
    private Socket sock;
    private Action actions;

    public ListenerThread(Socket sock , BufferedInputStream in , Action actions)
    {
        this.in = in;
        this.sock = sock;
        this.actions = actions;
    }

    public void run()
    {
        try
        {
            while(!actions.shouldTerminate())
            {
                int packetSize = in.available();
                if(packetSize > 0)
                {
                    byte[] inputPacket = new byte[packetSize];
                    in.read(inputPacket);
                    this.actions.gotMessageFromServer(inputPacket);
                    synchronized(this.actions.getWaiter())
                    {
                        this.actions.getWaiter().notifyAll();
                    }
                }
            }
            sock.close();        
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
}