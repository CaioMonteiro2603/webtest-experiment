package GPT4.ws07.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JSFIDDLE {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement logo = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a.header-logo")));
        Assertions.assertTrue(logo.isDisplayed(), "Logo should be displayed on homepage");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"), "Page title should contain 'JSFiddle'");
    }

    @Test
    @Order(2)
    public void testNewFiddleButtonExistsAndWorks() {
        driver.get(BASE_URL);
        WebElement newFiddleBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/']")));
        newFiddleBtn.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Clicking 'New' should keep you on the base URL");
    }

    @Test
    @Order(3)
    public void testLoginLinkNavigatesToLoginPage() {
        driver.get(BASE_URL);
        WebElement loginLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log in")));
        loginLink.click();
        wait.until(ExpectedConditions.urlContains("/auth/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/auth/login"), "URL should contain /auth/login after clicking login");
        WebElement loginHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(loginHeader.getText().toLowerCase().contains("log in"), "Login page should have appropriate header");
    }

    @Test
    @Order(4)
    public void testExternalLinkGitHub() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com']")));
        String originalWindow = driver.getWindowHandle();
        githubLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "GitHub link should open github.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testExternalLinkTwitter() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open twitter.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testExternalLinkTermsOfService() {
        driver.get(BASE_URL);
        WebElement tosLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Terms of Service")));
        tosLink.click();
        wait.until(ExpectedConditions.urlContains("/terms/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/terms/"), "Terms of Service page should load");
        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(title.getText().toLowerCase().contains("terms"), "Terms page should have appropriate title");
    }

    @Test
    @Order(7)
    public void testExternalLinkPrivacyPolicy() {
        driver.get(BASE_URL);
        WebElement privacyLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Privacy Policy")));
        privacyLink.click();
        wait.until(ExpectedConditions.urlContains("/privacy/"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/privacy/"), "Privacy Policy page should load");
        WebElement title = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        Assertions.assertTrue(title.getText().toLowerCase().contains("privacy"), "Privacy page should have appropriate title");
    }
}
