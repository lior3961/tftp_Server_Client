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
            short opCode = arrayToShort(b);
            switch (opCode)
            {
                case 3:
                    if(len >= 6)
                    {
                        byte [] dataSizeByts = new byte []{bytes[2] , bytes[3]};       
                        short dataSize = arrayToShort(dataSizeByts); //getting data size number from the array
                        if(len == dataSize + 5)
                        {
                            pushByte(nextByte);
                            return popBytes();
                        }
                    }
                    break;
                case 4:
                    if(len == 3)
                    {
                        pushByte(nextByte);
                        return popBytes();  
                    }
                    break;
                case 5:
                    if(len >=4 && nextByte == lastByte)
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
                default:
                    if(nextByte == lastByte)
                    {
                        return popBytes();
                    }       
            }
        }
        pushByte(nextByte);
        if(len == 2)
        {
            byte [] b = new byte []{bytes[0] , bytes[1]};       
            short opCode = arrayToShort(b);
            if(opCode == 6 || opCode == 10)
            {
                return popBytes();
            }
        }
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
        return  message;
    }

    private byte[] popBytes()
    {
        byte[] result = Arrays.copyOf(bytes, len);   
        len = 0;
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
}