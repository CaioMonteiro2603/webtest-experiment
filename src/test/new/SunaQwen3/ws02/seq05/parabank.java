package SunaQwen3.ws02.seq05;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
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
        assertEquals("ParaBank | Welcome | Online Banking", driver.getTitle(), "Page title should match after navigation");

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        try {
            WebElement errorMessage = driver.findElement(By.cssSelector(".error"));
            if (errorMessage.isDisplayed() && errorMessage.getText().contains("Please enter a valid username and password")) {
                Assertions.fail("Login failed - invalid credentials");
            }
        } catch (NoSuchElementException e) {
        }

        wait.until(ExpectedConditions.urlContains("overview.htm"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should be redirected to overview after login");
        assertTrue(driver.findElement(By.linkText("Log Out")).isDisplayed(), "Log Out link should be visible after login");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys("invaliduser");
        passwordField.sendKeys("wrongpass");
        loginButton.click();

        try {
            WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
            assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid credentials");
        } catch (TimeoutException e) {
            try {
                WebElement errorMessage = driver.findElement(By.xpath("//p[contains(text(), 'The username and password could not be verified.')]"));
                assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid credentials");
            } catch (NoSuchElementException ex) {
                WebElement errorMessage = driver.findElement(By.xpath("//*[contains(text(), 'invalid') or contains(text(), 'error') or contains(text(), 'incorrect')]"));
                assertTrue(errorMessage.isDisplayed(), "Error message should appear for invalid credentials");
            }
        }
    }

    @Test
    @Order(3)
    void testAccountServicesLinks() {
        testValidLogin();

        String[] links = {"Transfer Funds", "Bill Pay", "Find Transactions", "Update Contact Info", "Request Loan"};
        for (String linkText : links) {
            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
            link.click();

            String expectedUrlPart = linkText.toLowerCase().replace(" ", "").replace("info", "contact");
            expectedUrlPart = expectedUrlPart.replace("updatecontact", "updateprofile")
                    .replace("findtransactions", "findtrans")
                    .replace("requestloan", "requestloan");
            wait.until(ExpectedConditions.urlContains(expectedUrlPart));
            assertTrue(driver.getCurrentUrl().contains(expectedUrlPart), "Should navigate to " + expectedUrlPart + " page");
            driver.findElement(By.linkText("Accounts Overview")).click();
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
    }

    @Test
    @Order(4)
    void testAdminPageLink() {
        testValidLogin();

        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        adminLink.click();

        wait.until(ExpectedConditions.urlContains("admin.htm"));
        assertTrue(driver.getCurrentUrl().contains("admin.htm"), "Should navigate to admin page");
        assertEquals("ParaBank | Admin", driver.getTitle(), "Admin page title should match");
    }

    @Test
    @Order(5)
    void testExternalLinksInFooter() {
        testValidLogin();

        String[] externalLinks = {"Twitter", "Facebook", "LinkedIn"};
        String[] expectedDomains = {"twitter.com", "facebook.com", "linkedin.com"};

        for (int i = 0; i < externalLinks.length; i++) {
            String linkText = externalLinks[i];
            String expectedDomain = expectedDomains[i];

            WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
            String originalWindow = driver.getWindowHandle();
            link.click();

            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }

            assertTrue(driver.getCurrentUrl().contains(expectedDomain), "External link should open correct domain: " + expectedDomain);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(6)
    void testAboutLinkOpensNewTab() {
        testValidLogin();

        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String newWindow = null;
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(originalWindow)) {
                newWindow = windowHandle;
                break;
            }
        }

        assertNotNull(newWindow, "A new window should have been opened");
        driver.switchTo().window(newWindow);
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "About link should redirect to parasoft.com");
        driver.close();
        driver.switchTo().window(originalWindow);
    }

    @Test
    @Order(7)
    void testLogoutFunctionality() {
        testValidLogin();

        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.urlContains("index.htm"));
        assertTrue(driver.getCurrentUrl().contains("index.htm"), "Should return to home page after logout");
        assertTrue(driver.findElement(By.xpath("//input[@value='Log In']")).isDisplayed(), "Login button should be visible after logout");
    }

    @Test
    @Order(8)
    void testRegisterLinkAndForm() {
        driver.get(BASE_URL);
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();

        wait.until(ExpectedConditions.urlContains("register.htm"));
        assertTrue(driver.getCurrentUrl().contains("register.htm"), "Should navigate to registration page");
        assertEquals("ParaBank | Register for Free Online Account Access", driver.getTitle(), "Registration page title should match");

        assertTrue(driver.findElement(By.id("customer.firstName")).isDisplayed(), "First name field should be present");
        assertTrue(driver.findElement(By.id("customer.lastName")).isDisplayed(), "Last name field should be present");
        assertTrue(driver.findElement(By.id("customer.address.street")).isDisplayed(), "Address field should be present");
        assertTrue(driver.findElement(By.id("customer.phoneNumber")).isDisplayed(), "Phone number field should be present");
        assertTrue(driver.findElement(By.id("customer.ssn")).isDisplayed(), "SSN field should be present");
        assertTrue(driver.findElement(By.id("customer.username")).isDisplayed(), "Username field should be present");
        assertTrue(driver.findElement(By.id("customer.password")).isDisplayed(), "Password field should be present");
        assertTrue(driver.findElement(By.id("repeatedPassword")).isDisplayed(), "Confirm password field should be present");
    }

    @Test
    @Order(9)
    void testContactLink() {
        driver.get(BASE_URL);
        WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(@href, 'contact') or contains(text(), 'Contact')]")));
        contactLink.click();

        wait.until(ExpectedConditions.urlContains("contact"));
        assertTrue(driver.getCurrentUrl().contains("contact"), "Should navigate to contact page");
        assertEquals("ParaBank | Contact", driver.getTitle(), "Contact page title should match");
    }

    @Test
    @Order(10)
    void testServicesDropdownNavigation() {
        testValidLogin();

        WebElement servicesDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesDropdown.click();

        WebElement fundTransferLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds")));
        fundTransferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer"));
        assertTrue(driver.getCurrentUrl().contains("transfer"), "Should navigate to transfer funds page");

        servicesDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesDropdown.click();

        WebElement billPayLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay")));
        billPayLink.click();
        wait.until(ExpectedConditions.urlContains("billpay"));
        assertTrue(driver.getCurrentUrl().contains("billpay"), "Should navigate to bill pay page");
    }
}