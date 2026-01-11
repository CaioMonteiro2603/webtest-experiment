package SunaDeepSeek.ws09.seq07;

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
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser123";
    private static final String PASSWORD = "testuser123";

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
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.getTitle().contains("Conduit"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.navbar-brand")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testUserLogin() {
        driver.get(BASE_URL);
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/login']")));
        signInLink.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        signInButton.click();

        wait.until(ExpectedConditions.urlContains("/"));
        WebElement userProfile = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='/profile/']")));
        Assertions.assertTrue(userProfile.isDisplayed());
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        signInButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        login();
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.preview-link:first-child")));
        String articleTitle = articleLink.findElement(By.tagName("h1")).getText();
        articleLink.click();

        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals(articleTitle, articleTitleElement.getText());
    }

    @Test
    @Order(5)
    public void testProfilePage() {
        login();
        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/profile/']")));
        profileLink.click();

        wait.until(ExpectedConditions.urlContains("/profile/"));
        WebElement profileUsername = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h4")));
        Assertions.assertTrue(profileUsername.getText().contains(USERNAME));
    }

    @Test
    @Order(6)
    public void testSettingsPage() {
        login();
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/settings']")));
        settingsLink.click();

        wait.until(ExpectedConditions.urlContains("/settings"));
        WebElement settingsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Your Settings", settingsHeader.getText());
    }

    @Test
    @Order(7)
    public void testNewArticleCreation() {
        login();
        WebElement newArticleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/editor']")));
        newArticleLink.click();

        wait.until(ExpectedConditions.urlContains("/editor"));
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[placeholder='Article Title']")));
        titleField.sendKeys("Test Article Title");

        WebElement descriptionField = driver.findElement(
            By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        descriptionField.sendKeys("Test description");

        WebElement bodyField = driver.findElement(
            By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        bodyField.sendKeys("Test article body content");

        WebElement publishButton = driver.findElement(
            By.cssSelector("button[type='button']"));
        publishButton.click();

        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h1")));
        Assertions.assertEquals("Test Article Title", articleTitle.getText());
    }

    @Test
    @Order(8)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test GitHub link
        WebElement githubLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='github.com']")));
        githubLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testTagNavigation() {
        driver.get(BASE_URL);
        WebElement tagLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".tag-list a:first-child")));
        String tagText = tagLink.getText();
        tagLink.click();

        wait.until(ExpectedConditions.urlContains("/tag/"));
        WebElement tagHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".feed-toggle .nav-link.active")));
        Assertions.assertTrue(tagHeader.getText().contains(tagText));
    }

    @Test
    @Order(10)
    public void testUserLogout() {
        login();
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='/settings']")));
        settingsLink.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn-outline-danger")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("/"));
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href='/login']")));
        Assertions.assertTrue(signInLink.isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL + "login");
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        signInButton.click();
        wait.until(ExpectedConditions.urlContains("/"));
    }
}