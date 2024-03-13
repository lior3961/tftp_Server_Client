package bgu.spl.net.impl.tftp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class KeyBoardThread implements Runnable{

    private BufferedOutputStream out;
    private Socket sock;
    private Action actions;
    private Scanner scanner;

    public KeyBoardThread(Socket sock,BufferedOutputStream out, Action actions)
    {
        this.out = out;
        this.sock = sock;
        this.actions = actions;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void run()
    {
        try
        {
            while(!actions.shouldTerminate() && sock.isConnected())
            {
                while(actions.getReturnMesseage() == null)
                {
                    String input = scanner.nextLine();
                    actions.createAction(input);
                }
                out.write(actions.getReturnMesseage());
                out.flush();
                if(!this.actions.getKeepUserInput())
                {
                    try
                    {
                        synchronized(this.actions.getWaiter())
                        {
                            this.actions.getWaiter().notifyAll();
                            this.actions.getWaiter().wait();
                        }
                    }
                    catch(InterruptedException e)
                    {
                        System.out.println("Thread interrupted");
                    }
                }
                else
                {
                    this.actions.cleanReturnMassage();
                }
            }
            sock.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
            
        
    }
    
}
