package blockchain.client;

import blockchain.Blockchain;
import java.util.Random;

public class Client implements Runnable {
    private String name;
    private Blockchain blockchain;
    private long id;

    private Client(long id, Blockchain blockchain) {
        this.blockchain = blockchain;
        this.id = id;
        name = names[(int) id];
    }

    public static Client of(long id, Blockchain blockchain) {
        return new Client(id, blockchain);
    }

    @Override
    public void run() {
        Random random = new Random();

        while(true) {
            try {
                Thread.sleep(random.nextInt(5000));
            } catch (InterruptedException e) {
                return;
            }
            //TODO: send a message to blockchain with client name
        }
    }

    private static String[] names = {
        "James", "Mary", "John", "Linda", "Robert", "Michael", "Sarah", "William",
        "David", "Richard", "Lisa", "Joseph", "Thomas", "Jessica", "Charles", "Nancy",
        "Christopher", "Jennifer"
    };
}
