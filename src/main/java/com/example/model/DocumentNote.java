package com.example.model;

import java.time.LocalDateTime;

public class DocumentNote {

  private int id;
  private int userId;
  private String filePath;
  private String noteText;
  private LocalDateTime updatedAt;

  public DocumentNote(int id, int userId, String filePath, String noteText,
      LocalDateTime updatedAt) {
    this.id = id;
    this.userId = userId;
    this.filePath = filePath;
    this.noteText = noteText;
    this.updatedAt = updatedAt;
  }

  public int getId() {
    return id;
  }

  public int getUserId() {
    return userId;
  }

  public String getFilePath() {
    return filePath;
  }

  public String getNoteText() {
    return noteText;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
