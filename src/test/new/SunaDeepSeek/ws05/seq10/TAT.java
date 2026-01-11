package SunaDeepSeek.ws05.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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
    public void testLoginWithValidCredentials() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[data-test='username']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[data-test='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[data-test='login-button']"));
        
        usernameField.sendKeys("standard_user");
        passwordField.sendKeys("secret_sauce");
        loginButton.click();
        
        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), "Login failed - not redirected to inventory page");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        driver.get(BASE_URL);
        
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[data-test='username']")));
        WebElement passwordField = driver.findElement(By.cssSelector("input[data-test='password']"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[data-test='login-button']"));
        
        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();
        
        WebElement errorElement = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorElement.getText().contains("Username and password do not match"), 
            "Expected error message not displayed");
    }

    @Test
    @Order(3)
    public void testSortingDropdownOptions() {
        testLoginWithValidCredentials();
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select.product_sort_container")));
        sortDropdown.click();
        
        List<WebElement> options = driver.findElements(By.cssSelector("select.product_sort_container option"));
        Assertions.assertEquals(4, options.size(), "Unexpected number of sorting options");
        
        // Test each sorting option
        testSortingOption("az", "Name (A to Z)");
        testSortingOption("za", "Name (Z to A)");
        testSortingOption("lohi", "Price (low to high)");
        testSortingOption("hilo", "Price (high to low)");
    }

    private void testSortingOption(String value, String displayText) {
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select.product_sort_container")));
        sortDropdown.click();
        
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("option[value='" + value + "']")));
        option.click();
        
        WebElement firstItem = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".inventory_item_name")));
        
        if (value.equals("az")) {
            Assertions.assertTrue(firstItem.getText().startsWith("Sauce Labs Backpack"), 
                "Items not sorted A-Z correctly");
        } else if (value.equals("za")) {
            Assertions.assertTrue(firstItem.getText().startsWith("Test.allTheThings() T-Shirt (Red)"), 
                "Items not sorted Z-A correctly");
        }
    }

    @Test
    @Order(4)
    public void testMenuButtonFunctionality() {
        testLoginWithValidCredentials();
        
        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        // Test All Items
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory.html"), 
            "All Items link didn't work");
        
        // Open menu again
        menuButton.click();
        
        // Test About (external link)
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), 
            "About link didn't open correct external page");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Open menu again
        menuButton.click();
        
        // Test Logout
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"), 
            "Logout didn't redirect to login page");
        
        // Login again for remaining tests
        testLoginWithValidCredentials();
    }

    @Test
    @Order(5)
    public void testResetAppState() {
        testLoginWithValidCredentials();
        
        // Add item to cart
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("add-to-cart-sauce-labs-backpack")));
        addToCart.click();
        
        // Verify item was added
        WebElement cartBadge = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector(".shopping_cart_badge")));
        Assertions.assertEquals("1", cartBadge.getText(), "Item not added to cart");
        
        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("react-burger-menu-btn")));
        menuButton.click();
        
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(
            By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        List<WebElement> cartBadges = driver.findElements(By.cssSelector(".shopping_cart_badge"));
        Assertions.assertEquals(0, cartBadges.size(), "Cart not reset");
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        testLoginWithValidCredentials();
        
        // Test Twitter link
        testExternalLink("Twitter", "twitter.com");
        
        // Test Facebook link
        testExternalLink("Facebook", "facebook.com");
        
        // Test LinkedIn link
        testExternalLink("LinkedIn", "linkedin.com");
    }

    private void testExternalLink(String linkText, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), '" + linkText + "')]")));
        link.click();
        
        // Switch to new tab
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            linkText + " link didn't open correct external page");
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}