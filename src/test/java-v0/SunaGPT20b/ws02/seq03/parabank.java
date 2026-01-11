package SunaGPT20b.ws02.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void goToBase() {
        driver.get(BASE_URL);
    }

    private void login(String user, String pass) {
        WebElement userField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        userField.clear();
        userField.sendKeys(user);

        WebElement passField = driver.findElement(By.name("password"));
        passField.clear();
        passField.sendKeys(pass);

        WebElement loginBtn = driver.findElement(By.cssSelector("input[value='Log In']"));
        loginBtn.click();
    }

    private void ensureLoggedIn() {
        if (driver.getCurrentUrl().contains("login.htm") ||
                driver.findElements(By.cssSelector("input[value='Log In']")).size() > 0) {
            login(USERNAME, PASSWORD);
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        goToBase();
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
                "URL should contain 'overview.htm' after successful login");

        WebElement header = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Accounts Overview')]")));
        Assertions.assertTrue(header.isDisplayed(),
                "Accounts Overview header should be displayed after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        goToBase();
        login(USERNAME, "wrongPassword");
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid login");
    }

    @Test
    @Order(3)
    public void testTransferFundsNavigation() {
        goToBase();
        ensureLoggedIn();

        WebElement transferLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferLink.click();

        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("transfer.htm"),
                "URL should contain 'transfer.htm' after navigating to Transfer Funds");

        WebElement header = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Transfer Funds')]")));
        Assertions.assertTrue(header.isDisplayed(),
                "Transfer Funds header should be displayed");

        // Return to Accounts Overview
        WebElement overviewLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        overviewLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
                "Should return to Accounts Overview page");
    }

    @Test
    @Order(4)
    public void testExternalFooterLink() {
        goToBase();
        ensureLoggedIn();

        // Locate a footer link that points to an external domain (Parasoft)
        List<WebElement> footerLinks = driver.findElements(By.xpath("//footer//a[contains(@href,'parasoft.com')]"));
        Assertions.assertFalse(footerLinks.isEmpty(), "Expected at least one external footer link to parasoft.com");

        WebElement Link = footerLinks.get(0);
        String originalWindow = driver.getWindowHandle();
        Link.click();

        // Switch to new window/tab
        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        List<String> windowList = new ArrayList<>(windows);
        String newWindow = windowList.stream()
                .filter(handle -> !handle.equals(originalWindow))
                .findFirst()
                .orElseThrow(() -> new AssertionError("New window not found"));
        driver.switchTo().window(newWindow);

        // Verify external URL contains expected domain
        wait.until(ExpectedConditions.urlContains("parasoft.com"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("parasoft.com"),
                "External link should navigate to a URL containing 'parasoft.com'");

        // Close external window and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testLogout() {
        goToBase();
        ensureLoggedIn();

        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("login.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("login.htm"),
                "URL should contain 'login.htm' after logout");

        // Verify login button is present again
        WebElement loginBtn = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[value='Log In']")));
        Assertions.assertTrue(loginBtn.isDisplayed(),
                "Login button should be displayed after logout");
    }
}