/*
 * Copyright (C) open knowledge GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions
 *  and limitations under the License.
 */
package de.openknowledge.ldap.service;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;

public class Person {

  private String uid;

  private String givenName;

  private String surName;

  private String email;

  private String phone;

  public Person() {
  }

  public Person(final SearchResult searchResult) throws Exception {
    Attributes attributes = searchResult.getAttributes();

    this.uid = getValue(attributes, "uid");
    this.givenName = getValue(attributes, "cn");
    this.surName = getValue(attributes, "sn");
    this.email = getValue(attributes, "mail");
    this.phone = getValue(attributes, "homephone");
  }

  private String getValue(final Attributes attributes, final String key) throws NamingException {
    return (String)attributes.get(key)
        .get();
  }

  public String getUid() {
    return uid;
  }

  public void setUid(final String uid) {
    this.uid = uid;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(final String givenName) {
    this.givenName = givenName;
  }

  public String getSurName() {
    return surName;
  }

  public void setSurName(final String surName) {
    this.surName = surName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(final String phone) {
    this.phone = phone;
  }

  @Override
  public String toString() {
    return "Person{" + "uid='" + uid + '\'' + ", givenName='" + givenName + '\'' + ", surName='" + surName + '\'' + ", email='" + email
        + '\'' + ", phone='" + phone + '\'' + '}';
  }
}
