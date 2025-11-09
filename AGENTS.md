# RedG
      
RedG is a Java library for comfortably generating database data for unit tests, integration tests or other use cases. 
From a given database schema, it generates classes that define a data modeling API for database data that should 
be inserted into the target database schema.
This is a very similar to other database libraries like JOOQ (but JOOQ serves a different purpose, of course).

One main feature of RedG is that it does not require the API user to specify all fields or dependency of an entity 
they want to insert into the database. Everything the user does not specify but is still necessary (due to database
constraints) will be generated filled with default values in the background. That includes non-null columns
and dependencies modeled via non-null foreign keys.

RedG is highly configurable, both during code generation and runtime.

## Project structure
                                         
### Modules

- `redg-common`: Defines some common classes
- `redg-testing`: Provides the infrastructure for conducting unit tests against multiple different database systems 
  (using testcontainers). This module is intended for RedG development only and will not be published to maven central.
- `redg-schema-inspection`: A library for inspecting schemas. For a given database schema, it extracts all relevant 
  information (like tables and their columns, UDTs etc.) into an easy to navigate schema model (Java objects).
- `redg-models`: The data model used by `redg-generator` for generating code. Note that this model currently contains
  both information about the database schema and additional information for generating the code.
  These aspects will be separated in the future by using `redg-schema-inspection`'s model as a schema model and 
  `redg-models` as generator model, containing exactly the information the code generator needs.
- `redg-generator`: The code generator for generating the RedG data modeling API for a given schema.
- `redg-runtime`: Defines classes needed at runtime of the data insertion code.
- `redg-maven-plugin`: A maven plugin for invoking the RedG code generator.
- `redg-extractor`: Tooling for generating RedG code from existing data. The main purpose of this module is to provide
  help for migrating from other data modeling tools to RedG.
- `redg-jpa-providers`: Code generator customization providers for JPA.
- `redg-tests`: Some integration tests. 

Here is a dependency graph boiled down to individual dependencies:

```
redg-testing -> redg-common
redg-schema-inspection -> redg-common
redg-maven-plugin -> redg-jpa-providers
redg-jpa-providers -> redg-generator
redg-generator -> redg-common
redg-generator -> redg-models
redg-tests -> redg-generator
redg-tests -> redg-extractor
redg-extractor -> redg-runtime
redg-runtime -> redg-models
```

As you can see, `redg-generator` does not yet depend on `redg-schema-inspection`, but only `redg-models`. 
However, this is a short-term goal, since the `redg-schema-inspection` provides a better model of the schema itself than
`redg-models`. The model in `redg-models` should be changed so it provides exactly the information needed by RedG's 
code generator. So RedG will first extract all information from the physical database schema to the model defined in
`redg-schema-inspection`. Then it will map it into the code generator model from it (`redg-models`), taking into account
the code generator configuration. Finally, RedG will use the code generator model to generate the API code. 
The generated classes are used by a developer to model database data. During execution of that code, the generated
code will make use of the classes in `redg-runtime`.

## Coding Rules

* All new unit tests should make use of testcontainers and the test infrastructure defined in `redg-testing` (unless
  the unit tests do not depend on a physical database at all).
* Unit tests must make use of AssertJ whenever possible.
* Use tabs for indentation.