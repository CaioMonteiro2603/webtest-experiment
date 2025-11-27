package SunaQwen3.ws06.seq06;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class SiteTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String LOGIN_PAGE_URL = BASE_URL + "#/login";
    private static final String INVENTORY_PAGE_URL = BASE_URL + "#/room";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "password";

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
    public void testLoginPageLoads() {
        driver.get(LOGIN_PAGE_URL);
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("#/login"), "URL should contain #/login");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        assertNotNull(usernameField, "Username field should be present");
        assertNotNull(passwordField, "Password field should be present");
        assertNotNull(loginButton, "Login button should be present");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("#/room"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("#/room"), "Should be redirected to room page after login");

        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertEquals("Rooms", pageTitle.getText(), "Page title should be 'Rooms'");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(LOGIN_PAGE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys("invaliduser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert")));
        assertTrue(errorMessage.getText().contains("Invalid"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        loginIfNeeded();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items")));
        allItemsLink.click();

        wait.until(ExpectedConditions.urlContains("#/room"));
        assertTrue(driver.getCurrentUrl().contains("#/room"), "Should navigate to room page");

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }

        assertNotNull(newWindow, "New window should have been opened");
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("github.com"), "About link should open GitHub page");

        driver.close();
        driver.switchTo().window(originalWindow);

        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State")));
        resetLink.click();

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert")));
        assertTrue(successMessage.getText().contains("Reset"), "Reset success message should appear");
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        loginIfNeeded();

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
        assertEquals(3, footerLinks.size(), "Footer should have 3 social links");

        String originalWindow = driver.getWindowHandle();

        for (WebElement link : footerLinks) {
            String linkText = link.getText();
            link.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));

            String newWindow = null;
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    newWindow = handle;
                    break;
                }
            }

            assertNotNull(newWindow, "New window should have been opened for " + linkText);
            driver.switchTo().window(newWindow);

            String currentUrl = driver.getCurrentUrl();
            if (linkText.contains("Twitter")) {
                assertTrue(currentUrl.contains("twitter.com"), "Twitter link should open correct domain");
            } else if (linkText.contains("Facebook")) {
                assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
            } else if (linkText.contains("LinkedIn")) {
                assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
            }

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(6)
    public void testRoomSorting() {
        loginIfNeeded();

        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();

        WebElement optionNameAsc = driver.findElement(By.cssSelector("option[value='za']"));
        optionNameAsc.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Name (Z to A)"));

        List<WebElement> roomNames = driver.findElements(By.cssSelector(".room-name"));
        assertTrue(roomNames.size() > 0, "At least one room should be displayed");

        String firstRoomName = roomNames.get(0).getText();

        sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.className("product_sort_container")));
        sortDropdown.click();
        WebElement optionNameDesc = driver.findElement(By.cssSelector("option[value='az']"));
        optionNameDesc.click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".product_sort_container"), "Name (A to Z)"));

        List<WebElement> roomNamesAfterSort = driver.findElements(By.cssSelector(".room-name"));
        String newFirstRoomName = roomNamesAfterSort.get(0).getText();

        assertNotEquals(firstRoomName, newFirstRoomName, "Room order should change after sorting");
    }

    @Test
    @Order(7)
    public void testLogoutFunctionality() {
        loginIfNeeded();

        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("#/login"));
        assertTrue(driver.getCurrentUrl().contains("#/login"), "Should be redirected to login page after logout");

        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doLogin")));
        assertNotNull(loginButton, "Login button should be visible on login page");
    }

    private void loginIfNeeded() {
        driver.get(INVENTORY_PAGE_URL);
        try {
            wait.until(ExpectedConditions.urlContains("#/room"));
            if (driver.getCurrentUrl().contains("#/room")) {
                return;
            }
        } catch (Exception ignored) {}

        driver.get(LOGIN_PAGE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.id("doLogin"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("#/room"));
    }
}