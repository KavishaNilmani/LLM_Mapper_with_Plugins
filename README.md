# LLM Mapper Smart Service (Appian Plug-in)

## Overview

This project is an Appian Smart Service plug-in designed to perform LLM (Large Language Model) mapping using Azure OpenAI. It processes buysheet data, extracts structured information using LLMs, and outputs results in JSON format. The plug-in is built with Spring Boot and integrates with Appian via the Appian Plug-in SDK.

---

## Folder Structure

```
llmmapper/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/llmmapper/
│   │   │       ├── LlmmapperApplication.java
│   │   │       ├── smartservice/
│   │   │       │   └── LlmMapperSmartServicev2.java
│   │   │       └── util/
│   │   │           ├── PromptUtil.java
│   │   │           └── EnvUtil.java
│   │   └── resources/
│   │       ├── appian-plugin.xml
│   │       ├── application.properties
│   │       └── com/example/llmmapper/smartservice/
│   │           └── llm-mapper-v2_en_US.properties
│   └── test/
│       ├── java/com/example/llmmapper/LlmmapperApplicationTests.java
│       └── resources/application.properties
├── lib/appian-plug-in-sdk.jar
├── pom.xml
├── output.json
├── buysheet 1.txt
├── .gitignore
├── HELP.md
└── README.md
```

---

## File Explanations

### Java Files

- **LlmmapperApplication.java**: Main Spring Boot application. Reads buysheet data, chunks it, sends prompts to Azure OpenAI, parses the LLM response, and writes structured output to `output.json`.
- **smartservice/LlmMapperSmartService.java**: Implements the Appian Smart Service. Handles input/output parameters, reads input files, calls Azure OpenAI, and writes results. Integrates with Appian's process framework.
- **util/PromptUtil.java**: Utility for generating the LLM prompt from buysheet data. Ensures consistent prompt formatting.
- **util/EnvUtil.java**: Utility for loading environment variables from a `.env` file using `java-dotenv`.
- **test/java/LlmmapperApplicationTests.java**: Basic Spring Boot test to ensure the application context loads.

### XML Files

- **appian-plugin.xml**: Declares the Appian plug-in, its metadata, and registers the smart service (`LlmMapperSmartService`).
- **pom.xml**: Maven build file. Declares dependencies (Spring Boot, OkHttp, Jackson, Appian SDK), plugins for building and packaging, and Java version.

### Properties Files

- **application.properties (main)**: Sets Spring Boot app name and disables web server mode.
- **application.properties (test)**: Activates the `test` profile for test runs.
- **llm-mapper_en_US.properties**: Provides display names and descriptions for the smart service and its parameters in Appian.

### Other Files

- **output.json**: Example output file containing extracted records from the buysheet, with fields like `Release Date`, `Season`, and `confidence`.
- **buysheet 1.txt**: Example input data file containing raw buysheet records to be processed by the LLM.
- **lib/appian-plug-in-sdk.jar**: Appian SDK JAR required for building the plug-in.
- **.gitignore**: Specifies files/folders to ignore in version control (e.g., build artifacts, secrets, output files).
- **HELP.md**: Additional Maven/Spring Boot documentation and tips.

---

## About Appian, Plug-ins, and Smart Services

**Appian** is a low-code automation platform for building enterprise applications. It supports custom plug-ins to extend its capabilities.

**Appian Plug-in**: A package (ZIP/JAR) containing Java classes, resources, and metadata (like `appian-plugin.xml`) that extends Appian with new functionality, such as smart services, functions, or connected systems.

**Appian Smart Service Plug-in**: A type of plug-in that adds new process nodes (smart services) to Appian's process modeler. These nodes can perform custom logic, interact with external systems, or process data.

This project provides a smart service for LLM-based data extraction, which can be used in Appian process models.

---

## Building the Project

### Prerequisites
- Java 17+
- Maven 3.6+
- Appian Plug-in SDK JAR (`lib/appian-plug-in-sdk.jar`)

### Build Commands

To build the project and create a deployable JAR:

```
mvn clean package
```

The output JAR will be in the `target/` directory.

---

## Running Locally (for development)

To run the Spring Boot application (for local testing, not as an Appian plug-in):

```
mvn spring-boot:run
```

This will process `buysheet 1.txt` and generate `output.json` using your Azure OpenAI credentials (set in your `.env` file).

---

## Deploying to Appian (Plug-in Testing)

1. **Build the plug-in** using `mvn clean package`.
2. **Locate the JAR** in the `target/` directory (e.g., `llmmapper-0.0.1-SNAPSHOT.jar`).
3. **Upload the JAR** to your Appian environment:
   - Go to **Admin Console > Plug-ins** in your Appian instance.
   - Click **Deploy New Plug-in** and upload the JAR.
4. **Configure environment variables** (Azure OpenAI credentials) in Appian if needed.
5. **Use the Smart Service** in your process models as "LLM Mapper Smart Service".

---

## Environment Variables

The following environment variables must be set (in a `.env` file or Appian environment):
- `AZURE_OPENAI_ENDPOINT`
- `AZURE_OPENAI_DEPLOYMENT`
- `AZURE_OPENAI_API_VERSION`
- `AZURE_OPENAI_API_KEY`

---

## References & Further Reading
- [Appian Plug-in Documentation](https://docs.appian.com/suite/help/latest/Custom_Smart_Services.html)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Maven](https://maven.apache.org/)
- [Azure OpenAI](https://learn.microsoft.com/en-us/azure/cognitive-services/openai/)

---

## License

This project is provided by Lowcode Minds Technology Pvt Ltd. See `pom.xml` for license info. 