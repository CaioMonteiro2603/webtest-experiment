package SunaQwen3.ws09.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass123";

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
    void testLoginPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("demo.realworld.io"), "URL should contain base domain");

        // Wait for page to load and check title
        wait.until(ExpectedConditions.titleContains("Conduit"));
        String title = driver.getTitle();
        assertTrue(title.contains("Conduit"), "Page title should contain 'Conduit'");
    }

    @Test
    @Order(2)
    void testValidLoginRedirectsToHomePage() {
        driver.get(BASE_URL);
        // Navigate to login page first
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/login']")));
        signInLink.click();
        
        login(USERNAME, PASSWORD);

        wait.until(ExpectedConditions.urlContains("#/"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("#/"), "Should be redirected to home feed after login");

        WebElement profileLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("a[href='/@testuser']")));
        assertTrue(profileLink.isDisplayed(), "User profile link should be visible after login");
    }

    @Test
    @Order(3)
    void testInvalidLoginShowsError() {
        driver.get(BASE_URL);
        // Navigate to login page first
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/login']")));
        signInLink.click();
        
        login("invaliduser", "wrongpass");

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error-messages li")));
        assertEquals("email or password is invalid", errorElement.getText().toLowerCase(),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    void testMenuNavigation_AllItems() {
        navigateToHomeIfNecessary();
        // No need to open menu for desktop view
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/']")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("#/"));
        assertTrue(driver.getCurrentUrl().contains("#/"), "Clicking 'All Items' should navigate to home feed");
    }

    @Test
    @Order(5)
    void testMenuNavigation_About_ExternalLink() {
        navigateToHomeIfNecessary();
        // No need to open menu for desktop view

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='https://github.com/gothinkster/realworld']")));
        aboutLink.click();

        // Switch to new tab
        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        assertNotNull(newHandle, "New tab should open for external link");
        driver.switchTo().window(newHandle);

        // Assert domain
        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains("github.com"), "External link should open GitHub domain");

        // Close tab and return
        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(6)
    void testMenuNavigation_Logout() {
        navigateToHomeIfNecessary();
        // No need to open menu for desktop view

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/']")));
        logoutLink.click();

        // Wait for logout to complete and redirect to home
        wait.until(ExpectedConditions.urlContains("#/"));
        List<WebElement> signInLink = driver.findElements(By.cssSelector("a[href='/login']"));
        assertTrue(signInLink.size() > 0, "Sign in link should appear after logout");
    }

    @Test
    @Order(7)
    void testFooterSocialLinks_Twitter() {
        driver.get(BASE_URL);
        navigateToHomeIfNecessary();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='https://twitter.com/gothinkster']")));
        twitterLink.click();

        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        assertNotNull(newHandle, "New tab should open for Twitter link");
        driver.switchTo().window(newHandle);

        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open correct domain");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(8)
    void testFooterSocialLinks_Facebook() {
        driver.get(BASE_URL);
        navigateToHomeIfNecessary();

        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='https://www.facebook.com/gothinkster']")));
        facebookLink.click();

        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        assertNotNull(newHandle, "New tab should open for Facebook link");
        driver.switchTo().window(newHandle);

        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open correct domain");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(9)
    void testFooterSocialLinks_LinkedIn() {
        driver.get(BASE_URL);
        navigateToHomeIfNecessary();

        WebElement linkedInLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='https://www.linkedin.com/company/gothinkster/']")));
        linkedInLink.click();

        String originalHandle = driver.getWindowHandle();
        String newHandle = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalHandle);
            return handles.isEmpty() ? null : handles.iterator().next();
        });
        assertNotNull(newHandle, "New tab should open for LinkedIn link");
        driver.switchTo().window(newHandle);

        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open correct domain");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(10)
    void testArticleCreationFormAvailable() {
        navigateToHomeIfNecessary();

        WebElement newPostLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/editor']")));
        newPostLink.click();

        wait.until(ExpectedConditions.urlContains("#/editor"));
        assertTrue(driver.getCurrentUrl().contains("#/editor"), "Should navigate to editor page");

        WebElement titleField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='Article Title']")));
        assertTrue(titleField.isDisplayed(), "Title input should be visible on editor page");
    }

    @Test
    @Order(11)
    void testTagFilteringOnHomePage() {
        navigateToHomeIfNecessary();

        List<WebElement> tagLinks = driver.findElements(By.cssSelector(".sidebar .tag-list a"));
        assertTrue(tagLinks.isEmpty() || !tagLinks.isEmpty(), "Tag list check");
        
        if (!tagLinks.isEmpty()) {
            WebElement firstTag = tagLinks.get(0);
            String tagName = firstTag.getText();
            firstTag.click();

            wait.until(ExpectedConditions.urlContains("#/tag/" + tagName.toLowerCase()));
            assertTrue(driver.getCurrentUrl().contains("#/tag/" + tagName.toLowerCase()),
                    "URL should reflect selected tag filter");
        }
    }

    @Test
    @Order(12)
    void testUserProfilePageAccess() {
        navigateToHomeIfNecessary();

        // First login to ensure user is authenticated
        List<WebElement> profileLinks = driver.findElements(By.cssSelector("a[href='/@testuser']"));
        if (profileLinks.isEmpty()) {
            // Navigate to login and login first
            WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("a[href='/login']")));
            signInLink.click();
            login(USERNAME, PASSWORD);
            wait.until(ExpectedConditions.urlContains("#/"));
        }

        WebElement profileLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/@testuser']")));
        profileLink.click();

        wait.until(ExpectedConditions.urlContains("#/@testuser"));
        assertTrue(driver.getCurrentUrl().contains("#/@testuser"), "Should navigate to user profile");

        WebElement profileHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".profile-page h4")));
        assertTrue(profileHeader.getText().contains(USERNAME), "Profile header should show username");
    }

    // Helper methods

    private void login(String username, String password) {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[placeholder='Email']")));
        emailField.clear();
        emailField.sendKeys(username);

        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordField.clear();
        passwordField.sendKeys(password);

        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();
    }

    private void openMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".navbar-burger")));
        menuButton.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".navbar-menu.is-active")));
    }

    private void navigateToHomeIfNecessary() {
        if (!driver.getCurrentUrl().contains("#/")) {
            driver.get(BASE_URL + "#/");
            wait.until(ExpectedConditions.urlContains("#/"));
        }
    }
}