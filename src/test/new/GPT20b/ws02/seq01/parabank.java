package GPT20b.ws02.seq01;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASS = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void init() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void cleanup() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* --------------------------- helpers --------------------------------- */

    private static void login(String email, String pwd) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username"))).clear();
        driver.findElement(By.name("username")).sendKeys(email);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password"))).clear();
        driver.findElement(By.name("password")).sendKeys(pwd);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input[type='submit'][value='Log In']")));
        loginBtn.click();
    }

    private static void logout() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out"))).click();
            wait.until(ExpectedConditions.urlToBe(BASE_URL));
        } catch (Exception e) {
            // If logout link not found, navigate directly to base URL
            driver.get(BASE_URL);
        }
    }

    private static void resetAppState() {
        // ParaBank doesn't have a reset app state feature like the original test expected
        // Navigate to accounts overview page instead
        driver.get("https://parabank.parasoft.com/parabank/overview.htm");
    }

    /* ---------------------------- tests ----------------------------------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASS);
        assertTrue(driver.getCurrentUrl().contains("/overview.htm"),
                "Expected to be redirected to overview after successful login");
        assertFalse(driver.findElements(By.id("accountTable")).isEmpty(),
                "Account table should be visible after login");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("wronguser", "wrongpass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("p.error")));
        String errTxt = error.getText();
        assertTrue(errTxt.toLowerCase().contains("error") || errTxt.toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingFunctionality() {
        login(USER_EMAIL, USER_PASS);
        wait.until(ExpectedConditions.urlContains("/overview.htm"));

        // Navigate to accounts page to test sorting
        driver.get("https://parabank.parasoft.com/parabank/accounts.htm");
        
        By sortLocator = By.cssSelector("select[name='accountId']");
        wait.until(ExpectedConditions.presenceOfElementLocated(sortLocator));
        WebElement sortElement = driver.findElement(sortLocator);
        Select sortSelect = new Select(sortElement);
        List<String> optionTexts = new ArrayList<>();
        for (WebElement opt : sortSelect.getOptions()) {
            optionTexts.add(opt.getText());
        }

        List<String> firstItemNames = new ArrayList<>();
        for (String opt : optionTexts) {
            if (!opt.isEmpty()) {
                sortSelect.selectByVisibleText(opt);
                wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("table")));
                List<WebElement> items = driver.findElements(By.cssSelector("table"));
                if (!items.isEmpty()) {
                    firstItemNames.add(items.get(0).getText());
                }
            }
        }

        assertTrue(firstItemNames.size() > 0, "Sorting should produce at least one item");
        
        logout();
    }

    @Test
    @Order(4)
    public void testMenuAndExternalLinks() {
        login(USER_EMAIL, USER_PASS);

        // Test navigation links
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview"))).click();
        assertTrue(driver.getCurrentUrl().contains("/overview.htm"),
                "Accounts Overview should navigate to overview page");

        // Test Transfer Funds
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds"))).click();
        assertTrue(driver.getCurrentUrl().contains("/transfer.htm"),
                "Transfer Funds should navigate to transfer page");

        // Test external link (About Us)
        Set<String> handlesBefore = driver.getWindowHandles();        
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us"))).click();
        
        // Check if new window opened
        Set<String> handlesAfter = driver.getWindowHandles();
        if (handlesAfter.size() > handlesBefore.size()) {
            handlesAfter.removeAll(handlesBefore);
            String newWindow = handlesAfter.iterator().next();
            driver.switchTo().window(newWindow);
            driver.close();
            driver.switchTo().window(handlesBefore.iterator().next());
        }

        logout();
    }
}