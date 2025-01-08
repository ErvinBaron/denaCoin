import java.util.Date;
import java.util.List;

public class Block {
    private int index; // Position of the block in the chain
    private long timestamp; // Time of block creation
    private List<String> transactions; // List of transactions in the block
    private String previousHash; // Hash of the previous block
    private String hash; // Current block's hash
    private long nonce; // Counter used for mining

    // Constructor
    public Block(int index, List<String> transactions, String previousHash) {
        this.index = index;
        this.timestamp = new Date().getTime();
        this.transactions = transactions;
        this.previousHash = previousHash;
        this.hash = calculateHash();
    }

    // Calculate hash using block attributes, including nonce
    public String calculateHash() {
        String inputToHash = index + timestamp + transactions.toString() + previousHash + nonce;
        return StringUtil.applySHA256(inputToHash);
    }

    // Mine the block by finding a hash that satisfies the difficulty
    public void mineBlock(int difficulty) {
        // Create a target string with the required number of leading zeros
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block mined: " + hash);
    }

    // Getters
    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getHash() {
        return hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public List<String> getTransactions() {
        return transactions;
    }

    public long getNonce() {
        return nonce;
    }
}
