package SunaQwen3.ws10.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class SiteTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String LOGIN = "superadmin@brasilagritest.com.br";
    private static final String PASSWORD = "10203040";

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
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys(LOGIN);
        passwordInput.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should be redirected to dashboard after login");
        assertTrue(driver.findElement(By.tagName("body")).getText().contains("Dashboard"), "Dashboard page should contain 'Dashboard' text");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        emailInput.sendKeys("invalid@user.com");
        passwordInput.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-danger")));
        assertNotNull(errorMessage, "Error message should be displayed for invalid login");
        assertTrue(errorMessage.getText().contains("credenciais"), "Error message should mention invalid credentials");
    }

    @Test
    @Order(3)
    public void testMenuNavigation() {
        driver.get(BASE_URL);
        // Login first
        loginIfNotLoggedIn();

        // Open menu
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Itens")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/itens"));
        assertTrue(driver.getCurrentUrl().contains("/itens"), "Should navigate to items page");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click About (external)
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sobre")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        // Switch to new tab
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }

        // Assert URL contains expected domain
        assertTrue(driver.getCurrentUrl().contains("brasilagritest.com"), "About link should open brasilagritest.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Reset App State
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Resetar Estado")));
        resetLink.click();
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert-success")));
        assertTrue(driver.findElement(By.cssSelector(".alert-success")).isDisplayed(), "Reset success message should be displayed");

        // Open menu again
        menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();

        // Click Logout
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sair")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Should return to login page after logout");
    }

    @Test
    @Order(4)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        loginIfNotLoggedIn();

        // Find footer social links
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[href*='twitter'], footer a[href*='facebook'], footer a[href*='linkedin']"));
        String originalWindow = driver.getWindowHandle();

        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            String domain;
            if (href.contains("twitter")) {
                domain = "twitter.com";
            } else if (href.contains("facebook")) {
                domain = "facebook.com";
            } else if (href.contains("linkedin")) {
                domain = "linkedin.com";
            } else {
                continue;
            }

            // Open link in new tab
            ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));

            // Switch to new tab
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            // Assert URL contains expected domain
            assertTrue(driver.getCurrentUrl().contains(domain), "Social link should open " + domain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(5)
    public void testSortingDropdown() {
        driver.get(BASE_URL);
        loginIfNotLoggedIn();

        // Navigate to items page
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".navbar-toggler")));
        menuButton.click();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Todos os Itens")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/itens"));

        // Find sorting dropdown
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.name("sort")));
        
        // Get initial item list
        List<WebElement> initialItems = driver.findElements(By.cssSelector(".item-name"));
        assertTrue(initialItems.size() > 0, "Should have at least one item in the list");

        // Test each sorting option
        Select select = new Select(sortDropdown);
        List<WebElement> options = select.getOptions();

        for (WebElement option : options) {
            String optionValue = option.getAttribute("value");
            select.selectByValue(optionValue);

            // Wait for list to update
            wait.until(ExpectedConditions.stalenessOf(initialItems.get(0)));

            // Get new item list
            List<WebElement> sortedItems = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".item-name")));
            assertTrue(sortedItems.size() > 0, "Item list should not be empty after sorting");

            // Verify sorting changed (simple check that order is different from initial)
            if (!optionValue.isEmpty()) {
                String firstItemText = sortedItems.get(0).getText();
                assertNotEquals(initialItems.get(0).getText(), firstItemText, "Sorting should change item order for option: " + optionValue);
            }
        }
    }

    private void loginIfNotLoggedIn() {
        if (driver.getCurrentUrl().equals(BASE_URL)) {
            WebElement emailInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("email")));
            WebElement passwordInput = driver.findElement(By.name("password"));
            WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

            emailInput.sendKeys(LOGIN);
            passwordInput.sendKeys(PASSWORD);
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("/dashboard"));
        }
    }
}