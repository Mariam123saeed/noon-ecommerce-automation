package driverFactory;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class GetChromeDriver {
    private static WebDriver driver = null;
    public static WebDriver getChromeDriver()
    {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--incognito");
            driver = new ChromeDriver(options);
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
