package minium.developer.service;

import java.io.IOException;

import minium.developer.webdriver.ChromeDriverDownloader;
import minium.developer.webdriver.DriverLocator;
import minium.developer.webdriver.IEDriverDownloader;
import minium.developer.webdriver.PhantomJSDownloader;
import minium.developer.webdriver.RuntimeConfig;
import minium.developer.webdriver.WebDriverRelease;
import minium.developer.webdriver.WebDriverReleaseManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebDriverService {

    @Autowired
    private DriverLocator driverLocator;

    private WebDriverReleaseManager releaseManager;
    private ChromeDriverDownloader chromeDownloader;
    private IEDriverDownloader IEDownloader;
    private PhantomJSDownloader phantomJSDownloader;


    public WebDriverService() {
        driverLocator = new DriverLocator();
        releaseManager = RuntimeConfig.getReleaseManager();
    }

    public void webDriverExists(String browserName) throws IOException {
        if (!driverLocator.webDriverExists(browserName)) {
            // download the webdrivers
            switch (browserName) {
            case "chrome":
                downloadChromeDriver();
                break;
            case "internet explorer":
                downloadIEDriver();
                break;
            case "phantomjs":
                downloadPhantomJs();
                break;
            default:
                break;
            }
        }
    }

    protected void downloadChromeDriver() throws IOException {
        WebDriverRelease chromeDriverLatestVersion = releaseManager.getChromeDriverLatestVersion();
        String chromeVersion = chromeDriverLatestVersion.getPrettyPrintVersion(".");
        chromeDownloader = new ChromeDriverDownloader(chromeVersion, driverLocator.getDriversDir().getPath());
        chromeDownloader.download();
    }

    protected void downloadPhantomJs() throws IOException {
        phantomJSDownloader = new PhantomJSDownloader("1.9.8", driverLocator.getDriversDir().getPath() );
        phantomJSDownloader.download();
    }

    protected void downloadIEDriver() throws IOException {
        WebDriverRelease ieDriverLatestVersion = releaseManager.getIeDriverLatestVersion();
        String version = ieDriverLatestVersion.getPrettyPrintVersion(".");
        IEDownloader = new IEDriverDownloader(version, driverLocator.getDriversDir().getPath());
        IEDownloader.download();
    }

    /**
     * Download the webdriver
     * Only download if webdriver doesn't exists
     * @throws IOException
     */
    public void downloadAllWebDrivers() throws IOException {

        if (RuntimeConfig.getOS().isWindows() && !driverLocator.webDriverExists("internet explorer")) {
            downloadIEDriver();
        }

        if (!driverLocator.webDriverExists("phantomjs")) {
            downloadPhantomJs();
        }

        if (!driverLocator.webDriverExists("chrome")) {
            downloadChromeDriver();
        }

    }

    /**
     * Update the version of the webdrivers
     *  Even if the webdrivers exists they will updated
     * @throws IOException
     */
    public void updateAllWebDrivers() throws IOException {

        if (RuntimeConfig.getOS().isWindows()) {
            downloadIEDriver();
        }

        downloadPhantomJs();
        downloadChromeDriver();
    }

}