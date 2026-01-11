package SunaDeepSeek.ws03.seq09;

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

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

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
    public void testLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        
        WebElement accountInfo = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".account-info")));
        Assertions.assertTrue(accountInfo.isDisplayed(), "Login was not successful");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        
        WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        
        emailField.sendKeys("invalid@email.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();
        
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-message")));
        Assertions.assertTrue(errorMessage.isDisplayed(), "Error message not shown for invalid login");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        driver.get(BASE_URL);
        login();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();
        
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "All Items link did not navigate correctly");
        
        menuButton.click();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        about.click();
        
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs.com"), "About page did not open correctly");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        menuButton.click();
        WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logout.click();
        Assertions.assertTrue(driver.getCurrentUrl().equals(BASE_URL), "Logout did not work correctly");
    }

    @Test
    @Order(4)
    public void testSorting() {
        driver.get(BASE_URL);
        login();
        
        Select sortDropdown = new Select(driver.findElement(By.className("product_sort_container")));
        
        // Test Name (A to Z)
        sortDropdown.selectByValue("az");
        List<WebElement> items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().compareTo(items.get(1).getText()) < 0, 
            "Sorting A-Z not working correctly");
        
        // Test Name (Z to A)
        sortDropdown.selectByValue("za");
        items = driver.findElements(By.className("inventory_item_name"));
        Assertions.assertTrue(items.get(0).getText().compareTo(items.get(1).getText()) > 0, 
            "Sorting Z-A not working correctly");
        
        // Test Price (low to high)
        sortDropdown.selectByValue("lohi");
        List<WebElement> prices = driver.findElements(By.className("inventory_item_price"));
        double firstPrice = Double.parseDouble(prices.get(0).getText().substring(1));
        double secondPrice = Double.parseDouble(prices.get(1).getText().substring(1));
        Assertions.assertTrue(firstPrice <= secondPrice, 
            "Sorting price low to high not working correctly");
        
        // Test Price (high to low)
        sortDropdown.selectByValue("hilo");
        prices = driver.findElements(By.className("inventory_item_price"));
        firstPrice = Double.parseDouble(prices.get(0).getText().substring(1));
        secondPrice = Double.parseDouble(prices.get(1).getText().substring(1));
        Assertions.assertTrue(firstPrice >= secondPrice, 
            "Sorting price high to low not working correctly");
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        driver.get(BASE_URL);
        login();
        
        String originalWindow = driver.getWindowHandle();
        
        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter a")));
        twitterLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link not working");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook a")));
        facebookLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link not working");
        driver.close();
        driver.switchTo().window(originalWindow);
        
        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin a")));
        linkedinLink.click();
        
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.contentEquals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link not working");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testResetAppState() {
        driver.get(BASE_URL);
        login();
        
        // Add item to cart first
        WebElement addToCart = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn_inventory")));
        addToCart.click();
        
        // Open menu and reset
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-button")));
        menuButton.click();
        WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        reset.click();
        
        // Verify cart is empty
        List<WebElement> cartItems = driver.findElements(By.className("shopping_cart_badge"));
        Assertions.assertEquals(0, cartItems.size(), "Reset app state did not clear the cart");
    }

    private void login() {
        if (driver.getCurrentUrl().equals(BASE_URL)) {
            WebElement emailField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
            
            emailField.sendKeys(LOGIN);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();
            
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".account-info")));
        }
    }
}