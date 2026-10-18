package org.example.pages.products;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pages.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductFilterPage extends BasePage {

    private static final Logger log = LogManager.getLogger(ProductFilterPage.class);

    private final By filterSidebar = By.id("catalog-page-container");
    private final By samsungCheckbox = By.id("facet-brand-samsung");
    private final By priceFilterButton = By.xpath("//h3[normalize-space()='Price']/ancestor::button");
    private final By minimumPriceField = By.cssSelector("input[data-qa='filter-min-input']");
    private final By maximumPriceField = By.cssSelector("input[data-qa='filter-max-input']");
    private final By priceGoButton = By.xpath(
            "//h3[normalize-space()='Price']" +
                    "/ancestor::div[contains(@class,'_container_')]" +
                    "//button[@type='submit']");
    private final By ratingFilterButton = By.xpath("//h3[normalize-space()='Product Rating']/ancestor::button");
    private final By ratingSlider = By.cssSelector("div[class*='_container_ideeh_'] input[type='range']");
    private final By ratingText = By.cssSelector("div[class*='_container_ideeh_'] span[class*='_rating_']");
    private final By productCards = By.cssSelector("div[class*='_wrapper_t6oh0_1']");
    private final By productName = By.cssSelector("[data-qa='plp-product-box-name']");
    private final By productPrice = By.cssSelector("[data-qa='plp-product-box-price'] strong");
    private final By productRating = By.cssSelector("div[class*='_textCtr_6uyte_16']");



    public ProductFilterPage(WebDriver driver) {
        super(driver);
    }



    public WebElement getSamsungCheckbox() {
        return findElement(samsungCheckbox);
    }

    public WebElement getRatingSlider() {
        return findElement(ratingSlider);
    }

    public String getMinimumPriceValue() {
        return getAttribute(minimumPriceField, "value");
    }

    public String getMaximumPriceValue() {
        return getAttribute(maximumPriceField, "value");
    }

    public String getRatingSliderValue() {
        return getAttribute(ratingSlider, "value");
    }

    public String getRatingLabel() {
        return getText(ratingText);
    }



    public void enterMinimumPrice(String minimumPrice) {
        sendKeysToElement(minimumPriceField, minimumPrice);
    }

    public void enterMaximumPrice(String maximumPrice) {
        sendKeysToElement(maximumPriceField, maximumPrice);
    }



    public void clickPriceFilter() {
        clickElement(priceFilterButton);
    }

    public void clickPriceGoButton() {
        clickElement(priceGoButton);
    }

    public void clickRatingFilter() {
        clickElement(ratingFilterButton);
    }



    public boolean isFilterSidebarDisplayed() {
        return isElementDisplayed(filterSidebar);
    }

    public boolean isMinimumPriceFieldDisplayed() {
        return isElementDisplayed(minimumPriceField);
    }

    public boolean isMaximumPriceFieldDisplayed() {
        return isElementDisplayed(maximumPriceField);
    }

    public boolean isRatingSliderDisplayed() {
        return isElementDisplayed(ratingSlider);
    }

    public boolean isSamsungSelected() {

        boolean selected = getSamsungCheckbox().isSelected();

        log.info("Samsung brand selected: {}", selected);

        return selected;
    }



    public void applyPriceFilter(String minimumPrice, String maximumPrice) {

        log.info("Applying price filter: minimum={}, maximum={}", minimumPrice, maximumPrice);

        enterMinimumPrice(minimumPrice);
        enterMaximumPrice(maximumPrice);
        clickPriceGoButton();

        log.info("Price filter submitted successfully");
    }

    public void selectRating(double targetRating) {

        log.info("Selecting rating: {}", targetRating);

        WebElement slider = getRatingSlider();

        double min = Double.parseDouble(slider.getAttribute("min"));
        double max = Double.parseDouble(slider.getAttribute("max"));
        double step = Double.parseDouble(slider.getAttribute("step"));

        log.info("Rating slider range: min={}, max={}, step={}", min, max, step);

        if (targetRating < min || targetRating > max) {
            throw new IllegalArgumentException("Rating must be between " + min + " and " + max);
        }

        double stepsFromMin = (targetRating - min) / step;

        if (Math.abs(stepsFromMin - Math.round(stepsFromMin)) > 0.001) {
            throw new IllegalArgumentException(
                    "Rating " + targetRating + " does not match the slider step " + step);
        }

        // Closed loop: read the value, move toward the target, verify, repeat.
        for (int round = 1; round <= 5; round++) {

            double current = currentRatingValue();

            int stepsToMove = (int) Math.round((targetRating - current) / step);

            if (stepsToMove == 0) {
                log.info("Rating slider is at {} (round {})", current, round);
                return;
            }

            Keys key = stepsToMove > 0 ? Keys.ARROW_RIGHT : Keys.ARROW_LEFT;

            log.info("Round {}: current={}, moving {} step(s) {}",
                    round, current, Math.abs(stepsToMove), key.name());

            for (int i = 0; i < Math.abs(stepsToMove); i++) {

                String before = readSliderValueNow();

                pressKey(ratingSlider, key);

                waitForRatingValueChange(before);
            }
        }

        double finalValue = currentRatingValue();

        if (Math.abs(finalValue - targetRating) > 0.001) {
            throw new AssertionError(
                    "Could not set the rating slider to " + targetRating
                            + ". Actual value: " + finalValue);
        }

        log.info("Rating slider is at {}", finalValue);
    }

    /** Reads the slider value without waiting (returns "" if it is re-rendering). */
    private String readSliderValueNow() {

        try {
            List<WebElement> sliders = driver.findElements(ratingSlider);

            if (sliders.isEmpty()) {
                return "";
            }

            String value = sliders.get(0).getAttribute("value");

            return value == null ? "" : value;

        } catch (StaleElementReferenceException e) {
            return "";
        }
    }

    private double currentRatingValue() {

        String value = wait.until(d -> {
            String v = readSliderValueNow();
            return v.isBlank() ? null : v;
        });

        return Double.parseDouble(value);
    }

    private void waitForRatingValueChange(String previousValue) {

        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(d -> {
                        String now = readSliderValueNow();
                        return !now.isBlank() && !now.equals(previousValue);
                    });

        } catch (TimeoutException e) {
            log.debug("Slider value did not change after the key press (previous={})", previousValue);
        }
    }

    public List<WebElement> waitForProductCards() {

        log.info("Waiting for the product list to finish loading");

        int[] lastCount = {-1};
        int[] stablePolls = {0};

        List<WebElement> cards = wait.until(d -> {

            List<WebElement> current = getCurrentProductCards();

            if (current.isEmpty()) {
                lastCount[0] = -1;
                stablePolls[0] = 0;
                return null;
            }

            if (current.size() == lastCount[0]) {
                stablePolls[0]++;
            } else {
                lastCount[0] = current.size();
                stablePolls[0] = 0;
            }

            return stablePolls[0] >= 2 ? current : null;
        });

        log.info("Product list is stable. Visible product cards: {}", cards.size());

        return cards;
    }

    public List<WebElement> getCurrentProductCards() {

        try {
            List<WebElement> cards = driver.findElements(productCards);
            List<WebElement> visibleProducts = new ArrayList<>();

            for (WebElement card : cards) {
                try {
                    if (card.isDisplayed()) {
                        visibleProducts.add(card);
                    }
                } catch (StaleElementReferenceException e) {
                    log.debug("Product card became stale while checking visibility");
                }
            }

            log.debug("Total product card elements: {}, Visible product cards: {}",
                    cards.size(), visibleProducts.size());

            return visibleProducts;

        } catch (StaleElementReferenceException e) {
            log.warn("Product list changed while retrieving cards. Returning empty list");
            return Collections.emptyList();
        }
    }


    public Map<String, Object> getProductDataAtIndex(int index) {

        return wait.until(d -> {
            try {
                List<WebElement> cards = getCurrentProductCards();

                if (index < 0 || index >= cards.size()) {
                    return null;
                }

                WebElement card = cards.get(index);

                String name = card.findElement(productName).getText().trim();
                String priceText = card.findElement(productPrice).getText().trim();

                double rating = 0.0;
                List<WebElement> ratingElements = card.findElements(productRating);

                if (!ratingElements.isEmpty()) {
                    String text = ratingElements.get(0).getText().trim();

                    if (!text.isEmpty()) {
                        rating = parseNumber(text);
                    }
                }

                Map<String, Object> productData = new HashMap<>();
                productData.put("name", name);
                productData.put("price", parseNumber(priceText));
                productData.put("rating", rating);

                log.debug("Product {} read: name={}, price={}, rating={}",
                        index + 1, name, productData.get("price"), rating);

                return productData;

            } catch (StaleElementReferenceException | NoSuchElementException e) {
                log.debug("Product {} changed while reading, retrying ({})",
                        index + 1, e.getClass().getSimpleName());
                return null;
            }
        });
    }

    public int getDisplayedProductsCount() {

        int count = (int) driver.findElements(productName)
                .stream()
                .filter(WebElement::isDisplayed)
                .count();

        log.info("Displayed products count: {}", count);

        return count;
    }

    private double parseNumber(String text) {

        Matcher matcher = Pattern.compile("\\d+(?:\\.\\d+)?").matcher(text.replace(",", ""));

        if (!matcher.find()) {
            throw new IllegalStateException("No number found in text: '" + text + "'");
        }

        return Double.parseDouble(matcher.group());
    }
}