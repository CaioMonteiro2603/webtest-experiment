package SunaGPT20b.ws02.seq08;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String VALID_USERNAME = "caio@gmail.com";
    private static final String VALID_PASSWORD = "123";

@BeforeAll
public static void setUp() {
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
public static void tearDown() {
    if (driver != null) {
        driver.quit();
    }
}

/** Helper to perform login */
private void login(String username, String password) {
    driver.get(BASE_URL);
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).clear();
    driver.findElement(By.name("username")).sendKeys(username);
    driver.findElement(By.name("password")).clear();
    driver.findElement(By.name("password")).sendKeys(password);
    driver.findElement(By.xpath("//input[@type='submit' and @value='Log In']")).click();
}

/** Helper to ensure we are logged in (used by tests that need a clean session) */
private void ensureLoggedIn() {
    login(VALID_USERNAME, VALID_PASSWORD);
    // Wait for URL to contain overview page after successful login
    wait.until(ExpectedConditions.urlContains("overview.htm"));
}

@Test
@Order(1)
public void testValidLogin() {
    login(VALID_USERNAME, VALID_PASSWORD);
    // Verify successful navigation to overview page
    wait.until(ExpectedConditions.urlContains("overview.htm"));
    String currentUrl = driver.getCurrentUrl();
    Assertions.assertTrue(currentUrl.contains("overview.htm"),
            "Expected URL to contain 'overview.htm' after successful login, but was: " + currentUrl);
    // Verify a known element on the overview page is displayed
    WebElement welcome = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(),'Accounts Overview')]")));
    Assertions.assertTrue(welcome.isDisplayed(),
            "Accounts Overview heading should be displayed after login.");
}

@Test
@Order(2)
public void testInvalidLogin() {
    login(VALID_USERNAME, "wrongPassword");
    // Expect error message on the same login page
    WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
    String bodyText = body.getText();
    Assertions.assertTrue(bodyText.contains("The username and password could not be verified"),
            "Expected error message for invalid credentials.");
    // Ensure URL has not changed to overview page
    Assertions.assertFalse(driver.getCurrentUrl().contains("overview.htm"),
            "URL should not contain 'overview.htm' after failed login.");
}

@Test
@Order(3)
public void testNavigateAboutUs() {
    ensureLoggedIn();
    // Click About Us link from the top menu
    WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'about.htm')]")));
    aboutLink.click();
    wait.until(ExpectedConditions.urlContains("about.htm"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("about.htm"),
            "URL should contain 'about.htm' after clicking About Us.");
    // Verify page header
    WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//h2[contains(text(),'About')]")));
    Assertions.assertTrue(header.isDisplayed(),
            "About page header should be displayed.");
}

@Test
@Order(4)
public void testNavigateServices() {
    ensureLoggedIn();
    // Click Services link from the top menu
    WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'services.htm')]")));
    servicesLink.click();
    wait.until(ExpectedConditions.urlContains("services.htm"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("services.htm"),
            "URL should contain 'services.htm' after clicking Services.");
    // Verify a known service link is present
    WebElement withdrawLink = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//a[contains(@href,'services/ParaBank') and contains(text(),'Withdraw Funds')]")));
    Assertions.assertTrue(withdrawLink.isDisplayed(),
            "Withdraw Funds link should be present on Services page.");
}

@Test
@Order(5)
public void testExternalForumLink() {
    ensureLoggedIn();
    // Locate the Forum link in the footer (external)
    WebElement forumLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//a[contains(@href,'forums.parasoft.com')]")));
    String originalWindow = driver.getWindowHandle();
    forumLink.click();

    // Wait for new window/tab
    wait.until(driver -> driver.getWindowHandles().size() > 1);
    Set<String> windows = driver.getWindowHandles();
    windows.remove(originalWindow);
    String newWindow = windows.iterator().next();
    driver.switchTo().window(newWindow);

    // Verify external domain
    wait.until(ExpectedConditions.urlContains("forums.parasoft.com"));
    Assertions.assertTrue(driver.getCurrentUrl().contains("forums.parasoft.com"),
            "External forum URL should contain 'forums.parasoft.com'.");

    // Close external tab and switch back
    driver.close();
    driver.switchTo().window(originalWindow);
}
}