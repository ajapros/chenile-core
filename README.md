# chenile-core
This contains the source code of all the core modules and test modules for Chenile

# About chenile

Chenile is an open source framework for creating Micro services using Java and Spring Boot. 
Please check the details out at https://chenile.org

It provides an interception framework to decouple functional and non-functional requirements.
Chenile avoids the need to write repetitive code. It encourages modular coding best practices. 

In addition to creating REST services, Chenile services can also be used to create event processors, 
schedulers (with quartz), a file watcher etc. without the need for rewriting the code. 

Chenile has a state machine and an orchestration engine.  

The orchestration engine is internally used by Chenile to provide an interception framework that helps in 
disinter-mediating traffic irrespective of the incoming protocol (HTTP, message etc.)

Hence Chenile also serves like an IN-VM message bus. Chenile also facilitates easy swagger documentation 
(using Spring doc). 
Chenile allows the development of Cucumber based BDD tests with most of the plumbing already in place.
Chenile also is integrated with [keycloak](https://www.keycloak.org/) for security. 

Finally, Chenile ships with its own code generators to ease the development of micro services. 
Please see [Code Generation Repository](https://github.com/rajakolluru/chenile-gen) for more information 
about the code generator.

# Multi datasource utility

The `multi-datasource-utils` module provides a Spring Boot configuration that routes JPA traffic
to different data sources based on `ContextContainer.getTenant()`. Configure it like this:

```yaml
chenile:
  multids:
    defaultTenantId: tenant1
    datasources:
      tenant1:
        jdbcUrl: jdbc:h2:mem:tenant1;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
        username: sa
        password: ''
      tenant2:
        jdbcUrl: jdbc:h2:mem:tenant2;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
        username: sa
        password: ''
```

