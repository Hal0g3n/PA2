package p2pOverlay.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.BitSet;

public class Encoding {

    public static ByteBuffer str_to_bb(String msg){ return str_to_bb(msg, CharsetUtil.US_ASCII);}

    public static ByteBuffer str_to_bb(String msg, Charset charset){
        return ByteBuffer.wrap(msg.getBytes(charset));
    }

    public static String bb_to_str(ByteBuf buffer){ return bb_to_str(buffer, CharsetUtil.US_ASCII);}

    public static String bb_to_str(ByteBuf buffer, Charset charset){
        return buffer.toString(charset);
    }

    public static BitSet stringToBitSet(String id) {
        if (id.matches("[0-1]+")) {
            BitSet bitSet = new BitSet(id.length());
            for (int i = 0; i < id.length(); i++) {
                if (id.charAt(i) == '1') bitSet.set(i);
            }

            return bitSet;
        } else throw new IllegalArgumentException("ID must be a binary string");
    }

    public static String bitSetToString(BitSet id) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < id.length(); i++) {
            string.append(id.get(i) ? '1' : '0');
        }

        return string.toString();
    }

    public static BitSet intToBitSet(int n, int bsLen){
        BitSet bitset = new BitSet(bsLen);
        int index = 0;
        while(n != 0){
            if((n & 1) == 1) bitset.set(index);
            index++;
            n >>>= 1;
        }
        return bitset;
    }

}
