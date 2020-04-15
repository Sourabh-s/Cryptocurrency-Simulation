package blockchain;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

public class Block implements Serializable {

    private final String prevBlockHash;
    private final long id;
    private final long timestamp;
    private String hash;
    private int magicNum;
    private long timeTook;

    private Block(final long id, final String prevBlockHash) {
        this.prevBlockHash = prevBlockHash;
        this.id = id;
        timestamp = new Date().getTime();
    }

    public static Block generateBlock(final long id, final String prevBlockHash, final int noOfStartZerosForHash) {
        Block block = new Block(id, prevBlockHash);
        final long startTime = System.nanoTime();
        block.hash = generateHash(block, noOfStartZerosForHash);
        final long endTime = System.nanoTime();
        block.timeTook = (long) ((endTime - startTime) / 1e9);
        return block;
    }

    private static String generateHash(Block block, int noOfStartZerosForHash) {
        String expectedStarting = "0".repeat(Math.max(0, noOfStartZerosForHash));
        Random random = new Random();

        while(true) {
            block.magicNum = random.nextInt();
            String computedHash = StringUtils.applySha256(block.toString());
            if(computedHash.startsWith(expectedStarting)) return computedHash;
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(prevBlockHash);
        str.append(id);
        str.append(timestamp);
        str.append(magicNum);
        return str.toString();
    }

    public long getId() { return id; }

    public long getTimestamp() { return timestamp; }

    public String getHash() { return hash; }

    public String getPrevBlockHash() { return prevBlockHash; }

    public long getTimeTook() { return timeTook; }

    public int getMagicNum() { return magicNum; }

    public boolean isConsistent() {
        return hash.equals(StringUtils.applySha256(this.toString()));
    }
}