package GPT4.ws09.seq06;

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
public class RealWorldUITest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("Conduit"));
        Assertions.assertTrue(driver.getTitle().contains("Conduit"), "Home page title should contain 'Conduit'");
    }

    @Test
    @Order(2)
    public void testSignInPageLoads() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#login']")));
        signInLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#login"), "URL should contain '#login'");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.clear();
        emailField.sendKeys("invalid@example.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.clear();
        passwordField.sendKeys("invalidpass");
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("email or password"), "Error message should indicate login failure");
    }

    @Test
    @Order(4)
    public void testValidLoginAndLogout() {
        driver.get(BASE_URL + "#login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        emailField.clear();
        emailField.sendKeys("demo@demo.com");
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        passwordField.clear();
        passwordField.sendKeys("demo");
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();
        WebElement navProfile = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.nav-link[href='#@demo']")));
        Assertions.assertTrue(navProfile.getText().contains("demo"), "Should be logged in and see username in nav");

        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#settings']")));
        settingsLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger")));
        logoutBtn.click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.nav-link[href='#login']")));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.nav-link[href='#login']")).isDisplayed(), "Login link should appear after logout");
    }

    @Test
    @Order(5)
    public void testNavigateToGlobalFeed() {
        driver.get(BASE_URL);
        WebElement globalFeedTab = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Global Feed')]")));
        globalFeedTab.click();
        WebElement articlePreview = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.article-preview")));
        Assertions.assertTrue(articlePreview.isDisplayed(), "At least one article preview should be visible in Global Feed");
    }

    @Test
    @Order(6)
    public void testArticleNavigation() {
        testNavigateToGlobalFeed();
        List<WebElement> articles = driver.findElements(By.cssSelector("div.article-preview a.preview-link"));
        if (!articles.isEmpty()) {
            String articleTitle = articles.get(0).getText().trim();
            articles.get(0).click();
            wait.until(ExpectedConditions.urlContains("#article"));
            WebElement articlePageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
            Assertions.assertTrue(articlePageTitle.getText().contains(articleTitle) || articlePageTitle.isDisplayed(), "Should navigate to article detail page");
        } else {
            Assertions.fail("No articles found in Global Feed to test navigation");
        }
    }

    @Test
    @Order(7)
    public void testFooterGitHubExternalLink() {
        driver.get(BASE_URL);
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='github.com/gothinkster']")));
        String originalWindow = driver.getWindowHandle();
        githubLink.click();
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "External GitHub link should open in new tab and go to GitHub");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}
