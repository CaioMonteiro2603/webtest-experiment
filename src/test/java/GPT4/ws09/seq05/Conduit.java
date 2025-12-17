package GPT4.ws09.seq05;

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
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("banner")));
        Assertions.assertTrue(banner.isDisplayed(), "Banner should be visible on home page");
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("conduit"), "Title should contain 'Conduit'");
    }

    @Test
    @Order(2)
    public void testNavigateToSignIn() {
        driver.get(BASE_URL);
        WebElement signIn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        signIn.click();
        wait.until(ExpectedConditions.urlContains("login"));
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        Assertions.assertTrue(emailField.isDisplayed(), "Email input should be visible");
    }

    @Test
    @Order(3)
    public void testSignInWithInvalidCredentials() {
        driver.get(BASE_URL + "#/login");
        WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys("invalid@example.com");
        password.sendKeys("wrongpassword");
        signInButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("email or password is invalid"),
                "Should show error for invalid credentials");
    }

    @Test
    @Order(4)
    public void testExternalFooterLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='github.com']")));
        ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", githubLink.getAttribute("href"));
        wait.until(d -> driver.getWindowHandles().size() > 1);

        Set<String> windows = driver.getWindowHandles();
        for (String window : windows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(d -> d.getCurrentUrl().startsWith("https://"));
                String url = driver.getCurrentUrl();
                Assertions.assertTrue(url.contains("github.com"), "External link should lead to GitHub");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(5)
    public void testGlobalFeedTabPresence() {
        driver.get(BASE_URL);
        WebElement globalFeed = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Global Feed")));
        globalFeed.click();
        WebElement articles = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("article-preview")));
        Assertions.assertTrue(articles.isDisplayed(), "Articles should be displayed in Global Feed");
    }

    @Test
    @Order(6)
    public void testArticleLinksWork() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Global Feed"))).click();
        List<WebElement> articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".article-preview a.preview-link")));
        Assertions.assertFalse(articles.isEmpty(), "There should be at least one article");

        WebElement firstArticle = articles.get(0);
        String href = firstArticle.getAttribute("href");
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstArticle);
        wait.until(ExpectedConditions.elementToBeClickable(firstArticle)).click();
        wait.until(ExpectedConditions.urlContains(href.split("#/")[1]));
        WebElement articlePage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("article-page")));
        Assertions.assertTrue(articlePage.isDisplayed(), "Should navigate to article page");
    }

    @Test
    @Order(7)
    public void testNavigateToSignUp() {
        driver.get(BASE_URL);
        WebElement signUp = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign up")));
        signUp.click();
        wait.until(ExpectedConditions.urlContains("register"));
        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Username']")));
        Assertions.assertTrue(username.isDisplayed(), "Username field should be visible on register page");
    }

    @Test
    @Order(8)
    public void testPopularTagsSection() {
        driver.get(BASE_URL);
        WebElement tagList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("tag-list")));
        List<WebElement> tags = tagList.findElements(By.tagName("a"));
        Assertions.assertTrue(tags.size() > 0, "There should be at least one popular tag");
    }

    @Test
    @Order(9)
    public void testClickPopularTagFiltersArticles() {
        driver.get(BASE_URL);
        WebElement tagList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("tag-list")));
        List<WebElement> tags = tagList.findElements(By.tagName("a"));
        WebElement firstTag = tags.get(0);
        String tagName = firstTag.getText().trim();
        firstTag.click();

        WebElement activeTab = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".nav-pills .nav-link.active")));
        Assertions.assertEquals(tagName, activeTab.getText().trim(), "Selected tag should be active");
        WebElement articles = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("article-preview")));
        Assertions.assertTrue(articles.isDisplayed(), "Articles should be filtered by selected tag");
    }

}
