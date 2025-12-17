package deepseek.ws01.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo{
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

    @BeforeAll
    public static void setup() {
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

    @Test
    @Order(1)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys("invalid_user");
        password.sendKeys("invalid_pass");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"), 
            "Expected error message for invalid credentials");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys(USERNAME);
        password.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), 
            "Expected to be on inventory page after login");
    }

    @Test
    @Order(3)
    public void testItemSorting() {
        login();
        Select sortDropdown = new Select(wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select.product_sort_container"))));
        
        // Test sorting by name A-Z
        sortDropdown.selectByValue("az");
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Sauce Labs Backpack"),
            "First item should be 'Sauce Labs Backpack' after A-Z sort");

        // Test sorting by name Z-A
        sortDropdown.selectByValue("za");
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().startsWith("Test.allTheThings() T-Shirt (Red)"),
            "First item should be 'Test.allTheThings() T-Shirt (Red)' after Z-A sort");

        // Test sorting by price low-high
        sortDropdown.selectByValue("lohi");
        items = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().startsWith("$7.99"),
            "First item should be '$7.99' after price low-high sort");

        // Test sorting by price high-low
        sortDropdown.selectByValue("hilo");
        items = driver.findElements(By.cssSelector(".inventory_item_price"));
        Assertions.assertTrue(items.get(0).getText().startsWith("$49.99"),
            "First item should be '$49.99' after price high-low sort");
    }

    @Test
    @Order(4)
    public void testMenuOptions() {
        login();
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();

        // Test Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOf(resetLink));

        // Test All Items
        menuButton.click();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItemsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"),
            "Expected to be on inventory page after clicking All Items");

        // Test About (external link)
        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("about_sidebar_link")));
        aboutLink.click();
        
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"),
            "Expected to be on saucelabs.com after clicking About");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Logout
        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"),
            "Expected to be on login page after logout");
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        login();
        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_twitter a")));
        twitterLink.click();
        assertExternalLink("twitter.com", originalWindow);

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_facebook a")));
        facebookLink.click();
        assertExternalLink("facebook.com", originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector(".social_linkedin a")));
        linkedinLink.click();
        assertExternalLink("linkedin.com", originalWindow);
    }

    private void login() {
        if (!driver.getCurrentUrl().contains("inventory.html")) {
            driver.get(BASE_URL);
            WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
            WebElement password = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            username.sendKeys(USERNAME);
            password.sendKeys(PASSWORD);
            loginButton.click();
            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void assertExternalLink(String expectedDomain, String originalWindow) {
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
            "Expected to be on " + expectedDomain + " after clicking social link");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}