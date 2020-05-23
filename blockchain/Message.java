package blockchain;

public class Message {
    private String author;
    private String message;
    private long creationTime;

    public Message(String author, String message) {
        this.author = author;
        this.message = message;
        creationTime = System.currentTimeMillis();
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "author='" + author + '\'' +
                ", message='" + message + '\'' +
                ", creationTime=" + creationTime +
                '}';
    }

    @Override
    public int hashCode() {
        return author.hashCode() + message.hashCode() + (int) creationTime;
    }
}
