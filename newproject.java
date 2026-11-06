package test;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Set;

public class newproject {
    public static void main(String[] args) throws Exception {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // ---------- Locators ----------
        // "Tea" link in the menu
        By teaCategory = By.xpath("(//a[normalize-space(.)='Tea' and contains(@href,'/tea')])[1]");
        // Sub-category tile on the Tea page (Black Tea)
        By subCategory = By.xpath("(//a[contains(@href,'Black-Tea')])[last()]");
        // Price on the product page: ignores the header cart total (৳0)
        By priceLocator = By.xpath("(//*[contains(text(),'৳') and not(ancestor::header) "
                + "and not(contains(text(),'৳0') and string-length(normalize-space(.))<5)])[1]");

        // JS: finds a product link = the link+image block that sits next to a non-zero price (outside header)
        final String findProductJs =
                "var els = Array.from(document.querySelectorAll('body *')).filter(function(e){"
              + "  if (e.closest('header') || e.closest('footer') || e.tagName=='SCRIPT' || e.tagName=='STYLE') return false;"
              + "  return Array.from(e.childNodes).some(function(n){"
              + "    return n.nodeType==3 && n.textContent.indexOf('\u09F3')>=0 && !/^\\s*\u09F3\\s*0\\s*$/.test(n.textContent);});"
              + "});"
              + "for (var i=0;i<els.length;i++){"
              + "  var el = els[i];"
              + "  for (var k=0;k<8 && el;k++){"
              + "    el = el.parentElement; if(!el) break;"
              + "    var img = el.querySelector('a[href] img');"
              + "    var a = (el.tagName=='A' && el.querySelector('img')) ? el : (img ? img.closest('a') : null);"
              + "    if (a && a.getAttribute('href') && a.getAttribute('href')!='/') return a;"
              + "  }"
              + "}"
              + "return null;";

        try {
            // ---------- Step 260 ----------
            Thread.sleep(1000); // wait 1 sec before step
            System.out.println("Step 260: Opening Shwapno website...");
            driver.get("https://www.shwapno.com/");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("body")));

            // Close promotional overlay if it appears
            try {
                WebElement closeBtn = new WebDriverWait(driver, Duration.ofSeconds(4)).until(
                        ExpectedConditions.elementToBeClickable(By.xpath(
                                "//button[contains(@class,'close') or contains(@aria-label,'lose')] "
                                + "| //span[contains(@class,'close')]")));
                closeBtn.click();
            } catch (TimeoutException ignored) {
                // no popup
            }

            // ---------- Step 261 ----------
            Thread.sleep(1000); // wait 1 sec before step
            System.out.println("Step 261: Navigating to the tea category...");
            try {
                WebElement teaLink = wait.until(ExpectedConditions.presenceOfElementLocated(teaCategory));
                js.executeScript("arguments[0].click();", teaLink);
            } catch (TimeoutException e) {
                driver.get("https://www.shwapno.com/tea");
            }
            wait.until(ExpectedConditions.urlContains("tea"));
            System.out.println("Current URL: " + driver.getCurrentUrl());

            // ---------- Step 262 ----------
            Thread.sleep(1000); // wait 1 sec before step
            System.out.println("Step 262: Verifying product listing display...");
            WebElement firstProduct = null;
            try {
                // Products directly on the Tea page?
                firstProduct = new WebDriverWait(driver, Duration.ofSeconds(6)).until(
                        d -> (WebElement) ((JavascriptExecutor) d).executeScript(findProductJs));
            } catch (TimeoutException e) {
                // The Tea page only shows sub-category tiles -> open Black Tea to reach the products
                System.out.println("No products on Tea page, opening Black Tea sub-category...");
                WebElement sub = wait.until(ExpectedConditions.presenceOfElementLocated(subCategory));
                js.executeScript("arguments[0].click();", sub);
                firstProduct = wait.until(
                        d -> (WebElement) ((JavascriptExecutor) d).executeScript(findProductJs));
            }
            boolean isListingDisplayed = firstProduct != null;
            System.out.println("Is product listing displayed? " + isListingDisplayed);
            if (!isListingDisplayed) {
                throw new AssertionError("Product listing is not displayed");
            }

            String listingUrl = driver.getCurrentUrl();
            String originalWindow = driver.getWindowHandle();
            System.out.println("Listing URL: " + listingUrl);

            // ---------- Step 263 ----------
            Thread.sleep(1000); // wait 1 sec before step
            System.out.println("Step 263: Opening a tea product...");
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", firstProduct);
            js.executeScript("arguments[0].click();", firstProduct);

            try {
                new WebDriverWait(driver, Duration.ofSeconds(10)).until(d ->
                        d.getWindowHandles().size() > 1 || !d.getCurrentUrl().equals(listingUrl));
            } catch (TimeoutException e) {
                throw new AssertionError("Product page did not open");
            }

            Set<String> allWindows = driver.getWindowHandles();
            if (allWindows.size() > 1) {
                for (String handle : allWindows) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
            }
            System.out.println("Product page: " + driver.getCurrentUrl());

            // ---------- Step 264 ----------
            Thread.sleep(1000); // wait 1 sec before step
            System.out.println("Step 264: Verifying product price display...");
            WebElement productPrice = wait.until(ExpectedConditions.visibilityOfElementLocated(priceLocator));
            boolean isPriceDisplayed = productPrice.isDisplayed();
            System.out.println("Is product price displayed? " + isPriceDisplayed);
            if (!isPriceDisplayed) {
                throw new AssertionError("Product price is not displayed");
            }

            // ---------- Step 265 ----------
            Thread.sleep(1000); // wait 1 sec before step
            System.out.println("Step 265: Printing product name...");
            String finalProductName = "";

            for (WebElement h : driver.findElements(By.xpath("//h1"))) {
                String text = h.getText().trim();
                if (!text.isEmpty()) {
                    finalProductName = text;
                    break;
                }
            }
            if (finalProductName.isEmpty()) {
                for (WebElement h : driver.findElements(By.xpath("//h2 | //h3"))) {
                    String text = h.getText().trim();
                    if (text.length() > 5 && !text.equalsIgnoreCase("Food") && !text.equalsIgnoreCase("Tea")) {
                        finalProductName = text;
                        break;
                    }
                }
            }
            if (finalProductName.isEmpty()) {
                finalProductName = driver.getTitle().replaceAll("\\|.*$", "").trim();
            }

            System.out.println("----------------------------------------");
            System.out.println("Product Name : " + finalProductName);
            System.out.println("Product Price: " + productPrice.getText().trim());
            System.out.println("----------------------------------------");

            // ---------- Step 266 ----------
            Thread.sleep(1000); // wait 1 sec before step
            System.out.println("Step 266: Navigating back to product listing...");
            if (driver.getWindowHandles().size() > 1) {
                driver.close();
                driver.switchTo().window(originalWindow);
            } else {
                driver.navigate().back();
            }

            wait.until(d -> (WebElement) ((JavascriptExecutor) d).executeScript(findProductJs));
            System.out.println("Successfully returned to product listing page: " + driver.getCurrentUrl());
            System.out.println("TEST PASSED");

        } catch (Exception e) {
            System.out.println("TEST FAILED: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("Closing browser...");
            Thread.sleep(2000);
            driver.quit();
        }
    }
}
