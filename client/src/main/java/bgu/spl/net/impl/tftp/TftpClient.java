package bgu.spl.net.impl.tftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TftpClient {

    public static Socket keyBoardSocket;
    public static Socket listenerSocket;
    public static boolean needTolisten = false;
    public static Scanner scanner = new Scanner(System.in);
    public static boolean connected = true;
    public static void main(String[] args) {
        Action actions = new Action(connected);
        Thread keyBoardHandler = new Thread( ()-> {
            try (Socket sock = new Socket(args[0], 7777);
                BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream()))
            {
                byte[] message;
                keyBoardSocket = sock;
                while(!actions.shouldTerminate())
                {
                    if(!needTolisten)
                    {
                        String input = scanner.nextLine();
                        message = actions.createAction(input);
                        if(message != null)
                        {
                            out.write(message);
                            out.flush();
                            needTolisten = true;
                        }
                    }
                }
                keyBoardSocket.close();
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        });
       
        Thread listening = new Thread( ()->
        {
            try (Socket sock = new Socket(args[0], 7777);
            BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream()))
            {
                listenerSocket = sock;
                while(!actions.shouldTerminate())
                {
                    int packetSize = in.available();
                    if(packetSize > 0)
                    {
                        byte[] inputPacket = new byte[packetSize];
                        in.read(inputPacket);
                        byte[] returnMessage = actions.gotMessageFromServer(inputPacket);
                        if(returnMessage != null)
                        {
                            out.write(returnMessage);
                            out.flush();
                        }
                        else
                        {
                            needTolisten = false;
                        }
                    }
                }
                listenerSocket.close();         
            }
            catch(IOException ex)
            {
                ex.printStackTrace();
            }
        });
        
        keyBoardHandler.start();
        listening.start();
        
    }
}

