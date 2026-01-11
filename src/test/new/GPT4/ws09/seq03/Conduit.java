package GPT4.ws09.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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

    @BeforeEach
    public void navigateHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.navbar-brand")));
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        Assertions.assertTrue(driver.getTitle().contains("Conduit"), "Home page title should contain 'Conduit'");
        WebElement brand = driver.findElement(By.cssSelector("a.navbar-brand"));
        Assertions.assertTrue(brand.isDisplayed(), "Navbar brand is not visible");
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        List<WebElement> navLinks = driver.findElements(By.cssSelector("ul.navbar-nav li a"));
        Assertions.assertTrue(navLinks.size() > 0, "No navigation links found");
        for (WebElement link : navLinks) {
            if (!link.getText().isEmpty() && link.isDisplayed()) {
                continue;
            }
            if (link.getText().isEmpty() && link.isDisplayed()) {
                continue;
            }
        }
    }

    @Test
    @Order(3)
    public void testSignInPageLoads() {
        driver.findElement(By.linkText("Sign in")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.text-xs-center")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"), "Did not navigate to login page");
        Assertions.assertEquals("Sign in", driver.findElement(By.cssSelector("h1.text-xs-center")).getText(), "Sign In header not found");
    }

    @Test
    @Order(4)
    public void testInvalidLogin() {
        driver.findElement(By.linkText("Sign in")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']"))).sendKeys("invalid@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-message")));
            Assertions.assertTrue(error.getText().toLowerCase().contains("email or password") || error.getText().toLowerCase().contains("invalid"), "Expected error message for invalid login");
        } catch (TimeoutException e) {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
            Assertions.assertTrue(error.getText().toLowerCase().contains("email or password") || error.getText().toLowerCase().contains("invalid"), "Expected error message for invalid login");
        }
    }

    @Test
    @Order(5)
    public void testExternalGitHubLink() {
        WebElement githubLink = driver.findElement(By.cssSelector("a[href*='github.com']"));
        String originalWindow = driver.getWindowHandle();
        githubLink.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"), "Did not navigate to GitHub");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testSignUpPageLoads() {
        driver.findElement(By.linkText("Sign up")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.text-xs-center")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("register"), "Did not navigate to register page");
        Assertions.assertEquals("Sign up", driver.findElement(By.cssSelector("h1.text-xs-center")).getText(), "Sign Up header not found");
    }

    @Test
    @Order(7)
    public void testGlobalFeedLoads() {
        WebElement globalFeedTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.nav-link")));
        if (!globalFeedTab.getText().contains("Global Feed")) {
            List<WebElement> navLinks = driver.findElements(By.cssSelector("a.nav-link"));
            for (WebElement link : navLinks) {
                if (link.getText().contains("Global Feed")) {
                    globalFeedTab = link;
                    break;
                }
            }
        }
        Assertions.assertTrue(globalFeedTab.getText().contains("Global Feed") || globalFeedTab.getText().contains("Global"), "Global Feed tab not found");

        List<WebElement> articlePreviews = driver.findElements(By.cssSelector(".article-preview"));
        Assertions.assertTrue(articlePreviews.size() > 0, "No articles found in Global Feed");
    }

    @Test
    @Order(8)
    public void testArticleNavigation() {
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        Assertions.assertTrue(articles.size() > 0, "No article titles found");
        String title = articles.get(0).findElement(By.tagName("h1")).getText();
        articles.get(0).findElement(By.tagName("h1")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals(title, driver.findElement(By.cssSelector("h1")).getText(), "Article title did not match after navigation");
    }

    @Test
    @Order(9)
    public void testClickTag() {
        List<WebElement> tags = driver.findElements(By.cssSelector(".tag-list a"));
        if (tags.size() == 0) {
            Assumptions.abort("No tags found on homepage to test tag filtering.");
        }
        String tagText = tags.get(0).getText();
        tags.get(0).click();
        WebElement activeTag = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".feed-toggle .nav-link.active")));
        Assertions.assertTrue(activeTag.getText().contains(tagText), "Tag feed not activated correctly");
        List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
        Assertions.assertTrue(articles.size() > 0, "No articles shown for selected tag");
    }
}