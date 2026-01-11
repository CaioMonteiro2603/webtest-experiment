package deepseek.ws06.seq04;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

@TestMethodOrder(OrderAnnotation.class)
public class RestfullBooker {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "https://automationintesting.online/";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "password";

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

    @Test
    @Order(1)
    public void testRoomBookingForm() {
        driver.get(BASE_URL);
        
        // Fill booking form
        WebElement nameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("name")));
        nameField.sendKeys("John Doe");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("john.doe@example.com");
        
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("1234567890");
        
        WebElement subjectField = driver.findElement(By.id("subject"));
        subjectField.sendKeys("Booking request");
        
        WebElement descriptionField = driver.findElement(By.id("description"));
        descriptionField.sendKeys("I would like to book a room for 2 nights");
        
        WebElement submitButton = driver.findElement(By.id("submitMessage"));
        submitButton.click();

        // Verify submission
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'contact')]//h2[contains(text(),'Thanks')]")));
        Assertions.assertTrue(successMessage.isDisplayed());
    }

    @Test
    @Order(2)
    public void testAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys(ADMIN_USERNAME);
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(ADMIN_PASSWORD);
        
        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();

        // Verify login success
        WebElement logoutButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout")));
        Assertions.assertTrue(logoutButton.isDisplayed());
    }

    @Test
    @Order(3)
    public void testInvalidAdminLogin() {
        driver.get(BASE_URL + "#/admin");
        
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys("invalid");
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys("credentials");
        
        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();

        // Verify error message
        WebElement errorMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".alert-danger")));
        Assertions.assertTrue(errorMessage.getText().contains("Bad credentials"));
    }

    @Test
    @Order(4) 
    public void testRoomCreation() {
        adminLogin();
        
        WebElement roomNumber = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("roomNumber")));
        roomNumber.sendKeys("101");
        
        WebElement roomPrice = driver.findElement(By.id("roomPrice"));
        roomPrice.sendKeys("100");
        
        WebElement createButton = driver.findElement(By.id("createRoom"));
        createButton.click();

        // Verify room created
        WebElement roomItem = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[@id='101']")));
        Assertions.assertTrue(roomItem.isDisplayed());
    }

    @Test
    @Order(5)
    public void testRoomDeletion() {
        adminLogin();
        
        // First create a room to delete
        testRoomCreation();
        
        WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//div[@id='101']//button[contains(text(),'Delete')]")));
        deleteButton.click();

        // Verify room deleted
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("101")));
        List<WebElement> rooms = driver.findElements(By.id("101"));
        Assertions.assertEquals(0, rooms.size());
    }

    @Test
    @Order(6)
    public void testAdminLogout() {
        adminLogin();
        
        WebElement logoutButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("logout")));
        logoutButton.click();

        // Verify logout
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doLogin")));
        Assertions.assertTrue(driver.getCurrentUrl().contains("#/admin"));
    }

    @Test
    @Order(7)
    public void testFooterLinks() {
        driver.get(BASE_URL);
        
        // Test Blog link
        testFooterLink("Blog", "https://automationintesting.com/");
        
        // Test API Documentation link
        testFooterLink("API Documentation", "https://automationintesting.online/swagger-ui.html");
    }

    private void testFooterLink(String linkText, String expectedUrl) {
        WebElement footerLink = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//footer//a[contains(text(),'" + linkText + "')]")));
        footerLink.click();

        if (expectedUrl.contains("swagger")) {
            // API docs open in new tab
            String originalWindow = driver.getWindowHandle();
            wait.until(ExpectedConditions.numberOfWindowsToBe(2));
            
            for (String windowHandle : driver.getWindowHandles()) {
                if (!windowHandle.equals(originalWindow)) {
                    driver.switchTo().window(windowHandle);
                    break;
                }
            }
            
            Assertions.assertTrue(driver.getCurrentUrl().contains(expectedUrl));
            driver.close();
            driver.switchTo().window(originalWindow);
        } else {
            wait.until(ExpectedConditions.urlContains(expectedUrl));
        }
    }

    private void adminLogin() {
        driver.get(BASE_URL + "#/admin");
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.sendKeys(ADMIN_USERNAME);
        
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.sendKeys(ADMIN_PASSWORD);
        
        WebElement loginButton = driver.findElement(By.id("doLogin"));
        loginButton.click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logout")));
    }
}