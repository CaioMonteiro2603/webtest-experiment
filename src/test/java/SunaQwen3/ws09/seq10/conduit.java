package SunaQwen3.ws09.seq10;

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
    void testLoginPageLoadsSuccessfully() {
        driver.get(BASE_URL);
        String title = driver.getTitle();
        assertTrue(title.contains("Conduit"), "Page title should contain 'Conduit'");
        Assertions.assertEquals(BASE_URL, driver.getCurrentUrl(), "Current URL should match base URL");
    }

    @Test
    @Order(2)
    void testValidLoginRedirectsToHomePage() {
        driver.get(BASE_URL);
        By signInLink = By.linkText("Sign in");
        WebElement signInButton = wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        signInButton.click();

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By submitButton = By.cssSelector("button[type='submit']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(submitButton).click();

        By homeHeader = By.cssSelector("h1");
        String headerText = wait.until(ExpectedConditions.visibilityOfElementLocated(homeHeader)).getText();
        assertEquals("Your Feed", headerText, "Should be redirected to home feed after login");
        assertTrue(driver.getCurrentUrl().contains("#/"), "URL should indicate home route");
    }

    @Test
    @Order(3)
    void testInvalidLoginShowsError() {
        driver.get(BASE_URL + "#/login");
        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By submitButton = By.cssSelector("button[type='submit']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        driver.findElement(passwordField).sendKeys("wrongpass");
        driver.findElement(submitButton).click();

        By errorList = By.cssSelector(".error-messages li");
        String errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(errorList)).getText();
        assertTrue(errorMessage.contains("email or password is invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    void testNavigationMenuAllItems() {
        navigateToHomeIfNecessary();
        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menu.click();

        By allItemsLink = By.linkText("All Articles");
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
        allItems.click();

        By articleList = By.cssSelector(".article-preview");
        wait.until(ExpectedConditions.presenceOfElementLocated(articleList));
        List<WebElement> articles = driver.findElements(articleList);
        assertTrue(articles.size() > 0, "At least one article should be displayed");
    }

    @Test
    @Order(5)
    void testNavigationMenuAboutExternalLink() {
        navigateToHomeIfNecessary();
        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menu.click();

        By aboutLink = By.linkText("About");
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        about.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }

        assertNotNull(newWindow, "New window should have been opened");
        driver.switchTo().window(newWindow);

        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains("realworld.io"), "External About link should open realworld.io domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    void testFooterSocialLinks() {
        driver.get(BASE_URL);
        By twitterLink = By.cssSelector("a[href*='twitter.com']");
        By facebookLink = By.cssSelector("a[href*='facebook.com']");
        By linkedinLink = By.cssSelector("a[href*='linkedin.com']");

        String originalWindow = driver.getWindowHandle();

        // Test Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        twitter.click();
        assertNewWindowAndClose(originalWindow, "twitter.com");

        // Test Facebook
        driver.switchTo().window(originalWindow);
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(facebookLink));
        facebook.click();
        assertNewWindowAndClose(originalWindow, "facebook.com");

        // Test LinkedIn
        driver.switchTo().window(originalWindow);
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(linkedinLink));
        linkedin.click();
        assertNewWindowAndClose(originalWindow, "linkedin.com");
    }

    @Test
    @Order(7)
    void testLogoutFunctionality() {
        // Ensure logged in
        loginIfNotLoggedIn();

        By menuButton = By.cssSelector(".navbar-burger");
        WebElement menu = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menu.click();

        By logoutLink = By.linkText("Log out");
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
        logout.click();

        By signInLink = By.linkText("Sign in");
        wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        assertTrue(driver.getCurrentUrl().contains("#/"), "Should be redirected to home after logout");
        assertTrue(driver.findElement(signInLink).isDisplayed(), "Sign in link should be visible after logout");
    }

    private void navigateToHomeIfNecessary() {
        if (!driver.getCurrentUrl().contains("#/")) {
            driver.get(BASE_URL);
            loginIfNotLoggedIn();
        }
    }

    private void loginIfNotLoggedIn() {
        try {
            By menuButton = By.cssSelector(".navbar-burger");
            if (driver.findElements(menuButton).size() > 0) {
                return; // Already logged in
            }
        } catch (NoSuchElementException ignored) {
        }

        driver.get(BASE_URL + "#/login");
        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By submitButton = By.cssSelector("button[type='submit']");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        driver.findElement(passwordField).sendKeys(PASSWORD);
        driver.findElement(submitButton).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".navbar-burger")));
    }

    private void assertNewWindowAndClose(String originalWindow, String expectedDomain) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }

        assertNotNull(newWindow, "New window should have been opened");
        driver.switchTo().window(newWindow);

        String newUrl = driver.getCurrentUrl();
        assertTrue(newUrl.contains(expectedDomain), "External link should open expected domain: " + expectedDomain);

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}