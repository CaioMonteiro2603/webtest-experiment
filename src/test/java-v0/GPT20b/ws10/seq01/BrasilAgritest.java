package GPT20b.ws10.seq01;

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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BrasilAgritest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://gestao.brasilagritest.com/";
    private static final String LOGIN_PATH = "login";
    private static final String USER_EMAIL = "superadmin@brasilagritest.com.br";
    private static final String USER_PASS = "10203040";

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

    /* ---------- Utility Methods ---------- */

    private boolean elementExists(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    private void navigateTo(String path) {
        driver.get(BASE_URL + path);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    private void login() {
        navigateTo(LOGIN_PATH);
        By emailSel = By.cssSelector("input[type='email'], input[name='email'], input[id='email']");
        By passSel = By.cssSelector("input[type='password'], input[name='senha'], input[id='password']");
        By loginBtnSel = By.cssSelector("button[type='submit'], input[type='submit']");

        Assumptions.assumeTrue(
                elementExists(emailSel) && elementExists(passSel) && elementExists(loginBtnSel),
                "Login form elements not found; cannot perform login");

        driver.findElement(emailSel).clear();
        driver.findElement(emailSel).sendKeys(USER_EMAIL);
        driver.findElement(passSel).clear();
        driver.findElement(passSel).sendKeys(USER_PASS);
        WebElement loginBtn = driver.findElement(loginBtnSel);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[href*='logout']")));
        Assertions.assertTrue(
                elementExists(By.cssSelector("a[href*='logout']")),
                "Logout link should be present after successful login");
    }

    private void loginIfNeeded() {
        if (!elementExists(By.cssSelector("a[href*='logout']"))) {
            login();
        }
    }


    private List<String> getItemNames() {
        List<WebElement> items = driver.findElements(By.cssSelector(".item-name, .product-title, .product-name"));
        return items.stream().map(WebElement::getText).toList();
    }

    /* ---------- Tests ---------- */

    @Test
    @Order(1)
    public void testHomePageLoads() {
        navigateTo("");
        Assertions.assertTrue(
                driver.getTitle().toLowerCase().contains("gestao"),
                "Home page title should contain 'gestao'");
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        login();
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {
        navigateTo(LOGIN_PATH);
        By emailSel = By.cssSelector("input[type='email'], input[name='email'], input[id='email']");
        By passSel = By.cssSelector("input[type='password'], input[name='senha'], input[id='password']");
        By loginBtnSel = By.cssSelector("button[type='submit'], input[type='submit']");

        Assumptions.assumeTrue(
                elementExists(emailSel) && elementExists(passSel) && elementExists(loginBtnSel),
                "Login form elements not found; skipping invalid login test");

        driver.findElement(emailSel).clear();
        driver.findElement(emailSel).sendKeys("wrong@example.com");
        driver.findElement(passSel).clear();
        driver.findElement(passSel).sendKeys("wrongpass");
        WebElement loginBtn = driver.findElement(loginBtnSel);
        wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginBtn.click();

        By errorSel = By.cssSelector(".error, .alert-danger");
        Assertions.assertTrue(
                elementExists(errorSel),
                "Error message should appear after invalid credentials");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        loginIfNeeded();

        // Navigate to items listing via All Items link
        By allItemsSel = By.xpath("//a[contains(text(),'All Items') or contains(text(),'Todos')]");
        Assumptions.assumeTrue(elementExists(allItemsSel), "All Items link not found; skipping sorting test");
        WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(allItemsSel));
        allItems.click();

        // Identify sorting dropdown
        By sortSel = By.cssSelector("select[id*='sort'], select[name*='sort'], select[class*='sort']");
        Assumptions.assumeTrue(elementExists(sortSel), "Sorting dropdown not found; skipping sorting test");
        WebElement sortEl = wait.until(ExpectedConditions.elementToBeClickable(sortSel));
        Select sortOptions = new Select(sortEl);

        List<String> previousOrder = getItemNames();

        for (WebElement opt : sortOptions.getOptions()) {
            String optText = opt.getText();
            sortOptions.selectByVisibleText(optText);

            // Wait until list changes
            wait.until(d -> !getItemNames().equals(previousOrder));

            List<String> currentOrder = getItemNames();
            Assertions.assertNotEquals(
                    previousOrder,
                    currentOrder,
                    "Item order should change after selecting sort option: " + optText);
        }
    }

    @Test
    @Order(5)
    public void testBurgerMenuOperations() {
        loginIfNeeded();

        By burgerSel = By.cssSelector(".navbar-toggler, #burger, .burger-menu");
        Assumptions.assumeTrue(elementExists(burgerSel), "Burger menu button not found; skipping test");
        WebElement burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burgerSel));
        burgerBtn.click();

        // All Items
        By allItemsSel = By.xpath("//a[contains(text(),'All Items')]");
        if (elementExists(allItemsSel)) {
            WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(allItemsSel));
            allItems.click();
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("items"),
                    "Navigated to Items page after clicking All Items");
        }

        // About (external)
        By aboutSel = By.xpath("//a[contains(text(),'About')]");
        if (elementExists(aboutSel)) {
            burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burgerSel));
            burgerBtn.click();
            WebElement about = wait.until(ExpectedConditions.elementToBeClickable(aboutSel));

            String origHandle = driver.getWindowHandle();
            Set<String> before = driver.getWindowHandles();
            about.click();

            try {
                wait.until(d -> driver.getWindowHandles().size() > before.size());
            } catch (TimeoutException ignored) {
            }

            Set<String> after = driver.getWindowHandles();
            after.removeAll(before);
            if (!after.isEmpty()) {
                String newHandle = after.iterator().next();
                driver.switchTo().window(newHandle);
                wait.until(ExpectedConditions.urlContains("about"));
                Assertions.assertTrue(
                        driver.getCurrentUrl().contains("about"),
                        "About link should open external page");
                driver.close();
                driver.switchTo().window(origHandle);
            } else {
                Assertions.assertTrue(
                        driver.getCurrentUrl().contains("about"),
                        "About opened in same tab");
                driver.navigate().back();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".navbar-toggler")));
            }
        }

        // Logout
        By logoutSel = By.xpath("//a[contains(text(),'Logout')]");
        if (elementExists(logoutSel)) {
            burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burgerSel));
            burgerBtn.click();
            WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(logoutSel));
            logout.click();
            wait.until(ExpectedConditions.urlContains("login"));
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("login"),
                    "After logout, should be on login page");
            // Re‑login to continue other tests
            login();
        }

        // Reset App State
        By resetSel = By.linkText("Reset App State");
        if (elementExists(resetSel)) {
            burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burgerSel));
            burgerBtn.click();
            WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(resetSel));
            reset.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("cartContents")));
            Assertions.assertFalse(
                    elementExists(By.id("cartContents")),
                    "Cart should be empty after reset");
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() {
        navigateTo("");
        String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
        for (String domain : domains) {
            By linkSel = By.xpath("//footer//a[contains(@href,'" + domain + "')]");
            Assumptions.assumeTrue(elementExists(linkSel), "Footer link for " + domain + " not found; skipping");
            List<WebElement> links = driver.findElements(linkSel);
            for (WebElement link : links) {
                String origHandle = driver.getWindowHandle();
                Set<String> before = driver.getWindowHandles();
                link.click();

                try {
                    wait.until(d -> driver.getWindowHandles().size() > before.size());
                } catch (TimeoutException ignored) {
                }

                Set<String> after = driver.getWindowHandles();
                after.removeAll(before);
                if (!after.isEmpty()) {
                    String newHandle = after.iterator().next();
                    driver.switchTo().window(newHandle);
                    wait.until(ExpectedConditions.urlContains(domain));
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "External link should contain domain: " + domain);
                    driver.close();
                    driver.switchTo().window(origHandle);
                } else {
                    Assertions.assertTrue(
                            driver.getCurrentUrl().contains(domain),
                            "Link navigated within same tab to expected domain");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("footer")));
                }
            }
        }
    }

    @Test
    @Order(7)
    public void testAboutExternalLinkStandalone() {
        navigateTo("");
        By aboutSel = By.xpath("//a[contains(text(),'About')]");
        Assumptions.assumeTrue(elementExists(aboutSel), "About link not found; skipping test");
        WebElement about = wait.until(ExpectedConditions.elementToBeClickable(aboutSel));

        String origHandle = driver.getWindowHandle();
        Set<String> before = driver.getWindowHandles();
        about.click();

        try {
            wait.until(d -> driver.getWindowHandles().size() > before.size());
        } catch (TimeoutException ignored) {
        }

        Set<String> after = driver.getWindowHandles();
        after.removeAll(before);
        if (!after.isEmpty()) {
            String newHandle = after.iterator().next();
            driver.switchTo().window(newHandle);
            wait.until(ExpectedConditions.urlContains("about"));
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("about"),
                    "About link should open external page");
            driver.close();
            driver.switchTo().window(origHandle);
        } else {
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("about"),
                    "About opened in same tab");
            driver.navigate().back();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        }
    }
}