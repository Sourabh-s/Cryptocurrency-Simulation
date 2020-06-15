package blockchain.user;

import java.util.Random;
import blockchain.*;
import blockchain.utils.*;

public class Miner extends User implements Runnable {

    private Block currentMiningBlock;
    private static final int BLIND_REPETITIONS = 100;
    private static final int SLEEP_WHEN_NO_WORK_MS = 1000;

    private Miner(long id, Blockchain blockchain) {
        super(id, blockchain);
        currentMiningBlock = null;
        random = null;
    }

    static Miner with(long id, Blockchain blockchain) {
        return new Miner(id, blockchain);
    }

    @Override
    public void run() {
        while (true) {
            random = new Random();
            int randNum = random.nextInt();
            if (randNum % id == 0) {
                doTransaction();
            }
            else {
                doMinning();
            }
        } 
    }

    private void doMinning() {
        boolean successful = false;
        
        while (!successful) {
            updateCurrentMiningBlock();
            if (currentMiningBlock == null) {
                try {
                    Thread.sleep(SLEEP_WHEN_NO_WORK_MS);
                } catch (InterruptedException ignored) {
                    return;
                }
                continue;
            }
            successful = blindMining();
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