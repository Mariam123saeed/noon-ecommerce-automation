package tests.home;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.example.pages.home.HomePage;
import org.example.pages.products.ProductFilterPage;

import org.testng.Assert;
import org.testng.annotations.Test;

import tests.BaseTest;
import tests.dataProvider.DataProviderTest;

import java.time.Duration;

public class HomeTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(HomeTest.class);

    HomePage homePage;
    ProductFilterPage productPage;

    @Test(dataProvider = "searchData", dataProviderClass = DataProviderTest.class)
    public void verifySearchWithInvalidInput(String searchText, String expectedMessage) {

        log.info("========== START: Search Validation with Invalid Input ==========");
        log.info("Test data: Search Text='{}', Expected Message='{}'", searchText, expectedMessage);

        homePage = new HomePage(driver);
        productPage = new ProductFilterPage(driver);


        log.info("Step 1: Verify Home Page is opened");

        String homeUrl = homePage.getCurrentUrl();

        Assert.assertTrue(
                homeUrl.contains("noon.com"),
                "Home page URL should contain noon.com. Actual URL: " + homeUrl);

        Assert.assertTrue(homePage.isSearchBarDisplayed(), "Search bar should be displayed");


        log.info("Step 2: Enter invalid special characters: {}", searchText);

        homePage.enterSearchText(searchText);

        Assert.assertEquals(
                homePage.getSearchBarValue(),
                searchText,
                "Search bar should contain the entered invalid text");


        log.info("Step 3: Click Search");

        homePage.clickSearch();

        Assert.assertTrue(
                homePage.waitForUrlChange(homeUrl, Duration.ofSeconds(10)),
                "Search was not submitted. URL did not change from: " + homeUrl);

        log.info("Step 4: Verify the no results message is displayed");

        Assert.assertTrue(
                homePage.isSearchErrorDisplayed(),
                "No results / error message should be displayed for invalid input. Current URL: "
                        + homePage.getCurrentUrl());

        Assert.assertEquals(
                homePage.getSearchErrorMessage(),
                expectedMessage,
                "Unexpected message shown for invalid search input: " + searchText);


        log.info("Step 5: Ensure no unrelated products are displayed");

        Assert.assertEquals(
                productPage.getDisplayedProductsCount(),
                0,
                "No products should be displayed for invalid search input: " + searchText);

        log.info("========== PASSED: Search Validation with Invalid Input ==========");
    }
}