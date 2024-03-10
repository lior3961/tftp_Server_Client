package bgu.spl.net.srv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;
import java.io.*;
import java.net.*;
import java.nio.file.*;


public class ServerActions {
    
    private ServerData serverData;
    private int connectionId;
    private int actionsCount;
    private Vector<Byte> dataBytes;
    private short blockNumber;
    private String filesPath;
    private Path filePath;


    public ServerActions(ServerData serverData, int connectionId)
    {
        this.serverData = serverData;
        this.connectionId = connectionId;
        this.actionsCount = 0;
        this.dataBytes = new Vector<Byte>();
        this.blockNumber = 1;
        this.filesPath = "${workspaceFolder}/Flies";


    }

    public byte[] act(short opCode, byte[] msg)
    {
        short block , errCode;
        String errMsg , fileName; 
        switch (opCode) {
            case 1:
                if(!this.serverData.getUserStatus(connectionId))
                {
                    errMsg = "User not logged in - Any opcode received before Login completes.";
                    errCode = 6;
                    msg = createErrorPacket(errMsg,errCode);
                    break;
                }
                fileName = this.getName(msg);
                if(!this.serverData.isFileExist(fileName))
                {
                    errMsg = "File not found - RRQ DELRQ of non-existing file.";
                    errCode = 1;
                    msg = createErrorPacket(errMsg , errCode);
                }
                else
                {
                    this.filePath = Paths.get(fileName);

                    msg = createDataPacket();
                }
                break;
            case 2:
                if(!this.serverData.getUserStatus(connectionId))
                {
                    errMsg = "User not logged in - Any opcode received before Login completes.";
                    errCode = 6;
                    msg = createErrorPacket(errMsg,errCode);
                    break;
                }
                fileName = this.getName(msg);
                if(this.serverData.isFileExist(fileName))
                {
                    errMsg = "File already exists - File name exists on WRQ.";
                    errCode = 6;
                    msg = createErrorPacket(errMsg,errCode);
                    break;                    
                }
                block = 0;
                msg = createACKPacket(block);  
                break;
            case 3:
                if(handleDataPacket(msg))
                {
                    try
                    {
                        block = this.blockNumber; //update block number
                        byte[] bytesArr = new byte[this.dataBytes.size()];
                        for(int i = 0 ; i < bytesArr.length ; i++) //moving the file bytes from the vector to an array
                        {
                            bytesArr[i] = dataBytes.remove(0);
                        }
                        FileOutputStream fos = new FileOutputStream(this.filesPath); //new file output stream with files path
                        fos.write(bytesArr); //creating file
                        fos.close();
                        msg = createACKPacket(block);
                        this.dataBytes.clear(); //clear dataByts vector
                        this.blockNumber = 1;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                block = this.blockNumber;
                msg = createACKPacket(block);
                break;
            case 4:
                byte[] packet = createDataPacket();
                if(packet == null)
                {
                    System.out.println("RRQ complete");
                }
                msg = packet; 
                
                break;
            case 5:
                
                break;
            case 6:
                
                break;
            case 7:
                String userName = this.getName(msg);
                if(!this.serverData.logInOrRegister(userName,this.connectionId) && this.actionsCount >= 1)
                {
                    errMsg = "User already logged in - Login username already connected."; // Example string
                    errCode = 7;
                    msg = createErrorPacket(errMsg , errCode);
                    break;
                }
                block = 0;
                msg = createACKPacket(block);
                break;
            case 8:
                if(!this.serverData.getUserStatus(connectionId))
                {
                    errMsg = "User not logged in - Any opcode received before Login completes.";
                    errCode = 6;
                    msg = createErrorPacket(errMsg,errCode);
                    break;
                }
                fileName = this.getName(msg);
                if(this.serverData.deleteFileFromServer(fileName))
                {
                    block = 0;
                    msg = createACKPacket(block);  
                }
                else
                {
                    errMsg = "File not found - RRQ DELRQ of non-existing file.";
                    errCode = 1;
                    msg = createErrorPacket(errMsg,errCode); 
                }                              
                break;
            case 9:
                
                break;
            case 10:
                
                break; 
            default:
                errMsg = "Illegal TFTP operation - Unknown Opcode.";
                errCode = 4;
                msg = createErrorPacket(errMsg, errCode);
                break;
        }
        return msg;
    }

    public String getName(byte[] msg)
    {
        byte[] name = Arrays.copyOfRange(msg, 2, msg.length);
        return new String(name, StandardCharsets.UTF_8);
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
        this.actionsCount++;
        short ackCode = 4;
        byte [] opCodeBytes = new byte []{( byte ) (ackCode >> 8) , ( byte ) (ackCode & 0xff)};
        byte [] blockNumberBytes = new byte []{( byte ) (block >> 8) , ( byte ) (block & 0xff)};
        byte[] result = Arrays.copyOf(opCodeBytes, opCodeBytes.length+blockNumberBytes.length);
        System.arraycopy(blockNumberBytes, 0, result, opCodeBytes.length, blockNumberBytes.length);
        //System.out.println("Created ACK Packet:" + result[0] + "," + result[1] + "," + result[2] + "," + result[3]);
        return result;
    }

    public byte[] createDataPacket()
    { 
        try (FileInputStream fileInputStream = new FileInputStream(this.filePath.toFile())) {
            long startPosition = (blockNumber - 1) * 512; // Calculate the starting position
            fileInputStream.skip(startPosition); // Skip to the starting position
            byte[] buffer = new byte[512];
            int bytesRead = fileInputStream.read(buffer); // Read 512 bytes from the file
            if (bytesRead != -1) 
            {
                byte[] packet = new byte[bytesRead];
                System.arraycopy(buffer, 0, packet, 0, bytesRead);
                this.blockNumber++;
                return packet;
            }
            else
            {
                this.blockNumber = 1;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return an empty byte array if there was an error
    }


    public boolean handleDataPacket(byte[] msg)
    {
        byte [] b = new byte []{msg[2] , msg[3]};       
        short dataSize = ( short ) ((( short ) b[0]) << 8 | ( short ) ( b[1]) ); //getting data size number from the array
        b = new byte []{msg[4] , msg[5]};       
        this.blockNumber = ( short ) ((( short ) b[0]) << 8 | ( short ) ( b[1]) ); //getting block number from the array
        byte[] data = Arrays.copyOfRange(msg, 6, msg.length); //copy data byts into array
        for(int i = 0 ; i < data.length ; i++)
        {
            this.dataBytes.add(data[i]);
        }
        if(dataSize == 512) // check if block number is maximum for waiting to another block
        {
            return false;
        }
        return true;
    }
 
}



