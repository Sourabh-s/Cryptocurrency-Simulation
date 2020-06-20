package blockchain;

import blockchain.user.Miner;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static blockchain.utils.SignatureUtils.verifySignature;

public class Blockchain implements Serializable {
    private long runningBlockId;
    private String runningPrevBlockHash;
    private final List<Block> chain;
    private final Block unprocessedBlocks[];
    private final Queue<Transaction> transactions;
    private BlockchainDriver creator;

    private int noOfStartZerosForHash;
    private String requiredPrefixForHash;

    private static final int BLOCK_CREATION_FREQUENCY_PER_MINUTE = 3;
    private static int FIXED_MINING_TIME_MS = (int) ((60 * 1e3) / BLOCK_CREATION_FREQUENCY_PER_MINUTE);
    // 15% deviation from fixed time is acceptable
    private static int ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS = ((FIXED_MINING_TIME_MS * 15) / 100);

    private long currentMiningBlockStartTimeMs;
    private long currentMiningBlockEndTimeMs;

    private AtomicLong transactionIdCounter = new AtomicLong();
    private long largestMessageIdTillPrevBlock;
    private ReentrantReadWriteLock largestMessageIdTillPrevBlockLock = new ReentrantReadWriteLock();

    private Blockchain() {
        runningBlockId = 1;
        runningPrevBlockHash = "0";
        chain = new LinkedList<>();
        unprocessedBlocks = new Block[1];
        transactions = new ConcurrentLinkedQueue<>();
        noOfStartZerosForHash = 0;
        requiredPrefixForHash = "";
        transactionIdCounter.set(1);
        largestMessageIdTillPrevBlock = 0L;
    }

    public static Blockchain generateBlockchain(Object caller) {
        if(!(caller instanceof BlockchainDriver)) throw new IllegalCallerException();
        Blockchain blockchain = new Blockchain();
        blockchain.creator = (BlockchainDriver) caller;

        blockchain.unprocessedBlocks[0] = blockchain.createBlock();
        blockchain.currentMiningBlockStartTimeMs = System.currentTimeMillis();
        return blockchain;
    }

    public boolean addTransaction(Transaction transaction) {
        largestMessageIdTillPrevBlockLock.readLock().lock();
        if (!validateMessage(transaction)) { return false; }
        transactions.add(transaction);
        largestMessageIdTillPrevBlockLock.readLock().unlock();

        synchronized (unprocessedBlocks) {
            if (unprocessedBlocks[0] == null) {
                unprocessedBlocks[0] = createBlock();
                currentMiningBlockStartTimeMs = System.currentTimeMillis();
            }
        }
        return true;
    }

    private Block createBlock() {
        largestMessageIdTillPrevBlockLock.writeLock().lock();
        largestMessageIdTillPrevBlock = transactions.stream()
                                        .map(Transaction::getId)
                                        .max(Long::compare).orElse(0L);

        List<Transaction> messages = new LinkedList<>();
        for (int i = 0; i < this.transactions.size(); i++) {
            messages.add(this.transactions.remove());
        }

        Block block = Block.with(runningBlockId++, messages, runningPrevBlockHash);
        runningPrevBlockHash = null;
        largestMessageIdTillPrevBlockLock.writeLock().unlock();
        return block;
    }

    public synchronized boolean submitBlock(Block block, Object caller) {
        if (!(caller instanceof Miner)) {
            throw new IllegalCallerException();
        }

        if (!areIdenticalBlocks(unprocessedBlocks[0], block)) { return false; }
        if (!block.getHash().startsWith(requiredPrefixForHash)) { return false; }
        if (!block.isConsistent()) { return false; }

        currentMiningBlockEndTimeMs = System.currentTimeMillis();

        block.setTimeTookForMiningMs(currentMiningBlockEndTimeMs - currentMiningBlockStartTimeMs);
        chain.add(block);

        updateMiningConstraints();
        runningPrevBlockHash = block.getHash();

        synchronized (unprocessedBlocks) {
            if (transactions.isEmpty()) {
                unprocessedBlocks[0] = null;
            } else {
                unprocessedBlocks[0] = createBlock();
                currentMiningBlockStartTimeMs = System.currentTimeMillis();
            }
        }

        creator.saveBlockchain();
        return true;
    }

    public Block getUnprocessedBlock() {
        try {
            return (Block) unprocessedBlocks[0].clone();
        } catch (Exception e) {
            return null;
        }
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

    public long getLength() { return chain.size(); }

    public Block getBlock(int index) { return chain.get(index); }

    public String getRequiredPrefixForHash() { return requiredPrefixForHash; }

    public static int getFixedMiningTimeMs() { return FIXED_MINING_TIME_MS; }

    public static int getAcceptableDeviationInMiningTimeMs() { return ACCEPTABLE_DEVIATION_IN_MINING_TIME_MS; }

    public long getTransactionId() {
        return transactionIdCounter.getAndIncrement();
    }

    private static boolean areIdenticalBlocks(Block b1, Block b2) {
        if (b1 == null || b2 == null) { return false; }
        if (b1 == b2) { return true; }

        if (!b1.getClass().equals(b2.getClass())) { return false; }
        if (b1.getId() != b2.getId()) { return false; }
        if (b1.getTimestamp() != b2.getTimestamp()) { return false; }
        if (!b1.getPrevBlockHash().equals(b2.getPrevBlockHash())) { return false; }
        if (!b1.getTransactionsToStringCached().equals(b2.getTransactionsToStringCached())) { return false; }

        return true;
    }

    private boolean validateMessage(Transaction transaction) {
        if (transaction.getId() < largestMessageIdTillPrevBlock) { return false; }
        if (!verifySignature(transaction.toString(), transaction.getSignature(), transaction.getPublicKey())) { return false; }
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
