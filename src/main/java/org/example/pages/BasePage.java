package org.example.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BasePage {

    protected final Logger log = LogManager.getLogger(getClass());

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public WebDriver getDriver() {
        return driver;
    }



    public WebElement findElement(By locator) {

        log.debug("Waiting for element: {}", locator);

        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));

        log.debug("Element is visible: {}", locator);

        return element;
    }

    public WebElement findElement(By locator, Duration duration) {

        log.debug("Waiting for element: {} with timeout: {} seconds", locator, duration.toSeconds());

        return new WebDriverWait(driver, duration)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement findElementPresent(By locator) {

        log.debug("Waiting for element to be present in DOM: {}", locator);

        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public List<WebElement> waitForElements(By locator) {

        log.debug("Waiting for all elements: {}", locator);

        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public void clickElement(By locator) {

        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));

        log.info("Clicking element: {}", locator);

        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Click intercepted, retrying with JavaScript click: {}", locator);
            jsClick(element);
        }
    }

    public void jsClick(WebElement element) {

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    public void sendKeysToElement(By locator, String text) {

        WebElement element = findElement(locator);

        element.clear();
        element.sendKeys(text);

        log.info("Entered text '{}' into element: {}", text, locator);
    }

    public void pressKey(By locator, Keys key) {

        findElement(locator).sendKeys(key);

        log.debug("Pressed key {} on element: {}", key.name(), locator);
    }

    public String getText(By locator) {

        return findElement(locator).getText().trim();
    }

    public String getAttribute(By locator, String attributeName) {

        return findElement(locator).getAttribute(attributeName);
    }


    public boolean isElementDisplayed(By locator) {

        try {
            return findElement(locator).isDisplayed();
        } catch (TimeoutException e) {
            log.debug("Element is not visible: {}", locator);
            return false;
        }
    }



    public String getCurrentUrl() {

        return driver.getCurrentUrl();
    }

    public boolean waitForUrlContains(String fragment) {

        log.debug("Waiting for URL to contain: {}", fragment);

        try {
            return wait.until(ExpectedConditions.urlContains(fragment));
        } catch (TimeoutException e) {
            log.warn("URL did not contain '{}'. Current URL: {}", fragment, getCurrentUrl());
            return false;
        }
    }

    public boolean waitForUrlChange(String oldUrl, Duration timeout) {

        try {
            return new WebDriverWait(driver, timeout)
                    .until(d -> !d.getCurrentUrl().equals(oldUrl));
        } catch (TimeoutException e) {
            return false;
        }
    }


    public void scrollToElement(WebElement element) {

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", element);
    }


}