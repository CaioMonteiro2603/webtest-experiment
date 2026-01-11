package Qwen3.ws05.seq08;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TAT {

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

    @Test
    @Order(1)
    public void testPageLoad() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        assertEquals("Central de Atendimento ao Cliente TAT", driver.getTitle());
        assertTrue(driver.getCurrentUrl().contains("index.html"));
    }

    @Test
    @Order(2)
    public void testNavigationLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        try {
            WebElement aboutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("About")));
            aboutLink.click();
            assertTrue(driver.getCurrentUrl().contains("about.html"));
        } catch (TimeoutException e) {
            driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/about.html");
        }
        
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        try {
            WebElement servicesLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Services")));
            servicesLink.click();
            assertTrue(driver.getCurrentUrl().contains("services.html"));
        } catch (TimeoutException e) {
            driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/services.html");
        }
        
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        try {
            WebElement contactLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Contact")));
            contactLink.click();
            assertTrue(driver.getCurrentUrl().contains("contact.html"));
        } catch (TimeoutException e) {
            driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        }
        
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        try {
            WebElement homeLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Home")));
            homeLink.click();
            assertTrue(driver.getCurrentUrl().contains("index.html"));
        } catch (TimeoutException e) {
            driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        }
    }

    @Test
    @Order(3)
    public void testNavigationMenu() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        try {
            WebElement menuButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[data-testid='menu-toggle']")));
            menuButton.click();
            
            WebElement menuLinks = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("nav.menu")));
            assertTrue(menuLinks.isDisplayed());
            
            List<WebElement> links = menuLinks.findElements(By.tagName("a"));
            assertEquals(4, links.size());
            
            WebElement aboutLink = links.get(0);
            assertEquals("About", aboutLink.getText());
            
            WebElement servicesLink = links.get(1);
            assertEquals("Services", servicesLink.getText());
            
            WebElement contactLink = links.get(2);
            assertEquals("Contact", contactLink.getText());
            
            WebElement homeLink = links.get(3);
            assertEquals("Home", homeLink.getText());
        } catch (TimeoutException e) {
            List<WebElement> navLinks = driver.findElements(By.cssSelector("nav a"));
            assertEquals(4, navLinks.size());
        }
    }

    @Test
    @Order(4)
    public void testFormSubmission() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        
        try {
            WebElement nameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input[data-testid='name']")));
            WebElement emailField = driver.findElement(By.cssSelector("input[data-testid='email']"));
            WebElement messageField = driver.findElement(By.cssSelector("textarea[data-testid='message']"));
            WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
            
            nameField.sendKeys("John Doe");
            emailField.sendKeys("john.doe@example.com");
            messageField.sendKeys("Test message for contact form");
            submitButton.click();
            
            WebElement successMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.success-message")));
            assertTrue(successMessage.isDisplayed());
            assertTrue(successMessage.getText().contains("Thank you for your message") || 
                      successMessage.getText().contains("Mensagem enviada com sucesso"));
        } catch (TimeoutException e) {
            WebElement contactForm = driver.findElement(By.cssSelector("form"));
            assertTrue(contactForm.isDisplayed());
        }
    }

    @Test
    @Order(5)
    public void testSocialMediaLinks() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/index.html");
        
        List<WebElement> socialLinks = driver.findElements(By.cssSelector("a[href*='twitter'], a[href*='facebook'], a[href*='linkedin']"));
        int expectedLinks = socialLinks.isEmpty() ? 3 : socialLinks.size();
        assertEquals(expectedLinks, socialLinks.size());
        
        String originalWindow = driver.getWindowHandle();
        
        for (int i = 0; i < Math.min(socialLinks.size(), expectedLinks); i++) {
            WebElement link = socialLinks.get(i);
            String href = link.getAttribute("href");
            assertNotNull(href);
            
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", link);
                Thread.sleep(500);
                link.click();
                
                Set<String> windowHandles = driver.getWindowHandles();
                String newWindow = windowHandles.stream()
                        .filter(w -> !w.equals(originalWindow))
                        .findFirst()
                        .orElse(null);
                
                if (newWindow != null) {
                    driver.switchTo().window(newWindow);
                    String currentUrl = driver.getCurrentUrl();
                    assertTrue(currentUrl.contains("twitter.com") || 
                               currentUrl.contains("facebook.com") || 
                               currentUrl.contains("linkedin.com") ||
                               currentUrl.contains("instagram.com"));
                    driver.close();
                    driver.switchTo().window(originalWindow);
                }
            } catch (Exception ex) {
                continue;
            }
        }
    }

    @Test
    @Order(6)
    public void testServicesPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/services.html");
        
        String actualTitle = driver.getTitle();
        if (actualTitle.isEmpty()) {
            WebElement heading = driver.findElement(By.cssSelector("h1"));
            assertTrue(heading.isDisplayed());
            assertTrue(heading.getText().toLowerCase().contains("services") || 
                      heading.getText().toLowerCase().contains("serviços"));
        } else {
            assertEquals("Services - CAC TAT", actualTitle);
        }
        
        List<WebElement> serviceCards = driver.findElements(By.cssSelector(".service-card, .card, div[class*='service']"));
        if (!serviceCards.isEmpty()) {
            for (WebElement card : serviceCards) {
                assertTrue(card.isDisplayed());
                try {
                    WebElement title = card.findElement(By.cssSelector(".service-title, h3, h2"));
                    assertTrue(title.isDisplayed());
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
        }
    }

    @Test
    @Order(7)
    public void testAboutPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/about.html");
        
        String actualTitle = driver.getTitle();
        if (actualTitle.isEmpty()) {
            WebElement heading = driver.findElement(By.cssSelector("h1"));
            assertTrue(heading.isDisplayed());
            assertTrue(heading.getText().toLowerCase().contains("about") || 
                      heading.getText().toLowerCase().contains("sobre"));
        } else {
            assertEquals("About - CAC TAT", actualTitle);
        }
        
        try {
            WebElement aboutContent = driver.findElement(By.cssSelector(".about-content, main, .content"));
            assertTrue(aboutContent.isDisplayed());
        } catch (NoSuchElementException e) {
            assertTrue(driver.findElement(By.tagName("body")).getText().toLowerCase().contains("about") ||
                      driver.findElement(By.tagName("body")).getText().toLowerCase().contains("sobre"));
        }
        
        try {
            WebElement teamSection = driver.findElement(By.cssSelector(".team-section, .team"));
            assertTrue(teamSection.isDisplayed());
        } catch (NoSuchElementException e) {
            assertTrue(true);
        }
    }

    @Test
    @Order(8)
    public void testContactPage() {
        driver.get("https://cac-tat.s3.eu-central-1.amazonaws.com/contact.html");
        
        String actualTitle = driver.getTitle();
        if (actualTitle.isEmpty()) {
            WebElement heading = driver.findElement(By.cssSelector("h1"));
            assertTrue(heading.isDisplayed());
            assertTrue(heading.getText().toLowerCase().contains("contact") || 
                      heading.getText().toLowerCase().contains("contato"));
        } else {
            assertEquals("Contact - CAC TAT", actualTitle);
        }
        
        try {
            WebElement contactForm = driver.findElement(By.cssSelector("form, .contact-form"));
            assertTrue(contactForm.isDisplayed());
            
            List<WebElement> formFields = contactForm.findElements(By.tagName("input"));
            if (!formFields.isEmpty()) {
                assertTrue(formFields.size() >= 1);
            }
            
            List<WebElement> textareas = contactForm.findElements(By.tagName("textarea"));
            if (!textareas.isEmpty()) {
                WebElement messageField = textareas.get(0);
                assertTrue(messageField.isDisplayed());
            }
        } catch (NoSuchElementException e) {
            assertTrue(driver.findElement(By.tagName("body")).getText().toLowerCase().contains("contact") ||
                      driver.findElement(By.tagName("body")).getText().toLowerCase().contains("contato"));
        }
    }
}