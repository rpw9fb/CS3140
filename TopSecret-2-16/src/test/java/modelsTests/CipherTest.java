package modelsTests;

import models.Cipher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


import java.io.IOException;
import java.nio.file.NoSuchFileException;


public class CipherTest {
    @Test
    public void testIncorrectFilePath() throws IOException{
        Assertions.assertThrows(NoSuchFileException.class, () -> {
            String keyPath = "ciphers/key";
            Cipher cipher = new Cipher(keyPath);
        });
    }

    @Test
    public void testDecipher() throws IOException {

        String keyPath = "ciphers/key.txt";


        Cipher cipher = new Cipher(keyPath);

        // Test lowercase shift
        assertEquals("abc", cipher.decrypt("bcd"));
        // Test uppercase (unchanged)
        assertEquals("WXY", cipher.decrypt("XYZ"));
        // Test numbers
        assertEquals("Z129", cipher.decrypt("1230"));
        // Test mixed
        assertEquals("Hello 123", cipher.decrypt("Ifmmp 234"));
    }
}
