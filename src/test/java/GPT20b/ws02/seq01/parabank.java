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
public class ParabankHeadlessTestSuite {

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
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("input[type='text']"))).get(0).clear();
        driver.findElement(By.cssSelector("input[type='text']")).sendKeys(email);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector[type='password']))).get(0).clear();
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys(pwd);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit'], button[value='Login']")));
        loginBtn.click();
    }

    private static void logout() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout"))).click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL));
    }

    private static void resetAppState() {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-icon"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset App State"))).click();
        wait.until(ExpectedConditions.urlContains("/inventory.htm"));
    }

    /* ---------------------------- tests ----------------------------------- */

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASS);
        assertTrue(driver.getCurrentUrl().contains("/inventory.htm"),
                "Expected to be redirected to inventory after successful login");
        assertFalse(driver.findElements(By.cssSelector(".item")).isEmpty(),
                "Inventory items should be visible after login");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("wronguser", "wrongpass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div[role='alert'], div[class*='error']")));
        String errTxt = error.getText();
        assertTrue(errTxt.toLowerCase().contains("wrong login") || errTxt.toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
        logout();
    }

    @Test
    @Order(3)
    public void testSortingFunctionality() {
        login(USER_EMAIL, USER_PASS);
        wait.until(ExpectedConditions.urlContains("/inventory.htm"));

        By sortLocator = By.cssSelector("select[name='sort'] , select#sort");
        wait.until(ExpectedConditions.presenceOfElementLocated(sortLocator));
        WebElement sortElement = driver.findElement(sortLocator);
        Select sortSelect = new Select(sortElement);
        List<String> optionTexts = new ArrayList<>();
        for (WebElement opt : sortSelect.getOptions()) {
            optionTexts.add(opt.getText());
        }

        List<String> firstItemNames = new ArrayList<>();
        for (String opt : optionTexts) {
            sortSelect.selectByVisibleText(opt);
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".item")));
            List<WebElement> items = driver.findElements(By.cssSelector(".item"));
            if (!items.isEmpty()) {
                firstItemNames.add(items.get(0).getText());
            }
        }

        assertTrue(firstItemNames.size() > 1, "Sorting should produce at least two different first item titles");
        assertNotEquals(firstItemNames.get(0), firstItemNames.get(1), "First item should change after sorting");

        resetAppState();
        logout();
    }

    @Test
    @Order(4)
    public void testMenuAndExternalLinks() {
        login(USER_EMAIL, USER_PASS);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("menu-icon"))).click();

        // All Items
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("All Items"))).click();
        assertTrue(driver.getCurrentUrl().contains("/inventory.htm"),
                "All Items should navigate to inventory page");

        // About (external)
        Set<String> handlesBefore = driver.getWindowHandles        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About"))).click();
        Set<String> handlesAfter = driver.getWindowHandles();
        handlesAfter.removeAll(handlesBefore);
        String newWindow = handlesAfter.iterator().next();
        driver.switchTo().window(newWindow);
        wait.until(ExpectedConditions.urlContains("about"));
        assertTrue(driver.getCurrentUrl().toLowerCase().contains("about"),
                "About page should open in external domain");
        driver.close();
        driver.switchTo().window(handlesBefore.iterator().next());

        // Reset App State
        resetAppState();

        logout();
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);

        
        List<WebElement> twitterLinks =
                driver.findElements(By.cssSelector("footer a[href*='twitter']"));
        if (!twitterLinks.isEmpty()) {
            WebElement link = twitterLinks.get(0);
            String current = driver.getWindowHandle();
            link.click();
            Set<String> handles = driver.getWindowHandles();
            handles.remove(current);
            String newHandle = handles.iterator().next();
            driverTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains("twitter"));
            assertTrue(driver.getCurrentUrl().toLowerCase().contains("twitter"),
                    "Twitter link should open twitter domain");
            driver.close();
            driver.switchTo().window(current);
        }

        // Facebook
        List<WebElement> fbLinks =
                driver.findElements(By.cssSelector("footer a[href*='facebook']"));
        if (!fbLinks.isEmpty()) {
            WebElement link = fbLinks.get(0);
            String current = driver.getWindowHandle();
            link.click();
            Set<String> handles = driver.getWindowHandles();
            handles.remove(current);
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
           (ExpectedConditions.urlContains(""));
            assertTrue(driver.getCurrentUrl().toLowerCase().contains("facebook"),
                    "Facebook link should open facebook domain");
            driver.close();
            driver.switchTo().window(current);
        }

        // LinkedIn
        List<WebElement> liLinks =
                driver.findElements(By.cssSelector("footer a[href*='linkedin']"));
        if (!liLinks.isEmpty()) {
            WebElement link = liLinks.get(0);
            String current = driver.getWindowHandle();
            link.click();
            Set<String> handles = driver.getWindowHandles();
            handles.remove(current);
            String newHandle = handles.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains("linkedin"));
            assertTrue(driver.getCurrentUrl().toLowerCase().contains("linkedin"),
                    "LinkedIn link should open linkedin domain");
            driver.close();
            driver.switchTo().window(current);
        }
    }
}