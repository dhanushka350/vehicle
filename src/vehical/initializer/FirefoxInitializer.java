/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vehical.initializer;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class FirefoxInitializer {

    private static FirefoxDriver driver;

    private FirefoxInitializer() {
        System.setProperty("webdriver.gecko.driver", "/var/lib/tomcat8/geckodriver");
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");
        FirefoxOptions options = new FirefoxOptions();
//        options.setHeadless(true);

        driver = new FirefoxDriver(options);
    }

    public static synchronized FirefoxDriver getDriver() {
        if (driver == null) {
            new FirefoxInitializer();
        }
        return driver;
    }

    public static void refresh() {
        driver.navigate().refresh();
    }
}
