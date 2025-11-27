package SunaQwen3.ws02.seq02;

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
public class ParaBankTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN = "caio@gmail.com";
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
        assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle());

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should be redirected to overview after login");
        assertTrue(driver.getPageSource().contains("Accounts Overview"), "Accounts Overview header should be present");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invaliduser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertEquals("The username and password could not be verified.", errorMessage.getText());
    }

    @Test
    @Order(3)
    public void testAccessAdminPage() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        adminLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Admin"));
        assertTrue(driver.getCurrentUrl().contains("admin.htm"), "Should navigate to admin page");
        assertTrue(driver.getPageSource().contains("Parameter Name"), "Admin page table header should be present");
    }

    @Test
    @Order(4)
    public void testAccessAboutPage() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        aboutLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | About Us"));
        assertTrue(driver.getCurrentUrl().contains("about.htm"), "Should navigate to about page");
        assertTrue(driver.getPageSource().contains("About ParaBank"), "About Us header should be present");
    }

    @Test
    @Order(5)
    public void testAccessContactPage() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
        contactLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Contact"));
        assertTrue(driver.getCurrentUrl().contains("contact.htm"), "Should navigate to contact page");
        assertTrue(driver.getPageSource().contains("Contact ParaBank"), "Contact header should be present");
    }

    @Test
    @Order(6)
    public void testAccessServicesPage() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Services"));
        assertTrue(driver.getCurrentUrl().contains("services.htm"), "Should navigate to services page");
        assertTrue(driver.getPageSource().contains("Online Banking Services"), "Services header should be present");
    }

    @Test
    @Order(7)
    public void testAccessFindOwnerPage() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement findOwnerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Owner")));
        findOwnerLink.click();

        wait.until(ExpectedConditions.titleIs("PetClinic :: a Spring Framework demonstration"));
        assertTrue(driver.getCurrentUrl().contains("petclinic"), "Should navigate to petclinic find owner");
        assertTrue(driver.getPageSource().contains("Find Owner"), "Find Owner header should be present");
    }

    @Test
    @Order(8)
    public void testAccessOnlineBankingPage() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement onlineBankingLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Online Banking")));
        onlineBankingLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should navigate to online banking home");
        assertTrue(driver.getPageSource().contains("Customer Login"), "Login form should be present");
    }

    @Test
    @Order(9)
    public void testAccessAdminPageFromMenu() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        adminLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Admin"));
        assertTrue(driver.getCurrentUrl().contains("admin.htm"), "Should navigate to admin page");
    }

    @Test
    @Order(10)
    public void testLogoutFunctionality() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should return to home page after logout");
        assertTrue(driver.getPageSource().contains("Customer Login"), "Login form should be visible after logout");
    }

    @Test
    @Order(11)
    public void testExternalFooterLinks() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        // Store original window handle
        String originalWindow = driver.getWindowHandle();

        // Test Facebook link
        WebElement facebookLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='facebook']")));
        facebookLink.click();

        // Switch to new tab
        String newWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.size() > 0 ? handles.iterator().next() : null;
        });
        driver.switchTo().window(newWindow);

        assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should open facebook.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test Twitter link
        WebElement twitterLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='twitter']")));
        twitterLink.click();

        newWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.size() > 0 ? handles.iterator().next() : null;
        });
        driver.switchTo().window(newWindow);

        assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should open twitter.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);

        // Test LinkedIn link
        WebElement linkedinLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href*='linkedin']")));
        linkedinLink.click();

        newWindow = wait.until(d -> {
            Set<String> handles = d.getWindowHandles();
            handles.remove(originalWindow);
            return handles.size() > 0 ? handles.iterator().next() : null;
        });
        driver.switchTo().window(newWindow);

        assertTrue(driver.getCurrentUrl().contains("linkedin.com"), "LinkedIn link should open linkedin.com domain");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(12)
    public void testAccountServicesMenu() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        // Open Account Services menu
        WebElement accountServicesMenu = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview")));
        accountServicesMenu.click();

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should navigate to accounts overview");

        // Test Transfer Funds
        WebElement transferFundsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        transferFundsLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Transfer Funds"));
        assertTrue(driver.getCurrentUrl().contains("transfer.htm"), "Should navigate to transfer funds page");

        // Test Bill Pay
        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Bill Pay"));
        assertTrue(driver.getCurrentUrl().contains("billpay.htm"), "Should navigate to bill pay page");

        // Test Find Transactions
        WebElement findTransactionsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions")));
        findTransactionsLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Find Transactions"));
        assertTrue(driver.getCurrentUrl().contains("findtrans.htm"), "Should navigate to find transactions page");

        // Test Update Contact Info
        WebElement updateContactInfoLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info")));
        updateContactInfoLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Update Profile"));
        assertTrue(driver.getCurrentUrl().contains("updateprofile.htm"), "Should navigate to update profile page");
    }

    @Test
    @Order(13)
    public void testOpenNewAccount() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement openNewAccountLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account")));
        openNewAccountLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Open New Account"));
        assertTrue(driver.getCurrentUrl().contains("openaccount.htm"), "Should navigate to open new account page");

        // Select account type and from account
        WebElement accountTypeSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("type")));
        accountTypeSelect.click();
        driver.findElement(By.cssSelector("option[value='1']")).click(); // Savings

        WebElement fromAccountSelect = driver.findElement(By.id("fromAccountId"));
        fromAccountSelect.click();
        List<WebElement> options = fromAccountSelect.findElements(By.tagName("option"));
        if (options.size() > 1) {
            options.get(1).click(); // Select first available account
        }

        WebElement openAccountButton = driver.findElement(By.cssSelector("input[value='Open New Account']"));
        openAccountButton.click();

        // Wait for success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertTrue(successMessage.getText().contains("Congratulations, your account is now open."), "Account should be opened successfully");
    }

    @Test
    @Order(14)
    public void testRequestLoan() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement requestLoanLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Request Loan")));
        requestLoanLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Loan Request"));
        assertTrue(driver.getCurrentUrl().contains("loan.htm"), "Should navigate to loan request page");

        // Fill loan request form
        WebElement amountField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        amountField.sendKeys("10000");

        WebElement downPaymentField = driver.findElement(By.id("downPayment"));
        downPaymentField.sendKeys("2000");

        WebElement fromAccountIdSelect = driver.findElement(By.id("fromAccountId"));
        fromAccountIdSelect.click();
        List<WebElement> options = fromAccountIdSelect.findElements(By.tagName("option"));
        if (options.size() > 1) {
            options.get(1).click(); // Select first available account
        }

        WebElement applyNowButton = driver.findElement(By.cssSelector("input[value='Apply Now']"));
        applyNowButton.click();

        // Wait for result
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        String resultText = driver.findElement(By.cssSelector(".error")).getText();
        assertTrue(resultText.contains("Approved") || resultText.contains("Denied"), "Loan should be either approved or denied");
    }

    @Test
    @Order(15)
    public void testUpdateProfile() {
        // Ensure logged in
        if (!driver.getCurrentUrl().contains("overview.htm")) {
            testValidLogin();
        }

        WebElement updateProfileLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info")));
        updateProfileLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Update Profile"));
        assertTrue(driver.getCurrentUrl().contains("updateprofile.htm"), "Should navigate to update profile page");

        // Update profile fields
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName")));
        firstNameField.clear();
        firstNameField.sendKeys("Caio");

        WebElement lastNameField = driver.findElement(By.id("customer.lastName"));
        lastNameField.clear();
        lastNameField.sendKeys("Silva");

        WebElement addressField = driver.findElement(By.id("customer.address.street"));
        addressField.clear();
        addressField.sendKeys("123 Main St");

        WebElement cityField = driver.findElement(By.id("customer.address.city"));
        cityField.clear();
        cityField.sendKeys("Anytown");

        WebElement stateField = driver.findElement(By.id("customer.address.state"));
        stateField.clear();
        stateField.sendKeys("CA");

        WebElement zipCodeField = driver.findElement(By.id("customer.address.zipCode"));
        zipCodeField.clear();
        zipCodeField.sendKeys("90210");

        WebElement phoneField = driver.findElement(By.id("customer.phoneNumber"));
        phoneField.clear();
        phoneField.sendKeys("555-123-4567");

        WebElement ssnField = driver.findElement(By.id("customer.ssn"));
        ssnField.clear();
        ssnField.sendKeys("123-45-6789");

        WebElement usernameField = driver.findElement(By.id("customer.username"));
        assertTrue(usernameField.getAttribute("value").equals(LOGIN), "Username should match login");

        WebElement updateProfileButton = driver.findElement(By.cssSelector("input[value='Update Profile']"));
        updateProfileButton.click();

        // Wait for success message
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertEquals("Profile Updated", successMessage.getText(), "Profile should be updated successfully");
    }
}