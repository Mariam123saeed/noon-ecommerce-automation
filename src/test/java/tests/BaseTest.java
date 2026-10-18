package tests;

import driverFactory.DriverFactory;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import utils.ConfigHandler;
import utils.ScreenShot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    public static WebDriver driver;
    public String browserName = "chrome";
    public ConfigHandler configHandler;



    @BeforeMethod
    public void setUp() throws IllegalAccessException {

        log.info("========== Test Setup Started ==========");
        log.info("Loading configuration file");

        configHandler = new ConfigHandler("src/main/resources/config.properties");
        browserName = configHandler.getProperty("browser");

        log.info("Browser selected: {}", browserName);
        String url = configHandler.getProperty("url");

        log.info("Application URL: {}", url);
        log.info("Creating WebDriver");

        driver = DriverFactory.getWebDriver(browserName);

        log.info("WebDriver created successfully");

        log.info("Opening application");

        driver.get(url);
        log.info("Application opened successfully");
        log.info("Maximizing browser window");
        driver.manage().window().maximize();
        log.info("========== Test Setup Completed ==========");
    }

    // ==============================
    // Tear Down
    // ==============================

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) throws IllegalAccessException {

        log.info("========== Test Tear Down Started ==========");

        try {

            if (result.getStatus() == ITestResult.FAILURE && driver != null) {

                log.error(
                        "Test failed: {}",
                        result.getMethod().getMethodName(),
                        result.getThrowable()
                );

                File image = ScreenShot.takeScreenShot(driver);

                if (image != null && image.exists()) {

                    try (FileInputStream fis = new FileInputStream(image)) {

                        Allure.addAttachment(
                                "Failure Screenshot - "
                                        + result.getMethod().getMethodName(),
                                "image/png",
                                fis,
                                "png"
                        );
                    }
                } else {
                    log.error("Failed to capture screenshot");
                }
            }

        } catch (Exception e) {

            log.error("Error while capturing or attaching screenshot", e);

        } finally {

            if (driver != null) {

                DriverFactory.quitWebDriver(browserName);
                driver = null;

                log.info("WebDriver closed successfully");

            } else {

                log.warn("WebDriver is already null");
            }

            log.info("========== Test Tear Down Completed ==========");
        }
    }
}