package org.example.pages.home;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pages.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage extends BasePage {

    private static final Logger log = LogManager.getLogger(HomePage.class);

    private final By noonLogo = By.id("btn_logo");
    private final By searchBar = By.id("search-input");
    private final By cartLink = By.cssSelector("a[data-qa='btn_cartLink-Header-Desktop']");
    private final By electronicsNavBar = By.xpath(
            "//*[@id='default-header-desktop']" +
                    "/div[1]/div/div/div/div/div/div[2]/ul/li[1]/a/span");
    private final By electronicsSection =  By.xpath("//h2[normalize-space()='Electronics']");
    private final By electronicsViewAllButton = By.xpath(
            "//h2[normalize-space()='Electronics']" +
                    "/ancestor::*[.//a[.//div[normalize-space()='View all']]][1]" +
                    "//a[.//div[normalize-space()='View all']]");;
    private final By shopByBrandIcon = By.xpath(
            "//*[@id='v-sensor-component-19-1-module-bannerModuleStrip-Banner Module Strip']" +
                    "/div[1]/div/span/div/div/img");
    private final By samsungBrandImage = By.cssSelector("img[alt*='/samsung?sort']");
    private final By searchErrorMessage = By.xpath("//h3[normalize-space()='An error has occurred']");
    private final By productResults = By.cssSelector("[data-qa='plp-product-box-name']");


    public HomePage(WebDriver driver) {
        super(driver);
    }


    public WebElement getSearchBar() {
        return findElement(searchBar);
    }
    public WebElement getElectronicsSection() {
        return findElement(electronicsSection);
    }

    public String getSearchBarValue() {
        return getAttribute(searchBar, "value");
    }

    public String getSearchErrorMessage() {
        return getText(searchErrorMessage);
    }

    public void enterSearchText(String searchText) {

        log.info("Entering search text: {}", searchText);

        sendKeysToElement(searchBar, searchText);
    }

    public void clickSearch() {

        log.info("Submitting the search");

        pressKey(searchBar, Keys.ENTER);
    }


    public void clickElectronicsViewAll() {
        log.info("Clicking View All in Electronics section");

        clickElement(electronicsViewAllButton);

        log.info("Clicked View All in Electronics section");
    }

    public void clickSamsungBrand() {

        log.info("Clicking Samsung brand image");

        WebElement samsungImage = findElement(samsungBrandImage);

        clickElement(samsungBrandImage);

        log.info("Clicked Samsung brand image successfully");
    }

    public void scrollToElectronicsSection() {
        log.info("Scrolling to Electronics section");

        scrollToElement(getElectronicsSection());

        log.info("Successfully scrolled to Electronics section");
    }

    public void scrollToShopByBrand() {

        log.info("Scrolling to Shop By Brand section");
        log.info(
                "Shop by brand icon found: {}",
                driver.findElements(shopByBrandIcon).size()
        );

        WebElement shopByBrand = findElementPresent(shopByBrandIcon);

        scrollToElement(shopByBrand);

        log.info("Scrolled to Shop by brand icon");
    }

    public void scrollToSamsungBrand() {

        log.info("Scrolling to Samsung brand image");
        log.info(
                "Samsung image elements found: {}",
                driver.findElements(samsungBrandImage).size()
        );

        WebElement samsungImage = findElementPresent(samsungBrandImage);

        scrollToElement(samsungImage);

        log.info("Scrolled to Samsung brand image");
    }


    public boolean isNoonLogoDisplayed() {
        return isElementDisplayed(noonLogo);
    }

    public boolean isSearchBarDisplayed() {
        return isElementDisplayed(searchBar);
    }

    public boolean isSearchErrorDisplayed() {return isElementDisplayed(searchErrorMessage);}

    public boolean isElectronicsSectionDisplayed() {
        return isElementDisplayed(electronicsSection);
    }

    public boolean isElectronicsViewAllDisplayed() {
        return isElementDisplayed(electronicsViewAllButton);
    }

    public boolean isShopByBrandDisplayed(){
        return isElementDisplayed(shopByBrandIcon);
    }

    public boolean isSamsungBrandDisplayed() {
        return isElementDisplayed(samsungBrandImage);
    }


    private void submitSearch(String searchText) {

        WebElement search = getSearchBar();

        search.clear();
        search.sendKeys(searchText);
        search.sendKeys(Keys.ENTER);
    }

    private boolean waitForProductResults(Duration timeout) {

        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(productResults));
            return true;

        } catch (TimeoutException e) {
            return false;
        }
    }

    public void searchProduct(String searchText) {

        log.info("Searching for: {}", searchText);

        submitSearch(searchText);

        if (waitForProductResults(Duration.ofSeconds(45))) {
            return;
        }

        log.warn("Search results did not load within 45s. URL: {}. Retrying once",
                getCurrentUrl());

        if (getCurrentUrl().contains("/search")) {
            driver.navigate().refresh();   // results page opened but stayed empty
        } else {
            submitSearch(searchText);      // the Enter press did not register
        }

        if (!waitForProductResults(Duration.ofSeconds(45))) {
            throw new TimeoutException(
                    "Search results for '" + searchText
                            + "' did not load. URL: " + getCurrentUrl());
        }
    }

    public void openCart() {
        wait.until(ExpectedConditions.elementToBeClickable(cartLink)).click();
    }


}