package blockchain.miner;

import blockchain.*;

public class MinerFactory {
    private long runningMinerId;
    private Blockchain blockchain;

    public MinerFactory(Blockchain blockchain) {
        this.blockchain = blockchain;
        runningMinerId = 1;
    }

    public Miner newMiner() {
        return Miner.of(blockchain, runningMinerId++);
    }
}
