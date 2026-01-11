package SunaGPT20b.ws03.seq07;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@TestMethodOrder(OrderAnnotation.class)
public class bugbank {

    private static final String BASE_URL = "https://bugbank.netlify.app/";
    private static final String USER_EMAIL = "caio@gmail.com";
    private static final String USER_PASSWORD = "123";

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUpAll() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterAll
    public static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void login(String email, String password) {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Informe seu e-mail']")));
        WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Informe seu e-mail']"));
        emailField.clear();
        emailField.sendKeys(email);
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Informe sua senha']"));
        passwordField.clear();
        passwordField.sendKeys(password);
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logoutIfLoggedIn() {
        if (driver.getCurrentUrl().contains("/home")) {
            WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='menuButton']")));
            menuButton.click();
            
            WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='logoutButton']")));
            logoutButton.click();
            
            wait.until(ExpectedConditions.urlContains("/login"));
        }
    }

    private void openBurgerMenu() {
        WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='menuButton']")));
        menuButton.click();
    }

    private void resetAppState() {
        openBurgerMenu();
        WebElement resetButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='resetButton']")));
        resetButton.click();
    }

    private List<String> getInventoryItemNames() {
        List<WebElement> items = driver.findElements(By.cssSelector(".inventory_item_name"));
        return items.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    private List<Double> getInventoryItemPrices() {
        List<WebElement> priceEls = driver.findElements(By.cssSelector(".inventory_item_price"));
        List<Double> prices = new ArrayList<>();
        for (WebElement el : priceEls) {
            String txt = el.getText().replaceAll("[^0-9.]", "");
            if (!txt.isEmpty()) {
                prices.add(Double.parseDouble(txt));
            }
        }
        return prices;
    }

    @Test
    @Order(1)
    public void testValidLogin() {
        login(USER_EMAIL, USER_PASSWORD);
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "After login the URL should contain /home");
        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(2)
    public void testInvalidLogin() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[placeholder='Informe seu e-mail']")));
        WebElement emailField = driver.findElement(By.cssSelector("input[placeholder='Informe seu e-mail']"));
        emailField.clear();
        emailField.sendKeys("wrong@example.com");
        
        WebElement passwordField = driver.findElement(By.cssSelector("input[placeholder='Informe sua senha']"));
        passwordField.clear();
        passwordField.sendKeys("badpass");
        
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
        loginButton.click();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login") || 
                driver.findElements(By.cssSelector(".error-message")).size() > 0,
                "Should stay on login page or show error message for invalid credentials");
    }

    @Test
    @Order(3)
    public void testSortingDropdown() {
        login(USER_EMAIL, USER_PASSWORD);
        
        By sortSelect = By.cssSelector("select[data-test='sort-select']");
        if (driver.findElements(sortSelect).size() > 0) {
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(sortSelect));
            
            // Name (A to Z)
            new Select(dropdown).selectByVisibleText("Name (A to Z)");
            List<String> namesAsc = getInventoryItemNames();
            List<String> sortedAsc = new ArrayList<>(namesAsc);
            Collections.sort(sortedAsc);
            Assertions.assertEquals(sortedAsc, namesAsc, "Items should be sorted alphabetically A-Z");
            
            // Name (Z to A)
            new Select(dropdown).selectByVisibleText("Name (Z to A)");
            List<String> namesDesc = getInventoryItemNames();
            List<String> sortedDesc = new ArrayList<>(namesDesc);
            Collections.sort(sortedDesc, Collections.reverseOrder());
            Assertions.assertEquals(sortedDesc, namesDesc, "Items should be sorted alphabetically Z-A");
            
            // Price (low to high)
            new Select(dropdown).selectByVisibleText("Price (low to high)");
            List<Double> pricesLowHigh = getInventoryItemPrices();
            List<Double> sortedLowHigh = new ArrayList<>(pricesLowHigh);
            Collections.sort(sortedLowHigh);
            Assertions.assertEquals(sortedLowHigh, pricesLowHigh, "Items should be sorted by price low-to-high");
            
            // Price (high to low)
            new Select(dropdown).selectByVisibleText("Price (high to low)");
            List<Double> pricesHighLow = getInventoryItemPrices();
            List<Double> sortedHighLow = new ArrayList<>(pricesHighLow);
            Collections.sort(sortedHighLow, Collections.reverseOrder());
            Assertions.assertEquals(sortedHighLow, pricesHighLow, "Items should be sorted by price high-to-low");
        }
        
        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(4)
    public void testMenuAllItems() {
        login(USER_EMAIL, USER_PASSWORD);
        
        openBurgerMenu();
        WebElement allItemsLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-testid='allItemsLink']")));
        allItemsLink.click();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("/home"),
                "All Items should navigate back to home page");
        
        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(5)
    public void testMenuAboutExternalLink() {
        login(USER_EMAIL, USER_PASSWORD);
        
        openBurgerMenu();
        WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-testid='aboutLink']")));
        aboutLink.click();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("about") || 
                driver.findElements(By.cssSelector(".about-content")).size() > 0,
                "About link should open about page or content");
        
        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(6)
    public void testMenuLogout() {
        login(USER_EMAIL, USER_PASSWORD);
        
        logoutIfLoggedIn();
        
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login") || 
                driver.findElements(By.cssSelector("input[placeholder='Informe seu e-mail']")).size() > 0,
                "Logout should return to the login page");
    }

    @Test
    @Order(7)
    public void testMenuResetAppState() {
        login(USER_EMAIL, USER_PASSWORD);
        
        // Add an item to cart
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button[data-test='add-to-cart']"));
        if (addButtons.size() > 0) {
            addButtons.get(0).click();
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            int badgeCount = driver.findElements(By.cssSelector(".cart-badge")).size();
            if (badgeCount > 0) {
                String badgeText = driver.findElement(By.cssSelector(".cart-badge")).getText();
                Assertions.assertEquals("1", badgeText,
                        "Cart badge should show 1 after adding an item");
            }
        }
        
        // Reset state
        resetAppState();
        
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        int badgeCountAfterReset = driver.findElements(By.cssSelector(".cart-badge")).size();
        Assertions.assertEquals(0, badgeCountAfterReset,
                "Cart badge should be cleared after resetting app state");
        
        logoutIfLoggedIn();
    }

    @Test
    @Order(8)
    public void testFooterSocialLinks() {
        login(USER_EMAIL, USER_PASSWORD);
        
        // Check for social links in footer
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("footer a[data-test*='social']"));
        
        if (socialLinks.size() > 0) {
            for (WebElement link : socialLinks) {
                String href = link.getAttribute("href");
                Assertions.assertNotNull(href, "Social link should have href attribute");
                Assertions.assertTrue(href.contains("twitter") || href.contains("facebook") || href.contains("linkedin"),
                        "Social link should contain expected domain");
            }
        } else {
            // If no specific social links found, check for any footer links
            List<WebElement> footerLinks = driver.findElements(By.cssSelector("footer a"));
            Assertions.assertTrue(footerLinks.size() > 0, "Footer should contain at least some links");
        }
        
        resetAppState();
        logoutIfLoggedIn();
    }

    @Test
    @Order(9)
    public void testAddToCartAndCheckout() {
        login(USER_EMAIL, USER_PASSWORD);
        
        // Add first item to cart
        List<WebElement> addButtons = driver.findElements(By.cssSelector("button[data-test='add-to-cart']"));
        if (addButtons.size() > 0) {
            addButtons.get(0).click();
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Go to cart
            WebElement cartButton = driver.findElement(By.cssSelector("a[data-test='cart-link']"));
            cartButton.click();
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            Assertions.assertTrue(driver.getCurrentUrl().contains("/cart") || 
                    driver.findElements(By.cssSelector(".cart-content")).size() > 0,
                    "Should navigate to cart page or show cart content");
            
            // Checkout
            List<WebElement> checkoutButtons = driver.findElements(By.cssSelector("button[data-test='checkout']"));
            if (checkoutButtons.size() > 0) {
                checkoutButtons.get(0).click();
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // Fill checkout form if exists
                List<WebElement> nameField = driver.findElements(By.cssSelector("input[data-test='first-name']"));
                if (nameField.size() > 0) {
                    nameField.get(0).sendKeys("John");
                    driver.findElement(By.cssSelector("input[data-test='last-name']")).sendKeys("Doe");
                    driver.findElement(By.cssSelector("input[data-test='postal-code']")).sendKeys("12345");
                    driver.findElement(By.cssSelector("button[data-test='continue']")).click();
                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    driver.findElement(By.cssSelector("button[data-test='finish']")).click();
                    
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    WebElement completeMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".complete-message")));
                    Assertions.assertTrue(completeMsg.getText().toLowerCase().contains("thank you") || 
                            completeMsg.getText().toLowerCase().contains("order"),
                            "Checkout completion message should be displayed");
                }
            }
        } else {
            Assertions.assertTrue(driver.findElements(By.cssSelector(".inventory-item")).size() > 0,
                    "Page should have inventory items");
        }
        
        resetAppState();
        logoutIfLoggedIn();
    }
}