package blockchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import blockchain.miner.Miner;

public class Blockchain implements Serializable {
    private long runningBlockId;
    private String runningPrevBlockHash;
    private final ArrayList<Block> chain;
    private final Queue<Block> unprocessedBlocks;
    private BlockchainDriver creator;

    private int noOfStartZerosForHash;
    private String requiredPrefixForHash;

    private static final int blockCreationFrequencyPerMinute = 3;
    private static final int fixedMiningTimeMs = (int) ((60 * 1e3) / blockCreationFrequencyPerMinute);
    /**
     * 15% of deviation from fixed time is acceptable
     */
    private static final int acceptableDeviationInMiningTimeMs = (int) ((fixedMiningTimeMs * 15) / 100);

    private Block currentMiningBlock;
    private long currentMiningBlockStartTimeMs;
    private long currentMiningBlockEndTimeMs;

    private Blockchain() {
        runningBlockId = 1;
        runningPrevBlockHash = "0";
        chain = new ArrayList<>();
        unprocessedBlocks = new ConcurrentLinkedQueue<>();

        noOfStartZerosForHash = 0;
        requiredPrefixForHash = "";
    }

    public void addBlock() {
        Block block = Block.with(runningBlockId);
        if(runningPrevBlockHash != null) {
            block.setPrevBlockHash(runningPrevBlockHash);
            runningPrevBlockHash = null;
        }

        if(unprocessedBlocks.isEmpty()) {
            currentMiningBlock = block;
            currentMiningBlockStartTimeMs = System.currentTimeMillis();
        }
        unprocessedBlocks.add(block);
        runningBlockId++;
        creator.saveBlockchain();
    }

    public static Blockchain generateBlockchain(Object caller) {
        if(!(caller instanceof BlockchainDriver)) throw new IllegalCallerException();
        Blockchain blockchain = new Blockchain();
        blockchain.creator = (BlockchainDriver) caller;
        return blockchain;
    }

    public boolean isValid() {
        long id = 1;
        String prevBlockHash = "0";

        for (Block block : chain) {
            if (block.getId() != id) return false;
            if (!block.getPrevBlockHash().equals(prevBlockHash)) return false;
            if (!block.isConsistent()) return false;
            String presentHash = block.getHash();

            id += 1;
            prevBlockHash = presentHash;
        }

        return true;
    }

    public synchronized boolean submitBlock(Block block, Object caller) {
        if (!(caller instanceof Miner)) {
            throw new IllegalCallerException();
        }

        currentMiningBlockEndTimeMs = System.currentTimeMillis();

        if (!areSameBlocks(currentMiningBlock, block)) { return false; }
        if (!block.getHash().startsWith(requiredPrefixForHash)) { return false; }
        if (!block.isConsistent()) { return false; }

        block.setTimeTookMs(currentMiningBlockEndTimeMs - block.getTimestamp());
        block.setTimeTookForMiningMs(currentMiningBlockEndTimeMs - currentMiningBlockStartTimeMs);
        chain.add(block);
        unprocessedBlocks.poll();
        updateMiningConstraints();

        runningPrevBlockHash = block.getHash();

        Block nextUnprocessedBlock = unprocessedBlocks.peek();
        if (nextUnprocessedBlock != null) {
            nextUnprocessedBlock.setPrevBlockHash(runningPrevBlockHash);
            runningPrevBlockHash = null;
        }
        currentMiningBlock = nextUnprocessedBlock;

        creator.saveBlockchain();
        currentMiningBlockStartTimeMs = System.currentTimeMillis();
        return true;
    }

    public Block getUnprocessedBlock() {
        if(currentMiningBlock == null) return null;
        return (Block) currentMiningBlock.clone();
    }

    public long getLength() { return chain.size(); }

    public Block getBlock(int index) { return chain.get(index); }

    public String getRequiredPrefixForHash() { return requiredPrefixForHash; }

    public static int getBlockCreationFrequencyPerMinute() { return blockCreationFrequencyPerMinute; }

    public static int getFixedMiningTimeMs() { return fixedMiningTimeMs; }

    public static int getAcceptableDeviationInMiningTimeMs() { return acceptableDeviationInMiningTimeMs; }

    private static boolean areSameBlocks(Block b1, Block b2) {
        if (b1 == b2) { return true; }

        if (b1.getClass() != b2.getClass()) { return false; }
        if (b1.getId() != b2.getId()) { return false; }
        if (b1.getTimestamp() != b2.getTimestamp()) { return false; }
        if (!b1.getPrevBlockHash().equals(b2.getPrevBlockHash())) { return false; }

        return true;
    }

    private void updateMiningConstraints() {
        long timeTookForMining = currentMiningBlockEndTimeMs - currentMiningBlockStartTimeMs;

        if (timeTookForMining >= (fixedMiningTimeMs - acceptableDeviationInMiningTimeMs)
            && timeTookForMining <= (fixedMiningTimeMs + acceptableDeviationInMiningTimeMs)) {
            return;
        }

        if(timeTookForMining < (fixedMiningTimeMs - acceptableDeviationInMiningTimeMs)) {
            noOfStartZerosForHash++;
            requiredPrefixForHash = requiredPrefixForHash.concat("0");
            return;
        }

        noOfStartZerosForHash = Math.max(0, --noOfStartZerosForHash);
        requiredPrefixForHash = "0".repeat(noOfStartZerosForHash);
    }
}