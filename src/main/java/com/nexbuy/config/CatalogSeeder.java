package com.nexbuy.config;

import com.nexbuy.entity.Category;
import com.nexbuy.entity.Product;
import com.nexbuy.patterns.builder.CategoryBuilder;
import com.nexbuy.patterns.builder.ProductBuilder;
import com.nexbuy.repository.CategoryRepository;
import com.nexbuy.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Seeds 5 categories and 25 sample products on first start (skipped when products already exist). */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        Map<String, Category> categories = seedCategories();
        List<Product> products = new ArrayList<>();

        add(products, categories.get("electronics"), "Wireless Noise-Cancelling Headphones", "129.99", 40,
                "Over-ear Bluetooth headphones with active noise cancellation and 30-hour battery life.");
        add(products, categories.get("electronics"), "4K Action Camera", "199.00", 25,
                "Waterproof 4K camera with image stabilization and a wide-angle lens.");
        add(products, categories.get("electronics"), "Smart Watch Series X", "149.50", 60,
                "Fitness tracking, heart-rate monitor, GPS, and a bright always-on display.");
        add(products, categories.get("electronics"), "Portable Bluetooth Speaker", "59.99", 80,
                "Compact speaker with deep bass, 12-hour playtime, and splash resistance.");
        add(products, categories.get("electronics"), "Mechanical Gaming Keyboard", "89.00", 35,
                "Hot-swappable mechanical keyboard with per-key RGB lighting.");

        add(products, categories.get("fashion"), "Classic Denim Jacket", "74.90", 30,
                "Timeless medium-wash denim jacket with a comfortable regular fit.");
        add(products, categories.get("fashion"), "Slim Fit Cotton Shirt", "34.99", 90,
                "Breathable 100% cotton shirt that works for the office or the weekend.");
        add(products, categories.get("fashion"), "Leather Casual Sneakers", "89.00", 45,
                "Low-top sneakers in soft leather with a cushioned insole.");
        add(products, categories.get("fashion"), "Wool Blend Scarf", "24.50", 70,
                "Warm, lightweight scarf in a soft wool blend.");
        add(products, categories.get("fashion"), "Canvas Backpack", "49.99", 55,
                "Durable canvas backpack with a padded laptop sleeve and two side pockets.");

        add(products, categories.get("home-kitchen"), "Non-Stick Cookware Set", "119.00", 20,
                "10-piece non-stick cookware set that works on gas, electric, and induction.");
        add(products, categories.get("home-kitchen"), "Automatic Drip Coffee Maker", "69.95", 38,
                "12-cup coffee maker with a programmable timer and reusable filter.");
        add(products, categories.get("home-kitchen"), "Memory Foam Pillow", "39.99", 100,
                "Contoured memory foam pillow with a washable, breathable cover.");
        add(products, categories.get("home-kitchen"), "Stainless Steel Water Bottle", "22.00", 150,
                "Vacuum-insulated bottle that keeps drinks cold for 24 hours or hot for 12.");
        add(products, categories.get("home-kitchen"), "LED Desk Lamp", "29.99", 65,
                "Dimmable LED lamp with adjustable color temperature and a USB charging port.");

        add(products, categories.get("books"), "Mastering Java Fundamentals", "42.50", 50,
                "A practical guide to core Java, from syntax to collections and streams.");
        add(products, categories.get("books"), "Angular in Practice", "38.00", 42,
                "Build real-world single-page applications with components, routing, and forms.");
        add(products, categories.get("books"), "Spring Boot Cookbook", "44.99", 36,
                "Recipes for REST APIs, security, and data access with Spring Boot.");
        add(products, categories.get("books"), "Design Patterns Explained", "36.75", 48,
                "Clear examples of the classic object-oriented design patterns.");
        add(products, categories.get("books"), "The Startup Playbook", "19.99", 75,
                "Lessons on validating ideas, building a team, and launching a first product.");

        add(products, categories.get("sports-outdoors"), "Yoga Mat Pro", "29.99", 85,
                "Extra-thick non-slip yoga mat with a carrying strap.");
        add(products, categories.get("sports-outdoors"), "Adjustable Dumbbell Set", "149.00", 15,
                "Space-saving adjustable dumbbells from 5 to 25 kg per hand.");
        add(products, categories.get("sports-outdoors"), "Running Shoes Air Lite", "94.99", 52,
                "Lightweight running shoes with responsive cushioning and a breathable mesh upper.");
        add(products, categories.get("sports-outdoors"), "Insulated Hiking Backpack", "79.50", 28,
                "35-litre hiking pack with a rain cover and ventilated back panel.");
        add(products, categories.get("sports-outdoors"), "Cycling Helmet", "54.00", 0,
                "Ventilated road helmet with an adjustable fit system. Currently out of stock.");

        productRepository.saveAll(products);
        log.info("Seeded {} categories and {} products", categories.size(), products.size());
    }

    private Map<String, Category> seedCategories() {
        Map<String, Category> result = new HashMap<>();
        for (String[] def : new String[][]{
                {"Electronics", "Headphones, cameras, wearables, and everyday gadgets."},
                {"Fashion", "Clothing, shoes, and accessories for every season."},
                {"Home & Kitchen", "Cookware, appliances, and essentials for your home."},
                {"Books", "Technical guides and popular reads."},
                {"Sports & Outdoors", "Gear for training, hiking, and cycling."}
        }) {
            String slug = slugify(def[0]);
            Category category = categoryRepository.findBySlug(slug)
                    .orElseGet(() -> categoryRepository.save(new CategoryBuilder()
                            .name(def[0])
                            .slug(slug)
                            .description(def[1])
                            .build()));
            result.put(slug, category);
        }
        return result;
    }

    private void add(List<Product> target, Category category, String name, String price, int stock, String description) {
        String slug = slugify(name);
        target.add(new ProductBuilder()
                .name(name)
                .slug(slug)
                .price(new BigDecimal(price))
                .stock(stock)
                .description(description)
                .image("https://picsum.photos/seed/" + slug + "/600/450")
                .image("https://picsum.photos/seed/" + slug + "-alt/600/450")
                .category(category)
                .build());
    }

    private static String slugify(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
    }
}
