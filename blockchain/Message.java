package blockchain;

import java.security.PublicKey;

public class Message {
    private long id;
    private String author;
    private String message;
    private long creationTime;
    private String signature;
    private PublicKey publicKey;

    public Message(long id, String author, String message, PublicKey publicKey) {
        this.id = id;
        this.author = author;
        this.message = message;
        this.publicKey = publicKey;
        this.creationTime = System.currentTimeMillis();
    }

    public long getId() { return id; }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", author='" + author + '\'' +
                ", message='" + message + '\'' +
                ", creationTime=" + creationTime +
                '}';
    }

    @Override
    public int hashCode() {
        return author.hashCode() + message.hashCode() + (int) creationTime;
    }
}
