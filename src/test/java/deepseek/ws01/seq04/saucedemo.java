package deepseek.ws01.seq04;

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
public class SauceDemoTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String USERNAME = "standard_user";
    private static final String PASSWORD = "secret_sauce";

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
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed());
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"));
    }

    @Test
    @Order(3)
    public void testInventorySorting() {
        login();
        
        Select sortDropdown = new Select(driver.findElement(By.className("product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.selectByValue("az");
        List<WebElement> itemsAZ = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(itemsAZ.get(0).getText().compareTo(itemsAZ.get(1).getText()) <= 0);
        
        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        List<WebElement> itemsZA = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(itemsZA.get(0).getText().compareTo(itemsZA.get(1).getText()) >= 0);
        
        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        List<WebElement> pricesLOHI = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(Double.parseDouble(pricesLOHI.get(0).getText().substring(1)) <= 
                            Double.parseDouble(pricesLOHI.get(1).getText().substring(1)));
        
        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        List<WebElement> pricesHILO = driver.findElements(By.className("inventory_item_price"));
        Assertions.assertTrue(Double.parseDouble(pricesHILO.get(0).getText().substring(1)) >= 
                            Double.parseDouble(pricesHILO.get(1).getText().substring(1)));
        
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"));
        
        // Open menu again
        menuButton.click();
        
        // Test About (external)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
        
        // Log back in for remaining tests
        login();
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        login();
        
        // Test Twitter link
        testSocialLink("social_twitter", "twitter.com");
        // Test Facebook link
        testSocialLink("social_facebook", "facebook.com");
        // Test LinkedIn link
        testSocialLink("social_linkedin", "linkedin.com");
    }

    private void testSocialLink(String linkId, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement socialLink = wait.until(ExpectedConditions.elementToBeClickable(By.className(linkId)));
        socialLink.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain));
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    private void resetAppState() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        
        // Close menu
        WebElement closeMenu = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeMenu.click();
    }
}