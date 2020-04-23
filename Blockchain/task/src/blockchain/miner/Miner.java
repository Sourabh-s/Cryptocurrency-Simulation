package blockchain.miner;

import java.util.Random;
import blockchain.*;
import blockchain.utils.*;

public class Miner implements Runnable {

    private Blockchain blockchain;
    private Block currentProcessingBlock;
    private Random random;
    private long id;
    private static final int BLINDREPITITIONS = 100;
    private static final int SLEEPWHENNOWORKMS = 1000;

    private Miner(Blockchain blockchain, long id) {
        this.blockchain = blockchain;
        this.id = id;
        currentProcessingBlock = null;
        random = null;
    }


    static Miner of(Blockchain blockchain, long id) {
        return new Miner(blockchain, id);
    }

    @Override
    public void run() {
        while (true) {
            updateCurrentProcessingBlock();
            if (currentProcessingBlock == null) {
                try {
                    Thread.sleep(SLEEPWHENNOWORKMS);
                } catch (InterruptedException ignored) {
                    return;
                }
                continue;
            }
            boolean successful = blindMining();
            if (successful) {
                currentProcessingBlock.setMinerId(id);
                blockchain.submitBlock(currentProcessingBlock, this);
            }
        }
    }

    private boolean blindMining() {
        String requiredPrefix = blockchain.getRequiredPrefixForHash();
        for (int i = 0; i < BLINDREPITITIONS; i++) {
            currentProcessingBlock.setMagicNum(random.nextInt());
            String computedHash = StringUtils.applySha256(currentProcessingBlock.toString());
            if (computedHash.startsWith(requiredPrefix)) {
                currentProcessingBlock.setHash(computedHash);
                return true;
            }
        }
        return false;
    }

    private void updateCurrentProcessingBlock() {
        Block block = blockchain.getUnprocessedBlock();
        if (currentProcessingBlock == null || !currentProcessingBlock.equals(block)) {
            currentProcessingBlock = block;
            random = new Random();
        }
    }
}