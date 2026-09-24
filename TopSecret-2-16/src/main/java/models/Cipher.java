package models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cipher {
    Map<Character, Character> decryptionMap =  new HashMap<>();

    public Cipher(String keyPath) throws IOException {
        loadKey(keyPath);
    }

    public void loadKey(String keyPath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(keyPath));

        if (lines.size() < 2) {
            throw new IllegalArgumentException("File must contain only two lines");
        }

        String plainText = lines.get(0);
        String cipherText = lines.get(1);

        if (plainText.length() != cipherText.length()) {
            throw new IllegalArgumentException("Line lengths do not match");
        }

        //mapping key-value in respect to cipherText and plainText
        for (int i = 0; i < cipherText.length(); i++) {
            decryptionMap.put(cipherText.charAt(i), plainText.charAt(i));
        }
    }
    public String decrypt(String input){
        if (input == null){
            return null;
        }

        char[] inputArray = input.toCharArray();
        char[] outputArray = new char[inputArray.length];

        for (int i = 0; i < inputArray.length; i++) {
            char cur =  inputArray[i];

            //checking if current character is in the Map
            if(decryptionMap.containsKey(cur)){
                //fetching decrypted value given cur and add to output array
                outputArray[i] = decryptionMap.get(cur);
            }else{
                //keep cur if not found, in the case of a space
                outputArray[i] = cur;
            }
        }

        return new String(outputArray);
    }

}
