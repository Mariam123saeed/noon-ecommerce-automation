# Noon.com Test Automation Framework (Guest User)

End-to-end UI test automation for [Noon Egypt](https://www.noon.com/egypt-en/) using **Java, Selenium WebDriver, TestNG, and Maven**.

The framework follows the **Page Object Model (POM)** design pattern and supports data-driven testing using CSV, Excel, Properties files, and TestNG DataProviders. It uses Log4j2 for logging and Allure for test reporting, including screenshots of failed tests when successfully captured and attached.

**Author:** Mariam Saeed

**Precondition:** All test scenarios are designed for guest users who are not logged in.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Test Scenarios](#2-test-scenarios)
3. [Technology Stack](#3-technology-stack)
4. [Prerequisites and Installation](#4-prerequisites-and-installation)
5. [Project Structure](#5-project-structure)
6. [Framework Architecture](#6-framework-architecture)
7. [Configuration and Test Data](#7-configuration-and-test-data)
8. [Page Objects and Utilities](#8-page-objects-and-utilities)
9. [Running the Tests](#9-running-the-tests)
10. [Allure Reports](#10-allure-reports)
11. [GitHub and Version Control](#11-github-and-version-control)
12. [Troubleshooting](#12-troubleshooting)
13. [Known Limitations](#13-known-limitations)

---

## 1. Project Overview

This project automates realistic e-commerce journeys on Noon Egypt using Selenium WebDriver.

The framework focuses on validating product discovery, filtering, shopping cart operations, and search behavior while maintaining a reusable and organized test automation structure.

### Project Objectives

| Objective | Implementation |
|---|---|
| Maintainable automation | Page Object Model (POM) |
| Reusable browser operations | Shared page methods and WebDriver factory |
| Data-driven testing | CSV, Excel, Properties, and TestNG DataProviders |
| Test execution management | TestNG and Maven Surefire |
| Debugging and traceability | Log4j2 logging and test reports |
| Failure investigation | Allure reports and failure screenshots |
| Browser configuration | Configurable browser selection |
| Version control | Git and GitHub |

### Project Scope

The framework covers four main scenarios:

1. Browse and filter products.
2. Add multiple products to the shopping cart.
3. Add multiple Free Shipping products to the shopping cart.
4. Validate search behavior with invalid input.

### Test Data Requirement

The project uses a **budget of 12,000 EGP** for the multiple-products scenarios.

Some test data files are confidential and are intentionally excluded from the GitHub repository. See [Configuration and Test Data](#7-configuration-and-test-data) for details.

---

## 2. Test Scenarios

**Website:** [https://www.noon.com/egypt-en/](https://www.noon.com/egypt-en/)

**Test type:** UI automation

**User type:** Guest

| # | Scenario | Test Class | Data Source |
|---|---|---|---|
| 1 | Browse and Filter Products | `tests.products.ProductFilterTest` | CSV |
| 2 | Add Multiple Products to Cart | `tests.products.AddMultipleProductsTest` | Excel |
| 3 | Add Multiple Free-Shipping Products to Cart | `tests.products.AddMultipleFreeShippingProductsTest` | Excel |
| 4 | Search Validation with Invalid Input | `tests.home.HomeTest` | CSV |

The class names above describe the intended test suite. Confirm that each class exists in the current repository and is included in `testng.xml` before running the complete suite.

### Scenario 1: Browse and Filter Products

**Objective:** Verify that the product listing displays products matching the selected filters.

#### Preconditions

- Noon Egypt is accessible.
- The user is not logged in.
- Product filter test data is available locally.

#### Test Steps

1. Open Noon Egypt.
2. Verify that the home page and Noon logo are displayed.
3. Navigate to the Electronics category.
4. Open the relevant product listing.
5. Select Samsung as the brand.
6. Verify that Samsung is selected.
7. Apply the minimum and maximum price filters from the test data.
8. Apply the minimum product rating.
9. Wait for the product listing to finish loading.
10. Validate the displayed products.

#### Expected Results

- The selected brand is applied correctly.
- Product prices are within the configured range.
- Product ratings meet the configured minimum.
- The product listing contains results that satisfy the selected criteria.

**Test data:** `productFilterData.csv`, provided locally.

**Note:** Sorting by customer ratings should be considered a separate validation unless the test explicitly verifies the ordering of the displayed products.

### Scenario 2: Add Multiple Products to Cart

**Objective:** Verify that multiple products can be added to the cart as a guest and that their details are correct.

#### Preconditions

- The user is not logged in.
- The search test data is available locally.
- The selected products fit the configured budget range.

#### Test Steps

1. Open Noon Egypt.
2. Verify that the home page and search bar are displayed.
3. Search for the configured product keyword.
4. Verify that search results are displayed.
5. Read the budget and minimum product count from the test data.
6. Select different products whose combined prices satisfy the budget rules.
7. Add each selected product to the cart with a quantity of one.
8. Navigate to the shopping cart.
9. Wait for the cart contents to load.
10. Verify that all selected products are present.
11. Verify each product's name and price.
12. Verify that each product quantity is one.
13. Compare the cart subtotal with the expected total.
14. Verify that the total satisfies the budget rules.

#### Expected Results

- The selected products are added successfully.
- Each product appears in the cart.
- Product names and prices are validated.
- Every selected product has a quantity of one.
- The cart subtotal is consistent with the cart item prices.
- The total is within the permitted budget range.

### Scenario 3: Add Multiple Free-Shipping Products to Cart

**Objective:** Verify that multiple products displaying Free Delivery in the search results also display Free Shipping eligibility in the cart.

#### Test Steps

1. Open Noon Egypt without logging in.
2. Search for the configured product keyword.
3. Verify that search results are displayed.
4. Identify products displaying the Free Delivery label.
5. Select at least four eligible products if a valid combination is available.
6. Ensure that the selected products satisfy the configured budget rules.
7. Add each selected product to the cart with a quantity of one.
8. Open the shopping cart.
9. Verify the product names, prices, and quantities.
10. Verify that each selected product displays the Free Shipping label in its own cart row.
11. Validate the cart subtotal.

#### Expected Results

- Only products meeting the Free Delivery selection criteria are selected.
- The selected products are present in the cart.
- Each product has a quantity of one.
- Each selected product displays the expected Free Shipping label in the cart.
- The cart subtotal satisfies the expected total and budget validations.

**Important implementation detail:** The search results use the label `Free Delivery`, while the cart may use `Free Shipping`. The framework validates these labels in their respective contexts.

### Scenario 4: Search Validation with Invalid Input

**Objective:** Validate the website's behavior when a user searches for invalid special-character input.

#### Example Inputs

- `###`
- `@%`
- `$!`

#### Test Steps

1. Open Noon Egypt.
2. Verify that the search bar is displayed.
3. Enter an invalid search term.
4. Submit the search.
5. Wait for the search results or error state.
6. Verify the expected no-results or error message.
7. Verify that no unrelated products are displayed.

#### Expected Results

- The search request is submitted.
- The expected error or no-results state is displayed.
- No unrelated products are returned.

**Note:** The exact message and expected product count should match the website's actual behavior and the assertions implemented in the test.

### Budget Rules

The budget and tolerance are configurable through the local Excel test data.

For a budget of **12,000 EGP** and a tolerance of **5%**, the expected ranges are:

| Scenario | Budget Rule | Allowed Total |
|---|---|---|
| Scenario 2 | Total must not exceed the budget and may be up to 5% below it | 11,400–12,000 EGP |
| Scenario 3 | Total may exceed the budget by no more than 5% | Depends on the implemented lower-bound rule, with a maximum of 12,600 EGP |

For Scenario 3, the implementation should explicitly define whether totals below 11,400 EGP are allowed.

Noon may display rounded prices in search results and more precise prices in the cart. The tests should account for this difference when comparing displayed product prices.

The cart subtotal should be validated against the actual cart item prices, using an appropriate currency-rounding tolerance.

---

## 3. Technology Stack

The following table summarizes the technologies used or intended for this project. Verify the exact dependency versions against `pom.xml`.

| Technology | Purpose |
|---|---|
| Java | Programming language |
| Selenium WebDriver | Browser automation |
| TestNG | Test execution, assertions, and DataProviders |
| Apache Maven | Build and dependency management |
| Maven Surefire | Test execution through Maven |
| Page Object Model | Test architecture and maintainability |
| Log4j2 | Logging |
| Allure TestNG | Test reporting and attachments |
| Allure Commandline | Report generation and viewing |
| OpenCSV | CSV test data reading |
| Apache POI | Excel test data reading |
| Git | Version control |
| GitHub | Source code hosting |

### Browser Support

The framework includes browser-specific driver classes for:

- Google Chrome
- Mozilla Firefox
- Microsoft Edge

Actual browser compatibility depends on the browser installation, WebDriver configuration, and successful execution of the tests.

Selenium Manager can manage compatible browser drivers when the installed Selenium version supports the selected browser and network access is available.

---

## 4. Prerequisites and Installation

The following instructions primarily target Ubuntu and Debian Linux.

### 4.1 Java JDK

Install a JDK compatible with the Java version configured in `pom.xml`.

For Java 25, if the package is available for your Ubuntu release:

```bash
sudo apt update
sudo apt install -y openjdk-25-jdk
```

Verify the installation:

```bash
java -version
javac -version
```

If the project requires Java 25, ensure that Maven also uses the appropriate JDK.

Check:

```bash
mvn -version
```

### 4.2 Apache Maven

Install Maven:

```bash
sudo apt install -y maven
```

Verify:

```bash
mvn -version
```

### 4.3 Google Chrome

Install Google Chrome using the official instructions:

[Google Chrome](https://www.google.com/chrome/)

On a compatible Ubuntu/Debian x86-64 system, the package can be downloaded and installed as follows:

```bash
wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb
sudo apt install -y ./google-chrome-stable_current_amd64.deb
```

Verify:

```bash
google-chrome --version
```

Selenium Manager may download the required driver automatically when the tests run.

### 4.4 Git

Install Git:

```bash
sudo apt install -y git
```

Verify:

```bash
git --version
```

### 4.5 Allure Commandline

Install Node.js and npm using a supported installation method, then install Allure Commandline:

```bash
npm install -g allure-commandline
```

Verify:

```bash
allure --version
```

If npm reports a permissions error, use an appropriate user-level npm configuration rather than changing permissions broadly on system directories.

### 4.6 IDE (Optional)

IntelliJ IDEA is recommended for Java development.

Open the project folder containing `pom.xml`. IntelliJ can import the Maven project and resolve its declared dependencies.

### 4.7 Verify the Environment

Run:

```bash
java -version
mvn -version
git --version
allure --version
google-chrome --version
```

Install the tools required by your selected browser and reporting workflow before running the tests.

---

## 5. Project Structure

The following is an overview of the main project components. It should be kept synchronized with the actual repository.

```text
Noon/
├── pom.xml
├── testng.xml
├── .gitignore
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── org/example/
    │   │       ├── Main.java
    │   │       └── pages/
    │   │           ├── BasePage.java
    │   │           ├── home/
    │   │           │   └── HomePage.java
    │   │           ├── products/
    │   │           │   ├── ProductFilterPage.java
    │   │           │   └── ProductsPage.java
    │   │           └── cart/
    │   │               └── CartPage.java
    │   └── resources/
    │       ├── config.properties
    │       └── log4j2.properties
    └── test/
        └── java/
            ├── driverFactory/
            │   ├── DriverFactory.java
            │   ├── GetChromeDriver.java
            │   ├── GetFirefoxDriver.java
            │   └── GetEdgeDriver.java
            ├── tests/
            │   ├── BaseTest.java
            │   ├── dataProvider/
            │   │   └── DataProviderTest.java
            │   ├── home/
            │   │   └── HomeTest.java
            │   └── products/
            │       ├── ProductFilterTest.java
            │       ├── AddMultipleProductsTest.java
            │       └── AddMultipleFreeShippingProductsTest.java
            └── utils/
                ├── ConfigHandler.java
                ├── ExcelFileManager.java
                ├── OpenCSVFileManager.java
                └── ScreenShot.java
```

**Important:** This is a documentation example, not a guarantee that every listed file currently exists at that exact path. Verify the tree against the actual repository before publishing.

The private test data files are intentionally not listed under the tracked resources in this structure.

---

## 6. Framework Architecture

The framework separates test execution, page interactions, browser management, and test data.

```text
testng.xml
    |
    v
Maven Surefire
    |
    v
TestNG
    |
    v
Test Class
    |
    +--> BaseTest
    |      |
    |      +--> Read configuration
    |      +--> Create WebDriver
    |      +--> Open the website
    |      +--> Capture screenshot on failure, when configured
    |      +--> Quit WebDriver
    |
    +--> TestNG DataProvider
    |      |
    |      +--> CSV test data
    |      +--> Excel test data
    |
    +--> Page Objects
           |
           +--> HomePage
           +--> ProductFilterPage
           +--> ProductsPage
           +--> CartPage

Test Execution
    |
    +--> Log4j2 logs
    |
    +--> Allure results and attachments
```

### Architecture Layers

| Layer | Responsibility |
|---|---|
| Test classes | Define scenarios, execute actions, and verify expected results |
| BaseTest | Shared test setup and teardown |
| DriverFactory | Browser driver creation and management |
| Page Objects | Page locators and user interactions |
| DataProviderTest | Supplies test data to TestNG tests |
| Utilities | Configuration, CSV, Excel, and screenshot operations |
| Log4j2 | Execution logging |
| Allure | Reporting and attachments |

### Reliability Practices

The framework aims to improve test reliability through:

- Explicit waits instead of relying on fixed delays.
- Reusable page actions and locators.
- Data-driven execution.
- Product name normalization when comparing search results and cart items.
- Logging around important test steps.
- Failure screenshots when screenshot capture and report attachment are configured successfully.

Tests against a live e-commerce website can still fail when the site changes, responds slowly, or returns unexpected content.

---

## 7. Configuration and Test Data

### 7.1 Browser and URL Configuration

The project uses:

`src/main/resources/config.properties`

An example configuration is:

```properties
browser=chrome
url=https://www.noon.com/egypt-en/
```

Adjust the values according to the actual configuration keys used by `ConfigHandler` and `BaseTest`.

Do not include passwords, access tokens, or other secrets in a publicly committed configuration file.

### 7.2 Private Test Data

The following files contain private test data and are intentionally excluded from the repository:

- `src/main/resources/AddMultipleProducts.xlsx`
- `src/main/resources/productFilterData.csv`
- `src/main/resources/searchData.csv`

These files must be supplied locally before running the scenarios that depend on them.

The expected names, sheet names, and column headers must match the requirements of the corresponding DataProviders.

**Do not commit these files to GitHub.**

The repository's `.gitignore` should include:

```gitignore
# Private test data
src/main/resources/AddMultipleProducts.xlsx
src/main/resources/productFilterData.csv
src/main/resources/searchData.csv
```

Git ignores untracked files that match these patterns. If a file was already committed, adding it to `.gitignore` does not remove it from the repository's history.

### 7.3 Product Filter Data

The private CSV data used by Scenario 1 is expected to provide values such as:

- Minimum price
- Maximum price
- Minimum rating

The exact column names must match the implementation in `DataProviderTest`.

### 7.4 Search Validation Data

The private CSV data used by Scenario 4 supplies invalid search terms and their expected outcomes.

The expected message must match the assertion implemented in the test class.

### 7.5 Multiple-Products Data

The private Excel workbook used by Scenarios 2 and 3 supplies parameters such as:

- `searchText`
- `budget`
- `tolerancePercent`
- `minItems`

The expected worksheet name and headers must match the Excel reader and DataProvider implementation.

Private test data is not included in the public repository. Anyone running the project must obtain the necessary data separately.

---

## 8. Page Objects and Utilities

The following descriptions summarize the intended responsibilities of the framework components. Method names should be verified against the current source code.

### 8.1 BasePage

`BasePage` provides common Selenium operations shared by page classes.

Typical responsibilities include:

- Finding elements.
- Waiting for elements.
- Clicking elements.
- Typing and pressing keyboard keys.
- Reading text and attributes.
- Checking element visibility.
- Waiting for URL changes.
- Scrolling to elements.

Centralizing these actions reduces duplication and makes it easier to maintain the framework.

### 8.2 HomePage

`HomePage` represents the website's home page and common navigation elements.

Typical responsibilities include:

- Checking the home page and logo.
- Interacting with the search bar.
- Submitting search queries.
- Navigating to product categories.
- Opening the shopping cart.
- Checking relevant messages and page elements.

### 8.3 ProductFilterPage

`ProductFilterPage` represents the product listing and its filtering controls.

Typical responsibilities include:

- Selecting a brand.
- Applying a price range.
- Adjusting the rating filter.
- Waiting for the product listing to load.
- Reading product names, prices, and ratings.
- Verifying that displayed products satisfy the selected criteria.

### 8.4 ProductsPage

`ProductsPage` represents the search results page used by the multiple-products scenarios.

Typical responsibilities include:

- Reading product names and prices.
- Identifying individual product cards.
- Checking Free Delivery eligibility.
- Selecting products that satisfy the budget rules.
- Adding selected products to the cart.

The multiple-products scenarios use the configured minimum product count and budget parameters to determine which products can be selected.

### 8.5 CartPage

`CartPage` represents the shopping cart.

Typical responsibilities include:

- Waiting for cart contents to load.
- Reading cart product names and prices.
- Reading product quantities.
- Reading the cart subtotal.
- Verifying Free Shipping labels for individual cart items.
- Comparing expected and actual cart contents.

Product details should be read from the relevant cart row to avoid accidentally matching another product with similar text.

### 8.6 DriverFactory

The driver factory separates browser creation from test logic.

The project contains browser-specific classes for Chrome, Firefox, and Edge.

The selected browser is configured through the project's browser configuration mechanism.

### 8.7 ConfigHandler

`ConfigHandler` reads configuration properties used by the framework.

Configuration should be loaded consistently so that tests use the intended browser and website URL.

### 8.8 ExcelFileManager

`ExcelFileManager` provides access to Excel-based test data.

It is used by the DataProvider responsible for the multiple-products scenarios.

### 8.9 OpenCSVFileManager

`OpenCSVFileManager` provides access to CSV test data.

It is used by the DataProviders for scenarios such as product filtering and search validation.

### 8.10 ScreenShot

`ScreenShot` contains screenshot-related functionality.

The test lifecycle can use this utility to capture browser screenshots when a test fails.

### 8.11 BaseTest

`BaseTest` provides common test setup and teardown.

Depending on the implementation, it may:

- Load configuration.
- Create a WebDriver instance.
- Open the website.
- Maximize the browser.
- Capture failure screenshots.
- Attach screenshots to Allure.
- Close the WebDriver after execution.

### 8.12 DataProviderTest

`DataProviderTest` supplies external test data to TestNG tests.

The providers should match the actual CSV and Excel file formats, worksheet names, column names, and test method parameters.

---

## 9. Running the Tests

Run Maven commands from the project directory containing `pom.xml`.

### 9.1 Clone the Repository

Replace the placeholders with the actual GitHub username and repository name.

```bash
git clone https://github.com/<your-username>/<repository-name>.git
cd <repository-name>
```

### 9.2 Provide the Private Test Data

Before running the tests, place the required private test data files in their expected local paths.

Do not commit these files to Git.

### 9.3 Compile the Project

```bash
mvn clean compile
```

This compiles the main source code.

To compile the test source code as well, use the Maven lifecycle configured by the project, for example:

```bash
mvn test-compile
```

### 9.4 Run the Test Suite

```bash
mvn clean test
```

If `pom.xml` configures Maven Surefire to use `testng.xml`, this command runs the configured suite.

### 9.5 Run a Single Test Class

Examples:

```bash
mvn test -Dtest=ProductFilterTest
```

```bash
mvn test -Dtest=AddMultipleProductsTest
```

```bash
mvn test -Dtest=AddMultipleFreeShippingProductsTest
```

```bash
mvn test -Dtest=HomeTest
```

Single-class execution depends on the Surefire configuration. If the project forces execution through a suite XML file, run the desired test class through the IDE or use a compatible Maven configuration.

### 9.6 Run from IntelliJ IDEA

1. Open the project folder containing `pom.xml`.
2. Allow Maven dependencies to load.
3. Verify the browser and test data configuration.
4. Run `testng.xml` to execute the configured suite, or run an individual test class.

### 9.7 Run Tests and Open Allure

To remove previous Allure results, run the test suite, and open a report even if Maven reports test failures:

```bash
rm -rf allure-results
mvn clean test -Dmaven.test.failure.ignore=true
allure serve allure-results
```

The Maven failure-ignore property allows the command to continue when tests fail, provided the build itself completes sufficiently to produce the results.

Review the test results rather than treating a successful Maven process as proof that every test passed.

---

## 10. Allure Reports

Allure can present test execution results, failure information, and configured attachments.

### 10.1 Open a Report

After generating the Allure results:

```bash
allure serve allure-results
```

This starts a local report server and opens the report in a browser.

Press `Ctrl + C` in the terminal to stop the server.

### 10.2 Generate a Static Report

```bash
allure generate allure-results --clean -o allure-report
```

Open the generated report:

```bash
allure open allure-report
```

### 10.3 What to Review

The report may include:

- Overall test execution results.
- Passed and failed test cases.
- Test execution details.
- Failure messages and stack traces.
- Screenshots and other attachments configured by the framework.

Screenshots appear only when the framework captures them and successfully attaches them to the report.

### 10.4 Generated Files

The following are generated artifacts and should normally remain outside version control:

- `target/`
- `allure-results/`
- `allure-report/`
- `test-output/`

These can be regenerated by running the tests and report commands.

---

## 11. GitHub and Version Control

This project uses Git for version control and GitHub for source code hosting.

### 11.1 Repository Setup

If the local repository has already been initialized, do not run `git init` again unnecessarily.

Check the current state:

```bash
git status
git branch
git remote -v
```

If the repository has no remote configured, add the existing GitHub repository:

```bash
git remote add origin https://github.com/<your-username>/<repository-name>.git
```

If `origin` already exists, inspect it and update its URL only if necessary.

### 11.2 Branch Name

The primary branch may be named `main`.

If you intend to rename the current branch:

```bash
git branch -M main
```

### 11.3 Push Existing Commits

After verifying the remote and checking that private files are not tracked:

```bash
git push -u origin main
```

Use the actual branch name if it differs from `main`.

### 11.4 Safe `.gitignore`

A `.gitignore` for this project should exclude generated files and confidential test data.

Example:

```gitignore
# Maven build output
target/

# Allure reports
allure-results/
allure-report/

# Test output
test-output/

# IDE files
.idea/
*.iml
.vscode/
out/

# Operating system
.DS_Store
Thumbs.db

# Logs
logs/
*.log

# Private test data
src/main/resources/AddMultipleProducts.xlsx
src/main/resources/productFilterData.csv
src/main/resources/searchData.csv

# Local secrets and overrides
.env
.env.*
config.local.properties
```

Review this example against the existing `.gitignore` rather than replacing working rules blindly.

**Important:** `.gitignore` does not remove files that Git already tracks. Previously committed confidential data requires additional remediation.

### 11.5 Verify Private Files Are Not Tracked

Run:

```bash
git ls-files \
src/main/resources/AddMultipleProducts.xlsx \
src/main/resources/productFilterData.csv \
src/main/resources/searchData.csv
```

No output means these paths are not currently tracked by Git.

To check that Git ignores the files:

```bash
git check-ignore -v \
src/main/resources/AddMultipleProducts.xlsx \
src/main/resources/productFilterData.csv \
src/main/resources/searchData.csv
```

These checks do not prove that the files never appeared in an earlier commit. Review repository history if there is any uncertainty.

### 11.6 Commit Changes Separately

For this project, changes are recorded in separate commits to make the history easier to understand.

Example:

```bash
git add README.md
git commit -m "docs: document Noon test automation framework"
```

For future code changes, stage only the intended file or files instead of automatically adding everything.

Check the staged changes before committing:

```bash
git status
git diff --cached
```

### 11.7 Authentication

GitHub supports HTTPS authentication through supported credential helpers or GitHub CLI, and SSH authentication through configured SSH keys.

Never place passwords or access tokens directly in source files or commit them to Git.

---

## 12. Troubleshooting

| Problem | Possible Cause | Suggested Action |
|---|---|---|
| Browser does not start | Incorrect browser configuration or missing browser | Verify the browser installation and configuration |
| WebDriver fails to initialize | Driver or browser compatibility issue | Check Selenium Manager output and browser versions |
| Maven cannot compile the project | JDK version mismatch | Compare the installed JDK with the Maven compiler configuration |
| Test data file is missing | Private data has not been provided locally | Add the required file to the expected local path |
| Excel worksheet cannot be found | Worksheet name mismatch | Verify the expected worksheet name in the Excel reader |
| CSV column is missing | Header names do not match | Compare CSV headers with DataProvider expectations |
| Search results do not load | Slow website response or changed page behavior | Review logs and screenshots, and inspect the current page |
| A Selenium locator stops working | The website's DOM changed | Inspect the current DOM and update the relevant page object |
| Product price comparison fails | Rounding differences or changed prices | Compare the current displayed values and use an appropriate tolerance |
| Cart displays an error | Temporary website or network issue | Inspect the page and determine whether retrying is appropriate |
| Allure report is empty | No results were generated or the wrong directory was used | Verify the test execution and `allure-results/` contents |
| `allure` command is unavailable | Allure Commandline is not installed or not on `PATH` | Verify installation and shell configuration |
| Git rejects a push | Authentication, remote, or branch mismatch | Verify `git remote -v`, the branch name, and authentication |
| Private files appear in Git status | The ignore rules are missing or incorrect | Review `.gitignore` and `git check-ignore -v` |
| Private files exist in earlier commits | They were committed before being ignored | Review history and clean it before publishing if necessary |

---

## 13. Known Limitations

- Tests run against the live Noon website, so the layout, prices, availability, and behavior may change.
- A failure may be caused by the website, network conditions, changed product data, or a test defect.
- Private test data files must be supplied locally before running dependent scenarios.
- Product selection depends on the available search results and the implemented budget-selection algorithm.
- The current multiple-products implementation may select exactly the configured `minItems` rather than dynamically increasing the number of products when no valid combination is found.
- Free Delivery eligibility in search results and Free Shipping eligibility in the cart are checked separately.
- Browser compatibility depends on the actual browser and WebDriver environment.
- Test reports and screenshots depend on successful test execution, result generation, and attachment configuration.
- Only guest-user scenarios are in scope; login and authenticated user journeys are not covered.
- The README documents the intended framework design and must be kept synchronized with the actual codebase.

---

## Author

**Mariam Saeed**

This project was developed for software testing and test automation practice.

**Disclaimer:** This project is for educational and testing purposes. Noon is a third-party website. This project is not affiliated with or endorsed by Noon.
