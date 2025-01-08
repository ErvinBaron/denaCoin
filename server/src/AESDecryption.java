import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Base64;
import java.util.List;

class Main {
    public static void main(String[] args) {
        Blockchain blockchain = new Blockchain(5); // Difficulty of 4
        System.out.println("Mining block 1...");
        blockchain.addBlock(List.of("Transaction 1", "Transaction 2"));

        System.out.println("Mining block 2...");
        blockchain.addBlock(List.of("Transaction 3", "Transaction 4"));

//        blockchain.getChain().forEach(block -> {
//            System.out.println("Block " + block.getIndex() + ":");
//            System.out.println("Hash: " + block.getHash());
//            System.out.println("Previous Hash: " + block.getPreviousHash());
//        });
    }
}