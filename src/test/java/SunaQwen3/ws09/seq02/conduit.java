package SunaQwen3.ws09.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign in")).click();

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Email']")));
        emailField.sendKeys(USERNAME);

        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.sendKeys(PASSWORD);

        WebElement signInButton = driver.findElement(By.xpath("//button[.//text()[contains(.,'Sign in')]]"));
        signInButton.click();

        wait.until(ExpectedConditions.urlContains("#/"));
        assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home after login");
        assertTrue(driver.findElements(By.cssSelector(".article-preview")).size() > 0, "Should see article previews");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Email']")));
        emailField.sendKeys("invalid@example.com");
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys("wrongpass");
        driver.findElement(By.xpath("//button[.//text()[contains(.,'Sign in')]]")).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        assertTrue(error.getText().contains("email or password is invalid"), "Should show invalid credentials error");
    }

    @Test
    @Order(3)
    void testNavigationToSettings() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign in")).click();
        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//button[.//text()[contains(.,'Sign in')]]")).click();
        wait.until(ExpectedConditions.urlContains("#/"));

        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".nav-link")));
        userMenu.click();

        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Settings")));
        settingsLink.click();

        wait.until(ExpectedConditions.urlContains("#/settings"));
        assertTrue(driver.getCurrentUrl().contains("#/settings"), "Should navigate to settings page");
    }

    @Test
    @Order(4)
    void testCreateNewArticle() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign in")).click();
        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//button[.//text()[contains(.,'Sign in')]]")).click();
        wait.until(ExpectedConditions.urlContains("#/"));

        WebElement newPostLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Post")));
        newPostLink.click();

        wait.until(ExpectedConditions.urlContains("#/editor"));

        WebElement titleField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[placeholder='Article Title']")));
        titleField.sendKeys("Test Article Title");

        driver.findElement(By.cssSelector("input[placeholder=\"What's this article about?\"]")).sendKeys("Article description");
        driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']")).sendKeys("This is the body of the test article.");

        driver.findElement(By.cssSelector("input[placeholder='Enter tags']")).sendKeys("testtag");
        driver.findElement(By.xpath("//button[.//text()[contains(.,'Publish Article')]]")).click();

        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".article-page h1")));
        assertEquals("Test Article Title", articleTitle.getText(), "Article title should match");
    }

    @Test
    @Order(5)
    void testVisitProfilePage() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign in")).click();
        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//button[.//text()[contains(.,'Sign in')]]")).click();
        wait.until(ExpectedConditions.urlContains("#/"));

        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".nav-link")));
        profileLink.click();

        String profileUrl = driver.getCurrentUrl();
        assertTrue(profileUrl.contains("#/@"), "Should navigate to user profile page");
    }

    @Test
    @Order(6)
    void testFooterTwitterLink() {
        driver.get(BASE_URL);
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://twitter.com/gothinkster']")));
        String originalWindow = driver.getWindowHandle();
        twitterLink.click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("twitter.com"));
        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Should open Twitter link in new tab");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    void testFooterFacebookLink() {
        driver.get(BASE_URL);
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://www.facebook.com/thinkster.io']")));
        String originalWindow = driver.getWindowHandle();
        facebookLink.click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("facebook.com"));
        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Should open Facebook link in new tab");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    void testFooterLinkedInLink() {
        driver.get(BASE_URL);
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='https://www.linkedin.com/company/thinkster-io']")));
        String originalWindow = driver.getWindowHandle();
        linkedinLink.click();

        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        wait.until(ExpectedConditions.urlContains("linkedin.com"));
        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "Should open LinkedIn link in new tab");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    void testLogoutFunctionality() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign in")).click();
        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys(USERNAME);
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//button[.//text()[contains(.,'Sign in')]]")).click();
        wait.until(ExpectedConditions.urlContains("#/"));

        WebElement userMenu = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".nav-link")));
        userMenu.click();

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Or click here to logout.")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("#/"));
        assertTrue(driver.findElements(By.linkText("Sign in")).size() > 0, "Should return to login state after logout");
    }
}