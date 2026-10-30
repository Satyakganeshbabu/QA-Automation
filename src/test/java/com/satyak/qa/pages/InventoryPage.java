package com.satyak.qa.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import java.util.ArrayList;
import org.openqa.selenium.support.ui.WebDriverWait;

public class InventoryPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By title = By.cssSelector(".title");
    private final By items = By.cssSelector(".inventory_item");
    private final By sort = By.cssSelector("[data-test='product-sort-container']");
    private final By badge = By.cssSelector(".shopping_cart_badge");
    private final By cartLink = By.cssSelector(".shopping_cart_link");
    private final By loginButton = By.cssSelector("[data-test='login-button']");

    public InventoryPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public boolean isLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(title));
        return driver.getCurrentUrl().contains("inventory.html")
            && driver.findElement(title).getText().equals("Products");
    }

    public void sortBy(String value) {
        List<String> before = names();
        new Select(wait.until(ExpectedConditions.elementToBeClickable(sort))).selectByValue(value);
        wait.until(d -> {
            List<String> current = names();
            if (current.size() != before.size() || current.isEmpty()) return false;
            if (!new Select(d.findElement(sort)).getFirstSelectedOption().getAttribute("value").equals(value)) return false;
            List<String> expectedNames = new ArrayList<>(current);
            List<Double> expectedPrices = new ArrayList<>(prices());
            switch (value) {
                case "az" -> expectedNames.sort(String.CASE_INSENSITIVE_ORDER);
                case "za" -> expectedNames.sort(String.CASE_INSENSITIVE_ORDER.reversed());
                case "lohi" -> expectedPrices.sort(Double::compareTo);
                case "hilo" -> expectedPrices.sort(java.util.Collections.reverseOrder());
                default -> throw new IllegalArgumentException("Unknown sort: " + value);
            }
            return switch (value) {
                case "az", "za" -> expectedNames.equals(current);
                default -> expectedPrices.equals(prices());
            };
        });
    }

    public List<String> names() {
        return driver.findElements(items).stream()
            .map(item -> item.findElement(By.cssSelector(".inventory_item_name")).getText()).toList();
    }

    public List<Double> prices() {
        return driver.findElements(items).stream()
            .map(item -> Double.parseDouble(item.findElement(By.cssSelector(".inventory_item_price"))
                .getText().replace("$", "").trim())).toList();
    }

    private WebElement product(String name) {
        return driver.findElements(items).stream()
            .filter(item -> item.findElement(By.cssSelector(".inventory_item_name")).getText().equals(name))
            .findFirst().orElseThrow(() -> new AssertionError("Product not found: " + name));
    }

    public void addProduct(String name) {
        WebElement button = product(name).findElement(By.cssSelector("button"));
        if (!button.getText().equalsIgnoreCase("Add to cart"))
            throw new AssertionError("Expected Add to cart button for " + name);
        button.click();
        wait.until(d -> product(name).findElement(By.cssSelector("button")).getText().equalsIgnoreCase("Remove"));
    }

    public void removeProduct(String name) {
        WebElement button = product(name).findElement(By.cssSelector("button"));
        if (!button.getText().equalsIgnoreCase("Remove"))
            throw new AssertionError("Expected Remove button for " + name);
        button.click();
        wait.until(d -> product(name).findElement(By.cssSelector("button")).getText().equalsIgnoreCase("Add to cart"));
    }

    public String cartCount() {
        List<WebElement> badges = driver.findElements(badge);
        return badges.isEmpty() ? "0" : badges.get(0).getText();
    }

    public void openCart() {
        wait.until(ExpectedConditions.elementToBeClickable(cartLink)).click();
    }

    public boolean loginButtonVisible() {
        return !driver.findElements(loginButton).isEmpty() && driver.findElement(loginButton).isDisplayed();
    }
}
