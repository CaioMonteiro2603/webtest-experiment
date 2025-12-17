package deepseek.ws09.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser@example.com";
    private static final String PASSWORD = "password123";
    private static WebDriverWait wait;

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
    public void testHomePageLoad() {
        driver.get(BASE_URL);
        WebElement banner = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".banner")));
        Assertions.assertTrue(banner.isDisplayed(), "Home page banner should be visible");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get(BASE_URL + "#/login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        signInButton.click();

        WebElement userAvatar = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("nav.navbar img.user-pic")));
        Assertions.assertTrue(userAvatar.isDisplayed(), "User should be logged in and avatar visible");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        WebElement email = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[type='email']")));
        WebElement password = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        email.sendKeys("invalid@email.com");
        password.sendKeys("wrongpassword");
        signInButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message should be shown for invalid login");
    }

    @Test
    @Order(4)
    public void testArticleCreation() {
        testValidLogin();
        WebElement newPostLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='editor']")));
        newPostLink.click();

        WebElement title = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Article Title']")));
        WebElement description = driver.findElement(By.cssSelector("input[placeholder*='article']"));
        WebElement body = driver.findElement(By.cssSelector("textarea[placeholder*='article']"));
        WebElement publishButton = driver.findElement(By.cssSelector("button[type='button']"));

        title.sendKeys("Test Article Title");
        description.sendKeys("This is a test article description");
        body.sendKeys("This is the body of the test article");
        publishButton.click();

        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Test Article Title", articleTitle.getText(), "Article should be created successfully");
    }

    @Test
    @Order(5)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".article-preview h1")));
        String expectedTitle = articleLink.getText();
        articleLink.click();

        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals(expectedTitle, articleTitle.getText(), "Should navigate to correct article");
    }

    @Test
    @Order(6)
    public void testTagSelection() {
        driver.get(BASE_URL);
        WebElement tagLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".tag-list a")));
        String tagName = tagLink.getText();
        tagLink.click();

        WebElement feedToggle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".feed-toggle li.active")));
        Assertions.assertTrue(feedToggle.getText().contains(tagName), "Should show articles for selected tag");
    }

    @Test
    @Order(7)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        WebElement mediumLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='medium.com']")));
        String originalWindow = driver.getWindowHandle();
        mediumLink.click();

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("medium.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testProfileNavigation() {
        testValidLogin();
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("nav.navbar a[href*='profile']")));
        profileLink.click();

        WebElement profileName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h4")));
        Assertions.assertTrue(profileName.isDisplayed(), "User profile should be visible");
    }

    @Test
    @Order(9)
    public void testLogout() {
        testValidLogin();
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("nav.navbar a[href*='settings']")));
        settingsLink.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn-outline-danger")));
        logoutButton.click();

        WebElement signInLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("a[href*='login']")));
        Assertions.assertTrue(signInLink.isDisplayed(), "Should be logged out and see login link");
    }
}