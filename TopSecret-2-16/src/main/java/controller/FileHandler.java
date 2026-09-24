package controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {

  private String content;

  public FileHandler() {
    this.content = "";
  }

  // String File Name -> String File Contents
  public String handle(String filename) {
    // Checks files only within data folder
    Path dataDir = Paths.get("data").toAbsolutePath();
    Path filePath = dataDir.resolve(filename).normalize().toAbsolutePath();

    // If filepath tries accessing parent directories, return invalid traversal
    if (!filePath.startsWith(dataDir)) {
      return "Path traversal attempt detected";
    }

    // Read the file and store into a string
    try {
      content = Files.readString(filePath);
    } catch (NoSuchFileException e) {
      content = "File Not Found.";
    } catch (IOException e) {
      content = "Insufficient File";
    }
    return content;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

}
