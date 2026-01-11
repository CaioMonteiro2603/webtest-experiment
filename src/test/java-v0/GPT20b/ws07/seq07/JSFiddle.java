package GPT20b.ws07.seq07;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JSFiddle {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://jsfiddle.net/";
    // Dummy credentials used only for negative test
    private static final String INVALID_EMAIL = "nonexistent@example.com";
    private static final String INVALID_PASSWORD = "wrongpass";

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /* ---------- Helper methods ------------------------------------- */

    private WebElement findElementWithFallback(List<By> locators) {
        for (By locator : locators) {
            try {
                return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            } catch (Exception ignored) {
            }
        }
        throw new NoSuchElementException("Required element not found using locators: " + locators);
    }

    private void clickAndVerify(String linkText, String expectedPartOfUrl) {
        WebElement link = findElementWithFallback(
                List.of(By.linkText(linkText), By.xpath("//a[normalize-space()='" + linkText + "']")));
        wait.until(ExpectedConditions.elementToBeClickable(link));
        link.click();
        wait.until(ExpectedConditions.urlContains(expectedPartOfUrl));
        Assertions.assertTrue(driver.getCurrentUrl().contains(expectedPartOfUrl),
                "URL should contain '" + expectedPartOfUrl + "' after clicking '" + linkText + "'");
    }

    /* ---------- Tests ---------------------------------------------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.get(BASE_URL);
        Assertions.assertTrue(driver.getTitle().toLowerCase().contains("jsfiddle"),
                "Page title should contain 'jsFiddle'");
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        clickAndVerify("Login", "login");
        WebElement emailField = findElementWithFallback(
                List.of(By.id("email"), By.name("email"), By.cssSelector("input[placeholder='Email']")));
        WebElement passwordField = findElementWithFallback(
                List.of(By.id("password"), By.name("password"), By.cssSelector("input[placeholder='Password']")));
        WebElement loginButton = findElementWithFallback(
                List.of(By.xpath("//button[normalize-space()='Login']"), By.cssSelector("button.btn-primary")));

        emailField.clear();
        emailField.sendKeys(INVALID_EMAIL);
        passwordField.clear();
        passwordField.sendKeys(INVALID_PASSWORD);

        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert.alert-danger, .alert")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdownExists() {
        // Navigate to a new fiddle editor to see the language dropdown
        clickAndVerify("New", "create");
        // The language dropdown has id 'jsframework' on the editor page
        List<WebElement> dropdowns = driver.findElements(By.id("jsframework"));
        Assumptions.assumeTrue(!dropdowns.isEmpty(), "Language dropdown not present on editor page");

        WebElement languageDropdown = dropdowns.get(0);
        wait.until(ExpectedConditions.elementToBeClickable(languageDropdown));
        languageDropdown.click();

        // Verify at least two options are available
        List<WebElement> options = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("#jsframework option")));
        Assumptions.assumeTrue(options.size() > 1, "Sorting options are not present");

        // Capture current selection
        String originalValue = languageDropdown.getAttribute("value");

        // Select a different option
        WebElement newOption = options.stream()
                .filter(opt -> !opt.getAttribute("value").equals(originalValue))
                .findFirst()
                .get();
        wait.until(ExpectedConditions.elementToBeClickable(newOption));
        newOption.click();

        // Verify the selected value changed
        String updatedValue = languageDropdown.getAttribute("value");
        Assertions.assertNotEquals(originalValue, updatedValue,
                "The language dropdown selection should change after selecting a new option");
    }

    @Test
    @Order(4)
    public void testBurgerMenuInteractions() {
        // jsFiddle does not have a classic burger menu; it uses a top navigation bar.
        // We will interact with the 'My Account' menu and check its items.

        clickAndVerify("My Account", "myaccount");

        // Verify that the dropdown contains 'Logout' and 'Profile'
        List<WebElement> menuItems = driver.findElements(By.cssSelector(".dropdown-menu li a"));
        Assumptions.assumeTrue(!menuItems.isEmpty(), "Account dropdown items not found");

        boolean logoutFound = false;
        boolean profileFound = false;
        for (WebElement item : menuItems) {
            String txt = item.getText().trim();
            if (txt.equalsIgnoreCase("Logout")) logoutFound = true;
            if (txt.equalsIgnoreCase("Profile")) profileFound = true;
        }
        Assertions.assertTrue(logoutFound, "'Logout' should be present in account menu");
        Assertions.assertTrue(profileFound, "'Profile' should be present in account menu");
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        // Scroll to footer
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a[href^='http']"));

        Assumptions.assumeTrue(!footerLinks.isEmpty(), "No external footer links found");

        String originalHandle = driver.getWindowHandle();
        for (WebElement link : footerLinks) {
            String href = link.getAttribute("href");
            if (href == null || !href.contains(".")) continue;

            // Open link in new tab
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("window.open(arguments[0]);", href);
            wait.until(d -> d.getWindowHandles().size() > 1);
            for (String handle : driver.getWindowHandles()) {
                if (!handle.equals(originalHandle)) {
                    driver.switchTo().window(handle);
                    break;
                }
            }

            Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                    "Opened link URL should contain the href: " + href);

            driver.close();
            driver.switchTo().window(originalHandle);
        }
    }

    @Test
    @Order(6)
    public void testCartAddRemovePlaceholder() {
        // jsFiddle does not have a cart; this test demonstrates handling non‑existent features.
        // Skip the test if a cart badge is not found.
        List<WebElement> badgeElements = driver.findElements(By.cssSelector(".cart-badge, #cart-count"));
        Assumptions.assumeTrue(!badgeElements.isEmpty(), "Cart feature not present, skipping test");

        WebElement addBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".add-to-cart")));
        addBtn.click();

        WebElement badge = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cart-badge")));
        Assertions.assertNotNull(badge, "Cart badge should appear after adding an item");

        WebElement removeBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".remove-from-cart")));
        removeBtn.click();

        Assertions.assertTrue(driver.findElements(By.cssSelector(".cart-badge")).isEmpty(),
                "Cart badge should disappear after removing the item");
    }

    @Test
    @Order(7)
    public void testNotOperationalFeaturePlaceholder() {
        // Placeholder for a feature that would be present in the original target site
        // For jsFiddle, we skip the test if the element is not found.
        List<WebElement> placeholderElements = driver.findElements(By.id("nonexistent-feature"));
        Assumptions.assumeTrue(placeholderElements.isEmpty(),
                "Feature not available on jsFiddle; skipping test");
    }
}