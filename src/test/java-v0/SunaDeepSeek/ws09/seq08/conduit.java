package SunaDeepSeek.ws09.seq08;

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
    private static final String PASSWORD = "testpass123";

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
        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.getTitle().contains("Conduit"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.navbar-brand")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testLoginFunctionality() {
        driver.get(BASE_URL + "login");
        wait.until(ExpectedConditions.urlContains("login"));

        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a[href*='@" + USERNAME + "']")).isDisplayed());
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        wait.until(ExpectedConditions.urlContains("login"));

        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorMessage.getText().contains("email or password is invalid"));
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains(BASE_URL));

        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".article-preview:first-child h1")));
        String articleTitle = articleLink.getText();
        articleLink.click();

        wait.until(ExpectedConditions.urlContains("/article/"));
        WebElement articleTitleElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".article-page h1")));
        Assertions.assertEquals(articleTitle, articleTitleElement.getText());
    }

    @Test
    @Order(5)
    public void testProfilePage() {
        driver.get(BASE_URL + "profile/" + USERNAME);
        wait.until(ExpectedConditions.urlContains("/profile/" + USERNAME));

        WebElement profileName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".user-info h4")));
        Assertions.assertTrue(profileName.getText().contains(USERNAME));
    }

    @Test
    @Order(6)
    public void testSettingsPage() {
        driver.get(BASE_URL + "settings");
        wait.until(ExpectedConditions.urlContains("settings"));

        WebElement settingsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".settings-page h1")));
        Assertions.assertEquals("Your Settings", settingsHeader.getText());
    }

    @Test
    @Order(7)
    public void testEditorPage() {
        driver.get(BASE_URL + "editor");
        wait.until(ExpectedConditions.urlContains("editor"));

        WebElement editorHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".editor-page h1")));
        Assertions.assertTrue(editorHeader.getText().contains("New Article"));
    }

    @Test
    @Order(8)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains(BASE_URL));

        // Test Twitter link
        testExternalLink(By.cssSelector("a[href*='twitter.com']"), "twitter.com");

        // Test GitHub link
        testExternalLink(By.cssSelector("a[href*='github.com']"), "github.com");
    }

    private void testExternalLink(By locator, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(locator));
        link.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testLogout() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.urlContains(BASE_URL));

        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[href*='settings']")));
        settingsLink.click();

        wait.until(ExpectedConditions.urlContains("settings"));
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".btn-outline-danger")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains(BASE_URL));
        Assertions.assertTrue(driver.findElements(By.cssSelector("a[href*='login']")).size() > 0);
    }
}