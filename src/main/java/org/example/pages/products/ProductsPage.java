package org.example.pages.products;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class ProductsPage extends BasePage {

    private static final Logger log = LogManager.getLogger(ProductsPage.class);

    private static final String TITLE = "h2[@data-qa='plp-product-box-name']";
    private static final String LOWER_TEXT = "translate(normalize-space(.)," + "'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')";

    private final By productNames = By.cssSelector("[data-qa='plp-product-box-name']");
    private final By productPrice = By.cssSelector("[data-qa='plp-product-box-price']");
    private final By productPriceValue = By.cssSelector("[data-qa='plp-product-box-price'] strong");
    private final By freeDeliveryLabel = By.xpath(".//*[not(*)][contains(" + LOWER_TEXT + ",'free delivery')]");



    public ProductsPage(WebDriver driver) {
        super(driver);
    }



    private String xpathLiteral(String value) {

        if (!value.contains("'")) {
            return "'" + value + "'";
        }

        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }

        String[] parts = value.split("'", -1);

        return "concat('" + String.join("', \"'\", '", parts) + "')";
    }


    private String normalizeSpaces(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }


    private String cardXpath(String productName) {
        return "//" + TITLE
                + "[normalize-space(@title)=" + xpathLiteral(productName) + "]"
                + "/ancestor::*[count(.//" + TITLE + ")=1]"
                + "[.//*[@data-qa='plp-product-box-price']][last()]";
    }

    private By productCard(String productName) {
        return By.xpath(cardXpath(productName));
    }

    private By addToCartButton(String productName) {
        return By.xpath(
                cardXpath(productName)
                        + "//button[.//img[@alt='add-to-cart']]"
        );
    }



    public List<String> getProductNames() {

        List<String> names = new ArrayList<>();

        for (WebElement element : waitForElements(productNames)) {

            String name = element.getAttribute("title");

            if (name == null || name.isBlank()) {
                name = element.getText();
            }

            name = normalizeSpaces(name);

            if (!name.isEmpty()) {
                names.add(name);
            }
        }

        log.info("Retrieved {} product names", names.size());

        return names;
    }


    public WebElement getProductCard(String productName) {

        List<WebElement> cards =
                driver.findElements(productCard(productName));

        if (cards.isEmpty()) {
            throw new NoSuchElementException(
                    "Product card not found: " + productName
            );
        }

        return cards.get(0);
    }

    public double getProductPrice(String productName) {

        WebElement card = getProductCard(productName);

        List<WebElement> current = card.findElements(productPriceValue);

        String priceText;

        if (!current.isEmpty()) {

            priceText = current.get(0).getText();
        } else {
            List<WebElement> container = card.findElements(productPrice);

            if (container.isEmpty()) {
                throw new NoSuchElementException(
                        "Price not found for: " + productName
                );
            }

            priceText = container.get(0).getText();
        }

        double price = parsePrice(priceText);

        log.info("Product: {} | Price: {} EGP", productName, price);

        return price;
    }

    public WebElement getFreeDeliveryLabel(String productName) {

        for (WebElement label : getProductCard(productName)
                .findElements(freeDeliveryLabel)) {

            if (label.isDisplayed()) {
                return label;
            }
        }

        throw new NoSuchElementException(
                "Free Delivery label not found for: " + productName
        );
    }


    public boolean hasFreeDelivery(String productName) {

        try {
            boolean result = getProductCard(productName)
                    .findElements(freeDeliveryLabel)
                    .stream()
                    .anyMatch(WebElement::isDisplayed);

            log.info("Product: {} | Free Delivery: {}", productName, result);

            return result;

        } catch (NoSuchElementException | StaleElementReferenceException e) {

            log.warn(
                    "Could not verify Free Delivery for '{}': {}",
                    productName,
                    e.getMessage()
            );

            return false;
        }
    }


    public Map<String, Double> selectProductsWithinBudget(
            double budget, double minimumBudget, int minItems) {

        return selectProducts(budget, minimumBudget, minItems, false);
    }


    public Map<String, Double> selectFreeDeliveryProductsWithinBudget(
            double budget, double minimumBudget, int minItems) {

        return selectProducts(budget, minimumBudget, minItems, true);
    }

    private Map<String, Double> collectProducts(
            double maxPrice, boolean freeDeliveryOnly) {

        Map<String, Double> products = new LinkedHashMap<>();

        for (String name : new LinkedHashSet<>(getProductNames())) {

            try {
                if (freeDeliveryOnly && !hasFreeDelivery(name)) {
                    continue;
                }

                double price = getProductPrice(name);

                if (price > 0 && price <= maxPrice) {
                    products.put(name, price);
                }

            } catch (RuntimeException e) {
                log.warn("Skipping product '{}': {}", name, e.getMessage());
            }
        }

        return products;
    }

    private Map<String, Double> selectProducts(
            double budget,
            double minimumBudget,
            int minItems,
            boolean freeDeliveryOnly) {

        String kind = freeDeliveryOnly ? "Free Delivery " : "";

        Map<String, Double> available =
                collectProducts(budget, freeDeliveryOnly);

        log.info(
                "Found {} {}products within the budget",
                available.size(),
                kind
        );

        if (available.size() < minItems) {
            throw new AssertionError(
                    "Not enough " + kind + "products. Required: "
                            + minItems + ", found: " + available.size()
            );
        }


        List<Map.Entry<String, Double>> candidates = new ArrayList<>(available.entrySet());

        candidates.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        List<Map.Entry<String, Double>> selected = new ArrayList<>();

        boolean found = findCombination(
                candidates,
                0,
                minItems,
                0.0,
                minimumBudget,
                budget,
                selected,
                new long[]{0}
        );

        if (!found) {
            throw new AssertionError(
                    "Could not find " + minItems + " " + kind
                            + "products with a total between "
                            + minimumBudget + " and " + budget
            );
        }

        Map<String, Double> result = new LinkedHashMap<>();

        for (Map.Entry<String, Double> product : selected) {
            result.put(product.getKey(), product.getValue());
        }

        double total = result.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        log.info("Selected {}products: {}", kind, result);
        log.info("Selected products total: {} EGP", total);

        return result;
    }


    private boolean findCombination(
            List<Map.Entry<String, Double>> items,
            int start,
            int needed,
            double total,
            double min,
            double max,
            List<Map.Entry<String, Double>> selected,
            long[] visitedNodes) {

        if (++visitedNodes[0] > 500_000) {
            return false;
        }

        if (needed == 0) {
            return total >= min - 1e-9 && total <= max + 1e-9;
        }

        int n = items.size();

        for (int i = start; i <= n - needed; i++) {


            double best = total;
            for (int k = i; k < i + needed; k++) {
                best += items.get(k).getValue();
            }

            if (best < min - 1e-9) {
                return false;
            }

            double next = total + items.get(i).getValue();


            double cheapest = next;
            for (int k = n - (needed - 1); k < n; k++) {
                cheapest += items.get(k).getValue();
            }

            if (cheapest > max + 1e-9) {
                continue;
            }

            selected.add(items.get(i));

            if (findCombination(
                    items, i + 1, needed - 1, next,
                    min, max, selected, visitedNodes)) {
                return true;
            }

            selected.remove(selected.size() - 1);
        }

        return false;
    }


    public void addProductToCart(String productName) {

        log.info("Adding product to cart: {}", productName);

        By buttonLocator = addToCartButton(productName);

        WebElement card = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        productCard(productName)
                )
        );

        scrollToElement(card);

        wait.until(ExpectedConditions.elementToBeClickable(buttonLocator));

        clickElement(buttonLocator);

        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                    .until(ExpectedConditions
                            .invisibilityOfElementLocated(buttonLocator));

            log.info("Add-to-cart confirmed for: {}", productName);

        } catch (TimeoutException e) {
            log.warn("Add-to-cart button did not disappear for: {}", productName);
        }
    }



    private double parsePrice(String priceText) {

        String normalized = priceText
                .replace(",", "")
                .replaceAll("[^0-9.]", "");

        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    "Cannot parse product price: " + priceText
            );
        }

        return Double.parseDouble(normalized);
    }
}