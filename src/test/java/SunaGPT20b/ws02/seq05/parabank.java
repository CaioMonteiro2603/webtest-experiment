package SunaGPT20b.ws02.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String VALID_USERNAME = "caio@gmail.com";
    private static final String VALID_PASSWORD = "123";

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        createUser(driver);
    }

    private static void createUser(WebDriver driver) {
        driver.get("https://parabank.parasoft.com/parabank/register.htm");
        driver.findElement(By.id("customer.firstName")).click();
        driver.findElement(By.id("customer.firstName")).sendKeys("a");
        driver.findElement(By.id("customer.lastName")).click();
        driver.findElement(By.id("customer.lastName")).sendKeys("a");
        driver.findElement(By.id("customer.address.street")).click();
        driver.findElement(By.id("customer.address.street")).sendKeys("a");
        driver.findElement(By.id("customer.address.city")).click();
        driver.findElement(By.id("customer.address.city")).sendKeys("a");
        driver.findElement(By.id("customer.address.state")).click();
        driver.findElement(By.id("customer.address.state")).sendKeys("a");
        driver.findElement(By.id("customer.address.zipCode")).click();
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("a");
        driver.findElement(By.id("customer.phoneNumber")).click();
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("a");
        driver.findElement(By.id("customer.ssn")).click();
        driver.findElement(By.id("customer.ssn")).sendKeys("a");
        driver.findElement(By.id("customer.username")).click();
        driver.findElement(By.id("customer.username")).sendKeys("caio@gmail.com");
        driver.findElement(By.id("customer.password")).sendKeys("123");
        driver.findElement(By.id("repeatedPassword")).sendKeys("123");
        driver.findElement(By.cssSelector("td > .button")).click();
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']")));

        userField.clear();
        userField.sendKeys(username);
        passField.clear();
        passField.sendKeys(password);
        loginBtn.click();
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
                "After login the URL should contain 'overview.htm'.");

        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Accounts Overview')]")));
        Assertions.assertTrue(header.isDisplayed(),
                "Accounts Overview header should be displayed after successful login.");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_pass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'The username and password could not be verified')]")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials.");

        // Ensure we are still on the login page
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"),
                "URL should remain on login page after failed login.");
    }

    @Test
    @Order(3)
    public void testLogoutFunctionality() {
        login(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"),
                "After logout the URL should contain 'index.htm'.");

        // Verify login form is present again
        Assertions.assertTrue(driver.findElements(By.name("username")).size() > 0,
                "Username field should be present after logout.");
    }

    @Test
    @Order(4)
    public void testExternalLinksFromOverview() {
        login(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("overview.htm"));

        // Find external links (href starting with http and not containing the base domain)
        List<WebElement> externalLinks = driver.findElements(By.xpath("//a[starts-with(@href,'http') and not(contains(@href,'parabank.parasoft.com'))]"));
        Assertions.assertFalse(externalLinks.isEmpty(), "There should be at least one external link on the overview page.");

        // Test the first external link
        WebElement link = externalLinks.get(0);
        String expectedDomain = link.getAttribute("href").split("/")[2]; // domain part
        String originalWindow = driver.getWindowHandle();

        link.click();

        // Wait for new window/tab
        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        // Verify the URL contains the expected domain
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                "External should navigate to a URL containing the expected domain.");

        // Close external window and switch back
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(5)
    public void testNavigationToContactPageAndBack() {
        login(VALID_USERNAME, VALID_PASSWORD);
        wait.until(ExpectedConditions.urlContains("overview.htm"));

        // Navigate to Contact page (one level below base)
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();
        wait.until(ExpectedConditions.urlContains("contact.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("contact.htm"),
                "URL should contain 'contact.htm' after navigating to Contact page.");

        // Verify a known element on Contact page
        WebElement contactHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Contact')]")));
        Assertions.assertTrue(contactHeader.isDisplayed(),
                "Contact page header should be displayed.");

        // Return to Overview page via the "Accounts Overview" link
        WebElement overviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        overviewLink.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
                "Should return to Overview page after clicking Accounts Overview link.");

    }
}