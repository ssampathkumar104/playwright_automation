# Playwright Automation (Java)

This repository contains a Java-based Playwright automation framework and a sample test project built for browser automation, reusable page objects, test configuration, reporting, and data-driven execution.

The project is designed for web UI automation using Java and Maven, with Playwright Java as the core browser automation library.

## Overview

The repository includes:

- `WebTestAutomation-pw` – reusable Playwright Java automation framework and utilities
- `sampleTestProject-pw` – sample project that demonstrates how to use the framework
- root `pom.xml` – Maven parent configuration for the repository

This repository is intended for teams that want to build reliable browser automation flows using Java while keeping the test logic modular, configurable, and reportable.

## Tech Stack

- Java 17
- Maven
- Playwright Java
- TestNG
- Extent Reports
- Log4j
- Rest Assured / JSON utilities for supporting API-related automation tasks

## Repository Structure

```text
playwright_automation/
├── README.md
├── pom.xml
├── WebTestAutomation-pw/
│   ├── pom.xml
│   └── src/
│       ├── main/java/
│       └── main/resources/
├── sampleTestProject-pw/
│   ├── pom.xml
│   ├── run-test-plan.bat
│   └── src/
└── .gitignore
```

## Prerequisites

Before running the project, make sure the following are installed:

1. JDK 17 or later
   - Download: https://www.oracle.com/java/technologies/downloads/
2. Maven 3.9+
   - Download: https://maven.apache.org/download.cgi
3. Git
   - Download: https://git-scm.com/downloads
4. Internet access for downloading Playwright browser binaries

## Clone the Repository

```bash
git clone https://github.com/ssampathkumar104/playwright_automation.git
cd playwright_automation
```

## Build the Project

From the repository root:

```bash
mvn clean install
```

If a module is built separately, navigate into the project folder and run:

```bash
cd WebTestAutomation-pw
mvn clean install
```

## Install Playwright Browsers

This project uses the Playwright Java library, so the browser binaries must be installed before running tests.

Official Playwright Java documentation:

- https://playwright.dev/java/docs/intro
- https://playwright.dev/java/docs/ci

Install dependencies and browsers:

```bash
cd WebTestAutomation-pw
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

If you are using a Linux environment that requires OS dependencies, use:

```bash
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install --with-deps"
```

## Running the Sample Project

The sample project includes a helper script for running a named test plan on Windows:

```batch
cd sampleTestProject-pw
run-test-plan.bat SamplePlan
```

This script runs:

```bash
mvn clean install -DTEST_PLAN="SamplePlan.xml"
```

You can also run Maven directly:

```bash
cd sampleTestProject-pw
mvn clean test
```

## Example Playwright Java Usage

```java
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class QuickTest {
    public static void main(String[] args) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://example.com");
            System.out.println(page.title());
            page.close();
            browser.close();
        }
    }
}
```

## How This Framework Is Structured

The main framework in `WebTestAutomation-pw` contains reusable automation components such as:

- configuration classes
- page objects / page factory patterns
- base test utilities
- reporting integrations
- reusable data helpers
- browser/page lifecycle management

The sample project demonstrates how to build test flows on top of the shared framework.

## Writing New Tests

A typical workflow is:

1. Create or update a page object under the framework package structure.
2. Add a test class in the sample project or module under `src/test/java` / `src/main/java` depending on your project structure.
3. Configure browser settings and test data.
4. Run the project with Maven.
5. Review generated reports and logs.

## Useful Maven Commands

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Run a specific test class or suite
mvn -Dtest=SomeTest test

# Install the built artifact into the local Maven repository
mvn clean install
```

## Troubleshooting

### Playwright browser not found

If you see browser-related errors, reinstall the browsers:

```bash
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

### Java version mismatch

Ensure Java 17 is installed and active:

```bash
java -version
mvn -version
```

### Dependencies not resolving

Run a clean dependency refresh:

```bash
mvn clean install -U
```

## Official Documentation

Use the official resources below for the most accurate guidance:

- Playwright Java docs: https://playwright.dev/java/docs/intro
- Playwright docs overview: https://playwright.dev/
- Java installation: https://www.oracle.com/java/technologies/downloads/
- Maven installation and usage: https://maven.apache.org/
- GitHub Playwright repo: https://github.com/microsoft/playwright-java

## License

This project does not currently include a declared license in the repository root. Please check the repository settings or source files before publishing or redistributing it commercially.

## Contributing

Contributions are welcome. To contribute:

1. Fork the repository.
2. Create a feature branch.
3. Commit your changes.
4. Open a pull request with a clear description.

## Summary

This repository provides a Java + Playwright automation foundation for browser-based testing and automation. It is suitable for building readable, reusable, and maintainable UI automation scripts with Maven-based project management and Playwright’s modern browser automation capabilities.
