package blockchain;

import java.io.Serializable;
import java.util.ArrayList;

public class Blockchain implements Serializable {
    private long id;
    private String prevBlockHash;
    private final ArrayList<Block> chain;
    private String fileName = null;
    private int noOfStartZerosForHash = 0;

    private Blockchain(String fileName, int noOfStartZerosForHash) {
        this.id = 1;
        prevBlockHash = "0";
        chain = new ArrayList<Block>();
        this.fileName = fileName;
        this.noOfStartZerosForHash = noOfStartZerosForHash;
    }

    public void addBlock() {
        Block block = Block.generateBlock(id, prevBlockHash, noOfStartZerosForHash);
        chain.add(block);
        id++;
        prevBlockHash = block.getHash();

        try{
            SerializationUtils.serialize(this, fileName);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Blockchain generateBlockchain (String fileName, int noOfStartZerosForHash, Object caller) {
        if(!(caller instanceof BlockchainDriver)) throw new IllegalCallerException();
        return new Blockchain(fileName, noOfStartZerosForHash);
    }

    public boolean isValid() {
        long id = 1;
        String prevBlockHash = "0";
        String expectedStarting = "0".repeat(Math.max(0, noOfStartZerosForHash));

        for (Block block : chain) {
            if (block.getId() != id) return false;
            if (!block.getPrevBlockHash().equals(prevBlockHash)) return false;
            if (!block.isConsistent()) return false;
            String presentHash = block.getHash();
            if (!presentHash.startsWith(expectedStarting)) return false;

            id += 1;
            prevBlockHash = presentHash;
        }

        return true;
    }

    public long getLength() { return chain.size(); }

    public Block getBlock(int index) { return chain.get(index); }

    public int getNoOfStartZerosForHash() { return noOfStartZerosForHash; }
}