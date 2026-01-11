package Qwen3.ws01.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class saucedemo {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");

        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.manage()
              .timeouts()
              .implicitlyWait(Duration.ofSeconds(10));
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
        driver.get("https://www.saucedemo.com/v1/index.html");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("standard_user");
        passwordField.sendKeys("secret_sauce");
        loginButton.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("inventory.html"), "Login should redirect to inventory page");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get("https://www.saucedemo.com/v1/index.html");

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test='error']")));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed on invalid login");
    }

    @Test
    @Order(3)
    public void testInventorySorting() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[data-test='product_sort_container']")));
        Select sortSelect = new Select(sortDropdown);

        sortSelect.selectByVisibleText("Name (A to Z)");
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertEquals("Sauce Labs Backpack", items.get(0).getText());

        sortSelect.selectByVisibleText("Name (Z to A)");
        items = driver.findElements(By.cssSelector(".inventory_item_name"));
        assertEquals("Test.allTheThings() T-Shirt (Red)", items.get(0).getText());

        sortSelect.selectByVisibleText("Price (low to high)");
        items = driver.findElements(By.cssSelector(".inventory_item_price"));
        assertEquals("$7.99", items.get(0).getText());

        sortSelect.selectByVisibleText("Price (high to low)");
        items = driver.findElements(By.cssSelector(".inventory_item_price"));
        assertEquals("$49.99", items.get(0).getText());
    }

    @Test
    @Order(4)
    public void testMenuFunctionality() {
        driver.get("https://www.saucedemo.com/v1/inventory.html");

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".bm-burger-button")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("inventory.html"), "Should navigate to inventory page");

        menuButton.click();
        WebElement aboutLink = driver.findElement(By.id("about_sidebar_link"));
        aboutLink.click();

        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                break;
            }
        }

        String aboutUrl = driver.getCurrentUrl();
        assertTrue(aboutUrl.contains("saucelabs"), "About page should open on saucelabs domain");
        driver.close();
        driver.switchTo().window(parentWindow);

        menuButton.click();
        WebElement logoutLink = driver.findElement(By.id("logout_sidebar_link"));
        logoutLink.click();

        String loginPageUrl = driver.getCurrentUrl();
        assertTrue(loginPageUrl.contains("index.html"), "Should navigate back to login page after logout");

        driver.get("https://www.saucedemo.com/v1/inventory.html");
        menuButton.click();
        WebElement resetAppLink = driver.findElement(By.id("reset_sidebar_link"));
        resetAppLink.click();

        // Reset done, should stay on same page
        String resetPageUrl = driver.getCurrentUrl();
        assertTrue(resetPageUrl.contains("inventory.html"), "Should remain on inventory page after reset");
    }

    @Test
    @Order(5)
    public void testExternalLinksInFooter() {
        driver.get("https://www.saucedemo.com/v1/index.html");

        String parentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(parentWindow)) {
                driver.switchTo().window(window);
                driver.close();
            }
        }

        // Footer links - Twitter
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_twitter a")));
        twitterLink.click();
        
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("twitter"), "Should open Twitter link in new tab");
        driver.close();
        driver.switchTo().window(parentWindow);

        // Footer links - Facebook
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_facebook a")));
        facebookLink.click();

        currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("facebook"), "Should open Facebook link in new tab");
        driver.close();
        driver.switchTo().window(parentWindow);

        // Footer links - LinkedIn
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".social_linkedin a")));
        linkedinLink.click();

        currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("linkedin"), "Should open LinkedIn link in new tab");
        driver.close();
        driver.switchTo().window(parentWindow);
    }
}