package blockchain.miner;

import java.util.Random;
import blockchain.*;
import blockchain.utils.*;

public class Miner implements Runnable {

    private Blockchain blockchain;
    private Block currentMiningBlock;
    private Random random;
    private long id;
    private static final int BLIND_REPETITIONS = 100;
    private static final int SLEEP_WHEN_NO_WORK_MS = 1000;

    private Miner(Blockchain blockchain, long id) {
        this.blockchain = blockchain;
        this.id = id;
        currentMiningBlock = null;
        random = null;
    }


    static Miner of(Blockchain blockchain, long id) {
        return new Miner(blockchain, id);
    }

    @Override
    public void run() {
        while (true) {
            updateCurrentMiningBlock();
            if (currentMiningBlock == null) {
                try {
                    Thread.sleep(SLEEP_WHEN_NO_WORK_MS);
                } catch (InterruptedException ignored) {
                    return;
                }
                continue;
            }
            boolean successful = blindMining();
            if (successful) {
                currentMiningBlock.setMinerId(id);
                blockchain.submitBlock(currentMiningBlock, this);
            }
        }
    }

    private boolean blindMining() {
        String requiredPrefix = blockchain.getRequiredPrefixForHash();
        for (int i = 0; i < BLIND_REPETITIONS; i++) {
            currentMiningBlock.setMagicNum(random.nextInt());
            String computedHash = StringUtils.applySha256(currentMiningBlock.toString());
            if (computedHash.startsWith(requiredPrefix)) {
                currentMiningBlock.setHash(computedHash);
                return true;
            }
        }
        return false;
    }

    private void updateCurrentMiningBlock() {
        Block block = blockchain.getUnprocessedBlock();
        if (currentMiningBlock == null || !currentMiningBlock.equals(block)) {
            currentMiningBlock = block;
            random = new Random();
        }
    }
}
