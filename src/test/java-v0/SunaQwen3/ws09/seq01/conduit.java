package SunaQwen3.ws09.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;


@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpass";

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
        Assertions.assertTrue(driver.getTitle().contains("Conduit"), "Page title should contain 'Conduit'");

        By signInLink = By.linkText("Sign in");
        WebElement signInElement = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signInElement.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(signInButton).click();

        By homeLink = By.linkText("Home");
        wait.until(ExpectedConditions.visibilityOfElementLocated(homeLink));

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("#/"), "URL should contain '#/' after login");
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

        By errorDiv = By.cssSelector(".error-messages li");
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(errorDiv));

        Assertions.assertEquals("email or password is invalid", errorElement.getText().toLowerCase(),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigation_AllItems() {
        // Ensure logged in
        testValidLogin();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By allItemsLink = By.linkText("All Articles");
        WebElement allItemsEl = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
        allItemsEl.click();

        By articleList = By.cssSelector(".article-preview");
        wait.until(ExpectedConditions.presenceOfElementLocated(articleList));

        List<WebElement> articles = driver.findElements(articleList);
        Assertions.assertTrue(articles.size() > 0, "At least one article should be displayed");
    }

    @Test
    @Order(4)
    public void testMenuNavigation_About_External() {
        // Ensure logged in
        testValidLogin();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By aboutLink = By.linkText("About");
        WebElement aboutEl = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        String originalWindow = driver.getWindowHandle();
        aboutEl.click();

        // Wait for new window and switch
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String aboutUrl = driver.getCurrentUrl();
        Assertions.assertTrue(aboutUrl.contains("realworld.io"), "About page should be on realworld.io domain");

        // Close external tab and return
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testMenuNavigation_Logout() {
        // Ensure logged in
        testValidLogin();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By logoutLink = By.linkText("Log out");
        WebElement logoutEl = wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
        logoutEl.click();

        // Wait for redirect to home
        By homeLink = By.linkText("Home");
        wait.until(ExpectedConditions.visibilityOfElementLocated(homeLink));

        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("#/"), "Should be redirected to home after logout");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks_Twitter() {
        driver.get(BASE_URL);

        By twitterLink = By.cssSelector("a[href*='twitter.com']");
        WebElement twitterEl = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        String originalWindow = driver.getWindowHandle();
        twitterEl.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String twitterUrl = driver.getCurrentUrl();
        Assertions.assertTrue(twitterUrl.contains("twitter.com"), "Twitter link should open twitter.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks_Facebook() {
        driver.get(BASE_URL);

        By facebookLink = By.cssSelector("a[href*='facebook.com']");
        WebElement facebookEl = wait.until(ExpectedConditions.elementToBeClickable(facebookLink));
        String originalWindow = driver.getWindowHandle();
        facebookEl.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String facebookUrl = driver.getCurrentUrl();
        Assertions.assertTrue(facebookUrl.contains("facebook.com"), "Facebook link should open facebook.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks_LinkedIn() {
        driver.get(BASE_URL);

        By linkedInLink = By.cssSelector("a[href*='linkedin.com']");
        WebElement linkedInEl = wait.until(ExpectedConditions.elementToBeClickable(linkedInLink));
        String originalWindow = driver.getWindowHandle();
        linkedInEl.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        String linkedInUrl = driver.getCurrentUrl();
        Assertions.assertTrue(linkedInUrl.contains("linkedin.com"), "LinkedIn link should open linkedin.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    public void testArticleList_SortingDropdown() {
        testValidLogin();

        By sortDropdown = By.cssSelector("select[ng-model='sortType']");
        if (driver.findElements(By.cssSelector("select[ng-model='sortType']")).size() > 0) {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortDropdown));
            dropdown.click();

            By newestOption = By.cssSelector("option[value='createdAt']");
            By oldestOption = By.cssSelector("option[value='-createdAt']");
            By popularOption = By.cssSelector("option[value='favoritesCount']");
            By unpopularOption = By.cssSelector("option[value='-favoritesCount']");

            // Test Newest First
            driver.findElement(newestOption).click();
            wait.until(ExpectedConditions.stalenessOf(driver.findElement(By.cssSelector(".article-preview"))));
            List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
            Assertions.assertTrue(articles.size() > 0, "At least one article should be displayed after sorting");

            // Test Oldest First
            dropdown.click();
            driver.findElement(oldestOption).click();
            wait.until(ExpectedConditions.stalenessOf(articles.get(0)));
            articles = driver.findElements(By.cssSelector(".article-preview"));
            Assertions.assertTrue(articles.size() > 0, "At least one article should be displayed after sorting");

            // Test Most Popular
            dropdown.click();
            driver.findElement(popularOption).click();
            wait.until(ExpectedConditions.stalenessOf(articles.get(0)));
            articles = driver.findElements(By.cssSelector(".article-preview"));
            Assertions.assertTrue(articles.size() > 0, "At least one article should be displayed after sorting");

            // Test Least Popular
            dropdown.click();
            driver.findElement(unpopularOption).click();
            wait.until(ExpectedConditions.stalenessOf(articles.get(0)));
            articles = driver.findElements(By.cssSelector(".article-preview"));
            Assertions.assertTrue(articles.size() > 0, "At least one article should be displayed after sorting");
        }
    }

    @Test
    @Order(10)
    public void testMenu_ResetAppState() {
        testValidLogin();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menuButtonEl = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonEl.click();

        By resetLink = By.linkText("Reset App State");
        if (driver.findElements(resetLink).size() > 0) {
            WebElement resetEl = wait.until(ExpectedConditions.elementToBeClickable(resetLink));
            resetEl.click();

            // Wait for any potential reload or state reset
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".article-preview")));

            List<WebElement> articles = driver.findElements(By.cssSelector(".article-preview"));
            Assertions.assertTrue(articles.size() > 0, "Articles should be visible after reset");
        }
    }
}