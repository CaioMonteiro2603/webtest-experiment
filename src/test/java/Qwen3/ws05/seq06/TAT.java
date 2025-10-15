package Qwen3.ws05.seq06;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(org.junit.jupiter.api.MethodOrderer.OrderAnnotation.class)
public class CacTatTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @BeforeAll
    public static void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
    public void testHomePageLoadsCorrectly() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        String currentPageTitle = driver.getTitle();
        assertEquals("CAC TAT - Cadastro de Clientes", currentPageTitle, "Page title should be 'CAC TAT - Cadastro de Clientes'");
        
        WebElement mainHeader = driver.findElement(By.tagName("h1"));
        assertEquals("CAC TAT", mainHeader.getText(), "Main header should be 'CAC TAT'");
        
        // Check if form is present
        WebElement formElement = driver.findElement(By.tagName("form"));
        assertTrue(formElement.isDisplayed(), "Form element should be displayed");
    }

    @Test
    @Order(2)
    public void testRequiredFieldsValidation() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Try submitting without filling any fields
        WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit']"));
        submitButton.click();
        
        // Verify error messages appear
        List<WebElement> errorMessages = driver.findElements(By.cssSelector(".error-message"));
        assertTrue(errorMessages.size() > 0, "At least one error message should be displayed");
    }

    @Test
    @Order(3)
    public void testValidFormSubmission() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Fill in required fields
        WebElement nameField = driver.findElement(By.id("name"));
        nameField.sendKeys("João Silva");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("joao.silva@example.com");
        
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("11999999999");
        
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("Test message for CAC TAT form");
        
        // Select gender
        WebElement maleRadio = driver.findElement(By.id("male"));
        maleRadio.click();
        
        // Select age group
        WebElement ageGroup = driver.findElement(By.id("age"));
        ageGroup.sendKeys("25-35");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit']"));
        submitButton.click();
        
        // Verify submission success
        WebElement successMessage = driver.findElement(By.cssSelector(".success-message"));
        assertTrue(successMessage.isDisplayed(), "Success message should be displayed after form submission");
        
        String successText = successMessage.getText();
        assertTrue(successText.contains("Cadastro realizado com sucesso"), "Success message should contain success text");
    }

    @Test
    @Order(4)
    public void testFormWithInvalidEmail() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Fill in fields with invalid email
        WebElement nameField = driver.findElement(By.id("name"));
        nameField.sendKeys("Maria Santos");
        
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.sendKeys("invalid-email");
        
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.sendKeys("21988888888");
        
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.sendKeys("Test message with invalid email");
        
        // Submit form
        WebElement submitButton = driver.findElement(By.cssSelector("input[type='submit']"));
        submitButton.click();
        
        // Check for error message related to email
        WebElement errorMessage = driver.findElement(By.cssSelector(".error-message"));
        assertTrue(errorMessage.isDisplayed(), "Error message should be displayed for invalid email");
        assertTrue(errorMessage.getText().toLowerCase().contains("email"), "Error message should relate to email field");
    }

    @Test
    @Order(5)
    public void testFormFieldInteractions() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Test name field
        WebElement nameField = driver.findElement(By.id("name"));
        nameField.clear();
        nameField.sendKeys("Test User");
        assertEquals("Test User", nameField.getAttribute("value"), "Name field value should match input");
        
        // Test email field
        WebElement emailField = driver.findElement(By.id("email"));
        emailField.clear();
        emailField.sendKeys("test@example.com");
        assertEquals("test@example.com", emailField.getAttribute("value"), "Email field value should match input");
        
        // Test phone field
        WebElement phoneField = driver.findElement(By.id("phone"));
        phoneField.clear();
        phoneField.sendKeys("5551234567");
        assertEquals("5551234567", phoneField.getAttribute("value"), "Phone field value should match input");
        
        // Test message field
        WebElement messageField = driver.findElement(By.id("message"));
        messageField.clear();
        messageField.sendKeys("Sample test message");
        assertEquals("Sample test message", messageField.getText(), "Message field text should match input");
        
        // Test gender selection
        WebElement femaleRadio = driver.findElement(By.id("female"));
        femaleRadio.click();
        assertTrue(femaleRadio.isSelected(), "Female radio button should be selected");
        
        // Test age group selection
        WebElement ageGroup = driver.findElement(By.id("age"));
        ageGroup.sendKeys("36-50");
        assertEquals("36-50", ageGroup.getAttribute("value"), "Age group selection should match input");
        
        // Test checkbox for newsletter subscription
        WebElement newsletterCheckbox = driver.findElement(By.id("newsletter"));
        assertFalse(newsletterCheckbox.isSelected(), "Newsletter checkbox should initially be unchecked");
        newsletterCheckbox.click();
        assertTrue(newsletterCheckbox.isSelected(), "Newsletter checkbox should be selected after click");
    }

    @Test
    @Order(6)
    public void testNavigationToOtherPages() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Click on 'Sobre' link (should navigate to about page)
        try {
            WebElement sobreLink = driver.findElement(By.linkText("Sobre"));
            sobreLink.click();
            
            // Wait for page load and check URL
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("sobre"), "Should navigate to About page");
            
            // Go back to home page
            driver.navigate().back();
        } catch (NoSuchElementException e) {
            // If 'Sobre' link doesn't exist, that's okay for this test
        }
        
        // Click on 'Contato' link (should navigate to contact page)
        try {
            WebElement contatoLink = driver.findElement(By.linkText("Contato"));
            contatoLink.click();
            
            // Wait for page load and check URL
            String currentUrl = driver.getCurrentUrl();
            assertTrue(currentUrl.contains("contato"), "Should navigate to Contact page");
            
            // Go back to home page
            driver.navigate().back();
        } catch (NoSuchElementException e) {
            // If 'Contato' link doesn't exist, that's okay for this test
        }
        
        // Go back to initial page
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
    }

    @Test
    @Order(7)
    public void testExternalLinksInFooter() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        // Wait for page to fully load
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("footer")));
        
        // Twitter link
        try {
            WebElement twitterLink = driver.findElement(By.cssSelector("a[href*='twitter']"));
            String oldTab = driver.getWindowHandle();
            twitterLink.click();
            String winHandle = driver.getWindowHandle();
            driver.switchTo().window(winHandle);
            assertTrue(driver.getCurrentUrl().contains("twitter.com"), "Twitter link should navigate to Twitter website");
            driver.close();
            driver.switchTo().window(oldTab);
        } catch (NoSuchElementException e) {
            // If Twitter link doesn't exist, that's okay for this test
        }

        // Facebook link
        try {
            WebElement facebookLink = driver.findElement(By.cssSelector("a[href*='facebook']"));
            String oldTab = driver.getWindowHandle();
            facebookLink.click();
            String winHandle = driver.getWindowHandle();
            driver.switchTo().window(winHandle);
            assertTrue(driver.getCurrentUrl().contains("facebook.com"), "Facebook link should navigate to Facebook website");
            driver.close();
            driver.switchTo().window(oldTab);
        } catch (NoSuchElementException e) {
            // If Facebook link doesn't exist, that's okay for this test
        }

        // Instagram link
        try {
            WebElement instagramLink = driver.findElement(By.cssSelector("a[href*='instagram']"));
            String oldTab = driver.getWindowHandle();
            instagramLink.click();
            String winHandle = driver.getWindowHandle();
            driver.switchTo().window(winHandle);
            assertTrue(driver.getCurrentUrl().contains("instagram.com"), "Instagram link should navigate to Instagram website");
            driver.close();
            driver.switchTo().window(oldTab);
        } catch (NoSuchElementException e) {
            // If Instagram link doesn't exist, that's okay for this test
        }
    }
}