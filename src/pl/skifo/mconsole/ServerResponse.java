package pl.skifo.mconsole;

public class ServerResponse {
    
    public static final AttributedBlock EMPTY_RESPONSE = new AttributedBlock("empty response");
    
    // TODO: dynamic change
    public static final boolean MINECRAFT_COLORING_ENABLED = true;
    
    public int id;
    private AttributedBlock responseBlock;
    
    public ServerResponse() {
        id = -1;
        responseBlock = EMPTY_RESPONSE;
    }
    
    public synchronized void setResponse(String r) {
        responseBlock = new AttributedBlock(r);
    }

    public synchronized void setResponse(byte[] buff, int cnt) {
        responseBlock = AttributedBlock.createAttributedBlock(buff, cnt); 
    }

    public synchronized AttributedBlock getResponseBlock() {
        return responseBlock;
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append("SrvResp[");
        out.append(id);
        out.append("]<");
        AttributedBlock tmp = getResponseBlock();
        out.append(tmp.toString());
        out.append(">");
        return out.toString();
    }
}
