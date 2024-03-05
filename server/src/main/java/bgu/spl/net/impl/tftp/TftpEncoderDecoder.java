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
        if (nextByte == 0) {
            return popBytes();
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
        ans[ans.length-1] = 0;

        return  ans;
    }

    private byte[] popBytes()
    {
        byte[] result = Arrays.copyOf(bytes, len);   
        len = 0;
        return result;
    }
}