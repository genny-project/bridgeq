# Genny Bridgeq

This service acts as a bridge between the Genny Backend and the Genny Frontends (Alyson, Tommy)

This system uses:

- OpenID Connect Authorization Code Flow to authenticate users
- Kafka to send and receive backend messages
- Infinispan to save and fetch cached entities

## Requirements

To compile and run this demo you will need:

- JDK 11+
- GraalVM
- Keycloak

### Configuring GraalVM and JDK 11+

Make sure that both the `GRAALVM_HOME` and `JAVA_HOME` environment variables have
been set, and that a JDK 11+ `java` command is on the path.

See the [Building a Native Executable guide](https://quarkus.io/guides/building-native-image)
for help setting up your environment.

## Building the application

Launch the Maven build on the checked out sources of this demo:

> ./mvnw package

## Starting and Configuring the Keycloak Server

To start a Keycloak Server you can use Docker and just run the following command:

```bash
docker run --name keycloak -e DB_VENDOR=H2 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -p 8180:8080 quay.io/keycloak/keycloak:15.0.2
```

You should be able to access your Keycloak Server at [localhost:8180/auth](http://localhost:8180/auth).

Log in as the `admin` user to access the Keycloak Administration Console.
Username should be `admin` and password `admin`.

Import the [realm configuration file](config/quarkus-realm.json) to create a new realm.
For more details, see the Keycloak documentation about how to [create a new realm](https://www.keycloak.org/docs/latest/server_admin/index.html#_create-realm).

### Live coding with Quarkus

The Maven Quarkus plugin provides a development mode that supports
live coding. To try this out:

> ./mvnw quarkus:dev

This command will leave Quarkus running in the foreground listening on port 8080.

1. Visit the default endpoint: [http://127.0.0.1:8080](http://127.0.0.1:8080).
    - You should be redirected to the login page at Keycloak

2. Authenticate as user `alice`
    - Username: `alice`
    - Password: `alice`

3. If the credentials you provided are valid and you were successfully authenticated, you should be redirected back to the application

4. You should be able to access now the `index.html` resource.

5. Visit the `/tokens` endpoint: [http://127.0.0.1:8080/tokens](http://127.0.0.1:8080/tokens).
    - You should have access to a HTML page that shows information based on the ID Token, Access Token and Refresh Token issued
    to the application. Where these tokens are available for injection as you can see in the `TokenResource` JAX-RS Resource.

_NOTE:_ Running the tests with, for instance, `mvn package` requires the Keycloak server to be down as it will launch its own one. However, when running the application, make sure it is up with the realm properly configured.

### Run Quarkus in JVM mode

When you're done iterating in developer mode, you can run the application as a
conventional jar file. First compile it:

> ./mvnw package

Then run it:

> java -jar ./target/quarkus-app/quarkus-run.jar

Have a look at how fast it boots, or measure the total native memory consumption.

### Run Quarkus as a native executable

You can also create a native executable from this application without making any
source code changes. A native executable removes the dependency on the JVM:
everything needed to run the application on the target platform is included in 
the executable, allowing the application to run with minimal resource overhead.

Compiling a native executable takes a bit longer, as GraalVM performs additional
steps to remove unnecessary codepaths. Use the  `native` profile to compile a
native executable:

> ./mvnw package -Dnative

After getting a cup of coffee, you'll be able to run this executable directly:

> ./target/bridge-runner
