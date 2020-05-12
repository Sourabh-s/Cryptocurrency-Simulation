package blockchain.miner;

import blockchain.Blockchain;

public class MinerFactory {
    private Blockchain blockchain;
    private long runningMinerId;

    public MinerFactory(Blockchain blockchain) {
        this.blockchain = blockchain;
        runningMinerId = 1;
    }

    public Miner newMiner() {
        return Miner.of(blockchain, runningMinerId++);
    }
}
