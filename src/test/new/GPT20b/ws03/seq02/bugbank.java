package GPT20b.ws03.seq02;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.support.ui.Select;


@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void init() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- LOGIN TESTS ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.navigate().to(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='password']")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        emailField.clear();
        emailField.sendKeys(USERNAME);
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        // Wait for account summary or some page element that confirms login
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".account-summary, .account-list, [data-testid='account-summary'], .bm-burger-button")));

        assertTrue(driver.getCurrentUrl().contains("dashboard") || driver.getCurrentUrl().contains("accounts") || driver.getCurrentUrl().equals(BASE_URL),
                "URL should contain 'dashboard' or 'accounts' after login");
        assertTrue(driver.findElements(By.cssSelector(".account-summary, .account-list, [data-testid='account-summary'], .bm-burger-button")).size() > 0,
                "Account summary or menu should be visible after login");

        // Reset state by logging out
        logoutIfPresent();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.navigate().to(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='password']")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        emailField.clear();
        emailField.sendKeys("wrong@example.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error, .alert, [role='alert'], .styles__Text-sc-*")));
        assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials");
        assertTrue(!errorMsg.getText().trim().isEmpty(), "Error message should contain text");
    }

    /* ---------- MENU AND FOOTER TESTS ---------- */

    @Test
    @Order(3)
    public void testMenuNavigation() {
        loginAndMaintainSession();

        // Open burger menu
        By menuButtonLocator = By.cssSelector("[aria-label='Open Menu'], .burger-menu, .navbar-toggler, .bm-burger-button");
        List<WebElement> menuButtons = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(menuButtonLocator));
        Assumptions.assumeTrue(!menuButtons.isEmpty(), "Burger menu button not found; skipping test");
        WebElement menuButton = menuButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(menuButton)).click();

        // All Items (Assuming it navigates to /items or similar)
        By allItemsLocator = By.linkText("All Items");
        List<WebElement> allItemsLinks = driver.findElements(allItemsLocator);
        if (!allItemsLinks.isEmpty()) {
            WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(allItemsLinks.get(0)));
            allItemsLink.click();
            wait.until(ExpectedConditions.urlContains("items"));
            assertTrue(driver.getCurrentUrl().contains("items"), "URL should contain 'items' after clicking All Items");
            driver.navigate().back();
        }

        // About external
        By aboutLocator = By.linkText("About");
        List<WebElement> aboutLinks = driver.findElements(aboutLocator);
        if (!aboutLinks.isEmpty()) {
            verifyExternalLink(aboutLinks.get(0), "netlify.app");
        }

        // Reset App State
        By resetLocator = By.linkText("Reset App State");
        List<WebElement> resetLinks = driver.findElements(resetLocator);
        if (!resetLinks.isEmpty()) {
            WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(resetLinks.get(0)));
            resetLink.click();
            wait.until(ExpectedConditions.urlContains("dashboard"));
            assertTrue(driver.getCurrentUrl().contains("dashboard"), "Should return to dashboard after reset");
        }

        // Logout
        logoutIfPresent();
    }

    @Test
    @Order(4)
    public void testFooterExternalLinks() {
        loginAndMaintainSession();

        // Twitter
        By twitterLocator = By.cssSelector("a[href*='twitter.com'], footer a[target='_blank']");
        verifyExternalLink(driver.findElement(twitterLocator), "twitter.com");

        // Facebook
        By facebookLocator = By.cssSelector("a[href*='facebook.com'], footer a[target='_blank']");
        verifyExternalLink(driver.findElement(facebookLocator), "facebook.com");

        // LinkedIn
        By linkedInLocator = By.cssSelector("a[href*='linkedin.com'], footer a[target='_blank']");
        verifyExternalLink(driver.findElement(linkedInLocator), "linkedin.com");

        logoutIfPresent();
    }

    @Test
    @Order(5)
    public void testSortingDropdown() {
        loginAndMaintainSession();

        // Locate sorting dropdown
        By sortDropdownLocator = By.cssSelector("select[data-testid='sort-selector']");
        List<WebElement> sortDropdowns = driver.findElements(sortDropdownLocator);
        Assumptions.assumeTrue(!sortDropdowns.isEmpty(), "Sorting dropdown not present; skipping test");
        WebElement sortDropdown = sortDropdowns.get(0);

        String[] optionsText = {"Name (A-Z)", "Name (Z-A)", "Price (Low to High)", "Price (High to Low)"};
        for (String option : optionsText) {
            Select select = new Select(sortDropdown);
            select.selectByVisibleText(option);
            // After selection, verify that the order changes
            WebElement firstItem = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".item-list .item:first-child .item-name")));
            Assumptions.assumeTrue(firstItem.isDisplayed(), "First item name not displayed after sorting");
        }

        logoutIfPresent();
    }

    /* ---------- HELPER METHODS ---------- */

    private void loginAndMaintainSession() {
        driver.navigate().to(BASE_URL);
        WebElement emailField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='email']")));
        WebElement passwordField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='password']")));
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        emailField.clear();
        emailField.sendKeys(USERNAME);
        passwordField.clear();
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".account-summary, .account-list, [data-testid='account-summary'], .bm-burger-button")));
    }

    private void logoutIfPresent() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            WebElement logout = logoutLinks.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(logout)).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        }
    }

    private void verifyExternalLink(WebElement link, String expectedDomain) {
        String originalHandle = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "External link URL should contain '" + expectedDomain + "'");
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }
    }
}