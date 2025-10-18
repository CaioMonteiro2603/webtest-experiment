package GPT20b.ws02.seq07;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParabankWebTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/index.htm";
    private static final String USERNAME = "caio@gmail.com";
    private static final String PASSWORD = "123";

    @BeforeAll
    static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
    }

    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* Helper methods ------------------------------------------- */

    private void login(String user, String pass) {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("userid"))
        );
        userField.clear();
        userField.sendKeys(user);

        WebElement passField = driver.findElement(By.id("password"));
        passField.clear();
        passField.sendKeys(pass);

        WebElement loginBtn = driver.findElement(By.id("loginButton"));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("accountOverview"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("accountOverview"),
                "Should navigate to account overview after successful login");
    }

    private void logout() {
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("Log Out")))
        ;
        logoutLink.click();
        wait.until(ExpectedConditions.urlContains("index.htm"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("index.htm"),
                "Should return to login page after logout");
    }

    private List<WebElement> getNavigationLinks() {
        return driver.findElements(By.cssSelector("ul.menuTop > li > a"));
    }

    private void clickAndReturn(String linkText) {
        WebElement link = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText(linkText))
        );
        String originalUrl = driver.getCurrentUrl();
        link.click();
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(originalUrl)));
        // after test, go back
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(originalUrl));
    }

    /* Tests ---------------------------------------------------- */

    @Test
    @Order(1)
    void testValidLogin() {
        login(USERNAME, PASSWORD);
        List<WebElement> accountRows = driver.findElements(By.cssSelector("table[id*='account'] tbody tr"));
        Assertions.assertFalse(accountRows.isEmpty(), "Account table should contain at least one row");
    }

    @Test
    @Order(2)
    void testInvalidLogin() {
        driver.get(BASE_URL);
        WebElement userField = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("userid"))
        );
        userField.clear();
        userField.sendKeys("invalid@example.com");

        WebElement passField = driver.findElement(By.id("password"));
        passField.clear();
        passField.sendKeys("wrongpass");

        WebElement loginBtn = driver.findElement(By.id("loginButton"));
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.errorMessage"))
        );
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid credentials");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid login");
    }

    @Test
    @Order(3)
    void testNavigationLinks() {
        login(USERNAME, PASSWORD);

        List<String> expectedPages = List.of("Accounts", "Transfer Funds", "Pay Bills", "Online Banking", "Contact");
        for (String page : expectedPages) {
            clickAndReturn(page);
            Assertions.assertTrue(driver.getCurrentUrl().toLowerCase().contains(page.toLowerCase()),
                    "URL should contain page name after navigating to " + page);
        }
    }

    @Test
    @Order(4)
    void testAboutExternalLink() {
        login(USERNAME, PASSWORD);

        WebElement aboutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.linkText("About"))
        );
        String originalHandle = driver.getWindowHandle();

        aboutLink.click();

        wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(originalHandle)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        Assumptions.assumeTrue(driver.getCurrentUrl().contains("parasoft.com"),
                "Assuming About page is external to ParmBank");

        driver.close();
        driver.switchTo().window(originalHandle);
    }

    @Test
    @Order(5)
    void testExternalFooterLinks() {
        login(USERNAME, PASSWORD);

        List<String> domains = List.of("twitter.com", "facebook.com", "linkedin.com");
        for (String domain : domains) {
            List<WebElement> links = driver.findElements(By.xpath("//a[contains(@href,'" + domain + "')]"));
            Assumptions.assumeTrue(!links.isEmpty(), "External link to " + domain + " not found, skipping");
            WebElement link = links.get(0);
            String originalHandle = driver.getWindowHandle();

            link.click();

            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                    "Should navigate to external domain: " + domain);

            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(6)
    void testAccountBalanceSorting() {
        login(USERNAME, PASSWORD);

        // Locate account table
        WebElement accountTable = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//table[contains(@id,'account')]"))
        );

        // Capture initial balances
        List<Double> beforeSort = accountTable.findElements(By.xpath(".//tbody/tr/td[3]"))
                .stream()
                .map(el -> Double.parseDouble(el.getText().replace("$", "").replace(",", "")))
                .collect(Collectors.toList());

        // Click 'Balance' header to sort ascending
        WebElement balanceHeader = accountTable.findElement(By.xpath(".//th[normalize-space()='Balance']"));
        wait.until(ExpectedConditions.elementToBeClickable(balanceHeader));
        balanceHeader.click();

        // Capture sorted balances
        List<Double> afterSortAsc = accountTable.findElements(By.xpath(".//tbody/tr/td[3]"))
                .stream()
                .map(el -> Double.parseDouble(el.getText().replace("$", "").replace(",", "")))
                .collect(Collectors.toList());

        List<Double> expectedAsc = beforeSort.stream()
                .sorted()
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedAsc, afterSortAsc,
                "Sorting by Balance ascending should match expected order");

        // Click again to sort descending
        balanceHeader.click();

        List<Double> afterSortDesc = accountTable.findElements(By.xpath(".//tbody/tr/td[3]"))
                .stream()
                .mapDouble(el.getText().replace("$", "").replace(",", "")))
                .collect(Collectors.toList());

        List<Double> expectedDesc = expectedAsc.stream()
                .sorted((a, b) -> Double.compare(b, a))
                .collect(Collectors.toList());

        Assertions.assertEquals(expectedDesc, afterSortDesc,
                "Sorting by Balance descending should match expected order");
    }

    @Test
    @Order(7)
    void testLogoutFunctionality() {
        login(USERNAME, PASSWORD);
        logout();
    }
}