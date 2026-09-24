package controllerTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import controller.FileHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileHandlerTest {

  private FileHandler fileHandler;

  @BeforeEach
  public void setUp() {
    fileHandler = new FileHandler();
  }

  // Case 1: Request for a file that works and is found within data
  @Test
  public void testHandle_ValidFile() {
    String filename = "test_data.txt"; 
    
    try {
        java.nio.file.Path dataDir = java.nio.file.Paths.get("data");
        if (!java.nio.file.Files.exists(dataDir)) {
            java.nio.file.Files.createDirectory(dataDir);
        }
        java.nio.file.Path testFile = dataDir.resolve("valid_test.txt");
        java.nio.file.Files.writeString(testFile, "Hello World");
        
        String content = fileHandler.handle("valid_test.txt");
        assertEquals("Hello World", content);
        
        java.nio.file.Files.deleteIfExists(testFile);
    } catch (java.io.IOException e) {
        e.printStackTrace();
    }
  }

  // Case 2: Request for a file that exists within project but is not within data
  @Test
  public void testHandle_FileInProjectNotInData() {

    String content = fileHandler.handle("../build.gradle");

    assertTrue(content.contains("Path traversal attempt detected") || content.contains("Invalid"));
  }

  // Case 3: Request for a file that does not exist
  @Test
  public void testHandle_FileDoesNotExist() {
    String content = fileHandler.handle("non_existent_file.txt");
    assertEquals("File Not Found.", content); // Matching instructions
  }

  // Case 4: Request for a file that doesn't contain text (Audio File, PNG, ... etc.)
  @Test
  public void testHandle_NonTextFile() {
      try {
        java.nio.file.Path dataDir = java.nio.file.Paths.get("data");
        if (!java.nio.file.Files.exists(dataDir)) {
            java.nio.file.Files.createDirectory(dataDir);
        }
        java.nio.file.Path binaryFile = dataDir.resolve("image.png");
        byte[] bytes = new byte[] { (byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47 }; // PNG header
        java.nio.file.Files.write(binaryFile, bytes);
        
        String content = fileHandler.handle("image.png");
        
        assertEquals("Insufficient File", content);
        
        java.nio.file.Files.deleteIfExists(binaryFile);
    } catch (java.io.IOException e) {
        e.printStackTrace();
    }
  }
  @Test
  public void testGetContent() {
      assertEquals("", fileHandler.getContent());
  }

  @Test
  public void testSetContent() {
      fileHandler.setContent("test content");
      assertEquals("test content", fileHandler.getContent());
  }
}
