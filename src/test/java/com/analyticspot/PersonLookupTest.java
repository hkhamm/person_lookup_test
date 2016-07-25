package com.analyticspot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.analyticspot.fakedbapi.DatabaseCallback;
import com.analyticspot.fakedbapi.KeyValueDatabase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Unit tests for {@link PersonLookup}.
 */
public class PersonLookupTest {
  private static final Logger log = LoggerFactory.getLogger(PersonLookupTest.class);

  // A lookup that returns valid JSON representing a person should succeed.
  @Test
  public void testSuccessfulLookup() throws Exception {
    // Set up a fake KeyValueDatabase that always returns the following string when queried.
    ValueReturningKeyValueDb db = new ValueReturningKeyValueDb(
        "{\"firstName\": \"Fred\", \"lastName\": \"Flintstone\", "
        + "\"birthDate\": \"2010-01-29\"}");
    PersonLookup lookup = new PersonLookup(db);

    CompletableFuture<Person> futurePerson = lookup.findById("foo");

    // Wait for the future to redeem
    Person person = futurePerson.get();

    assertThat(person.getFirstName()).isEqualTo("Fred");
    assertThat(person.getLastName()).isEqualTo("Flintstone");
    assertThat(person.getBirthDate()).isEqualTo(LocalDate.of(2010, 1, 29));
  }

  // A lookup in which the database layer throws an exception should result in that exception (or an exception that
  // wraps it) being re-thrown when a user attempts to get the value from a redeemed future.
  @Test
  public void testFailedLookup() throws Exception {
    // Set up a fake KeyValueDatabase that always throws an exception when queried.
    final String EXCEPTION_MESSAGE = "Nothing is wrong: test exception.";
    ExceptionThrowingKeyValueDb db = new ExceptionThrowingKeyValueDb(
        new RuntimeException(EXCEPTION_MESSAGE));
    PersonLookup lookup = new PersonLookup(db);

    final CompletableFuture<Person> futurePerson = lookup.findById("foo");

    // Wait for the future to redeem
    assertThatThrownBy(() -> futurePerson.get()).hasMessageContaining(EXCEPTION_MESSAGE);
  }

  // Tests that if the database returns invalid data the future will redeem with an exception.
  // NOTE: This the challenge. This test does not pass and hangs forever, your task is to figure out why!!
  @Test
  public void testInvalidDataResultsInException() {
    // Set up a fake KeyValueDatabase that always returns the following string when queried. Note that the JSON here
    // doesn't have the closing '}' and the lastName property is spelled wrong.
    ValueReturningKeyValueDb db = new ValueReturningKeyValueDb(
        "{\"firstName\": \"Fred\", \"lastNNName\": \"Flintstone\", "
        + "\"birthDate\": \"2010-01-29\"");
    PersonLookup lookup = new PersonLookup(db);

    CompletableFuture<Person> futurePerson = lookup.findById("foo");

    // Wait for the future to redeem. Since the JSON returned by the database is invalid we
    // expect the future to complete with an exception.
    assertThatThrownBy(() -> futurePerson.get()).isInstanceOf(Throwable.class);
  }

  // Fake KeyValueDatabase implementation that simply calls the onSuccess method of its callback with the data passed to
  // its constructor.
  private static class ValueReturningKeyValueDb implements KeyValueDatabase {
    private final String toReturn;

    private ValueReturningKeyValueDb(String toReturn) {
      this.toReturn = toReturn;
    }

    @Override
    public void get(String key, DatabaseCallback callback) {
      callback.onSuccess(toReturn);
    }
  }

  // Fake KeyValueDatabase implementation that simply calls the onFailure method of its callback with the Exception
  // passed to its constructor.
  private static class ExceptionThrowingKeyValueDb implements KeyValueDatabase {
    private final Throwable toThrow;

    private ExceptionThrowingKeyValueDb(Throwable toThrow) {
      this.toThrow = toThrow;
    }

    @Override
    public void get(String key, DatabaseCallback callback) {
      callback.onFailure(toThrow);
    }
  }
}
