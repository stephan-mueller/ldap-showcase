# ldap-showcase

## Build the service

Run the following command to build the service

```shell
mvn clean package
```

## Run the service

Run one of the following commands to run the service

```shell
mvn liberty:run
```

or

```shell
docker run -p 8080:8080 ldap-showcase/ldap-service:0
```
