package GPT4.ws01.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class saucedemo {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";

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

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.id("user-name")));
        WebElement passField = driver.findElement(By.id("password"));
        WebElement loginBtn = driver.findElement(By.id("login-button"));
        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    private void resetAppState() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement resetBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetBtn.click();
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("standard_user", "secret_sauce");
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "Login failed - URL does not contain 'inventory'");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "invalid_pass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='error']")));
        Assertions.assertTrue(error.isDisplayed(), "Error message not displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login("standard_user", "secret_sauce");
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown = driver.findElement(By.className("product_sort_container"));
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='az']")).click();
        List<WebElement> itemsAZ = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("inventory_item_name")));
        String firstAZ = itemsAZ.get(0).getText();

        sortDropdown = driver.findElement(By.className("product_sort_container"));
        sortDropdown.click();
        sortDropdown.findElement(By.cssSelector("option[value='za']")).click();
        List<WebElement> itemsZA = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("inventory_item_name")));
        String firstZA = itemsZA.get(0).getText();

        Assertions.assertNotEquals(firstAZ, firstZA, "Sorting does not change order of items");
        resetAppState();
    }

    @Test
    @Order(4)
    public void testMenuActions() {
        login("standard_user", "secret_sauce");

        // All Items
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItems.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"), "All Items navigation failed");

        // About (external)
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        String original = driver.getWindowHandle();
        Set<String> windows = driver.getWindowHandles();
        for (String handle : windows) {
            if (!handle.equals(original)) {
                driver.switchTo().window(handle);
                Assertions.assertTrue(driver.getCurrentUrl().contains("saucelabs"), "About link did not navigate to expected domain");
                driver.close();
                driver.switchTo().window(original);
                break;
            }
        }

        // Reset App State
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
        resetLink.click();
        WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
        closeBtn.click();

        // Logout
        menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("index"), "Logout failed");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        login("standard_user", "secret_sauce");

        String original = driver.getWindowHandle();

        // Twitter
        WebElement twitter = wait.until(ExpectedConditions.elementToBeClickable(By.className("social_twitter")));
        twitter.click();
        switchToNewWindowAndVerifyUrlContains("twitter", original);

        // Facebook
        WebElement facebook = wait.until(ExpectedConditions.elementToBeClickable(By.className("social_facebook")));
        facebook.click();
        switchToNewWindowAndVerifyUrlContains("facebook", original);

        // LinkedIn
        WebElement linkedin = wait.until(ExpectedConditions.elementToBeClickable(By.className("social_linkedin")));
        linkedin.click();
        switchToNewWindowAndVerifyUrlContains("linkedin", original);

        resetAppState();
    }

    private void switchToNewWindowAndVerifyUrlContains(String domain, String originalWindow) {
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String handle : handles) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                
                // FIXED: Use ExpectedCondition (singular) or just use a lambda
                wait.until(new ExpectedCondition<Boolean>() {
                    @Override
                    public Boolean apply(WebDriver driver) {
                        return driver.getCurrentUrl().contains(domain);
                    }
                });
                
                Assertions.assertTrue(driver.getCurrentUrl().contains(domain), "External link did not navigate to " + domain);
                driver.close();
                driver.switchTo().window(originalWindow);
                break;
            }
        }
    }
}