package driverFactory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class GetFirefoxDriver {
    private static WebDriver driver = null;
    public static WebDriver getFirefoxDriver()
    {
        if(driver==null)
        {
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.addArguments("-private");
            driver = new FirefoxDriver(firefoxOptions);
        }
       return driver;
    }

    public  static void quietDriver()
    {
        if(driver!=null)
        {
            driver.quit();
        }
        driver = null;
    }
}
