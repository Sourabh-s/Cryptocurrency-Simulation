package blockchain;

import java.io.Serializable;
import java.util.List;
import blockchain.utils.*;

public class Block implements Serializable, Cloneable {
    private final long id;
    private final long timestamp;
    private String prevBlockHash;
    private List<Message> messages;
    private String hash;
    private int magicNum;
    private long timeTookForMiningMs;
    private long minerId;
    private String messagesToString;

    private Block(final long id, final List<Message> messages, final String prevBlockHash) {
        this.id = id;
        this.messages = messages;
        this.prevBlockHash = prevBlockHash;
        timestamp = System.currentTimeMillis();
    }

    public static Block with(final long id, final List<Message> messages, final String prevBlockHash) {
        Block block =  new Block(id, messages, prevBlockHash);
        block.messagesToString = block.messages.stream()
                                    .map(Message::toString)
                                    .reduce("", String::concat);
        return block;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(prevBlockHash);
        str.append(id);
        str.append(messagesToString);
        str.append(timestamp);
        str.append(magicNum);
        return str.toString();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Something messed up!");
        }
    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }

        if (obj == null) { return false; }

        if (obj.getClass() != getClass()) { return false; }

        if (this.id != ((Block) obj).id) { return false; }

        if (this.timestamp != ((Block) obj).timestamp) { return false; }

        if (minerId != ((Block) obj).minerId) { return false; }

        if (magicNum != ((Block) obj).magicNum) { return false; }

        if (timeTookForMiningMs != ((Block) obj).timeTookForMiningMs) { return false; }

        if (!this.prevBlockHash.equals(((Block) obj).prevBlockHash)) { return false; }

        if (!this.hash.equals(((Block) obj).hash)) { return false; }

        //TODO - Two equal blocks will have same messages. Incorporate that condition.

        return true;
    }

    public boolean isConsistent() {
        return hash.equals(StringUtils.applySha256(toString()));
    }

    public long getId() { return id; }

    public long getTimestamp() { return timestamp; }

    public String getPrevBlockHash() { return prevBlockHash; }

    public String getHash() { return hash; }

    public void setHash(String hash) { this.hash = hash; }

    public int getMagicNum() { return magicNum; }

    public void setMagicNum(int magicNum) { this.magicNum = magicNum; }

    public long getTimeTookForMiningMs() { return this.timeTookForMiningMs; }

    public void setTimeTookForMiningMs(long timeTookForMiningMs) {
        this.timeTookForMiningMs = timeTookForMiningMs;
    }

    public long getMinerId() { return minerId; }

    public void setMinerId(long minerId) { this.minerId = minerId; }

    public List<Message> getMessages() {
        //TODO: Implement this getter
        throw new UnsupportedOperationException();
    }

    public String getMessagesToString() { return messagesToString; }
}