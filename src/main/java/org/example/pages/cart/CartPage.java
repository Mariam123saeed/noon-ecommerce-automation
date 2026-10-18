package org.example.pages.cart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class CartPage extends BasePage {

    private static final Logger log = LogManager.getLogger(CartPage.class);

    private static final String LOWER_TEXT = "translate(normalize-space(.)," + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')";

    private final By cartProductNames = By.cssSelector("a[data-qa='cart-product-name'], a[class*='_titleLink_']");
    private final By cartProductPrice = By.cssSelector("[data-qa='item-unit-price']");
    private final By cartQuantity = By.cssSelector("input[data-qa='item-quantity-input']");
    private final By freeShippingLabel = By.xpath(".//*[not(*)][contains(" + LOWER_TEXT + ",'free shipping')]");
    private final By cartSubtotal = By.xpath(
            "//*[contains(@class,'_summaryContent_')]" +
                    "//*[contains(@class,'_invoiceRow_')]" +
                    "[.//*[normalize-space()='Subtotal']]" +
                    "//*[contains(@class,'_amount_')]/b"
    );


    private static final String ROW_FROM_LINK =
            "./ancestor::*[count(.//a[@data-qa='cart-product-name' "
                    + "or contains(@class,'_titleLink_')])=1][last()]";


    public CartPage(WebDriver driver) {
        super(driver);
    }



    private boolean hasVisibleCartItem() {

        try {
            return driver.findElements(cartProductNames)
                    .stream()
                    .anyMatch(WebElement::isDisplayed);

        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    private boolean isErrorPage() {

        Object text = ((JavascriptExecutor) driver).executeScript(
                "return document.body ? document.body.innerText : '';"
        );

        return text != null
                && text.toString().contains("Something went wrong");
    }

    public void waitForCartToLoad() {

        waitForUrlContains("/cart");

        for (int attempt = 1; attempt <= 3; attempt++) {

            try {
                new WebDriverWait(driver, Duration.ofSeconds(15))
                        .until(d -> hasVisibleCartItem() || isErrorPage());

            } catch (TimeoutException e) {
                log.warn("Cart did not show items or an error within 15s");
            }

            if (hasVisibleCartItem()) {
                log.info("Cart items are loaded and visible (attempt {})", attempt);
                return;
            }

            if (!isErrorPage()) {
                break;
            }

            if (attempt < 3) {
                log.warn("Cart page shows an error, refreshing ({}/3)", attempt);
                driver.navigate().refresh();
            }
        }

        throw new AssertionError(
                "Cart items did not load. URL: " + getCurrentUrl()
        );
    }



    public List<String> getCartProductNames() {

        for (int attempt = 1; attempt <= 3; attempt++) {

            try {
                List<String> names = new ArrayList<>();

                for (WebElement link : driver.findElements(cartProductNames)) {

                    String name = link.getText().trim();

                    if (!name.isBlank()) {
                        names.add(name);
                    }
                }

                if (!names.isEmpty()) {
                    log.info("Cart product names: {}", names);
                    return names;
                }

            } catch (StaleElementReferenceException e) {
                log.warn("Cart changed while reading names. Attempt {}/3", attempt);
            }

            waitForRetry();
        }

        throw new AssertionError(
                "Could not retrieve cart product names after 3 attempts"
        );
    }

    public double getProductPrice(String productName) {

        return readCartProductValue(productName, row -> {
            double price = parsePrice(
                    row.findElement(cartProductPrice).getText()
            );

            log.info("Cart product: {} | Price: {} EGP", productName, price);

            return price;
        });
    }

    public int getProductQuantity(String productName) {

        return readCartProductValue(productName, row -> {
            int quantity = Integer.parseInt(
                    row.findElement(cartQuantity).getAttribute("value").trim()
            );

            log.info("Cart product: {} | Quantity: {}", productName, quantity);

            return quantity;
        });
    }

    public double getCartSubtotal() {

        double subtotal = parsePrice(getText(cartSubtotal));

        log.info("Cart subtotal: {} EGP", subtotal);

        return subtotal;
    }

    public WebElement getFreeShippingLabel(String productName) {

        return readCartProductValue(productName, row -> {

            for (WebElement label : row.findElements(freeShippingLabel)) {
                if (label.isDisplayed()) {
                    return label;
                }
            }

            throw new NoSuchElementException(
                    "Free Shipping label not found for: " + productName
            );
        });
    }



    public boolean isProductEligibleForFreeShipping(String productName) {

        boolean eligible = readCartProductValue(productName, row ->
                row.findElements(freeShippingLabel)
                        .stream()
                        .anyMatch(WebElement::isDisplayed)
        );

        log.info("Cart product: {} | Free Shipping: {}", productName, eligible);

        return eligible;
    }



    private WebElement getCartRow(String productName) {

        String expected = normalizeProductName(productName);

        for (WebElement link : driver.findElements(cartProductNames)) {

            if (normalizeProductName(link.getText()).equals(expected)) {
                return link.findElement(By.xpath(ROW_FROM_LINK));
            }
        }

        throw new AssertionError("Product not found in cart: " + productName);
    }

    private <T> T readCartProductValue(
            String productName,
            Function<WebElement, T> reader) {

        for (int attempt = 1; attempt <= 3; attempt++) {

            try {
                return reader.apply(getCartRow(productName));

            } catch (StaleElementReferenceException e) {
                log.warn(
                        "Cart changed while reading '{}'. Attempt {}/3",
                        productName,
                        attempt
                );
            }

            waitForRetry();
        }

        throw new AssertionError(
                "Could not read cart value after 3 attempts: " + productName
        );
    }

    private double parsePrice(String priceText) {

        String normalized = priceText
                .replace(",", "")
                .replaceAll("[^0-9.]", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    "Cannot parse price: " + priceText
            );
        }

        return Double.parseDouble(normalized);
    }

    public String normalizeProductName(String name) {

        if (name == null) {
            return "";
        }

        return name
                .replace('\u00A0', ' ')
                .replaceAll("[\\u200B-\\u200D\\uFEFF]", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void waitForRetry() {

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Interrupted while waiting to retry cart operation", e
            );
        }
    }
}