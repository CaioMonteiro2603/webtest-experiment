package SunaGPT20b.ws02.seq10;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String VALID_USER = "caio@gmail.com";
    private static final String VALID_PASS = "123";

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(
                ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']")));

        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(VALID_USER, VALID_PASS);
        // Successful login redirects to overview page
        wait.until(ExpectedConditions.urlContains("/parabank/overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/overview.htm"),
                "URL should contain '/parabank/overview.htm' after successful login");

        // Verify a known element on the overview page
        WebElement welcomeMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
        Assertions.assertTrue(welcomeMsg.getText().contains("Welcome"),
                "Overview page should contain a welcome message");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user@example.com", "wrongpass");
        // Expect to stay on login page and see an error message
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
        WebElement errorMsg = driver.findElement(By.id("rightPanel"));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("error"),
                "Error message should be displayed for invalid credentials");
    }

    @Test
    @Order(3)
    public void testHomePageLinksOneLevelDeep() throws MalformedURLException {
        // Ensure we are logged in
        login(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("/parabank/overview.htm"));

        // Collect all unique hrefs from the current page
        List<String> hrefs = new ArrayList<>();
        List<WebElement> linkElements = driver.findElements(By.tagName("a"));
        for (WebElement link : linkElements) {
            String href = link.getAttribute("href");
            if (href != null && !href.trim().isEmpty() && !hrefs.contains(href)) {
                hrefs.add(href);
            }
        }

        String baseDomain = "parabank.parasoft.com";

        for (String href : hrefs) {
            if (href.contains(baseDomain)) {
                // Internal link – navigate and verify page loads
                driver.navigate().to(href);
                wait.until(ExpectedConditions.titleContains("parabank"));
                Assertions.assertFalse(driver.getTitle().isEmpty(),
                        "Internal page title should not be empty for URL: " + href);
                // Return to overview page for next iteration
                driver.navigate().back();
                wait.until(ExpectedConditions.urlContains("/parabank/overview.htm"));
            } else {
                // External link – open in new tab, verify domain, then close
                String originalWindow = driver.getWindowHandle();
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
                Set<String> windows = driver.getWindowHandles();
                windows.remove(originalWindow);
                String newWindow = windows.iterator().next();
                driver.switchTo().window(newWindow);
                wait.until(ExpectedConditions.titleContains("parabank"));
                Assertions.assertTrue(driver.getCurrentUrl().contains(new java.net.URL(href).getHost()),
                        "External link should navigate to expected domain: " + href);
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }
}