package SunaGPT20b.ws09.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";

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
    public void resetState() {
        driver.manage().deleteAllCookies();
        driver.get(BASE_URL);
    }

    private WebElement waitClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    private WebElement waitVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void login(String email, String password) {
        waitClickable(By.linkText("Sign in")).click();
        waitVisible(By.cssSelector("form")).findElement(By.cssSelector("input[type='email']")).sendKeys(email);
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(password);
        waitClickable(By.cssSelector("button[type='submit']")).click();
        wait.until(ExpectedConditions.urlContains("/"));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("testuser@example.com", "test123");
        // Verify that the navigation now contains the user-specific links
        Assertions.assertTrue(driver.findElements(By.linkText("New Post")).size() > 0,
                "New Post link should be visible after successful login");
        Assertions.assertTrue(driver.findElements(By.linkText("Settings")).size() > 0,
                "Settings link should be visible after successful login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        waitClickable(By.linkText("Sign in")).click();
        waitVisible(By.cssSelector("form")).findElement(By.cssSelector("input[type='email']")).sendKeys("invalid@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("wrongpass");
        waitClickable(By.cssSelector("button[type='submit']")).click();

        WebElement error = waitVisible(By.cssSelector(".error-messages"));
        Assertions.assertTrue(error.isDisplayed(), "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(error.getText().toLowerCase().contains("email or password is invalid"),
                "Error text should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testUserMenuNavigation() {
        login("testuser@example.com", "test123");

        // New Post
        waitClickable(By.linkText("New Post")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/editor"),
                "Clicking New Post should navigate to the editor page");

        driver.navigate().back();

        // Settings
        waitClickable(By.linkText("Settings")).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/settings"),
                "Clicking Settings should navigate to the settings page");

        driver.navigate().back();

        // Logout
        waitClickable(By.linkText("Logout")).click();
        Assertions.assertTrue(driver.findElements(By.linkText("Sign in")).size() > 0,
                "Sign in link should be visible after logout");
    }

    @Test
    @Order(4)
    public void testFooterExternalLinks() {
        // Footer may contain social links; locate them by known domains
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            List<WebElement> links = driver.findElements(By.cssSelector("footer a[href*='" + domain + "']"));
            if (links.isEmpty()) {
                continue; // Skip if the link is not present on this page
            }
            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            Set<String> existingWindows = driver.getWindowHandles();

            link.click();

            // Wait for new window
            wait.until(driver -> driver.getWindowHandles().size() > existingWindows.size());
            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(existingWindows);
            String newWindow = newWindows.iterator().next();

            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    "External link should open a page containing domain: " + domain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    public void testArticleNavigation() {
        // Click the first article preview link
        List<WebElement> articleLinks = driver.findElements(By.cssSelector("a.preview-link"));
        Assertions.assertFalse(articleLinks.isEmpty(), "There should be at least one article preview link");
        articleLinks.get(0).click();

        Assertions.assertTrue(driver.getCurrentUrl().contains("/article/"),
                "URL should contain '/article/' after clicking an article");

        // Verify article title is displayed
        WebElement title = waitVisible(By.cssSelector("h1"));
        Assertions.assertTrue(title.isDisplayed(), "Article title should be displayed on article page");

        driver.navigate().back();
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL) || driver.getCurrentUrl().contains("/"),
                "Should return to the home page after navigating back");
    }

    @Test
    @Order(6)
    public void testTagFilter() {
        // Open first article to get a tag
        List<WebElement> articleLinks = driver.findElements(By.cssSelector("a.preview-link"));
        Assertions.assertFalse(articleLinks.isEmpty(), "There should be at least one article preview link");
        articleLinks.get(0).click();

        // Find first tag link
        List<WebElement> tags = driver.findElements(By.cssSelector("a.tag-default"));
        Assertions.assertFalse(tags.isEmpty(), "Article should contain at least one tag");
        String tagText = tags.get(0).getText();
        tags.get(0).click();

        // Verify URL contains the tag filter
        Assertions.assertTrue(driver.getCurrentUrl().contains("tag=" + tagText),
                "URL should contain the selected tag as a query parameter");

        // Verify that the filtered list shows articles with the tag
        List<WebElement> filteredTags = driver.findElements(By.cssSelector("a.tag-default"));
        boolean tagFound = filteredTags.stream().anyMatch(e -> e.getText().equals(tagText));
        Assertions.assertTrue(tagFound, "Filtered articles should display the selected tag");
    }
}