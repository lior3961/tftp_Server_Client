package bgu.spl.net.impl.tftp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;
public class Action {


    private boolean needToSendData;
    private boolean needToDeleteFile;
    private boolean needToDisc;
    private Vector<String> clientFiles;
    private String fileName;
    private String userName;
    private boolean connected;
    

    public Action(boolean connected)
    {
        this.needToSendData = false;
        this.needToDeleteFile = false;
        Path clientFilesFolderPath = (Paths.get("").toAbsolutePath()).resolve("Files");//get "Files" path
        this.clientFiles = getFileList(clientFilesFolderPath.toString());
        this.fileName = "";
        this.userName = "";
        this.connected = connected;
        this.needToDisc = false;
    }

    public byte[] createAction(String input)
    {
        byte[] msg = null;
        short opCode;
        int spaceIndex = input.indexOf(" ");
        if(spaceIndex != -1)
        {
            String action = input.substring(0,spaceIndex);
            String content = input.substring(spaceIndex+1);
            switch (action) 
            {
                case "LOGRQ":
                    this.userName = content;
                    opCode = 7;
                    this.needToSendData = false;
                    this.needToDeleteFile = false;
                    this.needToDisc = false;
                    msg = createPacket(opCode , content);
                    break;
                case "RRQ":
                    this.fileName = content;
                    if(!this.clientFiles.contains(content))
                    {
                        opCode = 1;
                        this.needToSendData = false;
                        this.needToDeleteFile = true;
                        this.needToDisc = false;
                        msg = createPacket(opCode , content);
                        updateFiles(content, false, null);
                        break;
                    }
                    System.out.println("File already exist");
                    break;
                case "WRQ":
                    this.fileName = content;
                    if(this.clientFiles.contains(content))
                    {
                        opCode = 2;
                        this.needToSendData = true;
                        this.needToDeleteFile = false;
                        this.needToDisc = false;
                        msg = createPacket(opCode , content);
                    }
                    System.out.println("File doesnt exist");
                    break;
                case "DELRQ":
                    this.fileName = content;
                    opCode = 8;
                    this.needToSendData = false;
                    this.needToDeleteFile = false;
                    this.needToDisc = false;
                    msg = createPacket(opCode , content);
                    break;
            }
        }
        else
        {
            switch (input) 
            {
                case "DISC":
                    opCode = 10;
                    this.needToSendData = false;
                    this.needToDeleteFile = false;
                    this.needToDisc = true;
                    msg = createPacket(opCode , input);
                    break;
                case "DIRQ":
                    opCode = 6;
                    this.needToSendData = false;
                    this.needToDeleteFile = false;
                    this.needToDisc = false;
                    msg = createPacket(opCode , input);
                    break;
            }
        }
        return msg;
    }

    public byte[] createPacket(short opCode, String content)
    {
        byte lastbyte = 0;
        byte[] result = null;
        int offset = 0;
        byte[] opCodeBytes;
        byte[] contentByts;
        switch (opCode) {
            case 1:
                result = new byte[2+content.length()+1];
                opCodeBytes = shortToArray(opCode);
                contentByts = content.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(opCodeBytes, 0, result, offset, opCodeBytes.length);
                offset += opCodeBytes.length;
                System.arraycopy(contentByts, 0, result, offset, contentByts.length);
                result[result.length-1] = lastbyte;
                break;
            case 2:
                result = new byte[2+content.length()+1];
                opCodeBytes = shortToArray(opCode);
                contentByts = content.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(opCodeBytes, 0, result, offset, opCodeBytes.length);
                offset += opCodeBytes.length;
                System.arraycopy(contentByts, 0, result, offset, contentByts.length);
                result[result.length-1] = lastbyte;
                break;
            case 6:
                opCodeBytes = shortToArray(opCode);
                result = Arrays.copyOf(opCodeBytes, opCodeBytes.length);
                break;
            case 7:
                result = new byte[2+content.length()+1];
                opCodeBytes = shortToArray(opCode);
                contentByts = content.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(opCodeBytes, 0, result, offset, opCodeBytes.length);
                offset += opCodeBytes.length;
                System.arraycopy(contentByts, 0, result, offset, contentByts.length);
                result[result.length-1] = lastbyte;
                break;
            case 8:
                result = new byte[2+content.length()+1];
                opCodeBytes = shortToArray(opCode);
                contentByts = content.getBytes(StandardCharsets.UTF_8);
                System.arraycopy(opCodeBytes, 0, result, offset, opCodeBytes.length);
                offset += opCodeBytes.length;
                System.arraycopy(contentByts, 0, result, offset, contentByts.length);
                result[result.length-1] = lastbyte;
                break;
            case 10:
                opCodeBytes = shortToArray(opCode);
                result = Arrays.copyOf(opCodeBytes, opCodeBytes.length);
                break;            
        }
        offset = 0;
        return result;
    }

    public byte[] gotMessageFromServer(byte[] packet)
    {
        if(packet == null)
        {
            return null;
        }
        short opCode = arrayToShort(packet);
        byte[] blockNumberBytes = new byte[2];
        short blockNumber;
        byte[] result = null;
        switch (opCode)
        {
            case 3:
                blockNumberBytes[0] = packet[2];
                blockNumberBytes[1] = packet[3];
                blockNumber = arrayToShort(blockNumberBytes);
                result = createAckPacket(blockNumber);
                byte[] data = Arrays.copyOfRange(packet, 6, packet.length);
                updateFiles(this.fileName, false, data);
                break;
            case 4:
                if(needToDisc)
                {
                    this.connected = false;
                }
                if(needToSendData)
                {
                    blockNumberBytes[0] = packet[2];
                    blockNumberBytes[1] = packet[3];
                    blockNumber = arrayToShort(blockNumberBytes);
                    System.out.println("ACK " + blockNumber);
                    blockNumber += 1;
                    result = createDataPacket(blockNumber);
                    break;
                }
                System.out.println("ACK 0");
                break;
            case 5:
            {
                byte[] errorMsgBytes = Arrays.copyOfRange(packet, 4, packet.length-1);
                String decodedString = new String(errorMsgBytes, StandardCharsets.UTF_8);
                System.out.println(decodedString);
                if(this.needToDeleteFile)
                {    
                    updateFiles(this.fileName , true , null);
                }
            }
            case 9:
            {
                byte[] fileNameBytes = Arrays.copyOfRange(packet, 3, packet.length-1);
                String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
                byte deleteOrAdded = packet[2];
                if(deleteOrAdded == 0)
                {
                    System.out.println("BCAST del " + fileName);
                }
                else
                {
                    System.out.println("BCAST add " + fileName);
                }
                break;
            }
        }
        return result;
    }

    public byte[] shortToArray(short num)
    {
        byte [] result = new byte []{( byte )( num >> 8) , ( byte ) ( num & 0xff ) };
        return result;

    }

    public short arrayToShort(byte[] arr)
    {
        short b_short = (short) (((short) arr[0]) << 8 | (short) (arr[1]) & 0x00ff);
        return b_short;
    }

    public byte[] createDataPacket(short blockNumber)
    {
        Path clientFilesFolderPath = (Paths.get("").toAbsolutePath()).resolve("Files");//get "Files" path
        try (FileInputStream fileInputStream = new FileInputStream(clientFilesFolderPath.resolve
        (this.fileName).toFile())) {
            long startPosition = (blockNumber - 1) * 512; // Calculate the starting position
            fileInputStream.skip(startPosition); // Skip to the starting position
            byte[] buffer = new byte[512];
            int bytesRead = fileInputStream.read(buffer); // Read 512 bytes from the file
            if (bytesRead != -1) 
            {
                byte[] packet = new byte[bytesRead + 6];
                codesOfDataPacket(packet , (short) bytesRead,blockNumber);
                System.arraycopy(buffer, 0, packet, 6, bytesRead);
                return packet;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // Return an empty byte array if there was an error
    }

    public void codesOfDataPacket(byte[] packet , short blockSize, short blockNumber)
    {
        short opCode = 3;
        byte [] opBytes = shortToArray(opCode);
        byte [] blockBytes = shortToArray(blockNumber);
        byte [] sizeBytes = shortToArray(blockSize);
        packet[0] = opBytes[0];
        packet[1] = opBytes[1];
        packet[2] = sizeBytes[0];
        packet[3] = sizeBytes[1];
        packet[4] = blockBytes[0];
        packet[5] = blockBytes[1];
    }

    public byte[] createAckPacket(short blockNumber)
    {
        short ackCode = 4;
        byte [] opCodeBytes = shortToArray(ackCode);
        byte [] blockNumberBytes = shortToArray(blockNumber);
        byte[] result = Arrays.copyOf(opCodeBytes, opCodeBytes.length+blockNumberBytes.length);
        System.arraycopy(blockNumberBytes, 0, result, opCodeBytes.length, blockNumberBytes.length);
        return result;
    }

    public void updateFiles(String fileName, boolean delete, byte[] data)
    {
        Path serverFilesFolderPath = (Paths.get("").toAbsolutePath()).resolve("Files");
        Path filePath = serverFilesFolderPath.resolve(fileName);
        if(delete)
        {
            this.clientFiles.remove(fileName);
            File file = filePath.toFile();
            file.delete();
        }
        else
        {
            if(!this.clientFiles.contains(fileName))
            {
                this.clientFiles.add(fileName);
                System.out.println();
            }
            try 
            {
                FileOutputStream fos = new FileOutputStream(fileName.toString(),true); //new file output stream with file path
                if(data != null)
                {
                    fos.write(data); //creating file
                    fos.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
            
        }
    }

    public boolean shouldTerminate()
    {
        return !this.connected;
    }

    
    public static Vector<String> getFileList(String directoryPath) {
    Vector<String> fileList = new Vector<String>();
    File directory = new File(directoryPath);      
    File[] files = directory.listFiles();
    if (files != null)
    {
        for (File file : files) {
            if (file.isFile()) {
                fileList.add(file.getName());
            }
        }
    }       
    return fileList;
    }

}
