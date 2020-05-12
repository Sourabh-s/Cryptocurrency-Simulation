package blockchain.client;

import blockchain.Blockchain;

public class ClientFactory {
    private Blockchain blockchain;
    private long runningClientId;

    public ClientFactory(Blockchain blockchain) {
        this.blockchain = blockchain;
        runningClientId = 1;
    }

    public Client newClient() {
        return Client.of(runningClientId++, blockchain);
    }
}
