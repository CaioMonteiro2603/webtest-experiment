package GPT5.ws09.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    // ---------------------- Helpers ----------------------

    private void openBase() {
        driver.get(BASE_URL);
        waitDocumentReady();
        // The app typically redirects to a hash route (#/)
        wait.until(d -> driver.getCurrentUrl().startsWith(BASE_URL));
        dismissOverlaysIfAny();
    }

    private void waitDocumentReady() {
        try {
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
        } catch (Exception ignored) {}
    }

    private void dismissOverlaysIfAny() {
        // Best-effort cookie banners / modals
        List<By> candidates = Arrays.asList(
                By.cssSelector("button[id*='accept'],button[class*='accept'],button[aria-label*='''); 