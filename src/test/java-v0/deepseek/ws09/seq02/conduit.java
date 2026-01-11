package deepseek.ws09.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "realworlduser";
    private static final String PASSWORD = "password123";

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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".navbar-brand")));
        Assertions.assertTrue(header.isDisplayed(),
            "Expected home page header to be visible");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL + "login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        signInButton.click();

        WebElement userProfile = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".user-pic")));
        Assertions.assertTrue(userProfile.isDisplayed(),
            "Expected user profile after login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys("invalid@email.com");
        password.sendKeys("wrongpass");
        signInButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMessage.isDisplayed(),
            "Expected error message for invalid login");
    }

    @Test
    @Order(4)
    public void testArticleCreation() {
        login();
        WebElement newArticle = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".ion-compose")));
        newArticle.click();

        WebElement title = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Article Title']")));
        WebElement description = driver.findElement(
            By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        WebElement body = driver.findElement(
            By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        WebElement publishButton = driver.findElement(
            By.cssSelector("button[type='button']"));

        title.sendKeys("Test Article");
        description.sendKeys("This is a test description");
        body.sendKeys("This is the article body content");
        publishButton.click();

        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Test Article", articleTitle.getText(),
            "Expected article title to match");
    }

    @Test
    @Order(5)
    public void testGlobalFeedNavigation() {
        login();
        WebElement globalFeed = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(),'Global Feed')]")));
        globalFeed.click();

        WebElement articles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.cssSelector(".article-preview"))).get(0);
        Assertions.assertTrue(articles.isDisplayed(),
            "Expected articles in global feed");
    }

    @Test
    @Order(6)
    public void testProfileNavigation() {
        login();
        WebElement profile = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".nav-link[href*='@realworlduser']")));
        profile.click();

        WebElement profileHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".user-info h4")));
        Assertions.assertTrue(profileHeader.getText().contains("realworlduser"),
            "Expected to navigate to user profile");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter']")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter"),
            "Expected to be on Twitter");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testPopularTags() {
        driver.get(BASE_URL);
        WebElement tag = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".tag-list a")));
        String tagText = tag.getText();
        tag.click();

        WebElement tagHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".feed-toggle .nav-link.active")));
        Assertions.assertTrue(tagHeader.getText().contains(tagText),
            "Expected to filter by selected tag");
    }

    private void login() {
        if (driver.findElements(By.cssSelector(".user-pic")).size() == 0) {
            driver.get(BASE_URL + "login");
            WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='email']")));
            email.sendKeys(USERNAME);
            driver.findElement(By.cssSelector("input[type='password']")).sendKeys(PASSWORD);
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".user-pic")));
        }
    }
}