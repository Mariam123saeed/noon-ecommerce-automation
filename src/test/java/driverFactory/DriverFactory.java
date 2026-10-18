package driverFactory;


import org.openqa.selenium.WebDriver;


public class DriverFactory {

    public static WebDriver getWebDriver(String browserName) throws IllegalAccessException {
        WebDriver driver;
        switch(browserName.toLowerCase())
        {
            case "chrome":
                driver = GetChromeDriver.getChromeDriver();
                break;

            case "firefox":
                driver = GetFirefoxDriver.getFirefoxDriver();
                break;

            default:
                throw new IllegalAccessException("Invalid browser name:"+browserName);
        }

        return driver;
    }

    public static void quitWebDriver(String browserName) throws IllegalAccessException {


        switch(browserName.toLowerCase())
        {
            case "chrome":
                GetChromeDriver.quietDriver();
                break;

            case "firefox":
                GetFirefoxDriver.quietDriver();
                break;

            default:
                throw new IllegalAccessException("Invalid browser name:"+browserName);
        }

    }
}
