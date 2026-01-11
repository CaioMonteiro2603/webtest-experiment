package SunaGPT20b.ws02.seq02;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class parabank {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String VALID_USER = "caio@gmail.com";
    private static final String VALID_PASS = "123";

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

    private void login(String username, String password) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        userField.clear();
        userField.sendKeys(username);
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        passField.clear();
        passField.sendKeys(password);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='Log In']")));
        loginBtn.click();
    }

    private void logout() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Log Out"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.urlContains("index.htm"));
        }
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(VALID_USER, VALID_PASS);
        wait.until(ExpectedConditions.urlContains("overview.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("overview.htm"),
                "URL should contain 'overview.htm' after successful login");
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Accounts Overview')]")));
        Assertions.assertTrue(heading.isDisplayed(),
                "Accounts Overview heading should be displayed after login");
        logout();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalidUser", "wrongPass");
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'could not be verified')]")));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"),
                "Should remain on login page after failed login");
    }

    @Test
    @Order(3)
    public void testTransferFunds() {
        login(VALID_USER, VALID_PASS);
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Transfer Funds")));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        WebElement heading = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//h1[contains(text(),'Transfer Funds')]")));
        Assertions.assertTrue(heading.isDisplayed(),
                "Transfer Funds heading should be displayed");

        Select fromSelect = new Select(wait.until(ExpectedConditions.elementToBeClickable(
                By.name("fromAccountId"))));
        fromSelect.selectByIndex(0);
        Select toSelect = new Select(wait.until(ExpectedConditions.elementToBeClickable(
                By.name("toAccountId"))));
        int toIndex = fromSelect.getOptions().size() > 1 ? 1 : 0;
        toSelect.selectByIndex(toIndex);
        WebElement amountField = wait.until(ExpectedConditions.elementToBeClickable(
                By.name("amount")));
        amountField.clear();
        amountField.sendKeys("100");
        WebElement transferBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//input[@value='Transfer']")));
        transferBtn.click();
        WebElement successMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'Transfer Complete')]")));
        Assertions.assertTrue(successMsg.isDisplayed(),
                "Transfer success message should be displayed");
        logout();
    }

    @Test
    @Order(4)
    public void testExternalFooterLink() {
        login(VALID_USER, VALID_PASS);
        List<WebElement> externalLinks = driver.findElements(By.linkText("Parasoft"));
        if (!externalLinks.isEmpty()) {
            WebElement externalLink = externalLinks.get(0);
            String originalWindow = driver.getWindowHandle();
            externalLink.click();
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String win : windows) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }
            String currentUrl = driver.getCurrentUrl();
            Assertions.assertTrue(currentUrl.contains("parasoft.com"),
                    "External link should navigate to a Parasoft domain");
            driver.close();
            driver.switchTo().window(originalWindow);
            wait.until(ExpectedConditions.urlContains("overview.htm"));
        }
        logout();
    }

    @Test
    @Order(5)
    public void testSortingDropdownOnTransferPage() {
        login(VALID_USER, VALID_PASS);
        WebElement transferLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Transfer Funds")));
        transferLink.click();
        wait.until(ExpectedConditions.urlContains("transfer.htm"));
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        Assertions.assertFalse(selects.isEmpty(),
                "At least one dropdown should be present on Transfer Funds page");
        for (WebElement selElem : selects) {
            Select sel = new Select(selElem);
            List<WebElement> options = sel.getOptions();
            Assertions.assertTrue(options.size() > 0,
                    "Dropdown should contain at least one option");
            // Exercise each option to ensure it can be selected
            for (int i = 0; i < options.size(); i++) {
                sel.selectByIndex(i);
                // simple verification: selected option text matches expectation
                Assertions.assertEquals(options.get(i).getText().trim(),
                        sel.getFirstSelectedOption().getText().trim(),
                        "Dropdown selection should reflect chosen option");
            }
        }
        logout();
    }

    @Test
    @Order(6)
    public void testBurgerMenuIfPresent() {
        login(VALID_USER, VALID_PASS);
        List<WebElement> menuButtons = driver.findElements(By.cssSelector("[class*='menu']"));
        if (!menuButtons.isEmpty()) {
            WebElement menuBtn = menuButtons.get(0);
            wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
            // Assume a menu panel appears with a known identifier
            List<WebElement> menuPanels = driver.findElements(By.id("menuPanel"));
            if (!menuPanels.isEmpty()) {
                Assertions.assertTrue(menuPanels.get(0).isDisplayed(),
                        "Menu panel should be displayed after clicking burger button");
                // Close the menu
                wait.until(ExpectedConditions.elementToBeClickable(menuBtn)).click();
                Assertions.assertFalse(menuPanels.get(0).isDisplayed(),
                        "Menu panel should be hidden after second click");
            }
        }
        logout();
    }

    @Test
    @Order(7)
    public void testFooterSocialLinks() {
        login(VALID_USER, VALID_PASS);
        // Social links typically contain the domain name in href
        String[] socialDomains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : socialDomains) {
            List<WebElement> links = driver.findElements(By.xpath("//footer//a[contains(@href,'" + domain + "')]"));
            if (!links.isEmpty()) {
                WebElement link = links.get(0);
                String originalWindow = driver.getWindowHandle();
                link.click();
                wait.until(d -> d.getWindowHandles().size() > 1);
                Set<String> windows = driver.getWindowHandles();
                for (String win : windows) {
                    if (!win.equals(originalWindow)) {
                        driver.switchTo().window(win);
                        break;
                    }
                }
                String currentUrl = driver.getCurrentUrl();
                Assertions.assertTrue(currentUrl.contains(domain),
                        "Social link should navigate to its domain: " + domain);
                driver.close();
                driver.switchTo().window(originalWindow);
                wait.until(ExpectedConditions.urlContains("overview.htm"));
            }
        }
        logout();
    }
}