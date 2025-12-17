package GPT4.ws02.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio1@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String username, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("username"))).clear();
        driver.findElement(By.name("username")).sendKeys(username);
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("input[type='submit']")).click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        WebElement accountOverviewLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Accounts Overview")));
        Assertions.assertTrue(accountOverviewLink.isDisplayed(), "Accounts Overview link should be visible after login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_pass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("error"), "Error message should be displayed for invalid login.");
    }

    @Test
    @Order(3)
    public void testNavigateAllLinksOneLevel() {
        login(USERNAME, PASSWORD);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href^='activity.htm'],a[href^='services'],a[href^='contact.htm'],a[href^='about.htm'],a[href^='admin.htm'],a[href^='accounts'],a[href^='loan'],a[href^='transfer'],a[href^='billpay'],a[href^='requestloan'],a[href^='updateprofile'],a[href^='logout.htm']"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href == null || href.trim().isEmpty()) continue;
            try {
                link.click();
                wait.until(ExpectedConditions.urlContains(".htm"));
                Assertions.assertTrue(driver.getCurrentUrl().contains(".htm"), "URL should contain '.htm' after navigating: " + href);
                driver.navigate().back();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Accounts Overview")));
            } catch (Exception e) {
                Assertions.fail("Failed to navigate to: " + href);
            }
        }
    }

    @Test
    @Order(4)
    public void testExternalLinksFooter() {
        driver.get(BASE_URL);
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("ul.footer a"));
        String originalWindow = driver.getWindowHandle();
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.trim().isEmpty()) continue;
            String domain = href.contains("facebook") ? "facebook.com"
                           : href.contains("twitter") ? "twitter.com"
                           : href.contains("linkedin") ? "linkedin.com"
                           : null;
            if (domain == null) continue;
            link.click();
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String handle : windows) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain), "External link should navigate to " + domain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    public void testLogout() {
        login(USERNAME, PASSWORD);
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "After logout, URL should contain 'index.htm'");
    }
}
