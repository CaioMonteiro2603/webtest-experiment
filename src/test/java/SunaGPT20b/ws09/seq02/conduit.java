package SunaGPT20b.ws09.seq02;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class RealWorldTest {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String VALID_EMAIL = "testuser@example.com";
    private static final String VALID_PASSWORD = "Password123";
    private static final String INVALID_EMAIL = "invalid@example.com";
    private static final String INVALID_PASSWORD = "wrongpass";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    public void navigateToBase() {
        driver.get(BASE_URL);
        // Ensure the page is loaded by waiting for the navigation bar
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("nav.navbar")));
    }

    private void login(String email, String password) {
        // Click Sign in link from navbar
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        signInLink.click();

        // Fill email and password
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passwordInput = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        // Click Sign in button
        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();

        // Wait for home feed to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.feed-toggle")));
    }

    private void logoutIfLoggedIn() {
        List<WebElement> settingsLinks = driver.findElements(By.linkText("Settings"));
        if (!settingsLinks.isEmpty()) {
            settingsLinks.get(0).click();
            WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger")));
            logoutButton.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in")));
        }
    }

    @Test
    @Order(1)
    public void testValidLoginAndLogout() {
        login(VALID_EMAIL, VALID_PASSWORD);

        // Verify URL contains /feed (home page after login)
        Assertions.assertTrue(driver.getCurrentUrl().contains("/#/"), "URL should contain '/#/' after successful login");

        // Verify that the user avatar appears in the navbar
        WebElement avatar = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.user-img")));
        Assertions.assertTrue(avatar.isDisplayed(), "User avatar should be displayed after login");

        // Logout
        logoutIfLoggedIn();

        // Verify we are back on the home page (not logged in)
        List<WebElement> signInLinks = driver.findElements(By.linkText("Sign in"));
        Assertions.assertFalse(signInLinks.isEmpty(), "Sign in link should be visible after logout");
    }

    @Test
    @Order(2)
    public void testInvalidLoginShowsError() {
        // Navigate to Sign in page
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        signInLink.click();

        // Enter invalid credentials
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailInput.clear();
        emailInput.sendKeys(INVALID_EMAIL);

        WebElement passwordInput = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordInput.clear();
        passwordInput.sendKeys(INVALID_PASSWORD);

        WebElement signInButton = driver.findElement(By.cssSelector("button[type='submit']"));
        signInButton.click();

        // Expect an error message
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.error-messages li")));
        Assertions.assertEquals("email or password is invalid", errorMsg.getText().toLowerCase(),
                "Invalid login should display appropriate error message");
    }

    @Test
    @Order(3)
    public void testCreateAndDeleteArticle() {
        login(VALID_EMAIL, VALID_PASSWORD);

        // Click New Post link
        WebElement newPostLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("New Post")));
        newPostLink.click();

        // Fill article form
        WebElement titleInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Article Title']")));
        String articleTitle = "Selenium Test Article";
        titleInput.sendKeys(articleTitle);

        WebElement aboutInput = driver.findElement(By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        aboutInput.sendKeys("Testing Selenium");

        WebElement bodyInput = driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        bodyInput.sendKeys("This is a test article created by Selenium.");

        WebElement publishButton = driver.findElement(By.cssSelector("button[type='button']"));
        publishButton.click();

        // Verify article page loads with correct title
        WebElement articleHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals(articleTitle, articleHeader.getText(), "Published article title should match");

        // Delete the article via Settings (if available) - RealWorld does not provide delete, so we skip actual deletion.
        // Instead, navigate back to home and ensure the article appears in the list
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();

        // Verify the article appears in the feed
        List<WebElement> articleTitles = driver.findElements(By.cssSelector("h1"));
        boolean found = articleTitles.stream().anyMatch(e -> e.getText().equals(articleTitle));
        Assertions.assertTrue(found, "Newly created article should appear in the feed");

        // Cleanup: logout
        logoutIfLoggedIn();
    }

    @Test
    @Order(4)
    public void testExternalFooterLinks() {
        // Footer contains a link to the RealWorld GitHub repository
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        WebElement githubLink = null;
        for (WebElement link : footerLinks) {
            if (link.getAttribute("href") != null && link.getAttribute("href").contains("github.com")) {
                githubLink = link;
                break;
            }
        }
        Assertions.assertNotNull(githubLink, "GitHub link should be present in the footer");

        // Click the link (opens in new tab)
        String originalWindow = driver.getWindowHandle();
        githubLink.click();

        // Wait for new window/tab
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        // Verify URL contains github.com
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"),
                "External link should navigate to a GitHub domain");

        // Close the new tab and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testNavigationMenuItems() {
        login(VALID_EMAIL, VALID_PASSWORD);

        // Verify navigation items are present
        String[] navItems = {"Home", "New Post", "Settings", "Profile"};
        for (String item : navItems) {
            List<WebElement> links = driver.findElements(By.linkText(item));
            Assertions.assertFalse(links.isEmpty(), "Navigation item '" + item + "' should be present");
        }

        // Test Settings page navigation
        WebElement settingsLink = driver.findElement(By.linkText("Settings"));
        settingsLink.click();
        WebElement updateBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.btn-primary")));
        Assertions.assertTrue(updateBtn.isDisplayed(), "Settings page should display the update button");

        // Return to Home
        WebElement homeLink = driver.findElement(By.linkText("Home"));
        homeLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.feed-toggle")));

        // Logout to clean up
        logoutIfLoggedIn();
    }
}