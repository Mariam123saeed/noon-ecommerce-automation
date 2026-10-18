package tests.products;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.example.pages.cart.CartPage;
import org.example.pages.home.HomePage;
import org.example.pages.products.ProductsPage;

import org.testng.Assert;
import org.testng.annotations.Test;

import tests.BaseTest;
import tests.dataProvider.DataProviderTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddMultipleProductsTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(AddMultipleProductsTest.class);
    private static final double PRICE_ROUNDING_TOLERANCE = 0.51;
    private HomePage homePage;
    private ProductsPage productsPage;
    private CartPage cartPage;

    @Test(dataProvider = "addMultipleProductsData", dataProviderClass = DataProviderTest.class)
    public void verifyAddMultipleProductsToCart(String searchText, double budget, double tolerancePercent, int minItems) {

        log.info("========== START: Add Multiple Products Test ==========");

        homePage = new HomePage(driver);
        productsPage = new ProductsPage(driver);
        cartPage = new CartPage(driver);



        log.info("Step 1: Verify home page");

        Assert.assertTrue(
                homePage.getCurrentUrl().contains("noon.com"),
                "Home page should be opened"
        );

        Assert.assertTrue(
                homePage.isSearchBarDisplayed(),
                "Search bar should be displayed"
        );



        log.info("Step 2: Search for products: {}", searchText);

        homePage.searchProduct(searchText);

        Assert.assertTrue(
                homePage.waitForUrlContains("/search"),
                "Search results page was not opened"
        );



        log.info("Step 3: Validate test data");

        Assert.assertTrue(
                budget > 0,
                "Budget must be greater than zero"
        );

        Assert.assertTrue(
                tolerancePercent >= 0 && tolerancePercent < 100,
                "Tolerance must be between 0 and 100"
        );

        Assert.assertTrue(
                minItems >= 1,
                "Minimum number of products must be at least 1"
        );

        double minimumBudget =
                budget * (1 - tolerancePercent / 100.0);

        log.info("Acceptable budget range: {} - {} EGP", minimumBudget, budget);



        log.info("Step 4: Verify search results");

        List<String> displayedProductNames =
                productsPage.getProductNames();

        Assert.assertFalse(
                displayedProductNames.isEmpty(),
                "No products were displayed for: " + searchText
        );

        log.info(
                "Number of displayed products: {}",
                displayedProductNames.size()
        );



        log.info("Step 5: Select products within the budget");

        Map<String, Double> selectedProducts =
                productsPage.selectProductsWithinBudget(
                        budget,
                        minimumBudget,
                        minItems
                );

        Assert.assertTrue(
                selectedProducts.size() >= minItems,
                "Not enough products were selected"
        );

        log.info("Selected products: {}", selectedProducts);



        log.info("Step 6: Verify selected products total");

        double expectedSubtotal = selectedProducts.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        log.info(
                "Expected subtotal: {} EGP",
                expectedSubtotal
        );

        Assert.assertTrue(
                expectedSubtotal >= minimumBudget
                        && expectedSubtotal <= budget,
                "Selected products total is outside the budget range"
        );



        log.info("Step 7: Add selected products to cart");

        for (String productName : selectedProducts.keySet()) {

            log.info("Adding product to cart: {}", productName);

            productsPage.addProductToCart(productName);
        }



        log.info("Step 8: Open shopping cart");

        homePage.openCart();



        log.info("Step 9: Wait for cart items to load");

        cartPage.waitForCartToLoad();

        List<String> cartProductNames =
                cartPage.getCartProductNames();

        log.info("Cart products: {}", cartProductNames);

        Assert.assertFalse(
                cartProductNames.isEmpty(),
                "Cart should contain products"
        );



        log.info("Step 10: Verify the number of products in cart");

        Assert.assertTrue(
                cartProductNames.size() >= minItems,
                "Expected at least " + minItems
                        + " products, but found "
                        + cartProductNames.size()
        );


        double cartItemsTotal = 0.0;
        log.info("Step 11: Verify product names, prices, and quantities");

        List<String> validationErrors = new ArrayList<>();

        int verifiedProducts = 0;

        for (Map.Entry<String, Double> product : selectedProducts.entrySet()) {

            String expectedName = product.getKey();
            double expectedPrice = product.getValue();

            log.info("Validating product: {}", expectedName);

            try {


                String normalizedExpectedName =
                        cartPage.normalizeProductName(expectedName);

                boolean productExists = cartProductNames.stream()
                        .map(cartPage::normalizeProductName)
                        .anyMatch(normalizedExpectedName::equals);

                if (!productExists) {

                    validationErrors.add(
                            "Product not found in cart: " + expectedName
                    );

                    log.error(
                            "Product not found in cart: {}",
                            expectedName
                    );

                    continue;
                }



                double actualPrice = cartPage.getProductPrice(expectedName);
                boolean priceMatches = Math.abs(actualPrice - expectedPrice) <= PRICE_ROUNDING_TOLERANCE;

                if (!priceMatches) {

                    validationErrors.add(
                            "Price mismatch for " + expectedName
                                    + ". Expected: " + expectedPrice
                                    + ", Actual: " + actualPrice
                    );

                    log.error(
                            "Price mismatch for '{}': expected={}, actual={}",
                            expectedName,
                            expectedPrice,
                            actualPrice
                    );
                }



                int actualQuantity = cartPage.getProductQuantity(expectedName);
                boolean quantityMatches = actualQuantity == 1;

                cartItemsTotal += actualPrice * actualQuantity;

                if (!quantityMatches) {

                    validationErrors.add(
                            "Quantity mismatch for " + expectedName
                                    + ". Expected: 1"
                                    + ", Actual: " + actualQuantity
                    );

                    log.error(
                            "Quantity mismatch for '{}': expected=1, actual={}",
                            expectedName,
                            actualQuantity
                    );
                }



                if (priceMatches && quantityMatches) {

                    verifiedProducts++;

                    log.info(
                            "Product PASSED: {} | Price: {} | Quantity: {}",
                            expectedName,
                            actualPrice,
                            actualQuantity
                    );

                } else {

                    log.warn(
                            "Product FAILED: {}",
                            expectedName
                    );
                }

            } catch (RuntimeException | AssertionError e) {

                validationErrors.add("Could not validate product: " + expectedName + ". Reason: " + e.getMessage());
                log.error(
                        "Failed to validate product: {}",
                        expectedName,
                        e
                );
            }
        }



        log.info("Step 12: Verify cart subtotal");

        double actualSubtotal = cartPage.getCartSubtotal();

        double subtotalTolerance = PRICE_ROUNDING_TOLERANCE * selectedProducts.size();


        if (Math.abs(actualSubtotal - expectedSubtotal) > subtotalTolerance) {
            validationErrors.add(
                    "Cart subtotal mismatch. Expected (rounded): " + expectedSubtotal
                            + ", Actual: " + actualSubtotal
            );
        }


        if (Math.abs(actualSubtotal - cartItemsTotal) > 0.01) {
            validationErrors.add(
                    "Cart subtotal does not match the sum of cart items. Items total: "
                            + cartItemsTotal + ", Subtotal: " + actualSubtotal
            );
        }



        log.info("Step 13: Verify subtotal is within budget");

        if (actualSubtotal < minimumBudget || actualSubtotal > budget) {

            validationErrors.add(
                    "Cart subtotal is outside the budget range. "
                            + "Expected: " + minimumBudget
                            + " - " + budget
                            + ", Actual: " + actualSubtotal
            );
        }



        log.info("========== FINAL VALIDATION SUMMARY ==========");

        log.info(
                "Selected products: {}",
                selectedProducts.size()
        );

        log.info(
                "Verified products: {}",
                verifiedProducts
        );

        log.info(
                "Expected subtotal: {} EGP",
                expectedSubtotal
        );

        log.info(
                "Actual subtotal: {} EGP",
                actualSubtotal
        );

        log.info(
                "Validation errors: {}",
                validationErrors.size()
        );

        for (String error : validationErrors) {
            log.error("VALIDATION FAILURE: {}", error);
        }

        Assert.assertTrue(
                validationErrors.isEmpty(),
                "Cart validation failed:\n"
                        + String.join("\n", validationErrors)
        );

        log.info(
                "========== PASSED: All {} products validated ==========",
                selectedProducts.size()
        );
    }
}