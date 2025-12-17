package SunaQwen3.ws06.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("https://automationintesting.online/#/admin", driver.getCurrentUrl(), "URL should redirect to admin page after login");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement inventoryList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventoryTable")));
        assertTrue(inventoryList.isDisplayed(), "Inventory table should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys("invalid");
        passwordField.sendKeys("invalid");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("errorMessage")));
        assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid credentials");
        assertEquals("Invalid credentials", errorMessage.getText(), "Error message text should match expected");
    }

    @Test
    @Order(3)
    void testSortingDropdown() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement sortSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sortSelect")));
        sortSelect.click();

        WebElement priceAscOption = driver.findElement(By.cssSelector("option[value='priceAsc']"));
        priceAscOption.click();

        wait.until(ExpectedConditions.attributeToBe(sortSelect, "value", "priceAsc"));
        assertEquals("priceAsc", sortSelect.getAttribute("value"), "Sort should be set to price ascending");

        sortSelect.click();
        WebElement priceDescOption = driver.findElement(By.cssSelector("option[value='priceDesc']"));
        priceDescOption.click();

        wait.until(ExpectedConditions.attributeToBe(sortSelect, "value", "priceDesc"));
        assertEquals("priceDesc", sortSelect.getAttribute("value"), "Sort should be set to price descending");
    }

    @Test
    @Order(4)
    void testMenuAllItems() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuButton")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("#/admin"));
        assertTrue(driver.getCurrentUrl().contains("#/admin"), "URL should contain #/admin after clicking All Items");
    }

    @Test
    @Order(5)
    void testMenuAboutExternal() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuButton")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("github.com"), "About link should open GitHub page");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    void testMenuLogout() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuButton")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        WebElement loginForm = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginForm")));
        assertTrue(loginForm.isDisplayed(), "Login form should be visible after logout");
    }

    @Test
    @Order(7)
    void testMenuResetAppState() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("menuButton")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetLink.click();

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        WebElement inventoryTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventoryTable")));
        assertTrue(inventoryTable.isDisplayed(), "Inventory table should be visible after reset");
    }

    @Test
    @Order(8)
    void testFooterTwitterLink() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter']")));
        twitterLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open Twitter domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(9)
    void testFooterFacebookLink() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='facebook']")));
        facebookLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open Facebook domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(10)
    void testFooterLinkedInLink() {
        driver.get(BASE_URL);
        loginIfNecessary();

        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='linkedin']")));
        linkedinLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open LinkedIn domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    private void loginIfNecessary() {
        if (driver.getCurrentUrl().contains("login")) {
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordField = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.id("doLogin"));

            usernameField.sendKeys(USERNAME);
            passwordField.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("inventoryTable")));
        }
    }
}