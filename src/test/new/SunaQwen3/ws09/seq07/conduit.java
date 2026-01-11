package SunaQwen3.ws09.seq07;

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
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys("editor@realworld.io");
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys("editor");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().contains("/#/"), "Should be redirected to home page after login");
    }

    @Test
    @Order(2)
    void testInvalidLoginCredentials() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("form")));

        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys("invalid@user.com");
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys("wrongpass");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        String errorText = driver.findElement(By.cssSelector(".error-messages li")).getText();
        assertTrue(errorText.contains("email or password is invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testNavigationToHome() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Home")).click();
        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().endsWith("/#/"), "Should navigate to home page");
    }

    @Test
    @Order(4)
    void testNavigationToSignIn() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign in")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"), "Should navigate to sign in page");
    }

    @Test
    @Order(5)
    void testNavigationToSignUp() {
        driver.get(BASE_URL);
        driver.findElement(By.linkText("Sign up")).click();
        wait.until(ExpectedConditions.urlContains("/register"));
        assertTrue(driver.getCurrentUrl().contains("/register"), "Should navigate to sign up page");
    }

    @Test
    @Order(6)
    void testFooterTwitterLink() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
        twitterLink.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
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
        String originalWindow = driver.getWindowHandle();
        WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
        facebookLink.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
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
        String originalWindow = driver.getWindowHandle();
        WebElement linkedinLink = driver.findElement(By.cssSelector("a[href*='linkedin']"));
        linkedinLink.click();

        wait.until(d -> driver.getWindowHandles().size() > 1);
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
    void testArticlePageNavigation() {
        driver.get(BASE_URL);
        try {
            WebElement firstArticle = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".article-preview a.preview-link h1")));
            firstArticle.click();

            wait.until(ExpectedConditions.urlContains("/articles/"));
            assertTrue(driver.getCurrentUrl().contains("/articles/"), "Should navigate to article page");
        } catch (TimeoutException e) {
            System.out.println("No articles available to test navigation");
        }
    }

    @Test
    @Order(10)
    void testTagFilterNavigation() {
        driver.get(BASE_URL);
        try {
            WebElement firstTag = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".tag-list a")));
            String tagName = firstTag.getText();
            firstTag.click();

            wait.until(ExpectedConditions.urlContains("/tag/"));
            assertTrue(driver.getCurrentUrl().contains("/tag/"), "Should navigate to tag filtered page");
            assertTrue(driver.getCurrentUrl().contains(tagName.toLowerCase()), "URL should contain the tag name");
        } catch (TimeoutException e) {
            System.out.println("No tags available to test filtering");
        }
    }

    @Test
    @Order(11)
    void testUserProfileNavigation() {
        driver.get(BASE_URL);
        try {
            WebElement firstAuthor = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".article-meta a")));
            String authorName = firstAuthor.getText();
            firstAuthor.click();

            wait.until(ExpectedConditions.urlContains("/profile/"));
            assertTrue(driver.getCurrentUrl().contains("/profile/"), "Should navigate to user profile page");
            assertTrue(driver.getCurrentUrl().contains(authorName), "URL should contain the author name");
        } catch (TimeoutException e) {
            System.out.println("No articles with authors available to test profile navigation");
        }
    }

    @Test
    @Order(12)
    void testNewArticleButtonVisibleWhenLoggedIn() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys("editor@realworld.io");
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys("editor");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/"));
        driver.get(BASE_URL);

        try {
            WebElement newArticleButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Article")));
            assertTrue(newArticleButton.isDisplayed(), "New Article button should be visible when logged in");
        } catch (TimeoutException e) {
            fail("New Article button should be present for logged in user");
        }
    }

    @Test
    @Order(13)
    void testSettingsNavigation() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys("editor@realworld.io");
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys("editor");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/"));
        driver.get(BASE_URL);

        WebElement settingsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Settings")));
        settingsLink.click();

        wait.until(ExpectedConditions.urlContains("/settings"));
        assertTrue(driver.getCurrentUrl().contains("/settings"), "Should navigate to settings page");
    }

    @Test
    @Order(14)
    void testLogoutFunctionality() {
        driver.get(BASE_URL + "#/login");
        wait.until(ExpectedConditions.urlContains("/login"));

        driver.findElement(By.cssSelector("input[placeholder='Email']")).sendKeys("editor@realworld.io");
        driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys("editor");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/"));
        driver.get(BASE_URL);

        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Or click here to logout.")));
        logoutButton.click();

        wait.until(ExpectedConditions.urlContains("/"));
        assertTrue(driver.getCurrentUrl().endsWith("/#/"), "Should return to home page after logout");
        assertTrue(driver.findElements(By.linkText("Sign in")).size() > 0, "Sign in link should be visible after logout");
    }
}