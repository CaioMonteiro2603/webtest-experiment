package GPT4.ws02.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).clear();
        driver.findElement(By.name("username")).sendKeys(USERNAME);
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Log In']"))).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[text()='Accounts Overview']")));
    }

    private void logout() {
        if (driver.findElements(By.linkText("Log Out")).size() > 0) {
            driver.findElement(By.linkText("Log Out")).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login();
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Login did not redirect to accounts overview");
        WebElement header = driver.findElement(By.xpath("//h1[text()='Accounts Overview']"));
        Assertions.assertTrue(header.isDisplayed(), "Accounts Overview header not visible");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys("invalid");
        driver.findElement(By.name("password")).sendKeys("wrong");
        driver.findElement(By.cssSelector("input[value='Log In']")).click();
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("error")));
        Assertions.assertTrue(error.getText().contains("error"), "Expected error message not displayed");
    }

    @Test
    @Order(3)
    public void testNavigateToServices() {
        login();
        driver.findElement(By.linkText("Services")).click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Services']")));
        Assertions.assertTrue(header.isDisplayed(), "Services page header not found");
        logout();
    }

    @Test
    @Order(4)
    public void testNavigateToAboutUs() {
        login();
        driver.findElement(By.linkText("About Us")).click();
        WebElement header = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='ParaSoft Demo Website']")));
        Assertions.assertTrue(header.isDisplayed(), "About Us page header not found");
        logout();
    }

    @Test
    @Order(5)
    public void testExternalLinkParaSoft() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("ParaSoft")));
        link.click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        Set<String> windows = driver.getWindowHandles();
        windows.remove(originalWindow);
        String newWindow = windows.iterator().next();
        driver.switchTo().window(newWindow);
        Assertions.assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "External link did not lead to parasoft.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        driver.get(BASE_URL);
        String originalWindow = driver.getWindowHandle();

        By[] socialLinks = {
            By.cssSelector("a[href*='twitter.com']"),
            By.cssSelector("a[href*='facebook.com']"),
            By.cssSelector("a[href*='linkedin.com']")
        };

        String[] expectedDomains = {
            "twitter.com", "facebook.com", "linkedin.com"
        };

        for (int i = 0; i < socialLinks.length; i++) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(socialLinks[i]));
            link.click();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomains[i]), "Expected external URL not reached");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    public void testOpenAccount() {
        login();
        driver.findElement(By.linkText("Open New Account")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[text()='Open New Account']")));
        WebElement openAccountBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='Open New Account']")));
        openAccountBtn.click();
        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Account Opened!']")));
        Assertions.assertTrue(confirmation.isDisplayed(), "Account Opened message not visible");
        logout();
    }

    @Test
    @Order(8)
    public void testTransferFunds() {
        login();
        driver.findElement(By.linkText("Transfer Funds")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//h1[text()='Transfer Funds']")));
        WebElement amount = driver.findElement(By.id("amount"));
        amount.clear();
        amount.sendKeys("100");
        WebElement transferBtn = driver.findElement(By.cssSelector("input[value='Transfer']"));
        transferBtn.click();
        WebElement confirmation = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[text()='Transfer Complete!']")));
        Assertions.assertTrue(confirmation.isDisplayed(), "Transfer Complete message not visible");
        logout();
    }
}
