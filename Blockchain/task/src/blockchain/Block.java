package blockchain;

import java.util.Date;

public class Block {

    private final String prevBlockHash;
    private final long id;
    private final long timestamp;
    private final String hash;

    public Block(final long id, final String prevBlockHash) {
        this.prevBlockHash = prevBlockHash;
        this.id = id;
        timestamp = new Date().getTime();
        hash = StringUtil.applySha256(toString());
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(prevBlockHash);
        str.append(id);
        str.append(timestamp);
        return str.toString();
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getHash() {
        return hash;
    }

    public String getPrevBlockHash() {
        return prevBlockHash;
    }
}