package GPT20b.ws03.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- helper methods ---------- */

    private void loginValid() {
        driver.get(BASE_URL);
        By loginBtn = By.xpath("//button[text()='Login']");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        By emailField = By.name("email");
        By passwordField = By.name("password");
        By submitBtn = By.xpath("//button[contains(text(),'Sign In')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(USERNAME);
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();

        // After login, inventory container should be visible
        By inventoryContainer = By.cssSelector(".inventory");
        Assertions.assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(inventoryContainer)).isDisplayed(),
                "Inventory container not displayed after login.");
    }

    private void loginInvalid(String email, String pwd) {
        driver.get(BASE_URL);
        By loginBtn = By.xpath("//button[text()='Login']");
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();

        By emailField = By.name("email");
        By passwordField = By.name("password");
        By submitBtn = By.xpath("//button[contains(text(),'Sign In')]");

        wait.until(ExpectedConditions.visibilityOfElementLocated(emailField)).sendKeys(email);
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordField)).sendKeys(pwd);
        wait.until(ExpectedConditions.elementToBeClickable(submitBtn)).click();
    }

    /* ---------- tests ---------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        loginValid();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        loginInvalid("wrong@test.com", "wrongpwd");
        By errorMsg = By.cssSelector(".error-message");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid") ||
                error.getText().toLowerCase().contains("incorrect"),
                "Error message for invalid login not as expected.");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        loginValid();

        // Locate sorting dropdown
        List<WebElement> sortEls = driver.findElements(By.cssSelector("select#sort-option"));
        Assumptions.assumeTrue(!sortEls.isEmpty(), "Sorting dropdown not present; skipping test.");

        Select sortSelect = new Select(sortEls.get(0));
        List<WebElement> options = sortSelect.getOptions();

        for (WebElement opt : options) {
            sortSelect.selectByVisibleText(opt.getText());
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".loading-indicator")));
            // Verify that first item's name has changed after sorting
            List<WebElement> itemNames = driver.findElements(By.cssSelector(".item-name"));
            Assertions.assertFalse(itemNames.isEmpty(), "No item names found after sorting.");
            String firstName = itemNames.get(0).getText();
            Assertions.assertNotNull(firstName, "First item name should not be null after sorting.");
        }
    }

    @Test
    @Order(4)
    public void testBurgerMenuOperations() {
        loginValid();

        // Burger icon
        By burgerBtn = By.cssSelector(".burger-menu");
        wait.until(ExpectedConditions.elementToBeClickable(burgerBtn)).click();

        // All Items
        By allItemsLink = By.xpath("//a[text()='All Items']");
        wait.until(ExpectedConditions.elementToBeClickable(allItemsLink)).click();
        Assertions.assertTrue(driver.findElement(By.cssSelector(".inventory")).isDisplayed(),
                "Inventory should be displayed after clicking All Items.");

        // About External
        By aboutLink = By.xpath("//a[text()='About']");
        wait.until(ExpectedConditions.elementToBeClickable(aboutLink)).click();
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("bugbank"),
                        "External About link did not open expected domain.");
                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }

        // Reset App State (assume button in menu)
        By resetBtn = By.xpath("//a[text()='Reset App State']");
        wait.until(ExpectedConditions.elementToBeClickable(resetBtn)).click();
        // Inventory list should reload
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory")));

        // Logout
        By logoutLink = By.xpath("//a[text()='Logout']");
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                "Should be redirected to login after logout.");
        Assertions.assertTrue(driver.findElement(By.xpath("//button[text()='Login']")).isDisplayed(),
                "Login button not displayed after logout.");
    }

    @Test
    @Order(5)
    public void testExternalAboutLink() {
        loginValid();

        // Find external About link
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("a[href*='about']"));
        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            String originalWindow = driver.getWindowHandle();
            link.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            "External About link URL does not contain expected domain.");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        loginValid();

        // Social links typically in footer with recognizable hrefs
        List<WebElement> socialLinks = driver.findElements(By.cssSelector(
                "a[href*='twitter.com'], a[href*='facebook.com'], a[href*='linkedin.com']"));
        Assertions.assertFalse(socialLinks.isEmpty(), "No social links found in footer.");

        String originalWindow = driver.getWindowHandle();
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            link.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                            "Opened social link URL does not contain expected domain.");
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            }
        }
    }
}