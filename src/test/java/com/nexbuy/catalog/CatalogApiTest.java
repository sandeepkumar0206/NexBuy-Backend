package com.nexbuy.catalog;

import com.nexbuy.entity.Category;
import com.nexbuy.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Runs the catalog endpoints end to end (seeder, security, JSON) against an in-memory H2 database. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:catalogtest;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class CatalogApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void listsFiveCategoriesAlphabetically() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[*].name", contains(
                        "Books", "Electronics", "Fashion", "Home & Kitchen", "Sports & Outdoors")));
    }

    @Test
    void returnsCategoryBySlugAndNotFoundForUnknown() throws Exception {
        mockMvc.perform(get("/api/categories/home-kitchen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Home & Kitchen")));

        mockMvc.perform(get("/api/categories/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Category not found")));
    }

    @Test
    void firstPageUsesDefaultSizeAndNewestFirst() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(12)))
                .andExpect(jsonPath("$.page", is(0)))
                .andExpect(jsonPath("$.size", is(12)))
                .andExpect(jsonPath("$.totalElements", is(25)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.first", is(true)))
                .andExpect(jsonPath("$.last", is(false)))
                .andExpect(jsonPath("$.content[0].name", is("Cycling Helmet")))
                .andExpect(jsonPath("$.content[0].category.slug", is("sports-outdoors")))
                .andExpect(jsonPath("$.content[0].images", hasSize(2)));
    }

    @Test
    void featuredReturnsEightInStockProductsNewestFirst() throws Exception {
        mockMvc.perform(get("/api/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].name", is("Insulated Hiking Backpack")))
                .andExpect(jsonPath("$[*].name", not(hasItem("Cycling Helmet"))))
                .andExpect(jsonPath("$[*].inStock", everyItem(is(true))));
    }

    @Test
    void lastPageHoldsTheRemainingProduct() throws Exception {
        mockMvc.perform(get("/api/products").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    void sizeIsCappedAtTheMaximum() throws Exception {
        mockMvc.perform(get("/api/products").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(50)))
                .andExpect(jsonPath("$.content", hasSize(25)));
    }

    @Test
    void sortsByPriceAndName() throws Exception {
        mockMvc.perform(get("/api/products").param("sort", "price_asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("The Startup Playbook")));

        mockMvc.perform(get("/api/products").param("sort", "price_desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("4K Action Camera")));

        mockMvc.perform(get("/api/products").param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("4K Action Camera")));
    }

    @Test
    void rejectsUnknownSort() throws Exception {
        mockMvc.perform(get("/api/products").param("sort", "cheapest"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(
                        "Unsupported sort 'cheapest'. Use one of: newest, price_asc, price_desc, name")));
    }

    @Test
    void filtersByCategoryId() throws Exception {
        Category electronics = categoryRepository.findBySlug("electronics").orElseThrow();

        mockMvc.perform(get("/api/products").param("categoryId", String.valueOf(electronics.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(5)));
    }

    @Test
    void filtersByPriceRange() throws Exception {
        mockMvc.perform(get("/api/products").param("minPrice", "100").param("maxPrice", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(4)));
    }

    @Test
    void rejectsInvalidPriceRanges() throws Exception {
        mockMvc.perform(get("/api/products").param("minPrice", "200").param("maxPrice", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("minPrice cannot be greater than maxPrice")));

        mockMvc.perform(get("/api/products").param("minPrice", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/products").param("minPrice", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'minPrice'")));
    }

    @Test
    void listsProductsByCategorySlug() throws Exception {
        mockMvc.perform(get("/api/products/category/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(5)))
                .andExpect(jsonPath("$.content[*].category.slug", contains("books", "books", "books", "books", "books")));

        mockMvc.perform(get("/api/products/category/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Category not found")));
    }

    @Test
    void searchesByNameIgnoringCase() throws Exception {
        mockMvc.perform(get("/api/products/search").param("q", "HEADPHONES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Wireless Noise-Cancelling Headphones")));
    }

    @Test
    void searchTreatsWildcardsAsPlainText() throws Exception {
        // Only the cotton shirt's description contains a literal "%" ("100% cotton"); an unescaped % would match all 25.
        mockMvc.perform(get("/api/products/search").param("q", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Slim Fit Cotton Shirt")));

        // No product contains an underscore; an unescaped _ would match every product.
        mockMvc.perform(get("/api/products/search").param("q", "_"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    void searchRequiresAQuery() throws Exception {
        mockMvc.perform(get("/api/products/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Missing required parameter 'q'")));

        mockMvc.perform(get("/api/products/search").param("q", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Search query is required")));
    }

    @Test
    void returnsOneProductWithPricingAndStock() throws Exception {
        mockMvc.perform(get("/api/products/search").param("q", "helmet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].inStock", is(false)))
                .andExpect(jsonPath("$.content[0].stock", is(0)))
                .andExpect(jsonPath("$.content[0].discountedPrice", is(54.0)));

        String id = mockMvc.perform(get("/api/products/search").param("q", "yoga"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replaceAll("(?s).*?\"id\":(\\d+).*", "$1");

        mockMvc.perform(get("/api/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Yoga Mat Pro")))
                .andExpect(jsonPath("$.price", is(29.99)))
                .andExpect(jsonPath("$.inStock", is(true)))
                .andExpect(jsonPath("$.description", notNullValue()));
    }

    @Test
    void missingOrMalformedProductIdFailsCleanly() throws Exception {
        mockMvc.perform(get("/api/products/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Product not found")));

        mockMvc.perform(get("/api/products/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Invalid value for parameter 'id'")));
    }

    @Test
    void catalogReadsStayPublicEvenWithAnExpiredToken() throws Exception {
        mockMvc.perform(get("/api/products").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThan(0)));
    }

    @Test
    void writesStillRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
