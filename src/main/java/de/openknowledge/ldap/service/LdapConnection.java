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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Hashtable;

@ApplicationScoped
public class LdapConnection {

  private static final Logger LOG = LoggerFactory.getLogger(LdapConnection.class);

  @Inject
  @ConfigProperty(name="LDAP_AD_SERVER", defaultValue = "ldap://localhost:389")
  private String LDAP_AD_SERVER;

  @Inject
  @ConfigProperty(name="LDAP_SEARCH_BASE", defaultValue = "ou=people,dc=openknowledge,dc=de")
  private String LDAP_SEARCH_BASE;

  @Inject
  @ConfigProperty(name="LDAP_USERNAME", defaultValue = "cn=admin,dc=openknowledge,dc=de")
  private String LDAP_USERNAME;

  @Inject
  @ConfigProperty(name="LDAP_PASSWORD", defaultValue = "admin")
  private String LDAP_PASSWORD;

  private Hashtable<String, Object> env = new Hashtable<>();

  public SearchResult findUserById(final String uid) throws NamingException {
    initEnv();

    InitialDirContext ctx = new InitialDirContext(env);

    String searchFilter = "(&(objectClass=person)(uid=" + uid + "))";

    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    NamingEnumeration<SearchResult> results = ctx.search(LDAP_SEARCH_BASE, searchFilter, searchControls);

    SearchResult searchResult = null;
    if (results.hasMoreElements()) {
      searchResult = (SearchResult)results.nextElement();

      //make sure there is not another item available, there should be only 1 match
      if (results.hasMoreElements()) {
        LOG.error("Matched multiple users for the uid: {}", uid);
        return null;
      }
    }

    return searchResult;
  }

  private void initEnv() {
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, LDAP_USERNAME);
    env.put(Context.SECURITY_CREDENTIALS, LDAP_PASSWORD);
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, LDAP_AD_SERVER);
    env.put("java.naming.ldap.attributes.binary", "objectSID");
  }
}
