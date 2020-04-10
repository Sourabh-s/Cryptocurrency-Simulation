package blockchain;

import java.util.ArrayList;

public class Blockchain {
    private long id;
    private String prevBlockHash;
    private final ArrayList<Block> chain;

    public Blockchain() {
        this.id = 1;
        prevBlockHash = "0";
        chain = new ArrayList<Block>();
    }

    public Block createBlock() {
        Block block = new Block(id, prevBlockHash);
        chain.add(block);
        id += 1;
        prevBlockHash = block.getHash();
        return block;
    }

    public boolean isValid() {
        long id = 1;
        String prevBlockHash = "0";

        for (Block block : chain) {
            if (block.getId() != id) return false;
            if (!block.getPrevBlockHash().equals(prevBlockHash)) return false;
            String expectedHash = StringUtil.applySha256(block.toString());
            if (!block.getHash().equals(expectedHash)) return false;

            id += 1;
            prevBlockHash = block.getHash();
        }

        return true;
    }
}