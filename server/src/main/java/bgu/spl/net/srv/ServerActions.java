package bgu.spl.net.srv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import javax.swing.text.html.HTMLDocument.Iterator;

import java.io.*;
import java.net.*;
import java.nio.file.*;


public class ServerActions {
    
    private ServerData serverData;
    private int connectionId;
    private int actionsCount;
    private Vector<Byte> dataBytes;
    private short blockNumber;
    private Path serverFilesFolderPath;
    private String fileName;
    private byte needToBcast;
    private byte[] filesByts;


    public ServerActions(ServerData serverData, int connectionId)
    {
        this.serverData = serverData;
        this.connectionId = connectionId;
        this.actionsCount = 0;
        this.dataBytes = new Vector<Byte>();
        this.blockNumber = 1;
        this.fileName = "";
        this.serverFilesFolderPath = (Paths.get("").toAbsolutePath()).resolve("Flies");//get "Files" path
        this.needToBcast = -1;
        this.filesByts = vectorToBytes(this.serverData.getFiles());
    }

    public byte[] act(short opCode, byte[] msg)
    {
        short block , errCode;
        String errMsg; 
        switch (opCode) {
            case 1:
                if(!this.serverData.getUserStatus(connectionId))
                {
                    errMsg = "User not logged in - Any opcode received before Login completes.";
                    errCode = 6;
                    msg = createErrorPacket(errMsg,errCode);
                    break;
                }
                this.fileName = this.getName(msg);
                if(!this.serverData.isFileExist(this.fileName))
                {
                    errMsg = "File not found - RRQ DELRQ of non-existing file.";
                    errCode = 1;
                    msg = createErrorPacket(errMsg , errCode);
                }
                else
                {
                    msg = createDataPacketRRQ();
                    if(msg == null)
                    {
                        System.out.println("RRQ complete");
                    }
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
                this.fileName = this.getName(msg);
                if(this.serverData.isFileExist(this.fileName))
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
                        Path newFile = this.serverFilesFolderPath.resolve(this.fileName); //put new file path
                        FileOutputStream fos = new FileOutputStream(newFile.toString()); //new file output stream with file path
                        fos.write(bytesArr); //creating file
                        fos.close();
                        msg = createACKPacket(block);
                        this.dataBytes.clear(); //clear dataByts vector
                        this.blockNumber = 1;
                        this.serverData.addFileToServer(this.fileName);
                        this.needToBcast = 1;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                block = this.blockNumber;
                msg = createACKPacket(block);
                break;
            case 4:
                this.blockNumber = ( short ) ((( short ) msg [2]) << 8 | ( short ) ( msg [3]) );
                this.blockNumber++;
                short opCode1 = 1;
                this.act(opCode1, msg);
                break;
            case 5:
                
                break;
            case 6:
                if(!this.serverData.getUserStatus(connectionId))
                {
                    errMsg = "User not logged in - Any opcode received before Login completes.";
                    errCode = 6;
                    msg = createErrorPacket(errMsg,errCode);
                }   
                else
                {
                    msg = createDirqDataPacket();
                }
                break;
            case 7:
                String userName = this.getName(msg);
                if(!this.serverData.logInOrRegister(userName,this.connectionId) || this.actionsCount >= 1)
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
                if(!this.serverData.getUserStatus(connectionId)) //user login check
                {
                    errMsg = "User not logged in - Any opcode received before Login completes.";
                    errCode = 6;
                    msg = createErrorPacket(errMsg,errCode);
                    break;
                }
                this.fileName = this.getName(msg);
                if(this.serverData.deleteFileFromServer(this.fileName)) //check if file is exist , if true delete it.
                {
                    deleteFileFromServerFile(this.fileName);
                    block = 0;
                    msg = createACKPacket(block); 
                    this.needToBcast = 0;
                }
                else //file doesnt exist
                {
                    errMsg = "File not found - RRQ DELRQ of non-existing file.";
                    errCode = 1;
                    msg = createErrorPacket(errMsg,errCode); 
                }                              
                break;
            case 10:
                this.serverData.disconnectUser(this.connectionId);
                block = 0;
                msg = createACKPacket(block);
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

    public byte[] createDataPacketRRQ()
    { 
        try (FileInputStream fileInputStream = new FileInputStream(this.serverFilesFolderPath.resolve
        (this.fileName).toFile())) {
            long startPosition = (blockNumber - 1) * 506; // Calculate the starting position
            fileInputStream.skip(startPosition); // Skip to the starting position
            byte[] buffer = new byte[506];
            int bytesRead = fileInputStream.read(buffer); // Read 506 bytes from the file
            if (bytesRead != -1) 
            {
                byte[] packet = new byte[bytesRead + 6];
                codesOfDataPacket(packet , (short) bytesRead);
                System.arraycopy(buffer, 0, packet, 6, bytesRead);
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
    
    public byte[] createDirqDataPacket()
    { 
        short opcode = 3;
        short block = this.blockNumber;
        short dataSize;
        if(this.filesByts.length >= 512)
        {
            dataSize = 512;
        }
        else
        {
            dataSize = (short)this.filesByts.length;
        }
        return null;
    }

    public void codesOfDataPacket(byte[] packet , short blockSize)
    {
        short opCode = 3;
        byte [] opBytes = new byte []{( byte ) (opCode >> 8) , ( byte ) (opCode & 0xff)};
        byte [] blockBytes = new byte []{( byte ) (this.blockNumber >> 8) , ( byte ) (this.blockNumber & 0xff)};
        byte [] sizeBytes = new byte []{( byte ) (blockSize >> 8) , ( byte ) (blockSize & 0xff)};
        packet[0] = opBytes[0];
        packet[1] = opBytes[1];
        packet[2] = sizeBytes[0];
        packet[3] = sizeBytes[1];
        packet[4] = blockBytes[0];
        packet[5] = blockBytes[1];
    }

    public void deleteFileFromServerFile(String fileName)
    {
        File file = this.serverFilesFolderPath.resolve(fileName).toFile();
        file.delete();
    }

    public byte[] createBcastPacket(byte addOrRemove)
    {
        short opCode = 9;
        byte [] opCodeBytes = new byte []{( byte ) (opCode >> 8) , ( byte ) (opCode & 0xff)};
        byte[] fileNameBytes = this.fileName.getBytes(StandardCharsets.UTF_8);
        byte zero = 0;
        byte[] result = new byte[opCodeBytes.length+1+fileNameBytes.length+1];
        int offset = 0;
        System.arraycopy(opCodeBytes, 0, result, offset, opCodeBytes.length);
        offset += opCodeBytes.length;
        result[2] = addOrRemove;
        offset++;
        System.arraycopy(fileNameBytes, 0, result, offset, fileNameBytes.length);
        result[result.length-1] = zero;
        return result;
        
    }

    public byte getNeedToBcast()
    {
        return this.needToBcast;
    }

    public void doneBcast()
    {
        this.needToBcast = -1;
    }

    public static byte[] vectorToBytes(Vector<String> files)
    {
        List<Byte> byteList = new ArrayList<>();
        for (String file : files) {
            byte[] strBytes = file.getBytes(StandardCharsets.UTF_8);
            for (byte b : strBytes) {
                byteList.add(b);
            }
            byte zero = 0;
            byteList.add(zero);
        }

        byte[] result = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            result[i] = byteList.get(i);
        }

        return result;
    }
}



