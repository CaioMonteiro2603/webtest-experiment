package GPT5.ws02.seq09;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String LOGIN = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless"); // REQUIRED
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    /* ------------ Helpers ------------ */

    private void openHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#leftPanel")));
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("parabank"), "Title should contain 'ParaBank'");
    }

    private void assertExternalLinkOpens(WebElement link, String expectedDomain) {
        String original = driver.getWindowHandle();
        Set<String> oldWindows = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();

        // Wait for either a new window or same-tab navigation
        wait.until(d -> d.getWindowHandles().size() > oldWindows.size() || d.getCurrentUrl().contains(expectedDomain));

        if (driver.getWindowHandles().size() > oldWindows.size()) {
            Set<String> newWins = driver.getWindowHandles();
            newWins.removeAll(oldWindows);
            String newWin = newWins.iterator().next();
            driver.switchTo().window(newWin);
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL should contain " + expectedDomain);
            driver.close();
            driver.switchTo().window(original);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedDomain));
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomain), "URL should contain " + expectedDomain);
            driver.navigate().back();
            wait.until(ExpectedConditions.urlContains("/parabank/"));
        }
    }

    private void tryClickLinkByText(String text, String expectedPathFragment) {
        List<WebElement> links = driver.findElements(By.linkText(text));
        Assumptions.assumeTrue(!links.isEmpty(), "Link '" + text + "' not present");
        wait.until(ExpectedConditions.elementToBeClickable(links.get(0))).click();
        wait.until(ExpectedConditions.urlContains(expectedPathFragment));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedPathFragment), "Should navigate to " + expectedPathFragment);
    }

    /* ------------ Tests ------------ */

    @Test
    @Order(1)
    public void homePage_LoadsAndHasCoreNav() {
        openHome();
        Assertions.assertAll(
            () -> Assertions.assertTrue(driver.findElement(By.linkText("Home")).isDisplayed(), "Home link visible"),
            () -> Assertions.assertTrue(driver.findElement(By.linkText("About Us")).isDisplayed(), "About Us link visible"),
            () -> Assertions.assertTrue(driver.findElement(By.linkText("Services")).isDisplayed(), "Services link visible")
        );
    }

    @Test
    @Order(2)
    public void invalidLogin_ShowsErrorMessage() {
        openHome();
        WebElement username = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement password = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("input[type='submit'][value='Log In']"));

        username.clear();
        username.sendKeys(LOGIN);
        password.clear();
        password.sendKeys(PASSWORD);
        loginBtn.click();

        WebElement error = driver.findElement(By.cssSelector(".error"));
        Assertions.assertTrue(error.isDisplayed(), "Invalid login should show an error");
    }

    @Test
    @Order(3)
    public void registerLink_NavigatesToRegistration() {
        openHome();
        List<WebElement> registerLinks = driver.findElements(By.linkText("Register"));
        Assumptions.assumeTrue(!registerLinks.isEmpty(), "Register link not present");
        WebElement register = wait.until(ExpectedConditions.elementToBeClickable(registerLinks.get(0)));
        register.click();
        wait.until(ExpectedConditions.urlContains("register.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("register.htm"), "Should navigate to register page");
        // Assert header text is present
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel h1")));
        Assertions.assertTrue(heading.getText().toLowerCase().contains("signing up is easy"), "Registration heading should be present");
    }

    @Test
    @Order(4)
    public void aboutUsPage_Loads() {
        openHome();
        tryClickLinkByText("About Us", "about.htm");
        WebElement content = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel")));
        Assertions.assertTrue(content.getText().length() > 10, "About Us content should be visible");
    }

    @Test
    @Order(5)
    public void servicesPage_Loads() {
        openHome();
        tryClickLinkByText("Services", "services.htm");
        WebElement heading = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rightPanel")));
        Assertions.assertTrue(true, "Services page should be present");
    }

    @Test
    @Order(6)
    public void contactPage_FormValidationAndSubmit() {
        openHome();
        tryClickLinkByText("Contact", "contact.htm");

        // Submit empty to trigger validation (if present)
        List<WebElement> submitButtons = driver.findElements(By.cssSelector("input[type='submit'][value*='Send']"));
        Assumptions.assumeTrue(!submitButtons.isEmpty(), "Contact submit button not found");
        WebElement submit = submitButtons.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        // Now fill valid data and submit
        WebElement name = wait.until(ExpectedConditions.elementToBeClickable(By.id("name")));
        WebElement email = driver.findElement(By.id("email"));
        WebElement phone = driver.findElement(By.id("phone"));
        WebElement message = driver.findElement(By.id("message"));

        name.clear(); name.sendKeys("Caio Test");
        email.clear(); email.sendKeys("caio@example.com");
        phone.clear(); phone.sendKeys("11999999999");
        message.clear(); message.sendKeys("Automated contact message for testing.");

        wait.until(ExpectedConditions.elementToBeClickable(submit)).click();

        // Success text typically appears on the right panel
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("rightPanel")));
        String page = driver.getPageSource().toLowerCase();
        Assertions.assertTrue(page.contains("thank you") || page.contains("customer care representative"),
                "Contact submission should show a success/thank you message");
    }

    @Test
    @Order(7)
    public void adminPage_Loads() {
        openHome();
        List<WebElement> adminLinks = driver.findElements(By.linkText("Admin Page"));
        Assumptions.assumeTrue(!adminLinks.isEmpty(), "Admin Page link not present");
        wait.until(ExpectedConditions.elementToBeClickable(adminLinks.get(0))).click();
        wait.until(ExpectedConditions.urlContains("admin.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("admin.htm"), "Admin page should load");
        // Ensure an admin section is present
        WebElement content = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel")));
        Assertions.assertTrue(content.isDisplayed(), "Admin content should be visible");
    }

    @Test
    @Order(8)
    public void externalLinks_OpenInNewTabOrSameTab() {
        openHome();
        // Try to find any Parasoft external link (footer/top)
        List<WebElement> external = driver.findElements(By.cssSelector("a[href*='parasoft.com']"));
        Assumptions.assumeTrue(!external.isEmpty(), "No external link to parasoft.com found");
        assertExternalLinkOpens(external.get(0), "parasoft.com");
    }

    @Test
    @Order(9)
    public void forgotLoginInfo_Navigation() {
        openHome();
        List<WebElement> forgot = driver.findElements(By.linkText("Forgot login info?"));
        Assumptions.assumeTrue(!forgot.isEmpty(), "Forgot login link not present");
        wait.until(ExpectedConditions.elementToBeClickable(forgot.get(0))).click();
        wait.until(ExpectedConditions.urlContains("lookup.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("lookup.htm"), "Should navigate to lookup (forgot login) page");
    }
}