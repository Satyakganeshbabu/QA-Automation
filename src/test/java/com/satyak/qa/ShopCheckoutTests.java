package com.satyak.qa;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.DisplayName;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.satyak.qa.pages.LoginPage;
import com.satyak.qa.pages.InventoryPage;
import com.satyak.qa.pages.CartPage;
import com.satyak.qa.pages.CheckoutPage;

public class ShopCheckoutTests extends BaseTest {
    private LoginPage loginPage() { return new LoginPage(driver, wait); }
    private InventoryPage inventoryPage() { return new InventoryPage(driver, wait); }
    private CartPage cartPage() { return new CartPage(driver, wait); }
    private CheckoutPage checkoutPage() { return new CheckoutPage(driver, wait); }

    private void loginAs(String user) {
        loginPage().login(user, password());
    }

    @Test
    @DisplayName("TC01 standard user can log in")
    void TC01_standardUserLogin() {
        loginAs(standardUser());
        assertTrue(inventoryPage().isLoaded(), "Products page should be displayed after login");
    }

    @Test
    @DisplayName("TC02 locked-out user receives a login error")
    void TC02_lockedOutUser() {
        loginAs(lockedOutUser());
        assertTrue(loginPage().errorText().contains("locked out"), "Locked-out user should see a clear error");
        assertFalse(driver.getCurrentUrl().contains("inventory.html"));
    }

    @Test
    @DisplayName("TC03 price low to high sorts the complete list")
    void TC03_priceLowToHigh() {
        loginAs(standardUser());
        inventoryPage().sortBy("lohi");
        List<Double> prices = inventoryPage().prices();
        assertTrue(prices.size() > 1);
        List<Double> expected = new ArrayList<>(prices);
        expected.sort(Double::compareTo);
        assertEquals(expected, prices, "All prices should be sorted ascending");
    }

    @Test
    @DisplayName("TC04 adding two items and removing one updates badge and cart")
    void TC04_addTwoRemoveOne() {
        loginAs(standardUser());
        InventoryPage inventory = inventoryPage();
        inventory.addProduct("Sauce Labs Backpack");
        inventory.addProduct("Sauce Labs Bike Light");
        assertEquals("2", inventory.cartCount());
        inventory.removeProduct("Sauce Labs Bike Light");
        assertEquals("1", inventory.cartCount());
        inventory.openCart();
        assertEquals(List.of("Sauce Labs Backpack"), cartPage().itemNames());
    }

    @Test
    @DisplayName("TC05 checkout total equals subtotal plus tax and cart is empty")
    void TC05_checkoutTotalAndEmptyCart() {
        loginAs(standardUser());
        InventoryPage inventory = inventoryPage();
        inventory.addProduct("Sauce Labs Backpack");
        inventory.addProduct("Sauce Labs Bike Light");
        inventory.openCart();
        cartPage().checkout();

        CheckoutPage checkout = checkoutPage();
        checkout.fillCustomer("Alex", "Tester", "12345");
        checkout.continueToOverview();

        double subtotal = checkout.itemTotal();
        double tax = checkout.taxTotal();
        double total = checkout.grandTotal();
        assertEquals(Math.round((subtotal + tax) * 100), Math.round(total * 100),
            "Grand total must equal item total plus tax to the cent");

        checkout.finish();
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.cssSelector(".complete-header"))).getText().contains("Thank you"));
        assertEquals("0", inventory.cartCount(), "Cart badge should be empty after successful order");
        inventory.openCart();
        assertTrue(cartPage().itemNames().isEmpty(), "Cart should contain no items after order completion");
    }

    @ParameterizedTest(name = "TC06 checkout required field: {3}")
    @CsvSource(value = {
        "<EMPTY>|Tester|12345|First Name is required",
        "Alex|<EMPTY>|12345|Last Name is required",
        "Alex|Tester|<EMPTY>|Postal Code is required"
    }, delimiter = '|')
    void TC06_missingCheckoutFields(String first, String last, String zip, String error) {
        loginAs(standardUser());
        inventoryPage().addProduct("Sauce Labs Backpack");
        inventoryPage().openCart();
        cartPage().checkout();
        CheckoutPage checkout = checkoutPage();
        checkout.fillCustomer(first.equals("<EMPTY>") ? "" : first,
            last.equals("<EMPTY>") ? "" : last,
            zip.equals("<EMPTY>") ? "" : zip);
        checkout.continueToOverview();
        assertTrue(checkout.errorText().contains(error), "Expected validation: " + error);
    }

    @Test
    @DisplayName("TC07 logged-out user cannot open products page directly by URL")
    void TC07_protectedProductsUrl() {
        loginAs(standardUser());
        driver.findElement(By.id("react-burger-menu-btn")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout_sidebar_link"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-test='login-button']")));
        driver.get(baseUrl() + "inventory.html");
        assertFalse(driver.getCurrentUrl().contains("inventory.html"),
            "Logged-out user must not remain on the protected products page");
        assertTrue(loginPage().isLoginButtonVisible(), "Login page should be displayed");
    }

    @Test
    @DisplayName("TC08 product names sort A to Z and Z to A")
    void TC08_nameSorting() {
        loginAs(standardUser());
        InventoryPage inventory = inventoryPage();
        inventory.sortBy("az");
        List<String> asc = inventory.names();
        List<String> expectedAsc = new ArrayList<>(asc);
        expectedAsc.sort(String.CASE_INSENSITIVE_ORDER);
        assertEquals(expectedAsc, asc);

        inventory.sortBy("za");
        List<String> desc = inventory.names();
        List<String> expectedDesc = new ArrayList<>(desc);
        expectedDesc.sort(String.CASE_INSENSITIVE_ORDER.reversed());
        assertEquals(expectedDesc, desc);
    }

    @Test
    @DisplayName("TC09 price high to low sorts the complete list")
    void TC09_priceHighToLow() {
        loginAs(standardUser());
        inventoryPage().sortBy("hilo");
        List<Double> prices = inventoryPage().prices();
        List<Double> expected = new ArrayList<>(prices);
        expected.sort(Collections.reverseOrder());
        assertEquals(expected, prices);
    }
}
