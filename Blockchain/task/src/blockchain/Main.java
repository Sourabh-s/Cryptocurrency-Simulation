package blockchain;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter how many zeros the hash must starts with: ");
        int noOfStartZerosForHash = sc.nextInt();
        sc.close();

        BlockchainDriver driver = new BlockchainDriver();
        Blockchain blockchain = driver.getBlockchain(noOfStartZerosForHash);

        while(blockchain.getLength() < 5) {
            blockchain.addBlock();
        }

        for(int i=0; i<5; i++) {
            printBlock(blockchain.getBlock(i));
            System.out.print(i < 4 ? "\n" : "");
        }
    }

    private static void printBlock(Block block) {
        System.out.println("Block:");
        System.out.println("Id: " + block.getId());
        System.out.println("Timestamp: " + block.getTimestamp());
        System.out.println("Magic number: " + block.getMagicNum());
        System.out.println("Hash of the previous block: \n" + block.getPrevBlockHash());
        System.out.println("Hash of the block: \n" + block.getHash());
        System.out.printf("Block was generating for %d seconds\n", block.getTimeTook());
    }
}