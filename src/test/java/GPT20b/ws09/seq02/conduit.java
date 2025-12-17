package GPT20b.ws09.seq02;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Assumptions;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@TestMethodOrder(OrderAnnotation.class)
public class conduit {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://demo.realworld.io/";
    private static final String USERNAME = "demouser";
    private static final String PASSWORD = "demouser";

    @BeforeAll
    public static void init() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public static void cleanup() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        driver.navigate().to(BASE_URL);
        assertTrue(driver.getTitle().toLowerCase().contains("realworld"),
                "Page title should contain 'Realworld'");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
    }

    @Test
    @Order(2)
    public void testValidLogin() {
        driver.navigate().to(BASE_URL);
        clickLoginModal();
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='username']")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='password']")));
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        userField.clear();
        userField.sendKeys(USERNAME);
        passField.clear();
        passField.sendKeys(PASSWORD);
        submitBtn.click();

        wait.until(ExpectedConditions.urlContains("/articles"));
        WebElement profileName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(@href,'/settings') and contains(text(),'" + USERNAME + "')]")));
        assertTrue(profileName.isDisplayed(), "Profile username should be displayed after login");

        logoutIfPresent();
    }

    @Test
    @Order(3)
    public void testInvalidLogin() {        
    	driver.navigate().to(BASE_URL);
        clickLoginModal();
        WebElement userField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='username']")));
        WebElement passField = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[name='password']")));
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        userField.clear();
        userField.sendKeys("invaliduser");
        passField.clear();
        passField.sendKeys("wrongpass");
        submitBtn.click();

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".alert.alert-danger, .alert-danger") ));
        assertTrue(errorMsg.isDisplayed(), "Error message must appear for invalid credentials");
        assertFalse(errorMsg.getText().trim().isEmpty(), "Error message should contain text");
    }

    @Test
    @Order(4)
    public void testSortingDropdown() {
        driver.navigate().to(BASE_URL + "articles");
        List<WebElement> sortSelects = driver.findElements(By.cssSelector("select[aria-label='Sort']"));
        Assumptions.assumeTrue(!sortSelects.isEmpty(), "No sorting dropdown found – skipping test");
        Select sorter = new Select(sortSelects.get(0));
        List<WebElement> options = sorter.getOptions();
        Assumptions.assumeTrue(options.size() > 1, "Sorting dropdown has fewer than 2 options – skipping test");
        String firstBefore = getFirstArticleTitle();
        for (WebElement opt : options) {
            sorter.selectByVisibleText(opt.getText());
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".article-preview .title")));
            String firstAfter = getFirstArticleTitle();
            assertNotEquals(firstBefore, firstAfter, "Sorting by '" + opt.getText() + "' should change first article");
            firstBefore = firstAfter;
        }
        sorter.selectByVisibleText(options.get(0).getText());
    }

    @Test
    @Order(5)
    public void testExternalLinksOnHome() {
        driver.navigate().to(BASE_URL);
        List<WebElement> links = driver.findElements(By.cssSelector("a[href^='http']"));
        Assumptions.assumeFalse(links.isEmpty(), "No external links found on home page");
        for (WebElement link : links) {
            String href = link.getAttribute("href");
            if (!href.contains("demo.realworld.io")) {
                openAndAssertExternalLink(link, href);
            }
        }
    }

    @Test
    @Order(6)
    public void testBurgerMenuActions() {
        loginIfNeeded();
        List<WebElement> burgerButtons = driver.findElements(By.cssSelector("button[aria-label='Menu'], .navbar-toggler"));
        Assumptions.assumeTrue(!burgerButtons.isEmpty(), "Burger menu button not found – skipping test");
        WebElement burger = burgerButtons.get(0);
        burger.click();

        boolean allItemsVisited = false;
        boolean aboutVisited = false;
        boolean resetClicked = false;

        List<WebElement> menuLinks = driver.findElements(By.cssSelector("a"));
        for (WebElement link : menuLinks) {
            String txt = link.getText().trim();
            switch (txt) {
                case "All Items":
                case "Articles":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("/articles"));
                    assertTrue(driver.getCurrentUrl().contains("/articles"), "URL should contain '/articles' after click");
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(burger));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(burger));
                    burger.click();
                    allItemsVisited = true;
                    break;
                case "About":
                    openAndAssertExternalLink(link, link.getAttribute("href"));
                    driver.navigate().back();
                    wait.until(ExpectedConditions.elementToBeClickable(burger));
                    burger = wait.until(ExpectedConditions.elementToBeClickable(burger));
                    burger.click();
                    aboutVisited = true;
                    break;
                case "Reset App State":
                    link.click();
                    wait.until(ExpectedConditions.urlContains("/articles"));
                    resetClicked = true;
                    break;
                case "Logout":
                    link.click();
                    wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='button']")));
                    logoutIfPresent();
                    break;
                default:
                    // ignore
            }
        }

        assertTrue(allItemsVisited, "All Items link was not visited from menu");
        assertTrue(aboutVisited, "About link did not navigate externally from menu");
        assertTrue(resetClicked, "Reset App State link was not clicked");
    }

    /* Helper methods */

    private void clickLoginModal() {
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Login")));
        loginBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.modal-body")));
    }

    private void loginIfNeeded() {
        if (driver.findElements(By.linkText("Logout")).isEmpty()) {
            return;
        }
        // Already logged in, nothing needed
    }

    private void logoutIfPresent() {
        List<WebElement> logoutLinks = driver.findElements(By.linkText("Logout"));
        if (!logoutLinks.isEmpty()) {
            logoutLinks.get(0).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Login")));
        }
    }

    private String getFirstArticleTitle() {
        List<WebElement> items = driver.findElements(By.cssSelector(".article-preview .title"));
        return items.isEmpty() ? "" : items.get(0).getText();
    }

    private void openAndAssertExternalLink(WebElement link, String expectedUrlFragment) {
        String originalHandle = driver.getWindowHandle();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        wait.until(d -> driver.getWindowHandles().size() > 1);
        Set<String> handles = driver.getWindowHandles();
        for (String h : handles) {
            if (!h.equals(originalHandle)) {
                driver.switchTo().window(h);
                wait.until(ExpectedConditions.urlContains(expectedUrlFragment));
                assertTrue(driver.getCurrentUrl().contains(expectedUrlFragment),
                        "External link URL should contain " + expectedUrlFragment);
                driver.close();
                driver.switchTo().window(originalHandle);
                break;
            }
        }
    }
}