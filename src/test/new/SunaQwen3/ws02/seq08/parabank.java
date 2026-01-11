package SunaQwen3.ws02.seq08;

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
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testValidLogin() {
        driver.get(BASE_URL);
        assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview"));
        assertTrue(driver.getCurrentUrl().contains("overview"), "Should be redirected to overview after login");
        WebElement logoutLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out")));
        assertTrue(logoutLink.isDisplayed(), "Logout link should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invaliduser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertTrue(errorMessage.getText().contains("The username and password could not be verified"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testAboutLinkExternal() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//input[@value='Log In']")).click();
        wait.until(ExpectedConditions.urlContains("overview"));

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }
        assertNotNull(newWindow, "New window should have been opened");
        driver.switchTo().window(newWindow);

        assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "About link should open parasoft.com domain");

        driver.close();
        driver.switchTo().window(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("overview"), "Should return to original page after closing external tab");
    }

    @Test
    @Order(4)
    void testFooterSocialLinks() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        usernameField.sendKeys(USERNAME);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.xpath("//input[@value='Log In']")).click();
        wait.until(ExpectedConditions.urlContains("overview"));

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("div.footer a"));
        assertTrue(footerLinks.size() >= 3, "Footer should contain at least 3 social links");

        String originalWindow = driver.getWindowHandle();

        for (int i = 0; i < footerLinks.size(); i++) {
            WebElement link = footerLinks.get(i);
            String linkText = link.getText();

            if (linkText.isEmpty()) continue;

            link.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            String newWindow = null;
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    newWindow = handle;
                    break;
                }
            }
            assertNotNull(newWindow, "New window should open for social link: " + linkText);
            driver.switchTo().window(newWindow);

            String currentUrl = driver.getCurrentUrl();
            if (linkText.contains("Twitter")) {
                assertTrue(currentUrl.contains("twitter.com") || currentUrl.contains("x.com"), "Twitter link should open correct domain");
            } else if (linkText.contains("Facebook")) {
                assertTrue(currentUrl.contains("facebook.com"), "Facebook link should open correct domain");
            } else if (linkText.contains("LinkedIn")) {
                assertTrue(currentUrl.contains("linkedin.com"), "LinkedIn link should open correct domain");
            }

            driver.close();
            driver.switchTo().window(originalWindow);
            wait.until(ExpectedConditions.urlContains("overview"));
            footerLinks = driver.findElements(By.cssSelector("div.footer a"));
        }
    }

    @Test
    @Order(5)
    void testLogoutFunctionality() {
        driver.get(BASE_URL);

        loginIfNotLoggedIn();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.htm"));
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should be redirected to home page after logout");
        WebElement loginButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@value='Log In']")));
        assertTrue(loginButton.isDisplayed(), "Login button should be visible after logout");
    }

    @Test
    @Order(6)
    void testRegisterLinkNavigation() {
        driver.get(BASE_URL);

        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();

        wait.until(ExpectedConditions.urlContains("register.htm"));
        assertTrue(driver.getCurrentUrl().contains("register.htm"), "Should navigate to registration page");
        assertEquals("ParaBank | Register for Free Online Account Access", driver.getTitle());
    }

    @Test
    @Order(7)
    void testContactLinkNavigation() {
        driver.get(BASE_URL);

        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("CONTACT")));
        contactLink.click();

        wait.until(ExpectedConditions.urlContains("contact.htm"));
        assertTrue(driver.getCurrentUrl().contains("contact.htm"), "Should navigate to contact page");
        assertEquals("ParaBank | Contact", driver.getTitle());
    }

    @Test
    @Order(8)
    void testAdminPageLinkExternal() {
        driver.get(BASE_URL);

        loginIfNotLoggedIn();

        WebElement adminPageLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        adminPageLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }
        assertNotNull(newWindow, "Admin Page link should open in new window");
        driver.switchTo().window(newWindow);

        assertTrue(driver.getCurrentUrl().contains("parabank.parasoft.com"), "Admin Page should open on same domain");
        assertTrue(driver.getTitle().contains("Admin"), "Admin Page title should contain 'Admin'");

        driver.close();
        driver.switchTo().window(originalWindow);
        assertTrue(driver.getCurrentUrl().contains("overview"), "Should return to overview page after closing admin tab");
    }

    @Test
    @Order(9)
    void testBillPayNavigation() {
        driver.get(BASE_URL);

        loginIfNotLoggedIn();

        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.urlContains("billpay.htm"));
        assertTrue(driver.getCurrentUrl().contains("billpay.htm"), "Should navigate to bill pay page");
        assertEquals("ParaBank | Bill Pay", driver.getTitle());
    }

    @Test
    @Order(10)
    void testTransferFundsNavigation() {
        driver.get(BASE_URL);

        loginIfNotLoggedIn();

        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();

        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "Should navigate to transfer funds page");
        assertEquals("ParaBank | Transfer Funds", driver.getTitle());
    }

    @Test
    @Order(11)
    void testAccountsOverviewNavigation() {
        driver.get(BASE_URL);

        loginIfNotLoggedIn();

        WebElement accountsOverviewLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountsOverviewLink.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should navigate to accounts overview");
        assertEquals("ParaBank | Accounts Overview", driver.getTitle());
    }

    @Test
    @Order(12)
    void testOpenNewAccountNavigation() {
        driver.get(BASE_URL);

        loginIfNotLoggedIn();

        WebElement openNewAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openNewAccountLink.click();

        wait.until(ExpectedConditions.urlContains("openaccount.htm"));
        assertTrue(driver.getCurrentUrl().contains("openaccount.htm"), "Should navigate to open new account page");
        assertEquals("ParaBank | Open New Account", driver.getTitle());
    }

    @Test
    @Order(13)
    void testRequestLoanNavigation() {
        driver.get(BASE_URL);

        loginIfNotLoggedIn();

        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();

        wait.until(ExpectedConditions.urlContains("loan.htm"));
        assertTrue(driver.getCurrentUrl().contains("loan.htm"), "Should navigate to loan request page");
        assertEquals("ParaBank | Loan Request", driver.getTitle());
    }

    private void loginIfNotLoggedIn() {
        if (driver.getCurrentUrl().contains("index.htm") || driver.findElements(By.name("username")).size() > 0) {
            driver.get(BASE_URL);
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            usernameField.sendKeys(USERNAME);
            driver.findElement(By.name("password")).sendKeys(PASSWORD);
            driver.findElement(By.xpath("//input[@value='Log In']")).click();
            wait.until(ExpectedConditions.urlContains("overview"));
        }
    }
}