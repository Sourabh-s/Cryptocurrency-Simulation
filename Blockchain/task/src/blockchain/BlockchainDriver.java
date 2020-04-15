package blockchain;

public class BlockchainDriver {

    public Blockchain getBlockchain(int noOfStartZerosForHash){
        String fileName = "cache";
        Blockchain blockchain = null;

        try {
            blockchain = (Blockchain) SerializationUtils.deserialize(fileName);
        } catch(Exception ignored) {

        } finally {
            if(
                       blockchain == null
                    || blockchain.getNoOfStartZerosForHash() != noOfStartZerosForHash
                    ||!blockchain.isValid()
            ) {
                blockchain = Blockchain.generateBlockchain(fileName, noOfStartZerosForHash, this);
            }
        }

        return blockchain;
    }
}