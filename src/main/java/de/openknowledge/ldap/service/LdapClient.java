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

import javax.annotation.PostConstruct;
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
public class LdapClient {

  private static final Logger LOG = LoggerFactory.getLogger(LdapClient.class);

  @Inject
  @ConfigProperty(name="ldap.host", defaultValue = "localhost")
  private String LDAP_HOST;

  @Inject
  @ConfigProperty(name="ldap.port", defaultValue = "389")
  private String LDAP_PORT;

  @Inject
  @ConfigProperty(name="ldap.search.base", defaultValue = "ou=people,dc=openknowledge,dc=de")
  private String LDAP_SEARCH_BASE;

  @Inject
  @ConfigProperty(name="ldap.bind.dn", defaultValue = "cn=admin,dc=openknowledge,dc=de")
  private String LDAP_USERNAME;

  @Inject
  @ConfigProperty(name="ldap.bind.password", defaultValue = "admin")
  private String LDAP_PASSWORD;

  @PostConstruct
  public void init(){
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, LDAP_USERNAME);
    env.put(Context.SECURITY_CREDENTIALS, LDAP_PASSWORD);
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, String.format("ldap://%s:%s", LDAP_HOST, LDAP_PORT));
    env.put("java.naming.ldap.attributes.binary", "objectSID");
  }

  private Hashtable<String, Object> env = new Hashtable<>();

  public SearchResult findUserById(final String uid) throws NamingException {
    InitialDirContext ctx = new InitialDirContext(env);

    String searchFilter = "(&(objectClass=person)(uid=" + uid + "))";

    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);

    NamingEnumeration<SearchResult> results = ctx.search(LDAP_SEARCH_BASE, searchFilter, searchControls);

    SearchResult searchResult = null;
    if (results.hasMoreElements()) {
      searchResult = results.nextElement();

      if (results.hasMoreElements()) {
        LOG.warn("Matched multiple users for the uid: {}", uid);
        return null;
      }
    }

    return searchResult;
  }
}
