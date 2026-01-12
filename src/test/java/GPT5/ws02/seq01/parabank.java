package GPT5.ws02.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

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
        options.addArguments("--headless"); // REQUIRED
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
    public static void teardown() {
        if (driver != null) driver.quit();
    }

    /* -------------------- Helpers -------------------- */

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    private void performLogin(String username, String password) {
        openHome();
        WebElement user = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement pass = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input.button"));

        user.clear(); user.sendKeys(username);
        pass.clear(); pass.sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn)).click();
    }

    private boolean isLoggedIn() {
        return driver.findElements(By.linkText("Log Out")).size() > 0
                || driver.getCurrentUrl().contains("/overview.htm")
                || driver.findElements(By.linkText("Accounts Overview")).size() > 0;
    }

    private void logoutIfLoggedIn() {
        if (driver.findElements(By.linkText("Log Out")).size() > 0) {
            driver.findElement(By.linkText("Log Out")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        }
    }

    private void switchToNewTabAndVerify(String expectedDomain) {
        String original = driver.getWindowHandle();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        for (String w : driver.getWindowHandles()) {
            if (!w.equals(original)) {
                driver.switchTo().window(w);
                wait.until(ExpectedConditions.urlContains(expectedDomain));
                Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                        "Expected domain not found in URL: " + expectedDomain);
                driver.close();
                driver.switchTo().window(original);
                return;
            }
        }
        Assertions.fail("No new tab opened.");
    }

    private void clickExternalAndAssert(By linkLocator, String expectedDomain) {
        int before = driver.getWindowHandles().size();
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(linkLocator));
        link.click();
        if (driver.getWindowHandles().size() > before) {
            switchToNewTabAndVerify(expectedDomain);
        } else {
            // Same-tab navigation
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain),
                    "Expected navigation to " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.name("username")),
                    ExpectedConditions.visibilityOfElementLocated(By.linkText("Register"))
            ));
        }
    }

    /* -------------------- Tests -------------------- */

    @Test
    @Order(1)
    public void homePageLoads() {
        openHome();
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#leftPanel")));
        Assertions.assertTrue(logo.isDisplayed(), "Left panel should be visible.");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/parabank/index.htm"), "URL should be index.htm");
    }

    @Test
    @Order(2)
    public void loginWithProvidedCredentials_expectSuccessOrError() {
        performLogin(USERNAME, PASSWORD);

        boolean loggedIn = isLoggedIn();
        if (loggedIn) {
            // Success path
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(By.linkText("Accounts Overview")),
                    ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out"))
            ));
            Assertions.assertTrue(isLoggedIn(), "Should be logged in.");
            logoutIfLoggedIn();
        } else {
            // Error path
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error")));
            Assertions.assertTrue(error.isDisplayed(), "Error message should appear for invalid credentials.");
        }
    }

    @Test
    @Order(3)
    public void invalidLoginShowsError() {
        performLogin("wronguser", "wrongpass");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("error")
                        || error.getText().toLowerCase().contains("could not"),
                "Invalid login should show an error message.");
    }

    @Test
    @Order(4)
    public void navigateToRegisterPage() {
        openHome();
        WebElement register = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Register")));
        register.click();
        wait.until(ExpectedConditions.urlContains("register.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("register.htm"), "Should navigate to register page.");
    }

    @Test
    @Order(5)
    public void navigateToServicesPage() {
        openHome();
        WebElement services = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
        services.click();
        wait.until(ExpectedConditions.urlContains("services.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("services.htm"), "Should navigate to services page.");
    }

    @Test
    @Order(6)
    public void navigateToAboutUsPage() {
        openHome();
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About Us")));
        about.click();
        wait.until(ExpectedConditions.urlContains("about.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("about.htm"), "Should navigate to about us page.");
    }

    @Test
    @Order(7)
    public void navigateToContactUsPage() {
        openHome();
        WebElement contact = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact Us")));
        contact.click();
        wait.until(ExpectedConditions.urlContains("contact.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("contact.htm"), "Should navigate to contact page.");
    }

    @Test
    @Order(8)
    public void navigateToForgotLoginInfoPage() {
        openHome();
        WebElement forgot = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Forgot login info?")));
        forgot.click();
        wait.until(ExpectedConditions.urlContains("lookup.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("lookup.htm"), "Should navigate to forgot login page.");
    }

    @Test
    @Order(9)
    public void externalParasoftLinkOpens() {
        openHome();
        // Footer or header link to Parasoft corporate site
        By parasoftLink = By.xpath("//a[contains(@href,'parasoft.com')]");
        clickExternalAndAssert(parasoftLink, "parasoft.com");
    }
}
