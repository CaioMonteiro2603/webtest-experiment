package SunaGPT20b.ws09.seq07;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
    private static final String VALID_USERNAME = "Katharina_Bernier";
    private static final String VALID_PASSWORD = "s3cret";

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

    /** Helper to navigate to the base URL */
    private void goToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    /** Helper to perform login with given credentials */
    private void login(String username, String password) {
        goToHome();
        // Click the "Sign in" link
        WebElement signInLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Sign in")));
        signInLink.click();

        // Fill email and password
        WebElement emailInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailInput.clear();
        emailInput.sendKeys(username);

        WebElement passwordInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Password']")));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        // Click the login button
        WebElement loginButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        loginButton.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(VALID_USERNAME, VALID_PASSWORD);

        // Verify that the navigation bar now shows "Your Feed"
        WebElement yourFeed = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.linkText("Your Feed")));
        Assertions.assertTrue(yourFeed.isDisplayed(),
                "Your Feed link should be visible after successful login");

        // Verify URL contains a hash fragment (Angular routing)
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"),
                "URL should contain '#/' after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login(VALID_USERNAME, "wrongPassword");

        // Expect an error message element
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("email or password is invalid")
                        || errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        // Ensure we are logged in
        login(VALID_USERNAME, VALID_PASSWORD);

        // New Article
        WebElement newArticle = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("New Article")));
        newArticle.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/editor"),
                "URL should contain '#/editor' after clicking New Article");

        // Return to Home
        WebElement homeLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();
        wait.until(ExpectedConditions.urlContains("#/"));

        // Settings
        WebElement settings = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Settings")));
        settings.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/settings"),
                "URL should contain '#/settings' after clicking Settings");

        // Sign out
        WebElement signOut = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Sign out")));
        signOut.click();
        // After sign out, the Sign in link should be visible again
        WebElement signInLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.linkText("Sign in")));
        Assertions.assertTrue(signInLink.isDisplayed(),
                "Sign in link should be visible after signing out");
    }

    @Test
    @Order(4)
    public void testFooterExternalLinks() {
        // Ensure we are logged in to have the footer present
        login(VALID_USERNAME, VALID_PASSWORD);

        // Scroll to footer (simple approach: locate footer element)
        WebElement footer = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.tagName("footer")));

        // Define expected external domains
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};

        for (String domain : domains) {
            List<WebElement> links = footer.findElements(By.cssSelector("a[href*='" + domain + "']"));
            if (links.isEmpty()) {
                // If the link is not present, skip but note
                continue;
            }
            WebElement link = links.get(0);
            // Open link in new tab by clicking (browser will handle new window)
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Wait for new window
            wait.until(driver -> driver.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();

            driver.switchTo().window(newWindow);
            // Verify URL contains expected domain
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    "External link should navigate to a URL containing '" + domain + "'");

            // Close the new tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    public void testOneLevelInternalLinks() {
        // Start from home page
        goToHome();

        // Collect all anchor hrefs on the home page
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        for (WebElement anchor : anchors) {
            String href = anchor.getAttribute("href");
            // Consider only internal links (same host) and one level deep (no further slashes after base path)
            if (href == null || !href.startsWith(BASE_URL)) {
                continue;
            }
            String path = href.substring(BASE_URL.length());
            if (path.isEmpty() || path.contains("/") && path.indexOf('/') != path.length() - 1) {
                // Skip deeper than one level
                continue;
            }


            // Wait for navigation
            wait.until(ExpectedConditions.urlToBe(href));

            // Simple verification: page body should be present
            WebElement body = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Assertions.assertTrue(body.isDisplayed(),
                    "Body should be displayed on internal page: " + href);

            // Navigate back to home
            driver.navigate().back();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        }
    }
}
