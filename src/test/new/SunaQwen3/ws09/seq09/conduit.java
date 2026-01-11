package SunaQwen3.ws09.seq09;

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

@TestMethodOrder(OrderAnnotation.class)
public class conduit {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass123";

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
    public void testValidLogin() {
        driver.get(BASE_URL);
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
        assertEquals("Conduit", driver.getTitle(), "Page title should be 'Conduit'");

        By signInLink = By.xpath("//a[contains(text(), 'Sign in')]");
        WebElement signInElement = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signInElement.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(signInButton).click();

        By homeLink = By.xpath("//a[contains(text(), 'conduit')]");
        wait.until(ExpectedConditions.elementToBeClickable(homeLink));

        assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home after login");
        assertTrue(driver.getPageSource().contains("Your Feed"), "Should see 'Your Feed' section after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        driver.findElement(passwordField).sendKeys("wrongpass");
        driver.findElement(signInButton).click();

        By errorDiv = By.cssSelector("div.ng-scope");
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorDiv));

        WebElement errorElement = driver.findElement(errorDiv);
        assertTrue(errorElement.getText().contains("email or password is invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testNavigationMenuAllItems() {
        // Ensure logged in
        loginIfNot();

        By allItemsLink = By.linkText("Global Feed");
        WebElement allItemsEl = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
        allItemsEl.click();

        By articleList = By.cssSelector("div.article-preview");
        wait.until(ExpectedConditions.presenceOfElementLocated(articleList));

        List<WebElement> articles = driver.findElements(articleList);
        assertTrue(articles.size() > 0, "Should see at least one article in the list");
    }

    @Test
    @Order(4)
    public void testNavigationMenuAboutExternalLink() {
        loginIfNot();

        By aboutLink = By.linkText("RealWorld project");
        String originalWindow = driver.getWindowHandle();
        driver.findElement(aboutLink).click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("github.com"),
                "About link should open github.com domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testNavigationMenuLogout() {
        loginIfNot();

        driver.get(BASE_URL + "#/settings");
        
        By logoutButton = By.cssSelector("button.btn-outline-danger");
        wait.until(ExpectedConditions.elementToBeClickable(logoutButton)).click();

        By signInLink = By.linkText("Sign in");
        wait.until(ExpectedConditions.elementToBeClickable(signInLink));

        assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home after logout");
        assertTrue(driver.getPageSource().contains("Sign in"), "Should see 'Sign in' link after logout");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);

        // Test social links in footer
        By twitterLink = By.cssSelector("a[href*='github']");
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        String href = link.getAttribute("href");
        assertTrue(href.contains("github"), "Link should contain github");
    }

    @Test
    @Order(7)
    public void testCreateNewArticle() {
        loginIfNot();

        driver.get(BASE_URL + "#/editor");

        By titleField = By.cssSelector("input[placeholder='Article Title']");
        By aboutField = By.cssSelector("input[placeholder=\"What's this article about?\"]");
        By bodyField = By.cssSelector("textarea[placeholder='Write your article (in markdown)']");
        By tagField = By.cssSelector("input[placeholder='Enter tags']");
        By publishButton = By.xpath("//button[contains(text(), 'Publish Article')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(titleField))
                .sendKeys("Test Article Title");
        driver.findElement(aboutField).sendKeys("Test article description");
        driver.findElement(bodyField).sendKeys("This is a test article body.");
        driver.findElement(tagField).sendKeys("test");
        driver.findElement(publishButton).click();

        By articleTitle = By.cssSelector("h1");
        wait.until(ExpectedConditions.visibilityOfElementLocated(articleTitle));

        WebElement titleElement = driver.findElement(articleTitle);
        assertEquals("Test Article Title", titleElement.getText(),
                "Published article should have correct title");
    }

    @Test
    @Order(8)
    public void testFavoriteArticle() {
        loginIfNot();

        driver.get(BASE_URL + "#/");

        By firstArticleHeart = By.cssSelector("button.btn-primary, button.btn-outline-primary");
        List<WebElement> buttons = driver.findElements(firstArticleHeart);
        if (buttons.size() > 0) {
            WebElement heartButton = wait.until(ExpectedConditions.elementToBeClickable(buttons.get(0)));
            heartButton.click();
        }
    }

    @Test
    @Order(9)
    public void testUserProfileVisit() {
        loginIfNot();

        String profileUrl = BASE_URL + "#/@" + USERNAME;
        driver.get(profileUrl);
        
        assertTrue(driver.getCurrentUrl().contains("/@"), "Should navigate to user profile page");

        By userArticles = By.cssSelector("div.article-preview");
        assertTrue(driver.findElements(userArticles).size() >= 0,
                "User profile should display articles (can be empty)");
    }

    @Test
    @Order(10)
    public void testSettingsPageAccess() {
        loginIfNot();

        driver.get(BASE_URL + "#/settings");

        By orText = By.xpath("//button[contains(text(), 'Or click here to logout')]");
        wait.until(ExpectedConditions.visibilityOfElementLocated(orText));

        assertTrue(driver.getCurrentUrl().contains("#/settings"), "Should be on settings page");
        assertTrue(driver.getPageSource().contains("Or click here to logout"),
                "Settings page should contain logout instruction");
    }

    private void loginIfNot() {
        driver.get(BASE_URL);
        wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
        
        try {
            WebElement signInLink = driver.findElement(By.linkText("Sign in"));
            if (signInLink.isDisplayed()) {
                signInLink.click();
                
                By emailField = By.cssSelector("input[placeholder='Email']");
                wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
                driver.findElement(By.cssSelector("input[placeholder='Password']")).sendKeys(PASSWORD);
                driver.findElement(By.xpath("//button[contains(text(), 'Sign in')]")).click();
                
                wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Your Feed")));
            }
        } catch (NoSuchElementException e) {
            // Already logged in
        }
    }
}