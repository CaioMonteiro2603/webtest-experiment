package GPT20b.ws03.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assumptions;
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
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

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

    /* ---------------------------------------------------------------------- */
    /* Helper methods                                                         */
    /* ---------------------------------------------------------------------- */
    private void navigateToLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//input[@type='email' or @name='email' or @id='email']")));
    }

    private void login(String email, String password) {
        navigateToLogin();
        WebElement emailField = driver.findElement(
                By.xpath("//input[@type='email' or @name='email' or @id='email']"));
        WebElement passField = driver.findElement(
                By.xpath("//input[@type='password' or @name='password' or @id='password']"));
        WebElement loginBtn = driver.findElement(
                By.xpath("//button[contains(text(),'Login') or @id='login-submit' or @type='submit']"));

        emailField.clear(); emailField.sendKeys(email);
        passField.clear(); passField.sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        // Wait for an element that is only visible after successful login
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Dashboard') or @class='header']")));
    }

    private void resetAppState() {
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(resetLinks.get(0))).click();
            // Wait that dashboard reloads
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h1[contains(text(),'Dashboard') or @class='header']")));
        }
    }

    private void openLinkAndVerifyExternal(String linkText, String expectedDomain) {
        WebElement link = driver.findElement(By.linkText(linkText));
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        switchToNewWindow();
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "URL should contain " + expectedDomain + " after clicking " + linkText);
        driver.close();
        driver.switchTo().window(driver.getWindowHandles().iterator().next());
    }

    private void switchToNewWindow() {
        String original = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                break;
            }
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Tests                                                                  */
    /* ---------------------------------------------------------------------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard") || driver.getCurrentUrl().contains("/account"),
                "After login, URL should contain dashboard or account");
        Assertions.assertTrue(driver.findElements(
                By.xpath("//h1[contains(text(),'Dashboard') or @class='header']")).size() > 0,
                "Dashboard header should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        navigateToLogin();
        WebElement emailField = driver.findElement(
                By.xpath("//input[@type='email' or @name='email' or @id='email']"));
        WebElement passField = driver.findElement(
                By.xpath("//input[@type='password' or @name='password' or @id='password']"));
        WebElement loginBtn = driver.findElement(
                By.xpath("//button[contains(text(),'Login') or @id='login-submit' or @type='submit']"));

        emailField.clear(); emailField.sendKeys("wrong@test.com");
        passField.clear(); passField.sendKeys("wrongpass");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".error-message, .alert, .toast")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortDropdown() {
        login(USER_EMAIL, USER_PASSWORD);

        // Locate sort dropdown; if not present, skip test
        List<WebElement> sortDropdowns = driver.findElements(
                By.cssSelector("select[id='sortDropdown'], select[class*='sort']"));
        Assumptions.assumeTrue(!sortDropdowns.isEmpty(), "Sort dropdown not present, skipping test");

        WebElement sortDropdown = sortDropdowns.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(sortDropdown)).click();
        List<WebElement> options = sortDropdown.findElements(By.tagName("option"));
        Assumptions.assumeTrue(options.size() > 0, "No options in sort dropdown");

        // Capture first item before each sort
        WebElement firstItemBefore = driver.findElement(By.cssSelector(".item-title, .product-name"));
        String firstName = firstItemBefore.getText();
        for (WebElement option : options) {
            wait.until(ExpectedConditions.elementToBeClickable(option)).click();
            WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            localWait.until(ExpectedConditions.textToBePresentInElementLocated(
                    By.cssSelector(".item-title, .product-name"), option.getText()));
            WebElement firstItemAfter = driver.findElement(By.cssSelector(".item-title, .product-name"));
            Assertions.assertNotEquals(firstName, firstItemAfter.getText(),
                    "Item order should change after sorting by " + option.getText());
            // revert to original for next iteration
            firstName = firstItemAfter.getText();
        }
    }

    @Test
    @Order(4)
    public void testMenuActions() {
        login(USER_EMAIL, USER_PASSWORD);

        // Locate burger menu button
        List<WebElement> burgerButtons = driver.findElements(
                By.cssSelector("button[id='menu-toggle'], button[class*='hamburger']"));
        Assumptions.assumeTrue(!burgerButtons.isEmpty(), "Burger menu button not found, skipping test");

        WebElement burger = burgerButtons.get(0);
        // Click to open menu
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        // Click 'All Items' link
        List<WebElement> allItemsLinks = driver.findElements(By.linkText("All Items"));
        if (!allItemsLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(allItemsLinks.get(0))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".inventory-container, .products-list")));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory"),
                    "URL should contain inventory after clicking All Items");
        }

        // Reopen menu
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        // Click 'About' external link
        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        if (!aboutLinks.isEmpty()) {
            openLinkAndVerifyExternal("About", "bugbank.netlify.app");
        }

        // Reopen menu
        wait.until(ExpectedConditions.elementToBeClickable(burger)).click();

        // Click 'Logout'
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            wait.until(ExpectedConditions.elementToBeClickable(logoutLinks.get(0))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//input[@type='email' or @name='email' or @id='email']")));
            Assertions.assertTrue(driver.getCurrentUrl().contains("/log-in") || driver.getCurrentUrl().contains("index"),
                    "Should be back to login page after logout");
        } else {
            Assertions.fail("Logout link not found");
        }

        // Re-login and reset
        login(USER_EMAIL, USER_PASSWORD);
        resetAppState();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/dashboard") || driver.getCurrentUrl().contains("/account"),
                "After reset, should land on main dashboard/account page");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login(USER_EMAIL, USER_PASSWORD);

        // Twitter
        List<WebElement> twitterLinks = driver.findElements(
                By.xpath("//a[contains(@href,'twitter.com')]/img | //a[contains(@href,'twitter.com')]"));
        if (!twitterLinks.isEmpty()) {
            openLinkAndVerifyExternal("Twitter", "twitter.com");
        }

        // Facebook
        List<WebElement> fbLinks = driver.findElements(
                By.xpath("//a[contains(@href,'facebook.com')]/img | //a[contains(@href,'facebook.com')]"));
        if (!fbLinks.isEmpty()) {
            openLinkAndVerifyExternal("Facebook", "facebook.com");
        }

        // LinkedIn
        List<WebElement> liLinks = driver.findElements(
                By.xpath("//a[contains(@href,'linkedin.com')]/img | //a[contains(@href,'linkedin.com')]"));
        if (!liLinks.isEmpty()) {
            openLinkAndVerifyExternal("LinkedIn", "linkedin.com");
        }

        // Return to clean state
        resetAppState();
    }

    @Test
    @Order(6)
    public void testExternalAboutLinkStandalone() {
        login(USER_EMAIL, USER_PASSWORD);

        List<WebElement> aboutLinks = driver.findElements(By.linkText("About"));
        Assumptions.assumeTrue(!aboutLinks.isEmpty(), "About link not present, skipping test");

        openLinkAndVerifyExternal("About", "bugbank.netlify.app");

        // Return to clean state
        resetAppState();
    }
}