package pl.skifo.mconsole;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.util.Log;

public class ServerConnector {

    private static final String LOG_PREFIX = "SvConn";
    
    static private final int SERVERDATA_AUTH = 3;
    static private final int SERVERDATA_EXECCOMMAND = 2;
    static private final int SERVERDATA_AUTH_RESPONSE = 2;
    static private final int SERVERDATA_RESPONSE_VALUE = 0;

    private int idGenerator = 0;
    
    private int createRequestId() {
        int i = (idGenerator + 1) & 0xfffff;
        idGenerator = i;
        return i;
    }
    
    private static int put(byte[] buffer, int offset, int value) {
        buffer[offset + 0] = (byte)(value & 0xff);
        buffer[offset + 1] = (byte)((value >> 8) & 0xff);
        buffer[offset + 2] = (byte)((value >> 16) & 0xff);
        buffer[offset + 3] = (byte)((value >> 24) & 0xff);
        return 4;
    }
    
    private static int get(byte[] buffer, int off) {

        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "off = "+off+", buf[3] = <0x"+Integer.toHexString((buffer[off + 3] &  0xff))+"> buf[2] = <0x"+Integer.toHexString((buffer[off + 2] &  0xff))+"> buf[1] = <0x"+Integer.toHexString((buffer[off + 1] &  0xff))+"> buf[0] = <0x"+Integer.toHexString((buffer[off + 0] &  0xff))+">");
        
        int ret = (buffer[off + 3] << 24) | (((buffer[off + 2] &  0xff) << 16)) | (((buffer[off + 1] &  0xff) << 8)) | ((buffer[off + 0] &  0xff));
        return ret;
    }

    private int sendPacket(String text, int type) throws IOException {
        byte[] payload = text.getBytes();
        int id = (out == null) ? -1 : createRequestId();
        if (id != -1) {
            int len = 4 + 4 + payload.length + 1 + 1;
            byte scratch[] = new byte[len + 4];
            put(scratch, 0, len);
            put(scratch, 4, id);
            put(scratch, 8, type);
            int i = 0;
            for (; i < payload.length; i++) {
                scratch[12 + i] = payload[i];
            }
            i += 12;
            scratch[i++] = 0;
            scratch[i] = 0;
            out.write(scratch);
            out.flush();

            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "sendPacket: len = "+len+", id = "+id+", type = "+type+", body = <"+text+">");
        }
        else {
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "sendPacket: null out, connection error?");
        }
        return id;
    }
    
    private byte[] responseBuffer = new byte[4096];
    
    public ServerResponse getResponse() throws IOException {
        
        ServerResponse ret = new ServerResponse(); 
        if (in != null) {
            int off = 0;
            int left = 4 + 4 + 4; // len + id + type
            do {
                if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "getResponse(): in.read ==> (off = "+off+", left = "+left+")");
                int r = in.read(responseBuffer, off, left);
                if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "getResponse(): in.read <== r = "+r);
                if (r <= 0)
                    return ret;
                
                for (int z = 0; z < r; z++) {
                    if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "getResponse(): off["+(off+z)+"] = 0x"+Integer.toHexString(responseBuffer[z + off]&0xff));
                }

                left -= r;
                off += r;
            }
            while (left > 0);
            int len = get(responseBuffer, 0);
            ret.id = get(responseBuffer, 4);
            int type = get(responseBuffer, 8);
            
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "first response: len = "+len+"[0x"+Integer.toHexString(len)+"], id = "+ret.id+", type = "+type+", ret = <"+ret+">");
            
            left = len - 8;
            if (left > 0 && left <= responseBuffer.length) {
                off = 0;
                do {
                    if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "getResponse(): in.read 2 ==> (off = "+off+", left = "+left+")");
                    int r = in.read(responseBuffer, off, left);
                    if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "getResponse(): in.read 2 <== r = "+r);
                    if (r <= 0)
                        return ret;
                    left -= r;
                    off += r;
                }
                while (left > 0);
                int i = off - 1;
                for (; i >= 0 && responseBuffer[i] == 0; i--);
                if (i >= 0) {
                    
//                    for (int j = 0; j < i + 1; j++) {
//                        Log.d(LOG_PREFIX, "response["+j+"]: 0x"+Integer.toHexString(responseBuffer[j] & 0xff));
//                    }
                    //ret.setResponse(new String(responseBuffer, 0, i + 1));
                    ret.setResponse(responseBuffer, i + 1);
                }
            }
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "response: len = "+len+", id = "+ret.id+", type = "+type+", ret = <"+ret+">");
        }
        else {
            if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "null input, no connection?");
        }
        return ret;
    }

    
    private OutputStream out;
    private InputStream in;
    private Socket soc;
    
    public ServerConnector() {
        
    }
    
    public void connect(String server, int port) throws UnknownHostException, IOException {
        
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "connecting to server: "+server+":"+port);
        InetSocketAddress ia = null;
        try {
            ia = new InetSocketAddress(InetAddress.getByName(server), port);
        }
        catch (IllegalArgumentException fatal) {
            String msg = fatal.getMessage();
            throw new IOException(msg == null ? fatal.getClass().getName() : msg);
        }
        soc = new Socket();
        soc.connect(ia, 5000);
        soc.setKeepAlive(true);
        out = soc.getOutputStream();
        in = soc.getInputStream();
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "connected");
    }
    
    public boolean login(String password) throws IOException {
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "login with password <"+password+">");
        int id = sendPacket(password, SERVERDATA_AUTH);
        ServerResponse resp = getResponse();
        boolean ret = (resp.id != -1);
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(LOG_PREFIX, "login: "+((ret)?"OK":"failed"));
        return ret;
    }
    
    public void sendCommand(String command) throws IOException {
        int id = sendPacket(command, SERVERDATA_EXECCOMMAND);
    }
    
    public void disconnect() {
        
        if (MConsoleActivity.LOG_DEBUG) MConsoleActivity.d(MConsoleActivity.LOG_PREFIX, "socket disconnect");
        
        try {
            if (soc != null)
                soc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
