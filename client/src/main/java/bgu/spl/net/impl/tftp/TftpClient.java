package bgu.spl.net.impl.tftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class TftpClient {
    public static void main(String[] args) {
        if (args.length == 0) 
        {
            args = new String[]{"127.0.0.1", "7777"};
        }
        try
        {
            Socket sock = new Socket(args[0], Integer.parseInt(args[1]));
            BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());
            BufferedInputStream in = new BufferedInputStream(sock.getInputStream());
            Action actions = new Action();
            Thread keyBoardThread = new Thread(new KeyBoardThread(sock, out, actions));
            keyBoardThread.start();
            Thread listenerThread = new Thread(new ListenerThread(sock, in, actions));
            listenerThread.start();     
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}

