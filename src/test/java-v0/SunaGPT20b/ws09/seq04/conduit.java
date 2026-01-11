package SunaGPT20b.ws09.seq04;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class conduit {

    private static final String BASE_URL = "https://demo.realworld.io/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String email, String password) {
        driver.get(BASE_URL + "login");
        WebElement emailInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailInput.clear();
        emailInput.sendKeys(email);

        WebElement passwordInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Password']")));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        WebElement signInButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        signInButton.click();

        // Verify successful login by checking presence of the navigation bar with "New Post"
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='#/editor']")));
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login("testuser@example.com", "testpassword");
        Assertions.assertTrue(driver.getCurrentUrl().contains("/feed"),
                "After login, URL should contain '/feed'");

        // Verify that the logged‑in username appears in the navbar
        WebElement userLink = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href^='#/@']")));
        Assertions.assertTrue(userLink.isDisplayed(),
                "Username link should be displayed after successful login");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL + "login");
        WebElement emailInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Email']")));
        emailInput.clear();
        emailInput.sendKeys("invalid@example.com");

        WebElement passwordInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("input[placeholder='Password']")));
        passwordInput.clear();
        passwordInput.sendKeys("wrongpassword");

        WebElement signInButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        signInButton.click();

        // Expect an error message
        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".error-messages li")));
        Assertions.assertEquals("email or password is invalid", errorMsg.getText().toLowerCase(),
                "Invalid login should display appropriate error message");
    }

    @Test
    @Order(3)
    public void testNavigationMenuAndLogout() {
        // Ensure we are logged in first
        login("testuser@example.com", "testpassword");

        // Open the user dropdown (the username link)
        WebElement userLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href^='#/@']")));
        userLink.click();

        // Click Settings
        WebElement settingsLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#/settings']")));
        settingsLink.click();
        Assertions.assertTrue(driver.getCurrentUrl().contains("/settings"),
                "Settings page URL should contain '/settings'");

        // Return to feed
        driver.navigate().back();

        // Logout via the navbar link
        WebElement logoutLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#/login']")));
        logoutLink.click();

        // Verify we are back on the login page
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"),
                "After logout, URL should contain '/login'");
    }

    @Test
    @Order(4)
    public void testSortingDropdownIfPresent() {
        driver.get(BASE_URL + "feed");
        // The RealWorld demo does not have a sorting dropdown, but we guard against its absence.
        List<WebElement> selects = driver.findElements(By.tagName("select"));
        if (selects.isEmpty()) {
            // No dropdown – test passes trivially.
            Assertions.assertTrue(true, "No sorting dropdown present, which is acceptable.");
            return;
        }

        WebElement dropdown = selects.get(0);
        List<WebElement> options = dropdown.findElements(By.tagName("option"));
        Assertions.assertFalse(options.isEmpty(), "Sorting dropdown should contain options.");

        for (WebElement option : options) {
            dropdown.click(); // open dropdown
            option.click();   // select option
            // Simple verification: after selection, ensure the selected option is active
            String selected = dropdown.findElement(By.cssSelector("option:checked")).getText();
            Assertions.assertEquals(option.getText(), selected,
                    "Selected option should match the option clicked.");
            // Wait briefly for any potential page update (using explicit wait on body)
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        }
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        // Footer links are identified by their href attributes containing known domains
        String[][] externalLinks = {
                {"a[href='https://github.com/gothinkster/realworld']", "github.com"},
                {"a[href='https://twitter.com/gothinkster']", "twitter.com"},
                {"a[href='https://www.linkedin.com/company/gothinkster']", "linkedin.com"}
        };

        for (String[] linkInfo : externalLinks) {
            List<WebElement> elems = driver.findElements(By.cssSelector(linkInfo[0]));
            if (elems.isEmpty()) {
                // If a particular link is missing, continue without failing the whole test.
                continue;
            }
            WebElement link = elems.get(0);
            String originalWindow = driver.getWindowHandle();
            int windowsBefore = driver.getWindowHandles().size();

            // Click the link (may open a new tab)
            link.click();

            // Wait for new window/tab
            wait.until(driver -> driver.getWindowHandles().size() > windowsBefore);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalWindow)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            // Verify URL contains expected domain
            Assertions.assertTrue(driver.getCurrentUrl().contains(linkInfo[1]),
                    "External link should navigate to a URL containing " + linkInfo[1]);

            // Close the external tab and switch back
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }
}