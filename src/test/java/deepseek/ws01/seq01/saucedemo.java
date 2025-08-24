package deepseek.ws01.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class SauceDemoTest {
    private static WebDriver driver;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String LOGIN = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
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
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Login failed or didn't redirect to inventory page");
        Assertions.assertTrue(driver.findElements(By.className("inventory_item")).size() > 0, "Inventory items not loaded");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"), 
            "Expected error message not displayed");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Select sortingDropdown = new Select(driver.findElement(By.className("product_sort_container")));
        
        // Test Name (A to Z)
        sortingDropdown.selectByVisibleText("Name (A to Z)");
        List<WebElement> items = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
            By.className("inventory_item_name")));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"), 
            "First item not as expected for A-Z sorting");
        
        // Test Name (Z to A)
        sortingDropdown.selectByVisibleText("Name (Z to A)");
        wait.until(ExpectedConditions.textToBePresentInElement(
            driver.findElements(By.className("inventory_item_name")).get(0), "Test.allTheThings()"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings()"), 
            "First item not as expected for Z-A sorting");
        
        // Test Price (low to high)
        sortingDropdown.selectByVisibleText("Price (low to high)");
        wait.until(ExpectedConditions.textToBePresentInElement(
            driver.findElements(By.className("inventory_item_price")).get(0), "$7.99"));
        Assertions.assertTrue(driver.findElements(By.className("inventory_item_price")).get(0).getText().equals("$7.99"), 
            "First price not as expected for low-high sorting");
        
        // Test Price (high to low)
        sortingDropdown.selectByVisibleText("Price (high to low)");
        wait.until(ExpectedConditions.textToBePresentInElement(
            driver.findElements(By.className("inventory_item_price")).get(0), "$49.99"));
        Assertions.assertTrue(driver.findElements(By.className("inventory_item_price")).get(0).getText().equals("$49.99"), 
            "First price not as expected for high-low sorting");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[id='react-burger-menu-btn']")));
        menuButton.click();
        
        // Test All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItemsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), 
            "All Items link didn't redirect to inventory page");
        
        // Open menu again
        menuButton.click();
        
        // Test About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("about_sidebar_link")));
        aboutLink.click();
        
        // Switch to new tab and verify domain
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), 
            "About link didn't redirect to correct domain");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton.click();
        
        // Test Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
        Assertions.assertTrue(driver.getCurrentUrl().endsWith("index.html"), 
            "Logout didn't redirect to login page");
        
        // Login again for remaining tests
        testValidLogin();
        
        // Open menu once more
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[id='react-burger-menu-btn']")));
        menuButton.click();
        
        // Test Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        resetLink.click();
        WebElement closeButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("button[id='react-burger-cross-btn']")));
        closeButton.click();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login();
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_twitter a")));
        twitterLink.click();
        verifyExternalLink("twitter.com", originalWindow);
        
        // Test Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_facebook a")));
        facebookLink.click();
        verifyExternalLink("facebook.com", originalWindow);
        
        // Test LinkedIn
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_linkedin a")));
        linkedinLink.click();
        verifyExternalLink("linkedin.com", originalWindow);
    }

    private void verifyExternalLink(String expectedDomain, String originalWindow) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "Link didn't redirect to " + expectedDomain);
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }
}