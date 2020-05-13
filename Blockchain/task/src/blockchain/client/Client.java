package blockchain.client;

import blockchain.Blockchain;
import java.util.Random;

public class Client implements Runnable {
    private String name;
    private Blockchain blockchain;
    private long id;
    private static final int MAX_SLEEP_TIME = 50000;
    private static final int MIN_SLEEP_TIME = 1;

    private Client(long id, Blockchain blockchain) {
        this.blockchain = blockchain;
        this.id = id;
        name = names[(int) id];
    }

    public static Client with(long id, Blockchain blockchain) {
        return new Client(id, blockchain);
    }

    @Override
    public void run() {
        Random random = new Random();

        while (true) {
            try {
                int sleepTime = random.nextInt(MAX_SLEEP_TIME);
                while(sleepTime < MIN_SLEEP_TIME) {
                    sleepTime = random.nextInt(MAX_SLEEP_TIME);
                }
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                return;
            }
            //TODO: send a new random message to Blockchain. StringUtils::randomAlphaString can be helpful
        }
    }

    private static String[] names = {
        "James", "Mary", "John", "Linda", "Robert", "Michael", "Sarah", "William",
        "David", "Richard", "Lisa", "Joseph", "Thomas", "Jessica", "Charles", "Nancy",
        "Christopher", "Jennifer"
    };
}
