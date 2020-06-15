package blockchain.user;

import blockchain.Blockchain;

public class UserFactory {
    private Blockchain blockchain;
    private long runningUserId;
    private ArrayList<User> users = new ArrayList<User>(); 

    public UserFactory(Blockchain blockchain) {
        this.blockchain = blockchain;
        runningUserId = 1;
    }

    public User newUser() {
    	return users[runningUserId] = User.with(runningUserId++, blockchain);
    }

    public Miner newMiner() {
    	return users[runningUserId] = Miner.with(runningUserId++, blockchain);
    }

    public static User getUser(long id) {
    	return users.get(id);
    }

    public static long getNoOfUsers() {
    	return users.size();
    }
}
