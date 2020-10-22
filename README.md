# Cabinet Client
This module provides a client for the webservices of the Cabinet Application.

# Configuration
The module provides a number of beans that are configured by the `CabinetClientConfiguration` class.
This class, in turn, uses the `CabinetClientProperties` class to pickup external configuration using
standard Spring approaches. The class is configured to detect properties prefixed with `cabinet.client`.
Default values are set which can be expected to work with a standard `dev` profile Cabinet client running
on `localhost` connecting as the `admin` user.

## Configure for a remote or production application
Generally you will only need to change two properties (through environment variable or config files). These are:
`cabinet.client.base` which defaults to `http://localhost:8080/api/`. To connect to the production Cabinet system you
should set this to `https://cabinet.apps.kaleidobio.com/api/`. You will also need to provide a correct value for
`cabinet.client.user` (default is `admin`) and `cabinet.client.password` which should be the password for the user.

# Using the module in another application or module
The Cabinet client can be used in another Spring application (or module) if it is added as a dependency and the appropriate
package scans or imports take place.
## Dependencies
In the `pom.xml` include this dependency (adjusting the version as needed):
```xml
        <dependency>
            <groupId>com.kaleido</groupId>
            <artifactId>cabinet-client</artifactId>
            <version>1.1.3-RELEASE</version>
        </dependency>
```
### Injecting Beans
The simplest way to inject the beans of the module is to use the Spring `@Import(com.kaleido.cabinetclient.CabinetClientConfiguration.class)` 
annotation to bring in the `CabinetClientConfiguration` which provides all of the required `@Beans` and configuration needed to use the
module. This will allow `@Beans` from the module to be `@Autoinject`ed or using Springboot they can be automatically
injected via the constructor.

## Example application
Providing the module dependency is declared in the POM the following shows a simple Springboot application which uses
the `CabinetClient<Batch>` class to obtain a list of batches. 

```java
@SpringBootApplication
@Import(com.kaleido.cabinetclient.CabinetClientConfiguration.class)
public class KclientDemoApplication {

    Logger log = LoggerFactory.getLogger(KclientDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(KclientDemoApplication.class, args);
    }

    @Component
    class Runner implements CommandLineRunner{

        CabinetClient<Batch> batchClient;

        Runner(CabinetClient<Batch> batchClient){
            this.batchClient = batchClient;
        }


        @Override
        public void run(String... args) throws Exception {
            final ResponseEntity<List<Batch>> batches = batchClient.findAll();
            log.info("Here are the batches: {}", batches);
        }
    }
}
```

# Authentication
The Cabinet app and it's REST services are protected with username and password authentication and the identity of
the client must be provided with each service call using a JWT bearer token in the header. For convenience, the module
provides a `CabinetJWTRequestInterceptor` which implements `ClientHttpRequestInterceptor`. This interceptor will inspect
calls and if those calls are to `cabinet.client.base` it will inject the appropriate header. If no token is available
the interceptor will use the properties `cabinet.client.user` and `cabinet.client.password` to authenticate to the `/authenticate`
enpoint of `cabinet.client.base` to obtain a JWT token that is then used for subsequent calls. 
As long as these properties are correctly set (or the defaults are correct) no further action should be required by
the application using this module.

# Domain objects
The `com.kaleido.cabinetclient.domain` package holds a number of Java beans that represent the entity objects of Cabinet.
These are used by Jackson to marshal the JSON returned by Cabinet. If the Cabinet domain changes then matching changes
will need to be made in this package.

## New domain objects
If the Cabinet application is updated to provide new Entities then the following will be required:

1. A new bean in `com.kaleido.cabinetclient.domain` to represent the object
1. A new field (with public getters and setters) in `CabinetClientProperties` for the new endpoint for the bean, e.g. `private String newEntityEndpoint = "new-entities";`
1. Declare a `@Bean` in `CabinetClientConfiguration` for the new entity type. e.g. 
```java
    @Bean
    CabinetClient<NewEntity> newEntityClient(RestTemplate restTemplate){
        return new CabinetClient<>(cabinetClientProperties.getBase()+cabinetClientProperties.getNewEntityEndpoint(),
                restTemplate, NewEntity.class);
    }
```

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/Kaleido-Biosciences/fetch/tags). 

## Authors
* **Mark Schreiber** - *Initial work* - [@markjschreiber](https://github.com/markjschreiber)
* **Pat Kyle** - *Initial work* - [@psk788](https://github.com/psk788)
* **Daisy Flemming** - *Initial work* - [@daisyflemming](https://github.com/daisyflemming)
* **Wes Fowlks** - *Initial work* - [@wfowlks](https://github.com/wfowlks)

See also the list of [contributors](https://github.com/Kaleido-Biosciences/fetch/graphs/contributors) who participated in this project.

## License

This project is licensed under the BSD 3-clause "New" or "Revised" License - see the [LICENSE.md](LICENSE.md) file for details

