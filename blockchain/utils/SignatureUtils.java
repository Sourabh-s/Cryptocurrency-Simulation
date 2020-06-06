package blockchain.utils;

import java.security.*;

public class SignatureUtils {

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String generateSignature(String data, PrivateKey privateKey) {

    }

    public static boolean verifySignature(String data, String signature, PublicKey publicKey) {

    }

}
