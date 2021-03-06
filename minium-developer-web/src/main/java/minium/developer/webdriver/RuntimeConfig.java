package minium.developer.webdriver;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;

public class RuntimeConfig {

    private static OS currentOS = new OS();

    @Value("${app.home:.}")
    private File homedir;

    private static final String SELENIUM_URL = "https://selenium-release.storage.googleapis.com/";
    private static final String CHROME_DRIVER_URL = "https://chromedriver.storage.googleapis.com/LATEST_RELEASE";
    private static final String GECKO_RELEASES_URL = "https://github.com/mozilla/geckodriver/releases";

    public static OS getOS() {
        return currentOS;
    }

    public String getDriverPath() {
        return getDriversDir().getPath();
    }

    protected File getDriversDir() {
        File driverDir = homedir == null ? null : new File(homedir, "drivers");
        return driverDir != null && driverDir.exists() && driverDir.isDirectory() ? driverDir : null;
    }

    public static String getSeleniumUrl() {
        return SELENIUM_URL;
    }

    public static String getChromeDriverUrl() {
        return CHROME_DRIVER_URL;
    }

    public static String getGeckoDriverReleasesUrl() {
        return GECKO_RELEASES_URL;
    }
}
