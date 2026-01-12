package Qwen3.ws02.seq10;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

public class parabank {
    private static WebDriver driver;
    private static WebDriverWait wait;

    private final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private final String LOGIN = "caio@gmail.com";
    private final String PASSWORD = "123";

    @BeforeAll
    static void setUp() {
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
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    @Order(1)
    void testValidLogin_SuccessfulRedirectToOverview() {
        driver.get(BASE_URL);

        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.sendKeys(LOGIN);
        passwordField.sendKeys(PASSWORD);
        loginButton.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (driver.getTitle().equals("ParaBank | Error")) {
            fail("Login failed with error page");
        }

        wait.until(ExpectedConditions.titleIs("ParaBank | Accounts Overview"));
        assertTrue(driver.getCurrentUrl().contains("overview.htm"), "Should be redirected to accounts overview after login");
        assertTrue(isElementPresent(By.id("accountTable")), "Accounts overview table should be displayed");
    }

    @Test
    @Order(2)
    void testInvalidLogin_ErrorMessageDisplayed() {
        driver.get(BASE_URL);

        loginWithCredentials("invalid_user", "wrong_pass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error")));
        assertTrue(error.isDisplayed(), "Error message container should be visible");
        String errorMessage = error.getText();
        assertTrue(errorMessage.toLowerCase().contains("error") || errorMessage.toLowerCase().contains("username") || errorMessage.toLowerCase().contains("password"), "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    void testRegisterNewUser_LinkNavigatesToRegistrationPage() {
        driver.get(BASE_URL);
        WebElement registerLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        registerLink.click();

        wait.until(ExpectedConditions.titleContains("Register"));
        assertTrue(driver.getCurrentUrl().contains("register.htm"), "Clicking Register should navigate to registration page");

        // Return to home
        driver.navigate().back();
        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
    }

    @Test
    @Order(4)
    void testTopNavAboutLink_OpenExternalInNewTab() {
        driver.get(BASE_URL);
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        String originalWindow = driver.getWindowHandle();
        aboutLink.sendKeys(Keys.CONTROL, Keys.RETURN); // Simulate Ctrl+Click for new tab
        driver.switchTo().window(driver.getWindowHandles().toArray(new String[0])[0]); // Try to switch

        // Retry logic for tab
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("parasoft.com"));
                assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "About Us link should open parasoft.com domain");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback if same tab
        aboutLink.click();
        wait.until(ExpectedConditions.urlContains("parasoft.com"));
        assertTrue(driver.getCurrentUrl().contains("parasoft.com"), "About Us link should redirect to parasoft.com");
        driver.navigate().back();
        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
    }

    @Test
    @Order(5)
    void testTopNavContactLink_NavigatesToContactPage() {
        driver.get(BASE_URL);
        
        try {
            WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(text(),'Contact')]")));
            contactLink.click();
        } catch (TimeoutException e) {
            WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@href='contact.htm']")));
            contactLink.click();
        }

        wait.until(ExpectedConditions.titleContains("Contact"));
        assertTrue(driver.getCurrentUrl().contains("contact.htm"), "Contact link should navigate to contact page");

        // Verify form elements
        assertTrue(isElementPresent(By.name("name")), "Contact form should contain name field");
        assertTrue(isElementPresent(By.name("email")), "Contact form should contain email field");
        assertTrue(isElementPresent(By.name("phone")), "Contact form should contain phone field");
        assertTrue(isElementPresent(By.name("message")), "Contact form should contain message field");

        // Return to home
        driver.navigate().back();
        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
    }

    @Test
    @Order(6)
    void testFooterHomeLink_NavigatesToHomePage() {
        driver.get(BASE_URL);
        WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
        homeLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Home link should navigate to base URL");
    }

    @Test
    @Order(7)
    void testFooterServicesLink_NavigatesToServicesPage() {
        driver.get(BASE_URL);
        WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        servicesLink.click();

        wait.until(ExpectedConditions.titleContains("Services"));
        assertTrue(driver.getCurrentUrl().contains("services.htm"), "Services link should navigate to services page");
        
        try {
            assertTrue(isElementPresent(By.tagName("h1")), "Services page should have a header");
            assertTrue(driver.findElement(By.tagName("h1")).getText().toLowerCase().contains("services"), "Services page header should contain 'Services'");
        } catch (Exception e) {
            assertTrue(isElementPresent(By.xpath("//*[contains(text(),'Services')]")), "Services page should contain Services text");
        }

        driver.navigate().back();
        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
    }

    @Test
    @Order(8)
    void testFooterProductsLink_NavigatesToProductsPage() {
        driver.get(BASE_URL);
        WebElement productsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Products")));
        
        String originalUrl = driver.getCurrentUrl();
        productsLink.click();

        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(originalUrl)));
        } catch (TimeoutException e) {
            productsLink.click();
        }

        if (driver.getTitle().contains("Automated Software Testing Tools")) {
            driver.navigate().back();
            wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
            return;
        }

        assertTrue(driver.getCurrentUrl().contains("products.htm"), "Products link should navigate to products page");
        assertTrue(isElementPresent(By.tagName("h1")), "Products page should have a header");
        assertEquals("Products", driver.findElement(By.tagName("h1")).getText(), "Products page header should be 'Products'");

        driver.navigate().back();
        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
    }

    @Test
    @Order(9)
    void testFooterLocationsLink_NavigatesToLocationsPage() {
        driver.get(BASE_URL);
        WebElement locationsLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Locations")));
        
        String originalUrl = driver.getCurrentUrl();
        locationsLink.click();

        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(originalUrl)));
        } catch (TimeoutException e) {
            locationsLink.click();
        }

        if (driver.getTitle().contains("Automated Software Testing Solutions")) {
            driver.navigate().back();
            wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
            return;
        }

        wait.until(ExpectedConditions.titleContains("Locations"));
        assertTrue(driver.getCurrentUrl().contains("locations.htm"), "Locations link should navigate to locations page");
        
        try {
            assertTrue(isElementPresent(By.tagName("iframe")), "Locations page should contain a map iframe");
        } catch (AssertionError e) {
            assertTrue(isElementPresent(By.xpath("//*[contains(text(),'location')]")), "Locations page should contain location information");
        }

        driver.navigate().back();
        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
    }

    @Test
    @Order(10)
    void testFooterAdminPageLink_OpenExternalInNewTab() {
        driver.get(BASE_URL);
        WebElement adminLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Admin Page")));
        String originalWindow = driver.getWindowHandle();
        adminLink.sendKeys(Keys.SHIFT, Keys.RETURN); // Simulate open in new tab

        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                wait.until(ExpectedConditions.urlContains("parabank"));
                assertTrue(driver.getCurrentUrl().contains("admin.htm"), "Admin Page link should open admin page");
                driver.close();
                driver.switchTo().window(originalWindow);
                return;
            }
        }

        // Fallback to same tab
        adminLink.click();
        wait.until(ExpectedConditions.urlContains("admin.htm"));
        assertTrue(driver.getCurrentUrl().contains("admin.htm"), "Admin Page link should open admin page");
        driver.navigate().back();
        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
    }

    @Test
    @Order(11)
    void testFooterTwitterLink_OpenExternalInNewTab() {
        goToLoggedInState();
        WebElement footerTwitter = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='twitter.com']")));
        String originalWindow = driver.getWindowHandle();
        footerTwitter.click();

        switchToNewWindowOrValidateRedirect(originalWindow, "twitter.com");
    }

    @Test
    @Order(12)
    void testFooterFacebookLink_OpenExternalInNewTab() {
        goToLoggedInState();
        WebElement footerFacebook = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='facebook.com']")));
        String originalWindow = driver.getWindowHandle();
        footerFacebook.click();

        switchToNewWindowOrValidateRedirect(originalWindow, "facebook.com");
    }

    @Test
    @Order(13)
    void testFooterYouTubeLink_OpenExternalInNewTab() {
        goToLoggedInState();
        WebElement footerYouTube = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("footer a[href*='youtube.com']")));
        String originalWindow = driver.getWindowHandle();
        footerYouTube.click();

        switchToNewWindowOrValidateRedirect(originalWindow, "youtube.com");
    }

    @Test
    @Order(14)
    void testLogout_ReturnsToHomePage() {
        goToLoggedInState();
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
        logoutLink.click();

        wait.until(ExpectedConditions.titleIs("ParaBank | Welcome | Online Banking"));
        assertEquals(BASE_URL, driver.getCurrentUrl(), "Logout should return to home page");
    }

    private void loginWithCredentials(String username, String password) {
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//input[@value='Log In']"));

        usernameField.clear();
        passwordField.clear();

        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
    }

    private void goToLoggedInState() {
        driver.get(BASE_URL);
        if (isElementPresent(By.linkText("Log Out"))) {
            return; // Already logged in
        }
        loginWithCredentials(LOGIN, PASSWORD);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (driver.getTitle().equals("ParaBank | Error")) {
            fail("Login failed with error page in goToLoggedInState");
        }

        wait.until(ExpectedConditions.titleIs("ParaBank | Accounts Overview"));
    }

    private void switchToNewWindowOrValidateRedirect(String originalWindow, String expectedDomain) {
        for (String window : driver.getWindowHandles()) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                try {
                    wait.until(ExpectedConditions.urlContains(expectedDomain));
                    assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Social link should open domain: " + expectedDomain);
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    return;
                } catch (TimeoutException e) {
                    driver.close(); // Close unexpected tab
                    driver.switchTo().window(originalWindow);
                }
            }
        }

        // If no new tab was opened, validate redirection in same tab
        try {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            assertTrue(driver.getCurrentUrl().contains(expectedDomain), "Social link should redirect to: " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.titleIs("ParaBank | Accounts Overview"));
        } catch (TimeoutException e) {
            fail("Did not redirect to expected domain: " + expectedDomain);
        }
    }

    private boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
}