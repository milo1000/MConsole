package pl.skifo.mconsole;

import java.io.Serializable;

import android.net.Uri;

public class ServerInfo implements Serializable {

    private static final long serialVersionUID = -7363194944928056891L;
    
    public static final int DEFAULT_PORT = 25575;
    public static final String DEFAULT_ADDRESS = "127.0.0.1";
    
    private String address;
    private String password;
    private int port;
    private String name;
    private boolean cleanPasswdAfterUse = true;
    
    
    public ServerInfo(String address) {
        this.address = address;
        name = address;
        port = DEFAULT_PORT;
        password = "";
    }

    public ServerInfo() {
        this(DEFAULT_ADDRESS);
    }
    
    public void setAddress(String address) {
        this.address = (address == DEFAULT_ADDRESS) ? DEFAULT_ADDRESS:Uri.encode(address);
        if (name == DEFAULT_ADDRESS) {
            name = address;
        }
    }
    
    public void setEncodedName(String eName) {
        name = Uri.decode(eName);
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public String getEncodedName() {
        return Uri.encode(name);
    }

    public String getName() {
        return name;
    }
    
    public void setPort(int p) {
        if (p >= 0 && p < 65536) // validity check
            port = p;
    }
    
    public int getPort() {
        return port;
    }
    
    public String getAddress() {
        return address;
    }

    public void setPassword(String pswd) {
        password = (pswd == null)?"":pswd;
    }
    
    public String getPassword() {
        return password;
    }

    public void cleanAfterUse() {
        if (cleanPasswdAfterUse) {
            password = "";
        }
    }
    
    private String getEncodedPassword() {
        return Uri.encode(password);
    }
    
    public void setEncodedPassword(String ePassword) {
        password = Uri.decode(ePassword);
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public String toExternalForm() {
        return getAddress()+","+getEncodedName()+","+port+","+getEncodedPassword(); 
    }

    
}
