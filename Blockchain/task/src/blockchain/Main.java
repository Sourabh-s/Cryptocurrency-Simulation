package blockchain;

public class Main {
    public static void main(String[] args) {
        Blockchain blockChain = new Blockchain();

        for (int i = 0; i < 5; i++) {
            Block block = blockChain.createBlock();
            printBlock(block);
            System.out.print((i < 4) ? "\n" : "");
        }
    }

    private static void printBlock(Block block) {
        System.out.println("Block:");
        System.out.println("Id: " + block.getId());
        System.out.println("Timestamp: " + block.getTimestamp());
        System.out.println("Hash of the previous block: \n" + block.getPrevBlockHash());
        System.out.println("Hash of the block: \n" + block.getHash());
    }
}