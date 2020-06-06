package blockchain.client;

import blockchain.Blockchain;
import blockchain.Message;
import blockchain.utils.SignatureUtils;
import blockchain.utils.StringUtils;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Random;

public class Client implements Runnable {
    private String name;
    private Blockchain blockchain;
    private long id;
    private KeyPair keyPair = null;
    private static final int MAX_SLEEP_TIME = 5000;
    private static final int MIN_SLEEP_TIME = 1;

    private Client(long id, Blockchain blockchain) {
        this.blockchain = blockchain;
        this.id = id;
        name = names[(int) id];
        while (keyPair != null) {
            keyPair = SignatureUtils.generateKeyPair();
        }
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

            blockchain.addMessage(createMessage());
        }
    }

    private Message createMessage() {
        String messageData = StringUtils.randomAlphaString(new Random().nextInt(200));
        long messageId = blockchain.getMessageId();
        Message message = new Message(id, name, messageData, keyPair.getPublic());
        String signature = SignatureUtils.generateSignature(message.toString(), keyPair.getPrivate());
        message.setSignature(signature);
        return message;
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    private static String[] names = {
        "James", "Mary", "John", "Linda", "Robert", "Michael", "Sarah", "William",
        "David", "Richard", "Lisa", "Joseph", "Thomas", "Jessica", "Charles", "Nancy",
        "Christopher", "Jennifer"
    };
}
