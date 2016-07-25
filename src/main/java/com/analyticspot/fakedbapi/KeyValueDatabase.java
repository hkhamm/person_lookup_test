package com.analyticspot.fakedbapi;

/**
 * Interface that mimics a very simple key/value store database with keys and values both being String. The interface is
 * asynchronous but used callbacks, not CompletableFuture as is common for many libraries.
 */
public interface KeyValueDatabase {
  /**
   * Looks up {@code key} in the database. When the lookup is complete the provided callback's onSuccess method is
   * called with the data. If no data was found for {@code key} then the callback is called with a null value. If an
   * error was encountered (e.g. unable to communicate with the database due to network issues) then the callback's
   * onFailure method is called.
   */
  void get(String key, DatabaseCallback callback);
}
