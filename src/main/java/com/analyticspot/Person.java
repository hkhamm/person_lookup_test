package com.analyticspot;

import com.analyticspot.fakedbapi.KeyValueDatabase;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * When we call {@link KeyValueDatabase#get} we expect to get a JSON string representing one of these objects. This
 * class has Jackson annotations so that Jackson can be used to deserialize the data we get from the KeyValueDatabase.
 */
public class Person {
  private final String firstName;
  private final String lastName;
  private LocalDate birthDate;

  /**
   * Constructor.
   *
   * @param firstName the person's first name.
   * @param lastName the person's last name.
   * @param birthDateStr The person's birtday as a String formatted as YYYY-MM-DD
     */
  @JsonCreator
  public Person(@JsonProperty("firstName") String firstName,
                @JsonProperty("lastName") String lastName,
                @JsonProperty("birthDate") String birthDateStr) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.birthDate = LocalDate.parse(birthDateStr);
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }
}
