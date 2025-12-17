package GPT20b.ws08.seq01;

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
public class JPetStore{


private static WebDriver driver;
private static WebDriverWait wait;
private static final String BASE_URL = "https://jpetstore.aspectran.com/";

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

private void navigateToHome() {
    driver.get(BASE_URL);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
}

private void liftLoginIfNeeded() {
    // No login credentials are supplied; skip login steps if login form is absent.
    if (!elementExists(By.id("userId"))) {
        return;
    }
    WebElement userInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userId")));
    WebElement pwdInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
    WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.name("login")));

    userInput.clear();
    userInput.sendKeys("dummyUser");
    pwdInput.clear();
    pwdInput.sendKeys("dummyPass");
    loginBtn.click();

    // Wait for presence of a user menu to confirm login
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userMenu")));
}

private void resetAppState() {
    // Many e‑commerce sites have a "Clear Cart" or "Reset" link
    if (elementExists(By.linkText("Reset Cart"))) {
        WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Reset Cart")));
        resetLink.click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("cartContents")));
    }
}

private List<String> getItemNames() {
    List<WebElement> elements = driver.findElements(By.cssSelector(".product-title"));
    return elements.stream().map(WebElement::getText).toList();
}

/* ---------- Tests ---------- */

@Test
@Order(1)
public void testHomePageLoads() {
    navigateToHome();
    Assertions.assertTrue(
            driver.getTitle().toLowerCase().contains("jpetstore"),
            "Home page title should contain 'jpetstore'");
}

@Test
@Order(2)
public void testLoginFlow() {
    navigateToHome();
    By loginBtn = By.xpath("//a[contains(text(),'Sign In')]");
    Assumptions.assumeTrue(elementExists(loginBtn),
            "Login link not found; skipping login test.");

    driver.findElement(loginBtn).click();
    WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userId")));
    WebElement pwdField = driver.findElement(By.id("password"));
    WebElement loginButton = driver.findElement(By.name("login"));

    userField.clear();
    userField.sendKeys("dummyUser");
    pwdField.clear();
    pwdField.sendKeys("dummyPass");
    loginButton.click();

    // Verify presence of user menu
    Assertions.assertTrue(
            elementExists(By.id("userMenu")),
            "User menu should be visible after successful login");
}

@Test
@Order(3)
public void testWithInvalidLogin() {
    navigateToHome();
    By loginBtn = By.xpath("//a[contains(text(),'Sign In')]");
    Assumptions.assumeTrue(elementExists(loginBtn),
            "Login link not found; skipping invalid login test.");

    driver.findElement(loginBtn).click();
    WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userId")));
    WebElement pwdField = driver.findElement(By.id("password"));
    WebElement loginButton = driver.findElement(By.name("login"));

    userField.clear();
    userField.sendKeys("invalidUser");
    pwdField.clear();
    pwdField.sendKeys("wrongPass");
    loginButton.click();

    By errorMsg = By.cssSelector(".error");
    Assertions.assertTrue(
            elementExists(errorMsg),
            "Error message should appear after wrong credentials");
}

@Test
@Order(4)
public void testSortingDropdown() {
    liftLoginIfNeeded();
    navigateToHome();
    By sortSelect = By.id("sortBy");
    Assumptions.assumeTrue(elementExists(sortSelect),
            "Sorting dropdown not present; skipping test.");

    Select select = new Select(driver.findElement(sortSelect));
    List<WebElement> options = select.getOptions();

    // get initial list order
    List<String> previous = getItemNames();

    for (WebElement opt : options) {
        select.selectByVisibleText(opt.getText());
        // wait for list update
        wait.until(driver1 -> !getItemNames().equals(previous));
        List<String> current = getItemNames();
        Assertions.assertNotEquals(
                previous,
                current,
                "Item order should change after selecting sort option: " + opt.getText());
        
    }
}

@Test
@Order(5)
public void testBurgerMenuOperations() {
    navigateToHome();
    By burger = By.cssSelector(".burger-menu");
    Assumptions.assumeTrue(elementExists(burger),
            "Burger menu not found; skipping test.");
    WebElement burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burger));
    burgerBtn.click();

    // All Items
    By allItems = By.xpath("//a[contains(text(),'All Items')]");
    Assumptions.assumeTrue(elementExists(allItems),
            "All Items link missing; skipping that part.");
    WebElement allItemsLink = driver.findElement(allItems);
    allItemsLink.click();
    Assertions.assertTrue(
            driver.getCurrentUrl().contains("catalog"),
            "URL should contain 'catalog' after clicking All Items");

    // About (assumed external)
    By aboutLink = By.xpath("//a[contains(text(),'About')]");
    if (elementExists(aboutLink)) {
        burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burger));
        burgerBtn.click();
        WebElement about = driver.findElement(aboutLink);
        String originalHandle = driver.getWindowHandle();
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
                    "About should open external link");
            driver.close();
            driver.switchTo().window(originalHandle);
        } else {
            Assertions.assertTrue(
                    driver.getCurrentUrl().contains("about"),
                    "About opened in same tab");
            driver.navigate().back();
            wait.until(ExpectedConditions.visibilityOfElementLocated(burger));
        }
    }

    // Logout
    By logoutLink = By.xpath("//a[contains(text(),'Logout')]");
    if (elementExists(logoutLink)) {
        burgerBtn = wait.until(ExpectedConditions.elementToBeClickable(burger));
        burgerBtn.click();
        WebElement logout = driver.findElement(logoutLink);
        logout.click();
        wait.until(ExpectedConditions.urlContains("sign"));
        Assertions.assertTrue(
                driver.getCurrentUrl().contains("sign"),
                "After logout, should be on sign-in page");
    }

    // Reset App State
    By resetLink = By.linkText("Reset Cart");
    if (elementExists(resetLink)) {
        resetAppState();
        Assertions.assertFalse(
                elementExists(By.id("cartContents")),
                "Cart should be empty after reset");
    }
}

@Test
@Order(6)
public void testFooterSocialLinks() {
    navigateToHome();

    String[] domains = {"twitter.com", "facebook.com", "linkedin.com"};
    for (String domain : domains) {
        By linkLocator = By.xpath("//footer//a[contains(@href,'" + domain + "')]");
        Assumptions.assumeTrue(elementExists(linkLocator),
                "Footer link for " + domain + " not found, skipping");

        List<WebElement> links = driver.findElements(linkLocator);
        for (WebElement link : links) {
            String originalHandle = driver.getWindowHandle();
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
                driver.switchTo().window(originalHandle);
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
}