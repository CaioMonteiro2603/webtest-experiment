package GPT4.ws02.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
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
    private static final String LOGIN = "caio1@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
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

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='title']")));
        Assertions.assertEquals("ParaBank", header.getText(), "Home page title should be 'ParaBank'");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

        username.clear();
        password.clear();
        username.sendKeys("invaliduser");
        password.sendKeys("wrongpassword");
        loginButton.click();

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("error") || error.getText().toLowerCase().contains("invalid"), "Should display error for invalid login");
    }

    @Test
    @Order(3)
    public void testValidLogin() {
        driver.get(BASE_URL);
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[type='submit']"));

        username.clear();
        password.clear();
        username.sendKeys(LOGIN);
        password.sendKeys(PASSWORD);
        loginButton.click();

        WebElement accountsOverview = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(text(),'Accounts Overview')]")));
        Assertions.assertTrue(accountsOverview.isDisplayed(), "Login should redirect to authenticated user view");
    }

    @Test
    @Order(4)
    public void testAccountsOverviewPage() {
        testValidLogin();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        link.click();

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='title']")));
        Assertions.assertEquals("Accounts Overview", heading.getText(), "Should display Accounts Overview page");
    }

    @Test
    @Order(5)
    public void testTransferFundsPage() {
        testValidLogin();
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferLink.click();

        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[@class='title']")));
        Assertions.assertEquals("Transfer Funds", heading.getText(), "Should display Transfer Funds page");
    }

    @Test
    @Order(6)
    public void testExternalLinkToParaSoft() {
        driver.get(BASE_URL);
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Parasoft')]")));
        String originalWindow = driver.getWindowHandle();
        footerLink.click();

        wait.until(driver -> driver.getWindowHandles().size() > 1);
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);

        wait.until(ExpectedConditions.urlContains("parasoft.com"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("parasoft.com"), "External link should navigate to parasoft.com");

        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    public void testLogout() {
        testValidLogin();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should return to login page after logout");
    }
}