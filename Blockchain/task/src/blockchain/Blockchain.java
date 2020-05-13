package blockchain;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import blockchain.miner.Miner;

public class Blockchain implements Serializable {
    private long runningBlockId;
    private String runningPrevBlockHash;
    private final List<Block> chain;
    private final Queue<Message> messages;
    private BlockchainDriver creator;

    private int noOfStartZerosForHash;
    private String requiredPrefixForHash;

    private static final int BLOCK_CREATION_FREQUENCY_PER_MINUTE = 3;
    private static int FIXED_MINING_TIME_MS = (int) ((60 * 1e3) / BLOCK_CREATION_FREQUENCY_PER_MINUTE);
    // 15% deviation from fixed time is acceptable
    private static int ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS = (int) ((FIXED_MINING_TIME_MS * 15) / 100);

    private Block currentMiningBlock;
    private long currentMiningBlockStartTimeMs;
    private long currentMiningBlockEndTimeMs;

    private Blockchain() {
        runningBlockId = 1;
        runningPrevBlockHash = "0";
        chain = new LinkedList<>();
        messages = new ConcurrentLinkedQueue<Message>();
        //TODO: Create the first block

        noOfStartZerosForHash = 0;
        requiredPrefixForHash = "";
    }

    public static Blockchain generateBlockchain(Object caller) {
        if(!(caller instanceof BlockchainDriver)) throw new IllegalCallerException();
        Blockchain blockchain = new Blockchain();
        blockchain.creator = (BlockchainDriver) caller;
        return blockchain;
    }

    public void addMessage(Message message) {
        if (currentMiningBlock == null) {

        }
    }

    private void addBlock() {
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

        if (!areIdenticalBlocks(currentMiningBlock, block)) { return false; }
        if (!block.getHash().startsWith(requiredPrefixForHash)) { return false; }
        if (!block.isConsistent()) { return false; }

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

    public static int getFixedMiningTimeMs() { return FIXED_MINING_TIME_MS; }

    public static int getAcceptableDeviationInMiningTimeMs() { return ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS; }

    private static boolean areIdenticalBlocks(Block b1, Block b2) {
        if (b1 == b2) { return true; }

        if (b1.getClass() != b2.getClass()) { return false; }
        if (b1.getId() != b2.getId()) { return false; }
        if (b1.getTimestamp() != b2.getTimestamp()) { return false; }
        if (!b1.getPrevBlockHash().equals(b2.getPrevBlockHash())) { return false; }
        //TODO: The blockchain requires that two blocks contain same messages to be identical

        return true;
    }

    private void updateMiningConstraints() {
        long timeTookForMining = currentMiningBlockEndTimeMs - currentMiningBlockStartTimeMs;

        if (timeTookForMining >= (FIXED_MINING_TIME_MS - ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS)
            && timeTookForMining <= (FIXED_MINING_TIME_MS + ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS)) {
            return;
        }

        if(timeTookForMining < (FIXED_MINING_TIME_MS - ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS)) {
            noOfStartZerosForHash++;
            requiredPrefixForHash = requiredPrefixForHash.concat("0");
            return;
        }

        noOfStartZerosForHash = Math.max(0, --noOfStartZerosForHash);
        requiredPrefixForHash = "0".repeat(noOfStartZerosForHash);
    }
}