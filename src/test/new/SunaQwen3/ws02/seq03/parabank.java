package SunaQwen3.ws02.seq03;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;

@TestMethodOrder(OrderAnnotation.class)
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
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().contains("ParaBank"), "Page title should contain 'ParaBank'");

        WebElement usernameField = wait.until(visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        WebElement welcomeMessage = wait.until(visibilityOfElementLocated(By.xpath("//*[contains(text(),'Welcome')]")));
        Assertions.assertTrue(welcomeMessage.isDisplayed(), "Welcome message should be displayed after login");
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "URL should contain 'overview.htm' after login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invaliduser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(visibilityOfElementLocated(By.xpath("//*[contains(text(),'Please check your username and password')]")));
        Assertions.assertTrue(errorMessage.isDisplayed(),
                "Error message should be displayed for invalid login");
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should remain on login page after failed login");
    }

    @Test
    @Order(3)
    public void testLogoutFunctionality() {
        loginIfNotLoggedIn();

        WebElement logoutLink = wait.until(elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        WebElement loginButton = wait.until(visibilityOfElementLocated(By.xpath("//input[@value='Log In']")));
        Assertions.assertTrue(loginButton.isDisplayed(), "Login button should be visible after logout");
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"), "URL should redirect to login page after logout");
    }

    @Test
    @Order(4)
    public void testAboutLinkExternalNavigation() {
        loginIfNotLoggedIn();

        WebElement aboutLink = wait.until(elementToBeClickable(By.linkText("About")));
        aboutLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }

        Assertions.assertNotNull(newWindow, "New window should be opened for About link");

        driver.switchTo().window(newWindow);
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("parasoft.com"), "About link should navigate to parasoft.com domain");

        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window after closing About tab");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        loginIfNotLoggedIn();

        List<WebElement> footerLinks = driver.findElements(By.cssSelector("#footer a"));
        Assertions.assertTrue(footerLinks.size() >= 3, "Footer should contain at least 3 social links");

        String originalWindow = driver.getWindowHandle();

        // Test Twitter link
        WebElement twitterLink = footerLinks.get(0);
        String twitterHref = twitterLink.getAttribute("href");
        Assertions.assertTrue(twitterHref.contains("twitter.com"), "Twitter link should point to twitter.com");
        twitterLink.click();

        wait.until(numberOfWindowsToBe(2));
        switchToNewWindowAndClose(originalWindow, "twitter.com");

        // Test Facebook link
        driver.switchTo().window(originalWindow);
        WebElement facebookLink = driver.findElements(By.cssSelector("#footer a")).get(1);
        String facebookHref = facebookLink.getAttribute("href");
        Assertions.assertTrue(facebookHref.contains("facebook.com"), "Facebook link should point to facebook.com");
        facebookLink.click();

        wait.until(numberOfWindowsToBe(2));
        switchToNewWindowAndClose(originalWindow, "facebook.com");

        // Test LinkedIn link
        driver.switchTo().window(originalWindow);
        WebElement linkedinLink = driver.findElements(By.cssSelector("#footer a")).get(2);
        String linkedinHref = linkedinLink.getAttribute("href");
        Assertions.assertTrue(linkedinHref.contains("linkedin.com"), "LinkedIn link should point to linkedin.com");
        linkedinLink.click();

        wait.until(numberOfWindowsToBe(2));
        switchToNewWindowAndClose(originalWindow, "linkedin.com");
    }

    @Test
    @Order(6)
    public void testAdminPageLink() {
        loginIfNotLoggedIn();

        WebElement adminPageLink = wait.until(elementToBeClickable(By.linkText("Admin Page")));
        adminPageLink.click();

        String originalWindow = driver.getWindowHandle();
        wait.until(numberOfWindowsToBe(2));

        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }

        Assertions.assertNotNull(newWindow, "New window should be opened for Admin Page link");

        driver.switchTo().window(newWindow);
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("parabank.parasoft.com") || currentUrl.contains("parasoft.com"),
                "Admin Page should be within parasoft domain");

        driver.close();
        driver.switchTo().window(originalWindow);
        Assertions.assertEquals(originalWindow, driver.getWindowHandle(), "Should return to original window after closing Admin tab");
    }

    @Test
    @Order(7)
    public void testRequestLoanLink() {
        loginIfNotLoggedIn();

        WebElement requestLoanLink = wait.until(elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();

        WebElement loanFormTitle = wait.until(visibilityOfElementLocated(By.id("rightPanel")));
        Assertions.assertTrue(loanFormTitle.getText().contains("Loan Application"),
                "Should navigate to Loan Application page");
        Assertions.assertTrue(driver.getCurrentUrl().contains("loan.htm"), "URL should contain 'loan.htm'");
    }

    @Test
    @Order(8)
    public void testTransferFundsLink() {
        loginIfNotLoggedIn();

        WebElement transferFundsLink = wait.until(elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();

        WebElement transferTitle = wait.until(visibilityOfElementLocated(By.id("rightPanel")));
        Assertions.assertTrue(transferTitle.getText().contains("Transfer Funds"),
                "Should navigate to Transfer Funds page");
        Assertions.assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "URL should contain 'transfer.htm'");
    }

    @Test
    @Order(9)
    public void testBillPayLink() {
        loginIfNotLoggedIn();

        WebElement billPayLink = wait.until(elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();

        WebElement billPayTitle = wait.until(visibilityOfElementLocated(By.id("rightPanel")));
        Assertions.assertTrue(billPayTitle.getText().contains("Bill Payment Service"),
                "Should navigate to Bill Payment Service page");
        Assertions.assertTrue(driver.getCurrentUrl().contains("billpay.htm"), "URL should contain 'billpay.htm'");
    }

    @Test
    @Order(10)
    public void testUpdateContactInfoLink() {
        loginIfNotLoggedIn();

        WebElement updateContactLink = wait.until(elementToBeClickable(By.linkText("Update Contact Info")));
        updateContactLink.click();

        WebElement contactFormTitle = wait.until(visibilityOfElementLocated(By.id("rightPanel")));
        Assertions.assertTrue(contactFormTitle.getText().contains("Update Profile"),
                "Should navigate to Update Profile page");
        Assertions.assertTrue(driver.getCurrentUrl().contains("updateprofile.htm"), "URL should contain 'updateprofile.htm'");
    }

    @Test
    @Order(11)
    public void testOpenNewAccountLink() {
        loginIfNotLoggedIn();

        WebElement openNewAccountLink = wait.until(elementToBeClickable(By.linkText("Open New Account")));
        openNewAccountLink.click();

        WebElement accountFormTitle = wait.until(visibilityOfElementLocated(By.id("rightPanel")));
        Assertions.assertTrue(accountFormTitle.getText().contains("Open New Account"),
                "Should navigate to Open New Account page");
        Assertions.assertTrue(driver.getCurrentUrl().contains("openaccount.htm"), "URL should contain 'openaccount.htm'");
    }

    @Test
    @Order(12)
    public void testAccountsOverviewLink() {
        loginIfNotLoggedIn();

        WebElement accountsOverviewLink = wait.until(elementToBeClickable(By.linkText("Accounts Overview")));
        accountsOverviewLink.click();

        WebElement overviewTitle = wait.until(visibilityOfElementLocated(By.id("rightPanel")));
        Assertions.assertTrue(overviewTitle.getText().contains("Accounts Overview"),
                "Should navigate to Accounts Overview page");
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"), "URL should contain 'overview.htm'");
    }

    @Test
    @Order(13)
    public void testFindTransactionsLink() {
        loginIfNotLoggedIn();

        WebElement findTransactionsLink = wait.until(elementToBeClickable(By.linkText("Find Transactions")));
        findTransactionsLink.click();

        WebElement transactionsTitle = wait.until(visibilityOfElementLocated(By.id("rightPanel")));
        Assertions.assertTrue(transactionsTitle.getText().contains("Find Transactions"),
                "Should navigate to Find Transactions page");
        Assertions.assertTrue(driver.getCurrentUrl().contains("activity.htm"), "URL should contain 'activity.htm'");
    }

    private void loginIfNotLoggedIn() {
        driver.get(BASE_URL);
        try {
            WebElement logoutLink = driver.findElement(By.linkText("Log Out"));
            if (logoutLink.isDisplayed()) {
                return; // Already logged in
            }
        } catch (NoSuchElementException e) {
            // Not logged in, proceed with login
        }

        WebElement usernameField = wait.until(visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(visibilityOfElementLocated(By.linkText("Log Out")));
    }

    private void switchToNewWindowAndClose(String originalWindow, String expectedDomain) {
        String newWindow = null;
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalWindow)) {
                newWindow = handle;
                break;
            }
        }

        Assertions.assertNotNull(newWindow, "New window should be opened for external link");

        driver.switchTo().window(newWindow);
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains(expectedDomain),
                "External link should navigate to " + expectedDomain + " domain");

        driver.close();
        driver.switchTo().window(originalWindow);
    }
}