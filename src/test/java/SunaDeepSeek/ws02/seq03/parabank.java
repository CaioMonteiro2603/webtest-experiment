package SunaDeepSeek.ws02.seq03;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class parabank {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("ParaBank"));
        Assertions.assertTrue(driver.getTitle().contains("ParaBank"), "Home page title should contain 'ParaBank'");
    }

    @Test
    @Order(2)
    public void testSuccessfulLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement welcomeMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1.title")));
        Assertions.assertTrue(welcomeMessage.getText().contains("Welcome"), "Login should show welcome message");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys("invalid@user.com");
        passwordField.sendKeys("wrongpassword");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("p.error")));
        Assertions.assertTrue(errorMessage.getText().contains("Error"), "Should show error message for invalid login");
    }

    @Test
    @Order(4)
    public void testAccountOverviewPage() {
        login();
        driver.get(BASE_URL.replace("index.htm", "overview.htm"));
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        WebElement accountsTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("accountTable")));
        Assertions.assertTrue(accountsTable.isDisplayed(), "Account overview should show accounts table");
    }

    @Test
    @Order(5)
    public void testTransferFundsPage() {
        login();
        driver.get(BASE_URL.replace("index.htm", "transfer.htm"));
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        WebElement transferForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("transferForm")));
        Assertions.assertTrue(transferForm.isDisplayed(), "Transfer funds form should be visible");
    }

    @Test
    @Order(6)
    public void testBillPayPage() {
        login();
        driver.get(BASE_URL.replace("index.htm", "billpay.htm"));
        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        WebElement payeeForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("payeeForm")));
        Assertions.assertTrue(payeeForm.isDisplayed(), "Bill pay form should be visible");
    }

    @Test
    @Order(7)
    public void testFindTransactionsPage() {
        login();
        driver.get(BASE_URL.replace("index.htm", "findtrans.htm"));
        wait.until(ExpectedConditions.urlContains("findtrans.htm"));
        WebElement findForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("findForm")));
        Assertions.assertTrue(findForm.isDisplayed(), "Find transactions form should be visible");
    }

    @Test
    @Order(8)
    public void testRequestLoanPage() {
        login();
        driver.get(BASE_URL.replace("index.htm", "requestloan.htm"));
        wait.until(ExpectedConditions.urlContains("requestloan.htm"));
        WebElement loanForm = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("loanForm")));
        Assertions.assertTrue(loanForm.isDisplayed(), "Loan request form should be visible");
    }

    @Test
    @Order(9)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        testFooterLink("About Us", "about.htm");
        testFooterLink("Services", "services.htm");
        testFooterLink("Products", "products.htm");
        testFooterLink("Locations", "locations.htm");
        testFooterLink("Admin Page", "admin.htm");
    }

    @Test
    @Order(10)
    public void testExternalLinks() {
        driver.get(BASE_URL);
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("div#footerPanel ul li a"));
        
        for (WebElement link : footerLinks) {
            if (link.getText().equals("ParaSoft Demo Website")) {
                testExternalLink(link, "parasoft.com");
                break;
            }
        }
    }

    private void login() {
        driver.get(BASE_URL);
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input[value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();
        wait.until(ExpectedConditions.urlContains("overview.htm"));
    }

    private void testFooterLink(String linkText, String expectedUrlPart) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
        link.click();
        wait.until(ExpectedConditions.urlContains(expectedUrlPart));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedUrlPart), 
            "Clicking " + linkText + " should navigate to " + expectedUrlPart);
        driver.get(BASE_URL);
    }

    private void testExternalLink(WebElement link, String expectedDomain) {
        String originalWindow = driver.getWindowHandle();
        link.click();
        
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
        
        wait.until(ExpectedConditions.urlContains(expectedDomain));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), 
            "External link should open page with domain " + expectedDomain);
        
        driver.close();
        driver.switchTo().window(originalWindow);
    }
}