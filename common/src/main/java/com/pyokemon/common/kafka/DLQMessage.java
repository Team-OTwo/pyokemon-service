package com.pyokemon.common.kafka;

public class DLQMessage<T> {
  private String originalTopic;
  private String originalKey;
  private T originalMessage;
  private String errorMessage;
  private long timestamp;

  public static <T> DLQMessageBuilder<T> builder() {
    return new DLQMessageBuilder<>();
  }

  public static class DLQMessageBuilder<T> {
    private DLQMessage<T> message = new DLQMessage<>();

    public DLQMessageBuilder<T> originalTopic(String originalTopic) {
      message.originalTopic = originalTopic;
      return this;
    }

    public DLQMessageBuilder<T> originalKey(String originalKey) {
      message.originalKey = originalKey;
      return this;
    }

    public DLQMessageBuilder<T> originalMessage(T originalMessage) {
      message.originalMessage = originalMessage;
      return this;
    }

    public DLQMessageBuilder<T> errorMessage(String errorMessage) {
      message.errorMessage = errorMessage;
      return this;
    }

    public DLQMessageBuilder<T> timestamp(long timestamp) {
      message.timestamp = timestamp;
      return this;
    }

    public DLQMessage<T> build() {
      return message;
    }
  }

  public String getOriginalTopic() {
    return originalTopic;
  }

  public String getOriginalKey() {
    return originalKey;
  }

  public T getOriginalMessage() {
    return originalMessage;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
