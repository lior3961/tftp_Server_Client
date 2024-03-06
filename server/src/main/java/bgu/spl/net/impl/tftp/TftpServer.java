package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.ServerData;

public class TftpServer {
        public static void main(String[] args) {
        
        ServerData serverData = new ServerData();
        Server.threadPerClient(
                7777, //port
                () -> new TftpProtocol(serverData), //protocol factory
                TftpEncoderDecoder::new 
        ).serve();
    }
}
