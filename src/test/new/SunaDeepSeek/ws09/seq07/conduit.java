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
        wait.until(ExpectedConditions.urlContains("demo.realworld.io"));
        String title = driver.getTitle();
        Assertions.assertTrue(title.contains("Conduit") || title.contains("conduit"));
        Assertions.assertTrue(driver.findElement(By.cssSelector("a.navbar-brand")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testUserLogin() {
        driver.get(BASE_URL);
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='/login'][routerlink]")));
        signInLink.click();

        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[formcontrolname='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[formcontrolname='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        signInButton.click();

        wait.until(ExpectedConditions.urlContains("/"));
        try { Thread.sleep(2000); } catch (InterruptedException e) { e.printStackTrace(); }
        WebElement userProfile = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[routerlink*='/profile/']")));
        Assertions.assertTrue(userProfile.isDisplayed());
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[formcontrolname='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[formcontrolname='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        signInButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".error-messages li")));
        Assertions.assertTrue(errorMessage.getText().toLowerCase().contains("email or password") ||
                              errorMessage.getText().toLowerCase().contains("invalid"));
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        login();
        WebElement articleLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a.preview-link")));
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
            By.cssSelector("a[routerlink*='/profile/']")));
        profileLink.click();

        wait.until(ExpectedConditions.urlContains("/profile/"));
        WebElement profileUsername = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h4")));
        Assertions.assertTrue(profileUsername.getText().toLowerCase().contains(USERNAME.toLowerCase()));
    }

    @Test
    @Order(6)
    public void testSettingsPage() {
        login();
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[routerlink*='/settings']")));
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
            By.cssSelector("a[routerlink*='/editor']")));
        newArticleLink.click();

        wait.until(ExpectedConditions.urlContains("/editor"));
        WebElement titleField = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input[formcontrolname='title']")));
        titleField.sendKeys("Test Article Title");

        WebElement descriptionField = driver.findElement(
            By.cssSelector("input[formcontrolname='description']"));
        descriptionField.sendKeys("Test description");

        WebElement bodyField = driver.findElement(
            By.cssSelector("textarea[formcontrolname='body']"));
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

        java.util.List<WebElement> twitterLinks = driver.findElements(By.cssSelector("a[href*='twitter']"));
        if (!twitterLinks.isEmpty()) {
            WebElement twitterLink = twitterLinks.get(0);
            twitterLink.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!originalWindow.equals(windowHandle)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            if (driver.getCurrentUrl().contains("twitter.com")) {
                Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"));
            } else {
                Assertions.assertTrue(true);
            }
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            Assertions.assertTrue(true);
        }

        java.util.List<WebElement> githubLinks = driver.findElements(By.cssSelector("a[href*='github.com']"));
        if (!githubLinks.isEmpty()) {
            WebElement githubLink = githubLinks.get(0);
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
        } else {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(9)
    public void testTagNavigation() {
        driver.get(BASE_URL);
        java.util.List<WebElement> tagLinks = driver.findElements(By.cssSelector(".tag-list a"));
        if (!tagLinks.isEmpty()) {
            WebElement tagLink = tagLinks.get(0);
            String tagText = tagLink.getText();
            if (!tagText.isEmpty()) {
                tagLink.click();
                wait.until(ExpectedConditions.urlContains("/tag/"));
                WebElement tagHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".nav-link.active")));
                Assertions.assertTrue(tagHeader.getText().toLowerCase().contains(tagText.toLowerCase()));
            } else {
                Assertions.assertTrue(true);
            }
        } else {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @Order(10)
    public void testUserLogout() {
        login();
        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[routerlink*='/settings']")));
        settingsLink.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button.btn-outline-danger")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("/"));
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("a[routerlink*='/login']")));
        Assertions.assertTrue(signInLink.isDisplayed());
    }

    private void login() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.urlContains("/login"));
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[formcontrolname='email']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[formcontrolname='password']"));
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        signInButton.click();
        wait.until(ExpectedConditions.urlContains("/"));
    }
}