package com.analyticspot;

import com.analyticspot.fakedbapi.DatabaseCallback;
import com.analyticspot.fakedbapi.KeyValueDatabase;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * The API we use for looking up people in our key/value store. Unlike the API for the key/value store itself, this
 * uses CompletableFuture for asynchronous operations rather than requiring the user to pass a callback. This also
 * returns a {@link Person} instance while KeyValueDatabase returns a String, so this is responsible for deserializing
 * the String into a Person.
 */
public class PersonLookup {
  private static final Logger log = LoggerFactory.getLogger(PersonLookup.class);
  private static final ObjectMapper jsonMapper = new ObjectMapper();

  private final KeyValueDatabase db;

  public PersonLookup(KeyValueDatabase db) {
    this.db = db;
  }

  /**
   * Looks up the person with the given {@code personId} in the database and returns a CompleteableFuture that will
   * redeem with the corresponding {@link Person} if found. Redeems with a null value if the person is not found and
   * redeems with an exception if anything went wrong with the lookup, deserialization, etc.
   */
  CompletableFuture<Person> findById(String personId) {
    CompletableFuture<Person> result = new CompletableFuture<Person>();
    OnLookupComplete callback = new OnLookupComplete(result, personId);
    db.get(personId, callback);
    return result;
  }

  // Used as a callback for KeyValueDatabase#get. Takes the CompleteableFuture that was returned to the user as a
  // parameter and completes the future when the data is available.
  private static class OnLookupComplete implements DatabaseCallback {
    private final CompletableFuture<Person> finalResult;
    private final String personId;

    private OnLookupComplete(CompletableFuture<Person> finalResult, String personId) {
      this.finalResult = finalResult;
      this.personId = personId;
    }

    @Override
    public void onSuccess(String data) {
      log.info("Database returned {} for id {}", data, personId);
      try {
        Person person = jsonMapper.readValue(data, Person.class);
        finalResult.complete(person);
      } catch (IOException e) {
        log.error("Error converting data from database for id {} to Person class:", personId, e);
        finalResult.completeExceptionally(e);
      }
    }

    @Override
    public void onFailure(Throwable error) {
      log.error("Database threw an error on lookup of {}:", personId, error);
      finalResult.completeExceptionally(error);
    }
  }
}
