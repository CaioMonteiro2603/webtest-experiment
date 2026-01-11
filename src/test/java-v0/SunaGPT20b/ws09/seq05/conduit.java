package SunaGPT20b.ws09.seq05;

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

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static WebDriver driver;
    private static WebDriverWait wait;

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

    private void login(String email, String password) {
        driver.get(BASE_URL + "#/login");
        WebElement emailInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailInput.clear();
        emailInput.sendKeys(email);
        WebElement passwordInput = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Password']")));
        passwordInput.clear();
        passwordInput.sendKeys(password);
        WebElement signInBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Sign in')]")));
        signInBtn.click();
    }

    private void logoutIfLoggedIn() {
        try {
            // Open menu if needed
            WebElement menuBtn = driver.findElement(By.cssSelector("button.ion-navicon-round"));
            menuBtn.click();
            WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
            logoutLink.click();
            // Wait for login page
            wait.until(ExpectedConditions.urlContains("#/login"));
        } catch (NoSuchElementException | TimeoutException ignored) {
            // Not logged in; nothing to do
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        logoutIfLoggedIn();
        login("demo@realworld.io", "demo");
        // Verify successful login by checking presence of the feed toggle
        WebElement feedToggle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.nav-link.active")));
        Assertions.assertTrue(feedToggle.isDisplayed(), "Feed toggle should be visible after successful login");
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/"), "URL should contain hash routing after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        logoutIfLoggedIn();
        login("demo@realworld.io", "wrongpassword");
        // Expect error message
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        logoutIfLoggedIn();
        login("demo@realworld.io", "demo");
        // Open burger menu
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.ion-navicon-round")));
        menuBtn.click();

        // Verify menu items
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        Assertions.assertTrue(homeLink.isDisplayed(), "Home link should be visible in menu");

        WebElement newPostLink = driver.findElement(By.linkText("New Post"));
        Assertions.assertTrue(newPostLink.isDisplayed(), "New Post link should be visible in menu");

        WebElement settingsLink = driver.findElement(By.linkText("Settings"));
        Assertions.assertTrue(settingsLink.isDisplayed(), "Settings link should be visible in menu");

        // Logout via menu
        WebElement logoutLink = driver.findElement(By.linkText("Logout"));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("#/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/login"), "Should be redirected to login page after logout");
    }

    @Test
    @Order(4)
    public void testArticleNavigation() {
        logoutIfLoggedIn();
        login("demo@realworld.io", "demo");
        // Wait for article previews to load
        List<WebElement> articles = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".article-preview h1")));
        Assertions.assertFalse(articles.isEmpty(), "There should be at least one article preview");
        // Click the first article title
        articles.get(0).click();

        // Verify article page loaded
        wait.until(ExpectedConditions.urlContains("#/article/"));
        WebElement articleTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1")));
        Assertions.assertTrue(articleTitle.isDisplayed(), "Article title should be displayed on article page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        logoutIfLoggedIn();
        login("demo@realworld.io", "demo");
        // Scroll to footer
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        // Define social link selectors and expected domains
        String[][] socials = {
                {"a[href*='twitter.com']", "twitter.com"},
                {"a[href*='facebook.com']", "facebook.com"},
                {"a[href*='linkedin.com']", "linkedin.com"}
        };

        for (String[] social : socials) {
            String selector = social[0];
            String expectedDomain = social[1];
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
            String originalWindow = driver.getWindowHandle();
            Set<String> existingWindows = driver.getWindowHandles();

            link.click();

            // Wait for new window
            wait.until(driver1 -> {
                Set<String> handles = driver1.getWindowHandles();
                return handles.size() > existingWindows.size();
            });

            Set<String> newHandles = driver.getWindowHandles();
            newHandles.removeAll(existingWindows);
            String newWindow = newHandles.iterator().next();
            driver.switchTo().window(newWindow);

            // Verify URL contains expected domain
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains(expectedDomain),
                    "External link should navigate to a URL containing " + expectedDomain);

            // Close external tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}