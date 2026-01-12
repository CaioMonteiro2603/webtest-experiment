package SunaGPT20b.ws08.seq04;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Set;
import java.util.HashSet;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JPetStore{

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static final String USERNAME = "j2ee";
    private static final String PASSWORD = "j2ee";

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

    @BeforeEach
    public void setUp() {
        driver.get(BASE_URL);
        // Ensure a clean state by resetting the app via the menu if present
        try {
            WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
            menuBtn.click();
            WebElement resetLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("reset_sidebar_link")));
            resetLink.click();
            // close the menu
            WebElement closeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-cross-btn")));
            closeBtn.click();
        } catch (Exception ignored) {
        }
    }

    // ---------- Helper Methods ----------
    private void login(String user, String pass) {
        driver.get(BASE_URL + "catalog");
        // Click on the Sign In link instead of going to login page directly
        WebElement signInLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Sign In")));
        signInLink.click();
        
        // Use name attributes instead of id for username and password
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.name("username")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.name("password")));
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.name("signon")));

        userField.clear();
        userField.sendKeys(user);
        passField.clear();
        passField.sendKeys(pass);
        loginBtn.click();
    }

    private void openMenuAndClick(String linkId) {
        WebElement menuBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("react-burger-menu-btn")));
        menuBtn.click();
        WebElement target = wait.until(ExpectedConditions.elementToBeClickable(By.id(linkId)));
        target.click();
        // close menu if still open
        List<WebElement> closeButtons = driver.findElements(By.id("react-burger-cross-btn"));
        if (!closeButtons.isEmpty()) {
            closeButtons.get(0).click();
        }
    }

    // ---------- Tests ----------
    @Test
    @Order(1)
    public void testValidLogin() {
        login(USERNAME, PASSWORD);
        // Wait for successful login by checking for Sign Out link or catalog page
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign Out")));
        Assertions.assertTrue(driver.getPageSource().contains("Sign Out"),
                "After valid login, Sign Out link should be present");
        // Also check we're on catalog page
        Assertions.assertTrue(driver.getCurrentUrl().contains("/catalog") || driver.getCurrentUrl().equals(BASE_URL),
                "After valid login, should be on catalog page");
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        login("invalidUser", "invalidPass");
        // Wait for error message on the same page
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//font[contains(@color, 'red')]")));
        Assertions.assertTrue(errorMsg.isDisplayed(), "Error message should be displayed for invalid login");
        Assertions.assertTrue(errorMsg.getText().toLowerCase().contains("invalid") || 
                errorMsg.getText().toLowerCase().contains("failed") ||
                errorMsg.getText().toLowerCase().contains("error"),
                "Error message should indicate invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USERNAME, PASSWORD);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Sign Out")));
        
        // Navigate to a category that has products (e.g., Fish)
        WebElement fishLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//area[@alt='Fish']")));
        fishLink.click();
        
        // Wait for products to load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        
        // Get the product table
        List<WebElement> productLinks = driver.findElements(
                By.xpath("//table//tr[position()>1]//td[1]//a"));
        Assertions.assertFalse(productLinks.isEmpty(), "Should have products to sort");
        
        // Store original order
        String firstProductBefore = productLinks.get(0).getText();
        
        // Click on a different sorting option if available
        // Try clicking on column header to sort
        WebElement itemIdHeader = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//tr[1]//th[1]//a")));
        itemIdHeader.click();
        
        // Wait for page to reload with sorted data
        wait.until(ExpectedConditions.stalenessOf(productLinks.get(0)));
        
        // Check sorted order
        List<WebElement> productLinksAfter = driver.findElements(
                By.xpath("//table//tr[position()>1]//td[1]//a"));
        Assertions.assertFalse(productLinksAfter.isEmpty(), "Should still have products after sorting");
        
        String firstProductAfter = productLinksAfter.get(0).getText();
        Assertions.assertNotEquals(firstProductBefore, firstProductAfter,
                "First product should change after sorting");
    }

    @Test
    @Order(4)
    public void testMenuNavigation() {
        login(USERNAME, PASSWORD);
        
        // Navigate through main categories using the navigation areas
        String[] categories = {"Fish", "Dogs", "Cats", "Reptiles", "Birds"};
        
        for (String category : categories) {
            WebElement categoryLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//area[@alt='" + category + "']")));
            categoryLink.click();
            
            // Verify we're on the correct category page
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h2")));
            WebElement heading = driver.findElement(By.tagName("h2"));
            Assertions.assertTrue(heading.getText().contains(category),
                    "Should be on " + category + " category page");
            
            // Go back to main page
            driver.get(BASE_URL + "catalog");
        }
        
        // Test search functionality
        WebElement searchField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.name("keyword")));
        searchField.clear();
        searchField.sendKeys("fish");
        
        WebElement searchButton = driver.findElement(By.name("searchProducts"));
        searchButton.click();
        
        // Verify search results
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        Assertions.assertTrue(driver.getPageSource().toLowerCase().contains("fish"),
                "Search results should contain 'fish'");
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() {
        driver.get(BASE_URL);
        // Footer social links selectors (example IDs)
        String[] linkIds = {"twitter_link", "facebook_link", "linkedin_link"};
        String[] expectedDomains = {"twitter.com", "facebook.com", "linkedin.com"};

        for (int i = 0; i < linkIds.length; i++) {
            List<WebElement> links = driver.findElements(By.id(linkIds[i]));
            if (links.isEmpty()) continue; // skip if not present
            WebElement link = links.get(0);
            String originalWindow = driver.getWindowHandle();
            link.click();

            // Switch to new window/tab
            wait.until(driver1 -> driver1.getWindowHandles().size() > 1);
            Set<String> windows = driver.getWindowHandles();
            for (String win : windows) {
                if (!win.equals(originalWindow)) {
                    driver.switchTo().window(win);
                    break;
                }
            }
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedDomains[i]),
                    "External link should navigate to domain " + expectedDomains[i]);
            driver.close();
            driver.switchTo().window(originalWindow);
        }
    }

    @Test
    @Order(6)
    public void testOneLevelInternalLinks() {
        login(USERNAME, PASSWORD);
        driver.get(BASE_URL);
        // Collect all internal links on the base page (one level)
        List<WebElement> anchors = driver.findElements(By.cssSelector("a[href]"));
        Set<String> internalUrls = new HashSet<>();
        for (WebElement a : anchors) {
            String href = a.getAttribute("href");
            if (href != null && href.startsWith(BASE_URL) && !href.equals(BASE_URL)) {
                // Ensure only one level deeper (no further slashes after base path)
                String path = href.substring(BASE_URL.length());
                if (!path.isEmpty() && !path.contains("/")) {
                    internalUrls.add(href);
                }
            }
        }

        for (String url : internalUrls) {
            driver.navigate().to(url);
            // Simple verification: page loads and has a body element
            WebElement body = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));
            Assertions.assertTrue(body.isDisplayed(), "Page body should be displayed for " + url);
            // Return to base page for next iteration
            driver.navigate().back();
        }
    }
}