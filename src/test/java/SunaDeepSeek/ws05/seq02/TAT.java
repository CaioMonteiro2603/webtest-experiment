package SunaDeepSeek.ws05.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";

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
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        Assertions.assertEquals("Swag Labs", driver.getTitle());
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys("standard_user");
        password.sendKeys("secret_sauce");
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        Assertions.assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed());
    }

    @Test
    @Order(3)
    public void testFailedLogin() {
        driver.get(BASE_URL);
        WebElement username = driver.findElement(By.id("user-name"));
        WebElement password = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        username.sendKeys("locked_out_user");
        password.sendKeys("wrong_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"));
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        login();
        
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.className("product_sort_container")));
        sortDropdown.click();

        List<WebElement> options = driver.findElements(By.cssSelector(".product_sort_container option"));
        Assertions.assertEquals(4, options.size());

        testSortOption("az", "Sauce Labs Backpack");
        testSortOption("za", "Test.allTheThings() T-Shirt (Red)");
        testSortOption("lohi", "$7.99");
        testSortOption("hilo", "$49.99");
    }

    private void testSortOption(String value, String expectedFirstItem) {
        WebElement sortDropdown = driver.findElement(By.className("product_sort_container"));
        sortDropdown.sendKeys(value);

        List<WebElement> items = wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
            By.cssSelector(".inventory_item_name"), 0));
        Assertions.assertTrue(items.get(0).getText().contains(expectedFirstItem));
    }

    @Test
    @Order(5)
    public void testMenuNavigation() {
        login();
        
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        testMenuOption("inventory_sidebar_link", "inventory.html");
        testMenuOption("about_sidebar_link", "saucelabs.com", true);
        testMenuOption("logout_sidebar_link", "index.html");
        
        // Reset app state after logout test
        login();
        openMenu();
        driver.findElement(By.id("reset_sidebar_link")).click();
    }

    private void testMenuOption(String id, String expectedUrlContains) {
        testMenuOption(id, expectedUrlContains, false);
    }

    private void testMenuOption(String id, String expectedUrlContains, boolean isExternal) {
        openMenu();
        WebElement menuItem = wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
        menuItem.click();

        if (isExternal) {
            String originalWindow = driver.getWindowHandle();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedUrlContains));
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedUrlContains));
            if (expectedUrlContains.equals("inventory.html")) {
                Assertions.assertTrue(driver.findElement(By.className("inventory_list")).isDisplayed());
            }
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        login();
        
        testSocialLink("Twitter", "twitter.com");
        testSocialLink("Facebook", "facebook.com");
        testSocialLink("LinkedIn", "linkedin.com");
    }

    private void testSocialLink(String linkText, String expectedDomain) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(text(), '" + linkText + "')]")));
        link.click();

        String originalWindow = driver.getWindowHandle();
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
        if (driver.getCurrentUrl().contains("index.html")) {
            WebElement username = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
            WebElement password = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("login-button"));

            username.sendKeys("standard_user");
            password.sendKeys("secret_sauce");
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("inventory.html"));
        }
    }

    private void openMenu() {
        if (driver.findElements(By.id("react-burger-cross-btn")).size() == 0) {
            WebElement menuButton = driver.findElement(By.id("react-burger-menu-btn"));
            menuButton.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("react-burger-cross-btn")));
        }
    }
}