package SunaQwen3.ws09.seq04;

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
public class RealWorldAppTest {

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
    void testValidLogin() {
        driver.get(BASE_URL);
        assertTrue(driver.getTitle().contains("Conduit"), "Page title should contain 'Conduit'");

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
        assertTrue(driver.getCurrentUrl().contains("/"), "Should be redirected to home page after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL + "#/login");

        By emailField = By.cssSelector("input[placeholder='Email']");
        By passwordField = By.cssSelector("input[placeholder='Password']");
        By signInButton = By.xpath("//button[contains(text(), 'Sign in')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys("invalid@example.com");
        driver.findElement(passwordField).sendKeys("wrongpass");
        driver.findElement(signInButton).click();

        By errorDiv = By.cssSelector("div.error-messages");
        wait.until(ExpectedConditions.visibilityOfElementLocated(errorDiv));
        WebElement errorElement = driver.findElement(errorDiv);
        assertTrue(errorElement.getText().contains("email or password is invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testNavigationToAboutPage() {
        driver.get(BASE_URL);

        By aboutLink = By.linkText("About");
        WebElement aboutElement = wait.until(ExpectedConditions.elementToBeClickable(aboutLink));
        aboutElement.click();

        By aboutHeader = By.tagName("h1");
        wait.until(ExpectedConditions.visibilityOfElementLocated(aboutHeader));
        assertEquals("About", driver.findElement(aboutHeader).getText(),
                "About page should have 'About' header");
        assertTrue(driver.getCurrentUrl().contains("/about"), "URL should contain '/about'");
    }

    @Test
    @Order(4)
    void testFooterSocialLinks() {
        driver.get(BASE_URL);

        By twitterLink = By.cssSelector("a[href='https://twitter.com/gothinkster']");
        By facebookLink = By.cssSelector("a[href='https://www.facebook.com/thinkster.io']");
        By linkedinLink = By.cssSelector("a[href='https://www.linkedin.com/company/thinkster-io']");

        // Test Twitter link
        String originalWindow = driver.getWindowHandle();
        WebElement twitterElement = wait.until(ExpectedConditions.elementToBeClickable(twitterLink));
        twitterElement.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Facebook link
        WebElement facebookElement = wait.until(ExpectedConditions.elementToBeClickable(facebookLink));
        facebookElement.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinElement = wait.until(ExpectedConditions.elementToBeClickable(linkedinLink));
        linkedinElement.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    void testMenuNavigation() {
        // Ensure logged in
        loginIfNeeded();

        By menuButton = By.cssSelector("button.navbar-toggler");
        WebElement menuButtonElement = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonElement.click();

        By allItemsLink = By.linkText("All Articles");
        WebElement allItemsElement = wait.until(ExpectedConditions.elementToBeClickable(allItemsLink));
        allItemsElement.click();

        By homeHeader = By.tagName("h1");
        wait.until(ExpectedConditions.visibilityOfElementLocated(homeHeader));
        assertEquals("conduit", driver.findElement(homeHeader).getText(),
                "Should return to home feed");
        assertTrue(driver.getCurrentUrl().contains("/"), "Should be on home page");

        // Reopen menu
        menuButtonElement = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonElement.click();

        By favoritesLink = By.linkText("My Articles");
        WebElement favoritesElement = wait.until(ExpectedConditions.elementToBeClickable(favoritesLink));
        favoritesElement.click();

        wait.until(ExpectedConditions.urlContains("/@"));
        assertTrue(driver.getCurrentUrl().contains("/@"),
                "Should navigate to user favorites page");

        // Reopen menu
        menuButtonElement = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonElement.click();

        By settingsLink = By.linkText("Settings");
        WebElement settingsElement = wait.until(ExpectedConditions.elementToBeClickable(settingsLink));
        settingsElement.click();

        By settingsHeader = By.tagName("h1");
        wait.until(ExpectedConditions.visibilityOfElementLocated(settingsHeader));
        assertEquals("Your Settings", driver.findElement(settingsHeader).getText(),
                "Should navigate to settings page");
        assertTrue(driver.getCurrentUrl().contains("/settings"),
                "Should be on settings page");
    }

    @Test
    @Order(6)
    void testLogoutFunctionality() {
        loginIfNeeded();

        By menuButton = By.cssSelector("button.navbar-toggler");
        WebElement menuButtonElement = wait.until(ExpectedConditions.elementToBeClickable(menuButton));
        menuButtonElement.click();

        By logoutLink = By.linkText("Log out");
        WebElement logoutElement = wait.until(ExpectedConditions.elementToBeClickable(logoutLink));
        logoutElement.click();

        By signInLink = By.linkText("Sign in");
        wait.until(ExpectedConditions.elementToBeClickable(signInLink));
        assertTrue(driver.getCurrentUrl().contains("/"),
                "Should be redirected to home page after logout");
        assertTrue(driver.getPageSource().contains("Sign in"),
                "Login link should be visible after logout");
    }

    private void loginIfNeeded() {
        if (driver.getCurrentUrl().equals(BASE_URL) || driver.getCurrentUrl().startsWith(BASE_URL)) {
            try {
                By profileLink = By.cssSelector("a.nav-link");
                if (driver.findElements(profileLink).size() > 0) {
                    return; // Already logged in
                }
            } catch (Exception e) {
                // Continue with login
            }

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
        }
    }
}