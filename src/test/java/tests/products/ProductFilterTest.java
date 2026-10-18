package tests.products;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.example.pages.home.HomePage;
import org.example.pages.products.ProductFilterPage;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

import tests.BaseTest;
import tests.dataProvider.DataProviderTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Locale.ROOT;

public class ProductFilterTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(ProductFilterTest.class);

    HomePage homePage;
    ProductFilterPage productPage;

    @Test(dataProvider = "productFilterData", dataProviderClass = DataProviderTest.class)
    public void verifySamsungPriceAndRatingFilters(double minimumPrice, double maximumPrice, double minimumRating) {

        log.info("========== START: Samsung Price and Rating Filters Test ==========");
        log.info(
                "Test data: Minimum Price={}, Maximum Price={}, Minimum Rating={}", minimumPrice, maximumPrice, minimumRating);

        homePage = new HomePage(driver);
        productPage = new ProductFilterPage(driver);


        log.info("Step 1: Verify Home Page is opened");

        Assert.assertTrue(
                homePage.getCurrentUrl().contains("noon.com"),
                "Home page URL should contain noon.com. Actual URL: " + homePage.getCurrentUrl());

        Assert.assertTrue(homePage.isNoonLogoDisplayed(), "Noon logo should be displayed");


        log.info("Step 2: Scroll to Electronics section");

        homePage.scrollToElectronicsSection();

        Assert.assertTrue(
                homePage.isElectronicsSectionDisplayed(),
                "Electronics section should be displayed");

        log.info("Step 3: Verify Electronics View All button");

        Assert.assertTrue(
                homePage.isElectronicsViewAllDisplayed(),
                "Electronics View All button should be displayed");


        log.info("Step 4: Click View All in Electronics section");

        homePage.clickElectronicsViewAll();

        Assert.assertTrue(
                homePage.waitForUrlContains("/electronics"),
                "Expected Electronics page, but actual URL is: " + homePage.getCurrentUrl());


        log.info("Step 5: Scroll to Shop by Brand section");

        homePage.scrollToShopByBrand();

        Assert.assertTrue(
                homePage.isShopByBrandDisplayed(),
                "Shop by Brand section should be displayed");

        homePage.scrollToSamsungBrand();

        Assert.assertTrue(
                homePage.isSamsungBrandDisplayed(),
                "Samsung brand image should be displayed in Shop by Brand");


        log.info("Step 6: Select Samsung brand");

        homePage.clickSamsungBrand();

        Assert.assertTrue(
                homePage.waitForUrlContains("/egypt-en/samsung/"),
                "Expected Samsung products page, but actual URL is: " + homePage.getCurrentUrl());


        log.info("Step 7: Verify filter sidebar is displayed");

        Assert.assertTrue(
                productPage.isFilterSidebarDisplayed(),
                "Filter sidebar should be displayed on Samsung products page");


        log.info("Step 8: Verify Samsung is selected in Brand sidebar");

        Assert.assertTrue(
                productPage.isSamsungSelected(),
                "Samsung should be selected in the Brand sidebar");


        log.info("Step 9: Open Price filter");

        productPage.clickPriceFilter();

        Assert.assertTrue(
                productPage.isMinimumPriceFieldDisplayed(),
                "Minimum price field should be displayed");

        Assert.assertTrue(
                productPage.isMaximumPriceFieldDisplayed(),
                "Maximum price field should be displayed");

        log.info(
                "Step 10: Apply price filter: minimum = {}, maximum = {}",
                minimumPrice,
                maximumPrice
        );

        String minimumPriceText = String.valueOf((int) minimumPrice);
        String maximumPriceText = String.valueOf((int) maximumPrice);

        productPage.applyPriceFilter(minimumPriceText, maximumPriceText);

        Assert.assertEquals(
                productPage.getMinimumPriceValue(),
                minimumPriceText,
                "Minimum price should match the CSV test data"
        );

        Assert.assertEquals(
                productPage.getMaximumPriceValue(),
                maximumPriceText,
                "Maximum price should match the CSV test data"
        );

        log.info("Step 11: Open Product Rating filter");

        productPage.clickRatingFilter();

        Assert.assertTrue(
                productPage.isRatingSliderDisplayed(),
                "Product Rating slider should be displayed");


        log.info("Step 12: Select rating {}", minimumRating);

        productPage.selectRating(minimumRating);

        Assert.assertEquals(
                Double.parseDouble(productPage.getRatingSliderValue()),
                minimumRating,
                0.001,
                "Rating slider should match the CSV test data");


        log.info("Step 13: Verify Product Rating label");

        String ratingLabel = productPage.getRatingLabel();

        String expectedRatingText = minimumRating == Math.floor(minimumRating) ? String.valueOf((int) minimumRating) : String.valueOf(minimumRating);
        Assert.assertTrue(
                ratingLabel.contains(expectedRatingText),
                "Rating label should reflect the selected rating. Expected: "
                        + expectedRatingText
                        + ", Actual: "
                        + ratingLabel
        );


        log.info("Step 14: Wait for filtered product results");

        productPage.waitForProductCards();


        log.info("Step 15: Verify filtered product results");

        List<WebElement> products = productPage.getCurrentProductCards();

        Assert.assertFalse(
                products.isEmpty(),
                "No visible product cards are displayed after applying filters"
        );

        int totalProducts = products.size();

        log.info(
                "Number of visible products to verify on the current page: {}",
                totalProducts
        );

        List<String> validationErrors = new ArrayList<>();
        int verifiedProducts = 0;


        for (int i = 0; i < totalProducts; i++) {

            int productNumber = i + 1;

            log.info(
                    "========== Validating Product {} ==========",
                    productNumber
            );

            try {

                Map<String, Object> productData =
                        productPage.getProductDataAtIndex(i);

                String name = (String) productData.get("name");

                double price = (double) productData.get("price");

                double rating = (double) productData.get("rating");

                log.info(
                        "Product {}: Name={}, Price={}, Rating={}",
                        productNumber,
                        name,
                        price,
                        rating
                );

                boolean isSamsung = name
                        .toLowerCase(ROOT)
                        .contains("samsung");

                if (!isSamsung) {
                    validationErrors.add(
                            "Product " + productNumber
                                    + " does not contain Samsung in its name: "
                                    + name
                    );
                }

                if (price < minimumPrice || price > maximumPrice) {
                    validationErrors.add(
                            "Product " + productNumber
                                    + " has an out-of-range price. Expected: "
                                    + minimumPrice + " - " + maximumPrice
                                    + ", Actual: " + price
                    );
                }


                if (rating < minimumRating) {
                    validationErrors.add(
                            "Product " + productNumber
                                    + " has a rating below the minimum. Expected: "
                                    + minimumRating
                                    + ", Actual: " + rating
                    );
                }

                boolean productPassed =
                        isSamsung
                                && price >= minimumPrice
                                && price <= maximumPrice
                                && rating >= minimumRating;

                if (productPassed) {
                    verifiedProducts++;

                    log.info(
                            "Product {} PASSED all validations",
                            productNumber
                    );
                } else {
                    log.warn(
                            "Product {} FAILED validation",
                            productNumber
                    );
                }

            } catch (RuntimeException e) {

                String errorMessage =
                        "Product " + productNumber
                                + " could not be validated. Reason: "
                                + e.getClass().getSimpleName()
                                + ": " + e.getMessage();

                validationErrors.add(errorMessage);

                log.error(
                        "Failed to validate product {}",
                        productNumber,
                        e
                );
            }
        }

        log.info("========== FINAL VALIDATION SUMMARY ==========");

        log.info("Total products displayed: {}", totalProducts);

        log.info("Products passing all validations: {}", verifiedProducts);

        log.info("Validation errors: {}", validationErrors.size());

        for (String error : validationErrors) {
            log.error("VALIDATION FAILURE: {}", error);
        }

        Assert.assertTrue(
                validationErrors.isEmpty(),
                "Some products do not match the selected filters:\n"
                        + String.join("\n", validationErrors)
        );

        log.info(
                "========== PASSED: All {} products match the expected criteria ==========",
                totalProducts
        );
    }
}