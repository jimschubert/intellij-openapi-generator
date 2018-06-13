# OpenAPI Generator plugin for IntelliJ IDEs

This plugin allows for a sort of _spec_ driven development.

While designing your API, you can generate one or more clients from within the IDE to evaluate your changes.

Also install [intellij-swagger](https://plugins.jetbrains.com/plugin/8347) for a seamless Swagger/OpenAPI editor experience.

# Building

```
./gradlew clean buildPlugin
```

# Running in IntelliJ

First, you'll need to [setup a development environment](http://www.jetbrains.org/intellij/sdk/docs/basics/getting_started/setting_up_environment.html).

Then:

```
./gradlew runIde
```

NOTE: When running locally, if changes don't appear in the sandbox instance you may have forgotten to update the plugin version in `gradle.properties`.

# Usage

Open a Specification YAML or JSON file in IntelliJ. Supported specifications are: Swagger 2.0/OpenAPI 2.0/OpenAPI 3.0. With the document active, navigate to Code -> Swagger -> Generate from Swagger.

Choose your desired generator and fill out options. Then, choose an output directory and generate.

## License

This project is licensed under the Apache 2.0 license. See [./LICENSE](LICENSE) for more details.

Swagger® is a registered trademark of SmartBear Software, Inc.

This project is not maintained by SmartBear Software, Inc.
