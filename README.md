# LDAP Showcase

The showcase provides a demo application based on the [Open Liberty](https://openliberty.io) microservice framework.
It demonstrates features of the Java Naming and Directory Interface and the
[MicroProfile Config](https://microprofile.io/project/eclipse/microprofile-config) specification.

**Notable features:**
* Definition and usage of environment variables
* Connecting to a LDAP server

## How to run

Before running the application it needs to be compiled and packaged using `Maven`. It creates the WAR and Docker image and can be
run via `docker compose`:

```shell script
$ mvn clean package
$ docker compose up
```

Wait for a message log similar to this:

> [5/19/21, 10:12:44:764 UTC] 0000002b id=         com.ibm.ws.kernel.feature.internal.FeatureManager            A CWWKF0011I: The defaultServer server is ready to run a smarter planet. The defaultServer server started in 3.616 seconds.

and

> 60cae86a slapd starting

### Resolving issues

Sometimes it may happen that the containers did not stop as expected when trying to stop the pipeline early. This may
result in running containers although they should have been stopped and removed. To detect them you need to check
Docker:

```shell script
$ docker ps -a | grep ldap-showcase_service_1
```

If there are containers remaining although the application has been stopped you can remove them:

```shell script
$ docker compose rm -fv
```

## Features

### Application

The application is a simple REST API. It supports GET requests for retrieve a person from the LDAP server. The response is encoded using JSON.

Try the application with `curl`:
```shell script
$ curl -X GET http://localhost:8080/ldap-service/api/person/1
{"email":"max.weis@openknowledge.de","givenName":"Max","phone":"123456789","surName":"Weis","uid":"1"}
```

The application reads all the required variables from the `pom.xml` via MicroProfile Config and stores the values inside a `Hashtable`

_LdapClient.java_ - Attributes which are injected via MicroProfile Config
```java
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

private Hashtable<String, Object> env = new Hashtable<>();

@PostConstruct
public void init(){
  env.put(Context.SECURITY_AUTHENTICATION, "simple");
  env.put(Context.SECURITY_PRINCIPAL, LDAP_USERNAME);
  env.put(Context.SECURITY_CREDENTIALS, LDAP_PASSWORD);
  env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
  env.put(Context.PROVIDER_URL, String.format("ldap://%s:%s", LDAP_HOST, LDAP_PORT));
  env.put("java.naming.ldap.attributes.binary", "objectSID");
}
```

The `InitialDirContext` constructor accepts the Hashtable with the environment variables to create a LDAP connection.  
This method executes a search query against the LDAP server, which returns a person from the Active Directory.

_LdapClient.java_ - The method which returns a person from the Active Directory
```java
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
```

### OpenLDAP Server

_OpenLDAP_ - the docker compose service
```yaml
services:
  ldap:
    image: osixia/openldap:1.5.0
    ports:
      - 389:389
      - 636:636
    volumes:
      - ./config.ldif:/container/service/slapd/assets/config/bootstrap/ldif/custom/50-bootstrap.ldif
    environment:
      LDAP_ORGANISATION: Openknowledge
      LDAP_DOMAIN: openknowledge.de
      LDAP_BASE_DN: dc=openknowledge,dc=de
    command: --copy-service
```

The root DN can be created via environment variables inside the `docker compose` file

Further settings can by configured via a `.ldif` file. The config files has to be mounted to the `/container/service/slapd/assets/config/bootstrap/ldif/custom/` directory to be executed during startup. 
```ldif
dn: ou=people,dc=openknowledge,dc=de
ou: people
objectClass: top
objectClass: organizationalUnit

dn: cn=Max,ou=people,dc=openknowledge,dc=de
mail: max.weis@openknowledge.de
uid: 1
sn: Weis
cn: Max
homePhone: 123456789
objectClass: top
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson

...
```

To run the `.ldif` script during startup, this command has to be executed in the `docker-compose`:
```yaml
command: --copy-service
```
