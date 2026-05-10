package com.enterprise.config;

import com.enterprise.auth.entity.PinCode;
import com.enterprise.auth.entity.TerminalToken;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.repository.PinCodeRepository;
import com.enterprise.auth.repository.TerminalTokenRepository;
import com.enterprise.auth.repository.UserRepository;
import com.enterprise.organization.entity.Company;
import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.entity.Store;
import com.enterprise.organization.entity.Terminal;
import com.enterprise.organization.repository.CompanyRepository;
import com.enterprise.organization.repository.EmployeeRepository;
import com.enterprise.organization.repository.StoreRepository;
import com.enterprise.organization.repository.TerminalRepository;
import com.enterprise.product.entity.ProductCategory;
import com.enterprise.product.entity.ProductItem;
import com.enterprise.product.repository.ProductCategoryRepository;
import com.enterprise.product.repository.ProductItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * @file PosDemoDataSeeder.java
 * @description POS 本地試用資料初始化 / POS local demo data seeder
 * @description_en Seeds deterministic local store, terminal, cashier and PIN data for browser testing
 * @description_zh 初始化固定 UUID 的本地門店、終端、收銀員與 PIN，方便瀏覽器實測
 */
@Component
@Order(10)
@ConditionalOnProperty(name = "pos.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class PosDemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PosDemoDataSeeder.class);

    private static final UUID DEMO_COMPANY_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID DEMO_STORE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID DEMO_TERMINAL_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID DEMO_EMPLOYEE_ID = UUID.fromString("00000000-0000-0000-0000-000000000201");

    private final CompanyRepository companyRepository;
    private final StoreRepository storeRepository;
    private final TerminalRepository terminalRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PinCodeRepository pinCodeRepository;
    private final TerminalTokenRepository terminalTokenRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductItemRepository productItemRepository;
    private final PasswordEncoder passwordEncoder;

    public PosDemoDataSeeder(
            CompanyRepository companyRepository,
            StoreRepository storeRepository,
            TerminalRepository terminalRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository,
            PinCodeRepository pinCodeRepository,
            TerminalTokenRepository terminalTokenRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductItemRepository productItemRepository,
            PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.storeRepository = storeRepository;
        this.terminalRepository = terminalRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.pinCodeRepository = pinCodeRepository;
        this.terminalTokenRepository = terminalTokenRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productItemRepository = productItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Company company = seedCompany();
        Store store = seedStore(company.getId());
        Terminal terminal = seedTerminal(store.getId());
        User cashier = seedCashier();
        seedEmployee(company.getId(), cashier.getId());
        seedPin(cashier.getId());
        seedTerminalToken(store.getId(), terminal.getId());
        seedDemoCatalog();
        log.info("POS demo data initialized. Terminal: DEMO-T-001, PIN: 1234");
    }

    private Company seedCompany() {
        return companyRepository.findById(DEMO_COMPANY_ID)
                .or(() -> companyRepository.findByCode("DEMO-POS"))
                .orElseGet(() -> {
            Company company = new Company();
            company.setId(DEMO_COMPANY_ID);
            company.setCode("DEMO-POS");
            company.setName("Demo POS Company");
            company.setAddress("Taipei City");
            company.setPhone("02-0000-0000");
            company.setEmail("demo-pos@example.local");
            company.setLegalPerson("Demo Owner");
            return companyRepository.save(company);
        });
    }

    private Store seedStore(UUID companyId) {
        return storeRepository.findByStoreCode("XINYI-001").orElseGet(() -> {
            Store store = new Store();
            store.setId(DEMO_STORE_ID);
            store.setCompanyId(companyId);
            store.setStoreCode("XINYI-001");
            store.setName("Xinyi Flagship Store");
            store.setAddress("Demo Road 1");
            store.setCity("Taipei");
            store.setDistrict("Xinyi");
            store.setPhone("02-1234-5678");
            store.setEmail("xinyi@example.local");
            store.setOpeningTime(LocalTime.of(9, 0));
            store.setClosingTime(LocalTime.of(22, 0));
            store.setReceiptHeader("Xinyi Flagship Store");
            store.setReceiptFooter("Thank you for shopping with us.");
            store.setStatus("ACTIVE");
            return storeRepository.save(store);
        });
    }

    private Terminal seedTerminal(UUID storeId) {
        return terminalRepository.findByTerminalCode("DEMO-T-001")
                .or(() -> terminalRepository.findById(DEMO_TERMINAL_ID))
                .orElseGet(() -> {
            Terminal terminal = new Terminal();
            terminal.setId(DEMO_TERMINAL_ID);
            terminal.setStoreId(storeId);
            terminal.setTerminalCode("DEMO-T-001");
            terminal.setName("Demo Terminal 01");
            terminal.setDeviceType("POS");
            terminal.setDeviceModel("Browser Demo Terminal");
            terminal.setAppVersion("local-demo");
            terminal.setStatus("ONLINE");
            terminal.setLastHeartbeatAt(LocalDateTime.now());
            return terminalRepository.save(terminal);
        });
    }

    private User seedCashier() {
        return userRepository.findByUsername("cashier").orElseGet(() -> {
            User user = new User();
            user.setUsername("cashier");
            user.setPasswordHash(passwordEncoder.encode("123456"));
            user.setEmail("cashier@example.local");
            user.setStatus("ACTIVE");
            user.setFailedAttempts(0);
            user.setMfaEnabled(false);
            return userRepository.save(user);
        });
    }

    private void seedEmployee(UUID companyId, UUID userId) {
        employeeRepository.findById(DEMO_EMPLOYEE_ID)
                .or(() -> employeeRepository.findByEmployeeNo("EMP-POS-001"))
                .orElseGet(() -> {
            Employee employee = new Employee();
            employee.setId(DEMO_EMPLOYEE_ID);
            employee.setEmployeeNo("EMP-POS-001");
            employee.setUserId(userId);
            employee.setCompanyId(companyId);
            employee.setName("Demo Cashier");
            employee.setEmail("cashier@example.local");
            employee.setHireDate(LocalDate.now());
            employee.setStatus("ACTIVE");
            return employeeRepository.save(employee);
        });
    }

    private void seedPin(UUID userId) {
        pinCodeRepository.findByUserIdAndTerminalTypeAndActiveTrue(userId, "POS").orElseGet(() -> {
            PinCode pinCode = new PinCode();
            pinCode.setUserId(userId);
            pinCode.setPinHash(passwordEncoder.encode("1234"));
            pinCode.setTerminalType("POS");
            pinCode.setActive(true);
            pinCode.setFailedAttempts(0);
            return pinCodeRepository.save(pinCode);
        });
    }

    private void seedTerminalToken(UUID storeId, UUID terminalId) {
        terminalTokenRepository.findByTerminalIdAndActiveTrue(terminalId).orElseGet(() -> {
            TerminalToken token = new TerminalToken();
            token.setTerminalId(terminalId);
            token.setStoreId(storeId);
            token.setTokenHash(passwordEncoder.encode("local-demo-terminal-token"));
            token.setDeviceFingerprint("local-browser-demo");
            token.setActive(true);
            token.setRegisteredAt(LocalDateTime.now());
            token.setLastActiveAt(LocalDateTime.now());
            return terminalTokenRepository.save(token);
        });
    }

    private void seedDemoCatalog() {
        ProductCategory coffee = seedCategory("咖啡飲品", 10, "#B2C6FF");
        ProductCategory bakery = seedCategory("烘焙點心", 20, "#FFB86B");

        seedProduct("DEMO-LATTE-12OZ", "拿鐵 12oz", coffee.getId(), "120.00", "4710000000011");
        seedProduct("DEMO-AMERICANO-12OZ", "美式咖啡 12oz", coffee.getId(), "90.00", "4710000000012");
        seedProduct("DEMO-OAT-LATTE-12OZ", "燕麥拿鐵 12oz", coffee.getId(), "145.00", "4710000000013");
        seedProduct("DEMO-CROISSANT", "奶油可頌", bakery.getId(), "75.00", "4710000000021");
    }

    private ProductCategory seedCategory(String name, int sortOrder, String displayColor) {
        return productCategoryRepository.findByActiveTrueOrderBySortOrderAsc().stream()
                .filter(category -> name.equals(category.getName()))
                .findFirst()
                .orElseGet(() -> {
                    ProductCategory category = new ProductCategory();
                    category.setName(name);
                    category.setSortOrder(sortOrder);
                    category.setDisplayColor(displayColor);
                    category.setActive(true);
                    return productCategoryRepository.save(category);
                });
    }

    private void seedProduct(String sku, String name, UUID categoryId, String price, String barcode) {
        if (productItemRepository.existsBySku(sku)) {
            return;
        }

        ProductItem item = new ProductItem();
        item.setSku(sku);
        item.setName(name);
        item.setCategoryId(categoryId);
        item.setBasePrice(new BigDecimal(price));
        item.setUnit(ProductItem.UnitType.PCS);
        item.setBarcodePrimary(barcode);
        item.setTrackInventory(false);
        item.setSellable(true);
        item.setWeightBased(false);
        item.setActive(true);
        productItemRepository.save(item);
    }
}
