package com.analyticspot.fakedbapi;

/**
 * Interface for callbacks from our fictional KeyValueDatabase API. See {@link KeyValueDatabase} for details.
 */
public interface DatabaseCallback {
  /**
   * When a database lookup is complete this method gets called with the data that was found.
   */
  void onSuccess(String data);

  /**
   * When a database lookup fails this method gets called with a Throwable which indicates what the issue was.
   */
  void onFailure(Throwable error);
}
