package SunaGPT20b.ws08.seq01;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Set;

@TestMethodOrder(OrderAnnotation.class)
public class JPetStore {

    private static final String BASE_URL = "https://jpetstore.aspectran.com/";
    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Helper to navigate to base URL and wait for the page to be ready */
    private void goToHome() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.titleContains("JPetStore"));
    }

    @Test
    @Order(1)
    public void testHomePageLoads() {
        goToHome();
        Assertions.assertTrue(driver.getTitle().contains("JPetStore"),
                "Home page title should contain 'JPetStore'");
        WebElement logo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img[src*='logo'], .logo, header img, #Logo")));
        Assertions.assertTrue(logo.isDisplayed(), "Logo should be displayed on home page");
    }

    @Test
    @Order(2)
    public void testCategoryNavigation() {
        goToHome();
        List<WebElement> categoryLinks = driver.findElements(By.cssSelector("a[href*='viewCategory'], a[href*='categoryId']"));
        Assertions.assertFalse(categoryLinks.isEmpty(), "Category links should be present on home page");

        for (WebElement link : categoryLinks) {
            String categoryName = link.getText().trim();
            link.click();
            wait.until(ExpectedConditions.urlContains("category"));
            WebElement productTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table, .products")));
            Assertions.assertTrue(productTable.isDisplayed(),
                    "Product table should be displayed for category: " + categoryName);
            // Return to home for next iteration
            driver.navigate().back();
            wait.until(ExpectedConditions.titleContains("JPetStore"));
        }
    }

    @Test
    @Order(3)
    public void testProductDetailPage() {
        goToHome();
        // Open first category
        WebElement firstCategory = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='viewCategory'], a[href*='categoryId']")));
        firstCategory.click();
        wait.until(ExpectedConditions.urlContains("category"));

        // Click first product link
        WebElement firstProduct = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='viewProduct'], a[href*='productId']")));
        firstProduct.click();
        wait.until(ExpectedConditions.urlContains("product"));

        // Verify product details are displayed
        WebElement productName = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("h2")));
        Assertions.assertTrue(productName.isDisplayed(),
                "Product name header should be displayed on product detail page");

        // Verify Add to Cart button exists
        List<WebElement> addToCartButtons = driver.findElements(By.cssSelector("input[value*='Add'], button:contains('Add'), a:contains('Cart')"));
        Assertions.assertFalse(addToCartButtons.isEmpty(),
                "Add to Cart button should be present on product detail page");
    }

    @Test
    @Order(4)
    public void testSortingDropdownIfPresent() {
        goToHome();
        // Navigate to a category to get a product list
        WebElement firstCategory = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href*='viewCategory'], a[href*='categoryId']")));
        firstCategory.click();
        wait.until(ExpectedConditions.urlContains("category"));

        // Look for a sorting dropdown (common name 'sort')
        List<WebElement> sortElements = driver.findElements(By.name("sort"));
        if (sortElements.isEmpty()) {
            // No sorting dropdown; test is considered passed
            Assertions.assertTrue(true, "Sorting dropdown not present; skipping test.");
            return;
        }

        WebElement sortDropdown = sortElements.get(0);
        Select select = new Select(sortDropdown);
        List<WebElement> options = select.getOptions();
        Assertions.assertTrue(options.size() > 1, "Sorting dropdown should have multiple options");

        String firstItemBefore = driver.findElement(By.cssSelector("table tr:nth-child(2) td:nth-child(2)")).getText();

        for (WebElement option : options) {
            select.selectByVisibleText(option.getText());
            // Wait for page to reflect sorting (simple wait for first row text change)
            wait.until(driver -> {
                String currentFirst = driver.findElement(By.cssSelector("table tr:nth-child(2) td:nth-child(2)")).getText();
                return !currentFirst.equals(firstItemBefore);
            });
            // Verify that the selected option is indeed active
            Assertions.assertEquals(option.getText(), select.getFirstSelectedOption().getText(),
                    "Dropdown should reflect the selected sorting option");
        }
    }

    @Test
    @Order(5)
    public void testFooterExternalLinks() throws MalformedURLException {
        goToHome();
        // Locate footer external links (target="_blank")
        List<WebElement> externalLinks = driver.findElements(By.cssSelector("footer a[target='_blank'], footer a[href^='http']"));
        Assertions.assertFalse(externalLinks.isEmpty(), "There should be external links in the footer");

        for (WebElement link : externalLinks) {
            String href = link.getAttribute("href");
            Assertions.assertNotNull(href, "External link should have href attribute");
            Set<String> oldHandles = driver.getWindowHandles();
            link.click();

            // Wait for new window to open
            wait.until(webDriver -> webDriver.getWindowHandles().size() > oldHandles.size());
            
            // Switch to new window
            String newWindow = driver.getWindowHandles().stream().filter(handle -> !oldHandles.contains(handle)).findFirst().orElse(null);
            if (newWindow != null) {
                driver.switchTo().window(newWindow);
                String currentUrl = driver.getCurrentUrl();
                Assertions.assertTrue(currentUrl.contains(new URL(href).getHost()),
                        "External link should navigate to expected domain: " + href);

                // Close external tab and switch back
                driver.close();
            }
            driver.switchTo().window(oldHandles.iterator().next());
            
            // Refresh the page to ensure we're back on the original page
            driver.navigate().refresh();
        }
    }
}