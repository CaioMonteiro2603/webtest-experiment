package GPT4.ws02.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
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
        driver.get(BASE_URL);
    }

    @AfterAll
    public static void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement usernameInput = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("input.button"));

        usernameInput.clear();
        usernameInput.sendKeys(username);
        passwordInput.clear();
        passwordInput.sendKeys(password);
        loginButton.click();
        
        // Wait for page to load after login
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isLoggedIn() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//a[contains(text(), 'Log Out') or contains(text(), 'Logout')]")));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        Assertions.assertTrue(isLoggedIn(), "Login failed: Log Out link not found");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalid_user", "wrong_password");
        boolean errorDisplayed = driver.getPageSource().contains("The username and password could not be verified.");
        Assertions.assertTrue(errorDisplayed, "Expected login failure message not displayed");
    }

    @Test
    @Order(3)
    public void testNavigationLinks() {
        login(USERNAME, PASSWORD);
        
        // Wait for page to load completely
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Accounts Overview')]")));
        accountsOverviewLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview"), "Accounts Overview page not loaded");

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Transfer Funds')]")));
        transferFundsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "Transfer Funds page not loaded");

        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Bill Pay')]")));
        billPayLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("billpay.htm"), "Bill Pay page not loaded");

        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Find Transactions')]")));
        findTransactionsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("findtrans.htm"), "Find Transactions page not loaded");

        WebElement updateContactLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Update Contact Info')]")));
        updateContactLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("updateprofile.htm"), "Update Contact Info page not loaded");

        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Request Loan')]")));
        requestLoanLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("requestloan.htm"), "Request Loan page not loaded");
    }

    @Test
    @Order(4)
    public void testExternalLinksInFooter() {
        login(USERNAME, PASSWORD);
        String originalWindow = driver.getWindowHandle();

        List<WebElement> links = driver.findElements(By.cssSelector("div#footer a"));
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (href != null && (href.contains("facebook.com") || href.contains("twitter.com") || href.contains("linkedin.com"))) {
                ((JavascriptExecutor) driver).executeScript("window.open(arguments[0])", href);
                wait.until(ExpectedConditions.numberOfWindowsToBe(2));
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        Assertions.assertTrue(driver.getCurrentUrl().contains("facebook.com") ||
                                              driver.getCurrentUrl().contains("twitter.com") ||
                                              driver.getCurrentUrl().contains("linkedin.com"),
                                              "External link did not open expected domain");
                        driver.close();
                        break;
                    }
                }
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(5)
    public void testLogout() {
        login(USERNAME, PASSWORD);
        
        // Wait for page to load completely
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(), 'Log Out') or contains(text(), 'Logout')]")));
        logoutLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "Logout did not return to index page");
    }
}