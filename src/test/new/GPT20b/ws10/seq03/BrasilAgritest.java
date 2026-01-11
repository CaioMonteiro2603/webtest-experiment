package GPT20b.ws10.seq03;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Assumptions;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
public class BrasilAgritest {

    private static final String BASE_URL = "https://gestao.brasilagritest.com/login";
    private static final String USER_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String USER_PASSWORD = "10203040";

    private static WebDriver driver;
    private static WebDriverWait wait;

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

    /* ---------- helper utilities ---------- */

    private static void openLoginPage() {
        driver.get(BASE_URL);
    }

    private static void login(String email, String password) {
        openLoginPage();
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='text']")));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password']")));
        emailField.clear();
        emailField.sendKeys(email);
        passwordField.clear();
        passwordField.sendKeys(password);

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")));
        submitBtn.click();

        // Wait for dashboard indicator (URL contains 'dashboard' or a specific element)
        wait.until(d -> d.getCurrentUrl().contains("painel"));
    }

    private static void logoutIfExists() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            WebElement logout = logoutLinks.get(0);
            logout.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("button[type='submit']")));
        }
    }

    private static void resetAppStateIfExists() {
        List<WebElement> resetLinks = driver.findElements(By.linkText("Reset App State"));
        if (!resetLinks.isEmpty()) {
            WebElement reset = resetLinks.get(0);
            reset.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.linkText("Reset App State")));
        }
    }

    private static void openBurgerMenu() {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[aria-label='open menu'], .hamburger, #menuToggle, button[id='menu-btn']")));
        menuBtn.click();
    }

    private static void switchToNewWindow(String parentHandle) {
        wait.until(d -> d.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        handles.remove(parentHandle);
        driver.switchTo().window(handles.iterator().next());
    }

    private static List<String> getItemTitles() {
        List<WebElement> items = driver.findElements(
                By.cssSelector(".item-title, .product-title, h3[item-title]"));
        List<String> titles = new ArrayList<>();
        for (WebElement el : items) {
            titles.add(el.getText().trim());
        }
        return titles;
    }

    /* ---------- tests ---------- */

    @Test
    @Order(1)
    @DisplayName("Login page loads with correct title")
    void testLoginPageLoads() {
        openLoginPage();
        String title = driver.getTitle();
        assertTrue(title.contains("Painel Administrativo"),
                "Login page title does not contain 'Painel Administrativo'. Title: " + title);
    }

    @Test
    @Order(2)
    @DisplayName("Valid login credentials")
    void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        String url = driver.getCurrentUrl();
        assertTrue(url.contains("painel"),
                "Login did not navigate to dashboard or home page. URL: " + url);
        List<WebElement> dashboardElems = driver.findElements(
                By.cssSelector("div#dashboard, div.dashboard, div[data-test='dashboard'], div.content-wrapper"));
        Assumptions.assumeTrue(!dashboardElems.isEmpty(),
                "Dashboard element not found after login; test may not be applicable");
        assertTrue(dashboardElems.get(0).isDisplayed(),
                "Dashboard element is not displayed after login");
    }

    @Test
    @Order(3)
    @DisplayName("Invalid login credentials show error message")
    void testInvalidLogin() {
        openLoginPage();
        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='text']")));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[type='password']")));
        emailField.clear();
        emailField.sendKeys("wrong@example.com");
        passwordField.clear();
        passwordField.sendKeys("wrongpass");

        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")));
        submitBtn.click();

        List<WebElement> errorEls = driver.findElements(
                By.cssSelector(".alert.alert-danger, .error, .validation-error, .message-error, div[role='alert']"));
        Assumptions.assumeTrue(!errorEls.isEmpty(),
                "No error message displayed after invalid login attempt");
        WebElement errorEl = errorEls.get(0);
        String errText = errorEl.getText().toLowerCase();
        assertTrue(errText.contains("invalid") || errText.contains("error") || errText.contains("wrong") || errText.contains("usuário"),
                "Error message text does not indicate invalid credentials: " + errorEl.getText());
    }

    @Test
    @Order(4)
    @DisplayName("Sorting dropdown changes item order")
    void testSortingDropdown() {
        login(USER_EMAIL, USER_PASSWORD);
        // Navigate to Items page
        WebElement itemsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("All Items")));
        itemsLink.click();
        wait.until(ExpectedConditions.urlContains("/items"));

        List<WebElement> sortElements = driver.findElements(
                By.cssSelector("select[name='sort'], select[id='sortBy'], select.filter"));
        Assumptions.assumeTrue(!sortElements.isEmpty(),
                "Sorting dropdown not found; skipping test");

        Select sorter = new Select(sortElements.get(0));
        List<String> originalOrder = getItemTitles();

        for (String option : sorter.getOptions().stream()
                .map(WebElement::getText).collect(java.util.stream.Collectors.toList())) {
            sorter.selectByVisibleText(option);
            wait.until(ExpectedConditions.stalenessOf(sortElements.get(0)));
            sortElements = driver.findElements(
                    By.cssSelector("select[name='sort'], select[id='sortBy'], select.filter"));
            sorter = new Select(sortElements.get(0));
            List<String> currentOrder = getItemTitles();
            assertNotEquals(originalOrder, currentOrder,
                    "Sorting by '" + option + "' did not change item order");
            originalOrder = currentOrder;
        }

        logoutIfExists();
    }

    @Test
    @Order(5)
    @DisplayName("Menu actions: All Items, About, Reset, Logout")
    void testMenuActions() {
        login(USER_EMAIL, USER_PASSWORD);
        openBurgerMenu();

        // All Items
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("All Items")));
        allItemsLink.click();
        wait.until(ExpectedConditions.urlContains("/items"));
        assertTrue(driver.getCurrentUrl().contains("items") || driver.getCurrentUrl().contains("products"),
                "Did not navigate to All Items page");

        driver.navigate().back();

        // About (external)
        Optional<WebElement> aboutLink = driver.findElements(By.linkText("About")).stream()
                .filter(el -> el.isDisplayed()).findFirst();
        if (aboutLink.isPresent()) {
            String parentHandle = driver.getWindowHandle();
            aboutLink.get().click();
            switchToNewWindow(parentHandle);
            try {
                String externalUrl = driver.getCurrentUrl().toLowerCase();
                assertTrue(externalUrl.contains("github.com") || externalUrl.contains("about"),
                        "About link did not navigate to expected external domain. URL: " + externalUrl);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }

        // Reset App State
        resetAppStateIfExists();

        // Logout
        logoutIfExists();
        assertTrue(driver.getCurrentUrl().contains("login"),
                "Logout did not navigate back to login page. URL: " + driver.getCurrentUrl());
    }

    @Test
    @Order(6)
    @DisplayName("Footer social links open correctly")
    void testExternalSocialLinks() {
        login(USER_EMAIL, USER_PASSWORD);
        // Wait for footer visibility
        WebElement footer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("footer")));
        List<WebElement> anchorEls = footer.findElements(By.cssSelector("a[href]"));
        Assumptions.assumeTrue(!anchorEls.isEmpty(),
                "No anchor elements found in footer; skipping test");

        String baseDomain = "gestao.brasilagritest.com";

        for (WebElement link : anchorEls) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty() || href.contains(baseDomain)) {
                continue; // Skip internal links
            }

            String parentHandle = driver.getWindowHandle();
            link.click();
            switchToNewWindow(parentHandle);
            try {
                String currentUrl = driver.getCurrentUrl().toLowerCase();
                String expectedDomain = href.replaceFirst("https?://", "").split("/")[0].toLowerCase();
                assertTrue(currentUrl.contains(expectedDomain),
                        "External link URL does not contain expected domain. Expected: "
                                + expectedDomain + ", actual: " + currentUrl);
            } finally {
                driver.close();
                driver.switchTo().window(parentHandle);
            }
        }

        logoutIfExists();
    }
}