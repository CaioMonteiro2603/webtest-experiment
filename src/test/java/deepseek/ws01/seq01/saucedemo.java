package deepseek.ws01.seq01;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SauceDemoTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://www.saucedemo.com/v1/index.html";
    private static final String LOGIN = "standard_user";
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
    public void testLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("inventory.html"));
        WebElement inventoryContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inventory_container")));
        Assertions.assertTrue(inventoryContainer.isDisplayed(), "Inventory page should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("invalid_user");
        passwordField.sendKeys("invalid_password");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h3[data-test='error']")));
        Assertions.assertTrue(errorMessage.getText().contains("Username and password do not match"), "Error message should be displayed for invalid login");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        driver.get(BASE_URL);
        login();

        Select sortingDropdown = new Select(driver.findElement(By.cssSelector("select.product_sort_container")));
        List<WebElement> items = driver.findElements(By.cssSelector("div.inventory_item_name"));

        String firstItemNameBeforeSort = items.get(0).getText();
        sortingDropdown.selectByVisibleText("Name (Z to A)");
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElement(items.get(0), firstItemNameBeforeSort)));
        Assertions.assertNotEquals(firstItemNameBeforeSort, items.get(0).getText(), "First item name should change after sorting");

        sortingDropdown.selectByVisibleText("Price (low to high)");
        String firstItemPrice = driver.findElement(By.cssSelector("div.inventory_item_price")).getText();
        Assertions.assertTrue(firstItemPrice.startsWith("$"), "First item price should be displayed after sorting");
    }

    @Test
    @Order(4)
    public void testMenuOptions() {
        driver.get(BASE_URL);
        login();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("inventory_sidebar_link")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));

        menuButton.click();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("about_sidebar_link")));
        aboutLink.click();
        String currentWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains("saucelabs.com"));
        driver.close();
        driver.switchTo().window(currentWindow);

        menuButton.click();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.html"));
    }

    @Test
    @Order(5)
    public void testSocialLinks() {
        driver.get(BASE_URL);
        login();

        String currentWindow = driver.getWindowHandle();
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter.com']")));
        twitterLink.click();
        switchToNewWindowAndAssertDomain("twitter.com");

        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook.com']")));
        facebookLink.click();
        switchToNewWindowAndAssertDomain("facebook.com");

        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin.com']")));
        linkedinLink.click();
        switchToNewWindowAndAssertDomain("linkedin.com");

        driver.switchTo().window(currentWindow);
    }

    private void login() {
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-name")));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("inventory.html"));
    }

    private void switchToNewWindowAndAssertDomain(String expectedDomain) {
        String currentWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        driver.close();
        driver.switchTo().window(currentWindow);
    }
}