package blockchain;

import blockchain.miner.MinerFactory;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        var driver = BlockchainDriver.newDriver();
        var blockchain = driver.getBlockchain();
        var minerFactory = new MinerFactory(blockchain);

        var executor = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            executor.submit(minerFactory.newMiner());
        }

        for (int i = 0; i < 5; i++) {
            blockchain.addBlock();
        }

        for (int i = 0; i < 5; i++) {
            while (blockchain.getLength() < i+1) {
                // Wait if the block is not mined yet
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {

                }
            }
            printBlock(blockchain.getBlock(i));
            System.out.print(i < 4 ? "\n" : "");
        }

        executor.shutdownNow();
    }

    private static void printBlock(Block block) {
        System.out.println("Block:");
        System.out.println("Created by miner # " + block.getMinerId());
        System.out.println("Id: " + block.getId());
        System.out.println("Timestamp: " + block.getTimestamp());
        System.out.println("Magic number: " + block.getMagicNum());
        System.out.println("Hash of the previous block: \n" + block.getPrevBlockHash());
        System.out.println("Hash of the block: \n" + block.getHash());
        System.out.printf("Block was generating for %d seconds\n", block.getTimeTookMs());
        System.out.println(printNValueStatus(block));
    }


    private static int nValue = 0;
    private static String printNValueStatus(Block block) {
        long timeTook = block.getTimeTookForMiningMs();
        int fixedMiningTime = Blockchain.getFixedMiningTimeMs();
        int acceptableDeviation = Blockchain.getAcceptableDeviationInMiningTimeMs();

        if (timeTook >= (fixedMiningTime - acceptableDeviation)
            && timeTook <= (fixedMiningTime + acceptableDeviation)) {
            return "N stays the same";
        }

        if (timeTook < (fixedMiningTime - acceptableDeviation)) {
            return "N was increased to " + ++nValue;
        }

        nValue--;
        return "N was decreased by 1";
    }
}