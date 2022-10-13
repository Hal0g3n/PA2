package p2pOverlay.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Encoding {

    public static ByteBuffer str_to_bb(String msg){ return str_to_bb(msg, CharsetUtil.US_ASCII);}

    public static ByteBuffer str_to_bb(String msg, Charset charset){
        return ByteBuffer.wrap(msg.getBytes(charset));
    }

    public static String bb_to_str(ByteBuf buffer){ return bb_to_str(buffer, CharsetUtil.US_ASCII);}

    public static String bb_to_str(ByteBuf buffer, Charset charset){
        return buffer.toString(charset);
    }
}
