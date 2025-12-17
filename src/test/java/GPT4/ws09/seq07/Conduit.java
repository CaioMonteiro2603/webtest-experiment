package GPT4.ws09.seq07;

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
public class Conduit {

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
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("banner")));
        Assertions.assertTrue(banner.isDisplayed(), "Banner should be visible");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("conduit"), "Title should contain 'conduit'");
    }

    @Test
    @Order(2)
    public void testNavigateToSignInPage() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='#login']")));
        signInLink.click();
        WebElement signInHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Sign In']")));
        Assertions.assertTrue(signInHeader.isDisplayed(), "Sign In header should be visible");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#login"), "URL should contain '#login'");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys("invalid@example.com");
        password.sendKeys("wrongpassword");
        loginBtn.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error-messages")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("email or password is invalid"), "Should show login error");
    }

    @Test
    @Order(4)
    public void testValidLogin() {
        driver.get(BASE_URL + "#login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        email.clear();
        password.clear();
        email.sendKeys("demo@demo.com");
        password.sendKeys("demo");
        loginBtn.click();

        WebElement userLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href*='@demo']")));
        Assertions.assertTrue(userLink.isDisplayed(), "Username link should appear in navbar after login");
    }

    @Test
    @Order(5)
    public void testGlobalFeedIsVisible() {
        driver.get(BASE_URL);
        WebElement globalFeed = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Global Feed']")));
        globalFeed.click();

        WebElement articlePreview = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("article-preview")));
        Assertions.assertTrue(articlePreview.isDisplayed(), "At least one article preview should be visible");
    }

    @Test
    @Order(6)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".preview-link")));
        articleLink.click();

        WebElement articlePage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("article-page")));
        Assertions.assertTrue(articlePage.isDisplayed(), "Article page should load correctly");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#article"), "URL should contain '#article'");
    }

    @Test
    @Order(7)
    public void testFooterTwitterLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        driver.switchTo().window(windows.iterator().next());

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "External Twitter page should open");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testFooterFacebookLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement fbLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        fbLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        driver.switchTo().window(windows.iterator().next());

        wait.until(ExpectedConditions.urlContains("facebook.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "External Facebook page should open");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testFooterLinkedInLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement liLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        liLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        driver.switchTo().window(windows.iterator().next());

        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "External LinkedIn page should open");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    public void testLogout() {
        driver.get(BASE_URL);
        List<WebElement> logoutLinks = driver.findElements(By.xpath("//a[text()='Log out']"));
        if (logoutLinks.size() > 0) {
            logoutLinks.get(0).click();
            WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='#login']")));
            Assertions.assertTrue(signIn.isDisplayed(), "Sign in link should appear after logout");
        } else {
            Assertions.fail("Logout link not found. User may not be logged in.");
        }
    }
}
