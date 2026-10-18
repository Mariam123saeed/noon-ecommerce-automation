package driverFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.slf4j.LoggerFactory;

public class GetEdgeDriver extends DriverFactory {
    private static final Logger log = LogManager.getLogger(GetEdgeDriver.class);

    private static WebDriver driver = null;

    public static WebDriver getDriver() {
        if (driver == null) {
            log.info("Creating new Edge driver with --incognito");
            EdgeOptions options = new EdgeOptions();
            options.addArguments("--incognito");
            try {
                driver = new EdgeDriver(options);
                log.debug("Edge driver created");
            } catch (WebDriverException e) {
                log.error("Failed to create Edge driver. Check Edge and msedgedriver versions", e);
                throw e;
            }
        } else {
            log.debug("Reusing existing Edge driver");
        }
        return driver;
    }

    public static void quitDriver() {
        if (driver != null) {
            log.info("Quitting Edge driver");
            try {
                driver.quit();
                log.debug("Edge driver quit successfully");
            } catch (WebDriverException e) {
                log.error("Error while quitting Edge driver", e);
                throw e;
            } finally {
                driver = null;
            }
        } else {
            log.debug("quitDriver called but no driver is active");
        }
    }
}