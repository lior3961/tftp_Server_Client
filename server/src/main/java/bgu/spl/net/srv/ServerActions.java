package bgu.spl.net.srv;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;

public class ServerActions {
    
    private Connections<byte[]> connections;
    private ServerData serverData;
    private int connectionId;

    public ServerActions(Connections<byte[]> connections, ServerData serverData, int connectionId)
    {
        this.connections = connections;
        this.serverData = serverData;
        this.connectionId = connectionId;
    }

    public byte[] act(short opCode, byte[] msg)
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
                if(!this.serverData.logInOrRegister(userName,this.connectionId))
                {
                    String str = "User already logged in - Login username already connected."; // Example string
                    short errorCode = 7;
                    msg = createErrorPacket(str , errorCode);
                    break;
                }
                short block = 0;
                msg = createACKPacket(block);
                break;
            case 8:
                
                break;
            case 9:
                
                break;
            case 10:
                
                break; 
            default:
                String errorMsg = "Illegal TFTP operation - Unknown Opcode.";
                short errorCode = 4;
                msg = createErrorPacket(errorMsg, errorCode);
                break;
        }
        return msg;
    }

    public String getUserName(byte[] msg)
    {
        byte[] userName = Arrays.copyOfRange(msg, 2, msg.length-1);
        return new String(userName, StandardCharsets.UTF_8);
    }

    public byte[] createErrorPacket(String errorMsg, short errorCode)
    {
        short opCode = 5;
        byte [] opCodeBytes = new byte []{( byte ) (opCode >> 8) , ( byte ) (opCode & 0xff)};
        byte [] errorCodeBytes = new byte []{( byte ) (errorCode >> 8) , ( byte ) (errorCode & 0xff)};
        byte[] errorMsgBytes = errorMsg.getBytes(StandardCharsets.UTF_8);
        byte lastByteShort = 0; 
        int totalLength = opCodeBytes.length + errorCodeBytes.length + errorMsgBytes.length + 1;
        
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
        result[result.length-1] = lastByteShort;
        return result;
    }

    public byte[] createACKPacket(short block)
    {
        short ackCode = 4;
        byte [] opCodeBytes = new byte []{( byte ) (ackCode >> 8) , ( byte ) (ackCode & 0xff)};
        byte [] blockNumberBytes = new byte []{( byte ) (block >> 8) , ( byte ) (block & 0xff)};
        byte[] result = Arrays.copyOf(opCodeBytes, opCodeBytes.length+blockNumberBytes.length);
        System.arraycopy(blockNumberBytes, 0, result, opCodeBytes.length, blockNumberBytes.length);
        //System.out.println("Created ACK Packet:" + result[0] + "," + result[1] + "," + result[2] + "," + result[3]);
        return result;
    }
}



