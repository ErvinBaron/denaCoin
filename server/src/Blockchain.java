import java.util.ArrayList;
import java.util.List;

// The Blockchain class is a container for multiple blocks.
// It ensures that each new block references the previous one
// to maintain the chain's integrity.
public class Blockchain {
    private List<Block> chain;
    private int difficulty; // Mining difficulty (affects transaction speed)

    // Constructor to create the blockchain starting from the genesis block
    public Blockchain(int difficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = difficulty; // Set difficulty level
        chain.add(createGenesisBlock());
    }

    // Genesis block of the chain
    private Block createGenesisBlock() {
        List<String> genesisTransactions = new ArrayList<>();
        genesisTransactions.add("Genesis Block");
        Block genesisBlock = new Block(0, genesisTransactions, "0");
        genesisBlock.mineBlock(difficulty); // Mine the genesis block
        return genesisBlock;
    }

    // Get the latest block in the chain
    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    // Add a new block to the chain
    public void addBlock(List<String> transactions) {
        Block latestBlock = getLatestBlock();
        Block newBlock = new Block(chain.size(), transactions, latestBlock.getHash());
        newBlock.mineBlock(difficulty); // Mine the block before adding it
        chain.add(newBlock);
    }

    // Getter for the blockchain
    public List<Block> getChain() {
        return chain;
    }

    // Getter for difficulty
    public int getDifficulty() {
        return difficulty;
    }
}
