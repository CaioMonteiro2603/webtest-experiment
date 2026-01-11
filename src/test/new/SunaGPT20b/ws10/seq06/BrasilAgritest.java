package SunaGPT20b.ws10.seq06;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USERNAME = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.name("email")));
        emailInput.clear();
        emailInput.sendKeys(user);

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.clear();
        passwordInput.sendKeys(pass);

        WebElement loginBtn = driver.findElement(By.tagName("button"));
        loginBtn.click();
    }

    private void loginIfNeeded() {
        if (driver.getCurrentUrl().contains("/login") ||
                driver.findElements(By.name("email")).size() > 0) {
            login(USERNAME, PASSWORD);
            // Ensure login succeeded
            wait.until(ExpectedConditions.not(
                    ExpectedConditions.urlContains("/login")));
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        login(USERNAME, PASSWORD);
        // Verify we are no longer on the login page
        wait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("/login")));
        Assertions.assertFalse(driver.getCurrentUrl().contains("/login"),
                "After successful login the URL should not contain '/login'.");

        // Verify a logout element is present (common indicator of logged‑in state)
        List<WebElement> logoutElements = driver.findElements(By.id("logout_sidebar_link"));
        Assertions.assertTrue(!logoutElements.isEmpty() && logoutElements.get(0).isDisplayed(),
                "Logout element should be visible after successful login.");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.name("email")));
        emailInput.clear();
        emailInput.sendKeys("invalid@example.com");

        WebElement passwordInput = driver.findElement(By.name("password"));
        passwordInput.clear();
        passwordInput.sendKeys("wrongpassword");

        WebElement loginBtn = driver.findElement(By.tagName("button"));
        loginBtn.click();

        // Expect an error message to appear
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".error-message, .alert, .error, .alert-danger")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials.");
    }

    @Test
    @Order(3)
    void testMenuBurgerAndNavigation() {
        loginIfNeeded();

        // Open burger menu
        WebElement menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Locate the menu container (generic selector)
        WebElement menuContainer = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".bm-menu-wrap, .sidebar, .menu")));

        // Click "All Items"
        WebElement allItems = menuContainer.findElement(
                By.xpath(".//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'all items')]"));
        allItems.click();

        // Verify navigation to inventory or items page
        Assertions.assertTrue(driver.getCurrentUrl().contains("/inventory") ||
                        driver.getCurrentUrl().contains("/items"),
                "All Items should navigate to the inventory page.");

        // Re‑open menu for further actions
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        // Click "About" (external link)
        WebElement aboutLink = menuContainer.findElement(
                By.xpath(".//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'about')]"));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new tab/window
        wait.until(d -> d.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        // Verify external domain (generic check)
        Assertions.assertTrue(driver.getCurrentUrl().contains("example.com") ||
                        driver.getCurrentUrl().contains("saucedemo.com"),
                "About link should open an external site.");

        driver.close();
        driver.switchTo().window(originalWindow);

        // Re‑open menu to logout
        menuBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();

        WebElement logoutLink = menuContainer.findElement(
                By.xpath(".//a[contains(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'logout')]"));
        logoutLink.click();

        // Verify we are back on the login page
        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "Logout should return the user to the login page.");
    }

    @Test
    @Order(4)
    void testSortingDropdown() {
        loginIfNeeded();

        // Ensure we are on the inventory page
        if (!driver.getCurrentUrl().contains("/inventory")) {
            driver.get(BASE_URL.replace("/login", "/inventory"));
        }

        // Locate the sorting dropdown (common selectors)
        WebElement sortSelect = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.cssSelector("select[data-test='product_sort_container'], select#sort, select")));

        Select select = new Select(sortSelect);
        String[] options = {
                "Name (A to Z)",
                "Name (Z to A)",
                "Price (low to high)",
                "Price (high to low)"
        };

        for (String option : options) {
            select.selectByVisibleText(option);
            // Wait for the list to be refreshed
            WebElement firstItem = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector(".inventory_item_name, .item_name, .product_name")));
            Assertions.assertNotNull(firstItem.getText(),
                    "First item name should be present after sorting by '" + option + "'.");
        }
    }

    @Test
    @Order(5)
    void testFooterSocialLinks() {
        loginIfNeeded();

        // Scroll to bottom to ensure footer is in view
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");

        String[][] socials = {
                {"twitter.com", "twitter.com"},
                {"facebook.com", "facebook.com"},
                {"linkedin.com", "linkedin.com"}
        };

        for (String[] social : socials) {
            String domain = social[0];
            List<WebElement> links = driver.findElements(
                    By.cssSelector("a[href*='" + domain + "']"));
            if (links.isEmpty()) {
                continue; // Skip if the social link is not present
            }
            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Switch to the newly opened window/tab
            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    "Social link should open a page containing '" + domain + "'.");

            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}