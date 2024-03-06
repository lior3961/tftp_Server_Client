package bgu.spl.net.srv;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class ServerActions {
    
    private Connections<byte[]> connections;
    private ServerData serverData;

    public ServerActions(Connections<byte[]> connections, ServerData serverData)
    {
        this.connections = connections;
        this.serverData = serverData;
    }

    public void act(short opCode, byte[] msg)
    {
        switch (opCode) {
            case 1:
                
                break;
            case 2:
                
                break;
            case 3:
                
                break;
            case 4:
                
                break;
            case 5:
                
                break;
            case 6:
                
                break;
            case 7:
                String userName = this.getUserName(msg);
                if(!this.serverData.logInOrRegister(userName))
                {
                    String str = "User already logged in – Login username already connected.\r"; // Example string
                    msg = createErrorPacket(str , 7);
                    break;
                }
                msg = createACKPacket(0);
                break;
            case 8:
                
                break;
            case 9:
                
                break;
            case 10:
                
                break; 
        }
    }

    public String getUserName(byte[] msg)
    {
        byte[] userName = Arrays.copyOfRange(msg, 2, msg.length-1);
        return new String(userName, StandardCharsets.UTF_8);
    }

    public byte[] createErrorPacket(String errorMsg, int errorCode)
    {
        byte [] opCodeBytes = new byte []{( byte ) (5 >> 8) , ( byte ) (5 & 0xff)};
        byte [] errorCodeBytes = new byte []{( byte ) (errorCode >> 8) , ( byte ) (errorCode & 0xff)};
        byte[] errorMsgBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
        byte [] lastByte = new byte []{( byte ) (0 & 0xff)};  
        int totalLength = opCodeBytes.length + errorCodeBytes.length + errorMsgBytes.length + lastByte.length;
        
        // Create a new byte array to hold the unioned arrays
        byte[] result = new byte[totalLength];
        
        // Copy arrays into the unioned array
        int offset = 0;
        System.arraycopy(opCodeBytes, 0, result, offset, opCodeBytes.length);
        offset += opCodeBytes.length;
        System.arraycopy(errorCodeBytes, 0, result, offset, errorCodeBytes.length);
        offset += errorCodeBytes.length;
        System.arraycopy(errorMsgBytes, 0, result, offset, errorMsgBytes.length);
        offset += errorMsgBytes.length;
        System.arraycopy(lastByte, 0, result, offset, lastByte.length);
        
        return result;
    }

    public byte[] createACKPacket(int block)
    {
        byte [] opCodeBytes = new byte []{( byte ) (4 >> 8) , ( byte ) (4 & 0xff)};
        byte [] blockNumberBytes = new byte []{( byte ) (block >> 8) , ( byte ) (block & 0xff)};
        byte[] result = Arrays.copyOf(opCodeBytes, opCodeBytes.length+blockNumberBytes.length);
        System.arraycopy(blockNumberBytes, 0, result, opCodeBytes.length, blockNumberBytes.length);
        return result;
    }
}



