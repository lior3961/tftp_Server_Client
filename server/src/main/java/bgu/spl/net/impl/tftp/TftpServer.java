package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.ServerData;

public class TftpServer {
        public static void main(String[] args) {
        if(args.length == 0)
        {
                args = new String[]{"7777"};
        }
        ServerData serverData = new ServerData();
        Server.threadPerClient(
                Integer.parseInt(args[0]), //port
                () -> new TftpProtocol(serverData), //protocol factory
                TftpEncoderDecoder::new, serverData 
        ).serve();
    }
}
