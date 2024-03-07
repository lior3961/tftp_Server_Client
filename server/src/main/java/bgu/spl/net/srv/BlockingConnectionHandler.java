package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.tftp.TftpEncoderDecoder;
import bgu.spl.net.impl.tftp.TftpProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<byte[]> {

    private final TftpProtocol protocol;
    private final TftpEncoderDecoder encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private ServerData serverData;
    private int connectionId;

    public BlockingConnectionHandler(Socket sock, TftpEncoderDecoder reader, TftpProtocol protocol, int connectionId, ServerData serverData) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.serverData = serverData;
        this.connectionId = connectionId;
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;
            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());
            this.protocol.start(connectionId, serverData.getConnections());
            this.serverData.getConnections().connect(connectionId,this);
            System.out.println("Client: " + this.connectionId + " connected to the server");
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                byte[] nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) 
                {
                    System.out.println("Got message from client: " + this.connectionId);
                    this.protocol.process(nextMessage);
                    nextMessage = null;
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        this.serverData.getConnections().disconnect(this.connectionId);
        connected = false;
        sock.close();
    }

    @Override
    public void send(byte[] msg) {
        try
        {
            out.write(encdec.encode(msg));
            out.flush();
        } catch(IOException ex)
        {
            ex.printStackTrace();
        }

    }
}
