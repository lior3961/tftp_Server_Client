package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {

    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;

    @Override
    public byte[] decodeNextByte(byte nextByte)
    {
        byte lastByte = 0; 
        if (len >= 2)
        {
            byte [] b = new byte []{bytes[0] , bytes[1]};       
            short opCode = ( short ) ((( short ) b[0]) << 8 | ( short ) ( b[1]) );
            switch (opCode)
            {
                case 3:
                    if(len >= 6)
                    {
                        byte [] dataSizeByts = new byte []{bytes[2] , bytes[3]};       
                        short dataSize = ( short ) ((( short ) dataSizeByts[0]) << 8 | ( short ) ( dataSizeByts[1]) ); //getting data size number from the array
                        if(len == dataSize + 5)
                        {
                            pushByte(nextByte);
                            return popBytes();
                        }
                    }
                    break;
                case 4:
                    if(len == 4)
                    {
                        return popBytes();  
                    }
                    break;
                case 5:
                    if(len >=4 && nextByte == lastByte)
                    {
                        return popBytes();  
                    } 
                    break; 
                case 6:
                    if(len == 2)
                    {
                        return popBytes();
                    } 
                    break;               
                case 9:
                    if(len >=3 && nextByte == lastByte)
                    {
                        return popBytes();  
                    }
                    break; 
                case 10:
                    if(len == 2)
                    {
                        return popBytes();
                    }
                    break;
                default:
                    if(nextByte == lastByte)
                    {
                        return popBytes();
                    }       
            }
        }
        pushByte(nextByte);          
        return null;
    }

    private void pushByte(byte nextByte)
    {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    @Override
    public byte[] encode(byte[] message)
    {
        byte[] ans = Arrays.copyOf(message , message.length+1);
        byte lastByte = 0;
        ans[ans.length-1] = lastByte;
        return  message;
    }

    private byte[] popBytes()
    {
        byte[] result = Arrays.copyOf(bytes, len);   
        len = 0;
        return result;
    }
}