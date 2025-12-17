package SunaGPT20b.ws09.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpClass() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    public void navigateHome() {
        driver.get(BASE_URL);
    }

    private void login(String email, String password) {
        // Click Sign in link
        WebElement signInLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*=\"/login\"]")));
        signInLink.click();

        // Fill credentials
        WebElement emailInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passwordInput = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();

        // Wait for home feed to appear
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.nav-link.active[href*=\"/\"")));
    }

    private void logout() {
        // Open settings (gear icon)
        WebElement settingsLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*=\"/settings\"]")));
        settingsLink.click();

        // Click logout button
        WebElement logoutButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger")));
        logoutButton.click();

        // Verify redirected to login page
        wait.until(ExpectedConditions.urlContains("/login"));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("testuser@example.com", "test123");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/#/"),
                "After login the URL should contain '/#/'");
        WebElement feedHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.nav-link.active[href*=\"/\"")));
        Assertions.assertEquals("Home", feedHeader.getText(),
                "Home link should be active after successful login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        // Navigate to login page
        WebElement signInLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*=\"/login\"]")));
        signInLink.click();

        // Enter invalid credentials
        WebElement emailInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailInput.sendKeys("invalid@example.com");
        WebElement passwordInput = driver.findElement(By.cssSelector("input[placeholder='Password']"));
        passwordInput.sendKeys("wrongpass");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Verify error message
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.error-messages li")));
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("email or password"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testLogout() {
        login("testuser@example.com", "test123");
        logout();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "After logout the URL should contain '/login'");
    }

    @Test
    @Order(4)
    public void testExternalLinkGitHub() {
        // Footer GitHub link
        WebElement githubLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*=\"github.com\"]")));
        String originalWindow = driver.getWindowHandle();
        Set<String> existingWindows = driver.getWindowHandles();

        githubLink.click();

        // Wait for new window
        wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());
        Set<String> newWindows = driver.getWindowHandles();
        newWindows.removeAll(existingWindows);
        String newWindowHandle = newWindows.iterator().next();

        driver.switchTo().window(newWindowHandle);
        wait.until(ExpectedConditions.urlContains("github.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("github.com"),
                "External link should navigate to a GitHub domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testCreateAndDeleteArticle() {
        login("testuser@example.com", "test123");

        // Click New Post
        WebElement newPostLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*=\"/editor\"]")));
        newPostLink.click();

        // Fill article form
        String articleTitle = "Selenium Test Article " + System.currentTimeMillis();
        WebElement titleInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Article Title']")));
        titleInput.sendKeys(articleTitle);

        WebElement aboutInput = driver.findElement(By.cssSelector("input[placeholder=\"What's this article about?\"]"));
        aboutInput.sendKeys("Testing Selenium");

        WebElement bodyInput = driver.findElement(By.cssSelector("textarea[placeholder='Write your article (in markdown)']"));
        bodyInput.sendKeys("This is a test article created by automated Selenium test.");

        WebElement tagsInput = driver.findElement(By.cssSelector("input[placeholder='Enter tags']"));
        tagsInput.sendKeys("test,selenium");

        WebElement publishButton = driver.findElement(By.cssSelector("button[type='button']"));
        publishButton.click();

        // Verify article page
        WebElement articleHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertEquals(articleTitle, articleHeader.getText(),
                "Published article title should match the input title");

        // Delete the article via settings (gear icon on article page)
        WebElement articleSettings = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-outline-danger")));
        articleSettings.click();

        // Verify we are back to home and article no longer appears in profile
        driver.get(BASE_URL);
        WebElement profileLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*=\"/@testuser\"")));
        profileLink.click();

        List<WebElement> articles = driver.findElements(By.cssSelector("h1"));
        boolean articleFound = articles.stream()
                .anyMatch(el -> el.getText().equals(articleTitle));
        Assertions.assertFalse(articleFound, "Deleted article should no longer appear in the profile list");
    }
}