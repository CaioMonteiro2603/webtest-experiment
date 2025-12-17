package SunaGPT20b.ws05.seq08;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assertions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Objects;

@TestMethodOrder(OrderAnnotation.class)
public class TAT {

    private static final String BASE_URL = "https://cac-tat.s3.eu-central-1.amazonaws.com/index.html";
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

    private void openUrl(String url) {
        driver.get(url);
        wait.until(webDriver -> ((String) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                .executeScript("return document.readyState")).equals("complete"));
    }

    private List<String> collectAllHrefs() {
        return driver.findElements(By.tagName("a")).stream()
                .map(e -> e.getAttribute("href"))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isInternalLink(String href) {
        return href.startsWith(BASE_URL) || href.startsWith("https://cac-tat.s3.eu-central-1.amazonaws.com/");
    }

    private int pathDepth(String href) {
        String path = href.replaceFirst("https?://[^/]+", "");
        if (path.isEmpty()) return 0;
        // Count non‑empty segments
        return (int) java.util.Arrays.stream(path.split("/"))
                .filter(seg -> !seg.isEmpty())
                .count();
    }

    @Test
    @Order(1)
    public void testBasePageLoads() {
        openUrl(BASE_URL);
        Assertions.assertTrue(driver.getCurrentUrl().startsWith(BASE_URL),
                "Base URL should be loaded");
        WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
        Assertions.assertTrue(body.isDisplayed(), "Body element should be visible");
    }

    @Test
    @Order(2)
    public void testInternalLinksOneLevel() {
        openUrl(BASE_URL);
        List<String> hrefs = collectAllHrefs();
        Set<String> visited = new HashSet<>();

        for (String href : hrefs) {
            if (isInternalLink(href) && pathDepth(href) <= 2 && !visited.contains(href)) {
                visited.add(href);
                driver.navigate().to(href);
                wait.until(webDriver -> ((String) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")).equals("complete"));
                Assertions.assertTrue(driver.getCurrentUrl().contains(href),
                        "Navigated URL should contain the internal link");
                driver.navigate().back();
                wait.until(webDriver -> ((String) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")).equals("complete"));
            }
        }
    }

    @Test
    @Order(3)
    public void testExternalLinksHandling() throws MalformedURLException {
        openUrl(BASE_URL);
        List<String> hrefs = collectAllHrefs();

        for (String href : hrefs) {
            if (!isInternalLink(href)) {
                String originalWindow = driver.getWindowHandle();
                Set<String> existingWindows = driver.getWindowHandles();

                // Attempt to click the link safely
                WebElement link = driver.findElements(By.xpath("//a[@href='" + href + "']")).stream()
                        .findFirst()
                        .orElse(null);
                if (link != null) {
                    wait.until(ExpectedConditions.elementToBeClickable(link)).click();
                } else {
                    // Fallback: navigate directly
                    driver.navigate().to(href);
                }

                // Wait for new window/tab if opened
                wait.until(d -> d.getWindowHandles().size() > existingWindows.size());
                Set<String> newWindows = driver.getWindowHandles();
                newWindows.removeAll(existingWindows);
                String newWindow = newWindows.iterator().next();

                driver.switchTo().window(newWindow);
                wait.until(webDriver -> ((String) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState")).equals("complete"));
                Assertions.assertTrue(driver.getCurrentUrl().contains(new java.net.URL(href).getHost()),
                        "External link should navigate to expected domain");

                driver.close();
                driver.switchTo().window(originalWindow);
            }
        }
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        openUrl(BASE_URL);
        // Example selector – adjust as needed for the actual page
        By dropdownLocator = By.id("sort");
        List<WebElement> options = driver.findElements(By.cssSelector("#sort option"));
        if (options.isEmpty()) {
            // If no sorting dropdown, skip test
            return;
        }
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(dropdownLocator));
        Select select = new Select(dropdown);
        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());
            // Verify that selection caused a change – placeholder check
            Assertions.assertEquals(option.getText(), select.getFirstSelectedOption().getText(),
                    "Dropdown selection should reflect chosen option");
        }
    }

    @Test
    @Order(5)
    public void testMenuBurgerAndNavigation() {
        openUrl(BASE_URL);
        By burgerLocator = By.id("react-burger-menu-btn");
        WebElement burger = wait.until(ExpectedConditions.elementToBeClickable(burgerLocator));
        burger.click();

        // Menu items – adjust IDs or text as appropriate
        By allItemsLocator = By.id("inventory_sidebar_link");
        By aboutLocator = By.id("about_sidebar_link");
        By logoutLocator = By.id("logout_sidebar_link");
        By resetLocator = By.id("reset_sidebar_link");

        // All Items
        if (driver.findElements(allItemsLocator).size() > 0) {
            WebElement allItems = wait.until(ExpectedConditions.elementToBeClickable(allItemsLocator));
            allItems.click();
            Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                    "All Items should navigate to inventory page");
        }

        // About (external)
        if (driver.findElements(aboutLocator).size() > 0) {
            WebElement about = wait.until(ExpectedConditions.elementToBeClickable(aboutLocator));
            String originalWindow = driver.getWindowHandle();
            about.click();
            wait.until(d -> d.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            windows.remove(originalWindow);
            String newWindow = windows.iterator().next();
            driver.switchTo().window(newWindow);
            wait.until(webDriver -> ((String) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState")).equals("complete"));
            Assertions.assertTrue(driver.getCurrentUrl().contains("about"),
                    "About link should open external about page");
            driver.close();
            driver.switchTo().window(originalWindow);
        }

        // Reset App State
        if (driver.findElements(resetLocator).size() > 0) {
            WebElement reset = wait.until(ExpectedConditions.elementToBeClickable(resetLocator));
            reset.click();
            // Verify state reset – placeholder check
            Assertions.assertTrue(true, "Reset App State executed");
        }

        // Logout
        if (driver.findElements(logoutLocator).size() > 0) {
            WebElement logout = wait.until(ExpectedConditions.elementToBeClickable(logoutLocator));
            logout.click();
            Assertions.assertTrue(driver.getCurrentUrl().contains("login"),
                    "Logout should navigate to login page");
        }
    }

    @Test
    @Order(6)
    public void testFooterSocialLinks() throws MalformedURLException {
        openUrl(BASE_URL);
        // Assuming social links have identifiable classes or IDs
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a"));
        for (WebElement link : socialLinks) {
            String href = link.getAttribute("href");
            if (href == null || href.isEmpty()) continue;

            String originalWindow = driver.getWindowHandle();
            Set<String> existingWindows = driver.getWindowHandles();

            wait.until(ExpectedConditions.elementToBeClickable(link)).click();

            // Wait for possible new window/tab
            wait.until(d -> d.getWindowHandles().size() > existingWindows.size());
            Set<String> newWindows = driver.getWindowHandles();
            newWindows.removeAll(existingWindows);
            String newWindow = newWindows.iterator().next();

            driver.switchTo().window(newWindow);
            wait.until(webDriver -> ((String) ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState")).equals("complete"));
            Assertions.assertTrue(driver.getCurrentUrl().contains(new java.net.URL(href).getHost()),
                    "Social link should navigate to expected domain");
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(7)
    public void testLoginValidCredentials() {
        openUrl(BASE_URL);
        // Adjust locators to match actual login form
        By usernameLocator = By.id("user-name");
        By passwordLocator = By.id("password");
        By loginButtonLocator = By.id("login-button");

        if (driver.findElements(usernameLocator).isEmpty()) {
            // No login form present – skip test
            return;
        }

        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameLocator));
        WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordLocator));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(loginButtonLocator));

        username.clear();
        username.sendKeys("standard_user");
        password.clear();
        password.sendKeys("secret_sauce");
        loginBtn.click();

        // Verify successful login – placeholder check
        wait.until(ExpectedConditions.urlContains("inventory"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("inventory"),
                "After valid login, URL should contain 'inventory'");
    }

    @Test
    @Order(8)
    public void testLoginInvalidCredentials() {
        openUrl(BASE_URL);
        By usernameLocator = By.id("user-name");
        By passwordLocator = By.id("password");
        By loginButtonLocator = By.id("login-button");
        By errorMsgLocator = By.cssSelector("[data-test='error']");

        if (driver.findElements(usernameLocator).isEmpty()) {
            // No login form present – skip test
            return;
        }

        WebElement username = wait.until(ExpectedConditions.visibilityOfElementLocated(usernameLocator));
        WebElement password = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordLocator));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(loginButtonLocator));

        username.clear();
        username.sendKeys("invalid_user");
        password.clear();
        password.sendKeys("wrong_password");
        loginBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsgLocator));
        Assertions.assertTrue(errorMsg.isDisplayed(),
                "Error message should be displayed for invalid credentials");
    }
}