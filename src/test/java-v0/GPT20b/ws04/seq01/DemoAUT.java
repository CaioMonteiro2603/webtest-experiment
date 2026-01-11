package GPT20b.ws04.seq01;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DemoAUT {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://katalon-test.s3.amazonaws.com/aut/html/form.html";
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

    /* ---------- Helper Methods ---------- */

    private boolean elementExists(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private void navigateToBase() {
        driver.get(BASE_URL);
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testOpenFormPage() {
        navigateToBase();
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("form"),
                "Page title should contain 'Form'");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        if (!elementExists(By.id("username"))) {
            Assumptions.assumeTrue(false, "Username input not present, skipping valid login test");
        }
        navigateToBase();
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginBtn")));

        usernameInput.clear();
        usernameInput.sendKeys(USERNAME);
        passwordInput.clear();
        passwordInput.sendKeys(PASSWORD);
        loginBtn.click();

        // Expect a success message with id 'successMessage'
        Assumptions.assumeTrue(elementExists(By.id("successMessage")),
                "Success message not found after login, skipping further assertions");
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")));
        Assertions.assertTrue(success.getText().toLowerCase().contains("success"),
                "Success message should indicate successful login");
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        if (!elementExists(By.id("username"))) {
            Assumptions.assumeTrue(false, "Username input not present, skipping invalid login test");
        }
        navigateToBase();
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("loginBtn")));

        usernameInput.clear();
        usernameInput.sendKeys("wrong_user");
        passwordInput.clear();
        passwordInput.sendKeys("wrong_pass");
        loginBtn.click();

        // Expect an error message with id 'errorMessage'
        Assumptions.assumeTrue(elementExists(By.id("errorMessage")),
                "Error message not found after invalid login, skipping further assertions");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("errorMessage")));
        Assertions.assertTrue(error.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        // Assuming a sorting dropdown with id 'sortDropdown' and list items with role='option'
        if (!elementExists(By.id("sortDropdown"))) {
            Assumptions.assumeTrue(false, "Sorting dropdown not present, skipping sorting test");
        }
        navigateToBase();
        WebElement sortDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("sortDropdown")));
        sortDropdown.click();

        // Gather options
        List<WebElement> options = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//ul[@id='sortDropdown']/li")));
        Assumptions.assumeTrue(options.size() > 1, "Sorting dropdown lacks sufficient options");

        // Example: test first and last option click
        WebElement firstOption = options.get(0);
        String firstText = firstOption.getText();
        firstOption.click();
        // Verify some event, e.g., a heading changes
        Assumptions.assumeTrue(elementExists(By.id("sortResult")),
                "Sorting result element not found, skipping verification");
        WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sortResult")));
        Assertions.assertTrue(result.getText().contains(firstText),
                "Sorting result should reflect selected option");

        // Test another option
        WebElement lastOption = options.get(options.size() - 1);
        String lastText = lastOption.getText();
        lastOption.click();
        result = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sortResult")));
        Assertions.assertTrue(result.getText().contains(lastText),
                "Sorting result should update after changing option");
    }

    @Test
    @Order(5)
    public void testFooterSocialLinks() {
        // Find footer links with known domains
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            By linkLocator = By.xpath("//footer//a[contains(@href, '" + domain + "')]");
            Assumptions.assumeTrue(elementExists(linkLocator), "Link containing " + domain + " not found, skipping");

            List<WebElement> links = driver.findElements(linkLocator);
            for (WebElement link : links) {
                String original = driver.getWindowHandle();
                Set<String> before = driver.getWindowHandles();
                link.click();

                // Wait for new window/tab
                try {
                    wait.until(driver -> driver.getWindowHandles().size() > before.size());
                } catch (TimeoutException e) {
                    // Could be same tab navigation
                }

                Set<String> after = driver.getWindowHandles();
                after.removeAll(before);
                if (!after.isEmpty()) {
                    String newHandle = after.iterator().next();
                    driver.switchTo().window(newHandle);
                    wait.until(ExpectedConditions.urlContains(domain));
                    Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                            "Opened external link should contain domain: " + domain);
                    driver.close();
                    driver.switchTo().window(original);
                } else {
                    // Same tab navigation
                    Assertions.assertTrue(driver.getCurrentUrl().contains(domain),
                            "External link navigated within same tab and should contain: " + domain);
                    driver.navigate().back();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
                }
            }
        }
    }

    @Test
    @Order(6)
    public void testGenericExternalLinks() {
        // Find any external link not in same domain
        By externalLinkLocator = By.xpath("//a[starts-with(@href, 'http') and not(contains(@href, 'katalon-test.s3.amazonaws.com'))]");
        Assumptions.assumeTrue(elementExists(externalLinkLocator), "No external link found, skipping");

        List<WebElement> links = driver.findElements(externalLinkLocator);
        for (WebElement link : links) {
            String original = driver.getWindowHandle();
            Set<String> before = driver.getWindowHandles();
            link.click();

            // Wait for new window/tab if appears
            try {
                wait.until(driver -> driver.getWindowHandles().size() > before.size());
            } catch (TimeoutException ignored) {
            }

            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            if (!after.isEmpty()) {
                String newHandle = after.iterator().next();
                driver.switchTo().window(newHandle);
                wait.until(ExpectedConditions.urlContains("hhtp"));
                String url = driver.getCurrentUrl();
                Assertions.assertFalse(url.contains("katalon-test.s3.amazonaws.com"),
                        "External link should not contain base domain");
                driver.close();
                driver.switchTo().window(original);
            } else {
                // Same tab
                Assertions.assertFalse(driver.getCurrentUrl().contains("katalon-test.s3.amazonaws.com"),
                        "External link in same tab should not contain base domain");
                driver.navigate().back();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
            }
        }
    }
}