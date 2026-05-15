package com.enterprise.config;

import com.enterprise.auth.entity.PinCode;
import com.enterprise.auth.entity.Role;
import com.enterprise.auth.entity.TerminalToken;
import com.enterprise.auth.entity.User;
import com.enterprise.auth.entity.UserRole;
import com.enterprise.auth.repository.PinCodeRepository;
import com.enterprise.auth.repository.RoleRepository;
import com.enterprise.auth.repository.TerminalTokenRepository;
import com.enterprise.auth.repository.UserRoleRepository;
import com.enterprise.auth.repository.UserRepository;
import com.enterprise.organization.entity.Company;
import com.enterprise.organization.entity.Employee;
import com.enterprise.organization.entity.Store;
import com.enterprise.organization.entity.StoreEmployee;
import com.enterprise.organization.entity.Terminal;
import com.enterprise.organization.repository.CompanyRepository;
import com.enterprise.organization.repository.EmployeeRepository;
import com.enterprise.organization.repository.StoreEmployeeRepository;
import com.enterprise.organization.repository.StoreRepository;
import com.enterprise.organization.repository.TerminalRepository;
import com.enterprise.inventory.entity.StoreStock;
import com.enterprise.inventory.repository.StoreStockRepository;
import com.enterprise.product.entity.ProductCategory;
import com.enterprise.product.entity.ProductItem;
import com.enterprise.product.repository.ProductCategoryRepository;
import com.enterprise.product.repository.ProductItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PinCodeRepository pinCodeRepository;
    private final TerminalTokenRepository terminalTokenRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductItemRepository productItemRepository;
    private final StoreStockRepository storeStockRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public PosDemoDataSeeder(
            CompanyRepository companyRepository,
            StoreRepository storeRepository,
            TerminalRepository terminalRepository,
            EmployeeRepository employeeRepository,
            StoreEmployeeRepository storeEmployeeRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PinCodeRepository pinCodeRepository,
            TerminalTokenRepository terminalTokenRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductItemRepository productItemRepository,
            StoreStockRepository storeStockRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder) {
        this.companyRepository = companyRepository;
        this.storeRepository = storeRepository;
        this.terminalRepository = terminalRepository;
        this.employeeRepository = employeeRepository;
        this.storeEmployeeRepository = storeEmployeeRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.pinCodeRepository = pinCodeRepository;
        this.terminalTokenRepository = terminalTokenRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productItemRepository = productItemRepository;
        this.storeStockRepository = storeStockRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Company company = seedCompany();
        Store store = seedStore(company.getId());
        Terminal terminal = seedTerminal(store.getId());
        User cashier = seedCashier();
        seedDemoOperatorRoles(cashier.getId());
        Employee employee = seedEmployee(company.getId(), cashier.getId());
        seedStoreEmployee(store.getId(), employee.getId());
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
        releaseStoreCode();
        ensureDemoStoreRow(companyId);
        return storeRepository.findById(DEMO_STORE_ID)
                .map(store -> repairDemoStore(store, companyId))
                .orElseThrow(() -> new IllegalStateException("POS demo store was not created"));
    }

    private Terminal seedTerminal(UUID storeId) {
        releaseTerminalCode();
        ensureDemoTerminalRow(storeId);
        return terminalRepository.findById(DEMO_TERMINAL_ID)
                .map(terminal -> repairDemoTerminal(terminal, storeId))
                .orElseThrow(() -> new IllegalStateException("POS demo terminal was not created"));
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

    private void seedDemoOperatorRoles(UUID userId) {
        seedUserRole(userId, "CASHIER");
        seedUserRole(userId, "STORE_MANAGER");
    }

    private void seedUserRole(UUID userId, String roleCode) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException(roleCode + " role is required for POS demo data"));
        if (!userRoleRepository.existsByUserIdAndRoleId(userId, role.getId())) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRoleRepository.save(userRole);
        }
    }

    private Employee seedEmployee(UUID companyId, UUID userId) {
        releaseEmployeeNo(userId);
        ensureDemoEmployeeRow(companyId, userId);
        return employeeRepository.findById(DEMO_EMPLOYEE_ID)
                .map(employee -> repairDemoEmployee(employee, companyId, userId))
                .orElseThrow(() -> new IllegalStateException("POS demo employee was not created"));
    }

    private void releaseStoreCode() {
        storeRepository.findByStoreCode("XINYI-001")
                .filter(store -> !DEMO_STORE_ID.equals(store.getId()))
                .ifPresent(store -> {
                    store.setStoreCode(legacyCode("XINYI-001", store.getId()));
                    storeRepository.saveAndFlush(store);
                });
    }

    private void releaseTerminalCode() {
        terminalRepository.findByTerminalCode("DEMO-T-001")
                .filter(terminal -> !DEMO_TERMINAL_ID.equals(terminal.getId()))
                .ifPresent(terminal -> {
                    terminal.setTerminalCode(legacyCode("DEMO-T-001", terminal.getId()));
                    terminalRepository.saveAndFlush(terminal);
                });
    }

    private void releaseEmployeeNo(UUID userId) {
        jdbcTemplate.update("""
                update org_employees
                set user_id = null,
                    status = 'RESIGNED',
                    updated_at = now()
                where user_id = ?
                  and id <> ?
                """, userId, DEMO_EMPLOYEE_ID);
        employeeRepository.findByEmployeeNo("EMP-POS-001")
                .filter(employee -> !DEMO_EMPLOYEE_ID.equals(employee.getId()))
                .ifPresent(employee -> {
                    employee.setEmployeeNo(legacyCode("EMP-POS-001", employee.getId()));
                    employee.setUserId(null);
                    employee.setStatus("RESIGNED");
                    employeeRepository.saveAndFlush(employee);
                });
    }

    private void ensureDemoStoreRow(UUID companyId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "select exists(select 1 from org_stores where id = ?)",
                Boolean.class,
                DEMO_STORE_ID);
        if (Boolean.TRUE.equals(exists)) {
            return;
        }

        jdbcTemplate.update("""
                insert into org_stores (
                    id, company_id, store_code, name, address, city, district, phone, email,
                    opening_time, closing_time, receipt_header, receipt_footer, status,
                    timezone, currency, tax_rate, created_at, updated_at
                )
                values (?, ?, 'XINYI-001', 'Xinyi Flagship Store', 'Demo Road 1',
                    'Taipei', 'Xinyi', '02-1234-5678', 'xinyi@example.local',
                    '09:00:00', '22:00:00', 'Xinyi Flagship Store',
                    'Thank you for shopping with us.', 'ACTIVE',
                    'Asia/Taipei', 'TWD', 0.05, now(), now())
                """, DEMO_STORE_ID, companyId);
    }

    private void ensureDemoTerminalRow(UUID storeId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "select exists(select 1 from org_terminals where id = ?)",
                Boolean.class,
                DEMO_TERMINAL_ID);
        if (Boolean.TRUE.equals(exists)) {
            return;
        }

        jdbcTemplate.update("""
                insert into org_terminals (
                    id, store_id, terminal_code, name, device_type, device_model,
                    app_version, status, registered_at, last_heartbeat_at, created_at, updated_at
                )
                values (?, ?, 'DEMO-T-001', 'Demo Terminal 01', 'POS',
                    'Browser Demo Terminal', 'local-demo', 'ONLINE',
                    now(), now(), now(), now())
                """, DEMO_TERMINAL_ID, storeId);
    }

    private void ensureDemoEmployeeRow(UUID companyId, UUID userId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "select exists(select 1 from org_employees where id = ?)",
                Boolean.class,
                DEMO_EMPLOYEE_ID);
        if (Boolean.TRUE.equals(exists)) {
            return;
        }

        jdbcTemplate.update("""
                insert into org_employees (
                    id, employee_no, user_id, company_id, name, email, hire_date,
                    status, created_at, updated_at
                )
                values (?, 'EMP-POS-001', ?, ?, 'Demo Cashier',
                    'cashier@example.local', current_date, 'ACTIVE', now(), now())
                """, DEMO_EMPLOYEE_ID, userId, companyId);
    }

    private void seedStoreEmployee(UUID storeId, UUID employeeId) {
        storeEmployeeRepository.findByStoreIdAndEmployeeId(storeId, employeeId)
                .ifPresentOrElse(assignment -> {
                    assignment.setIsPrimaryStore(true);
                    assignment.setRoleAtStore("STORE_MANAGER");
                    assignment.setActive(true);
                    assignment.setUnassignedAt(null);
                    storeEmployeeRepository.save(assignment);
                }, () -> {
                    StoreEmployee assignment = new StoreEmployee();
                    assignment.setStoreId(storeId);
                    assignment.setEmployeeId(employeeId);
                    assignment.setIsPrimaryStore(true);
                    assignment.setRoleAtStore("STORE_MANAGER");
                    assignment.setActive(true);
                    storeEmployeeRepository.save(assignment);
                });
    }

    private Employee repairDemoEmployee(Employee employee, UUID companyId, UUID userId) {
        employee.setUserId(userId);
        employee.setCompanyId(companyId);
        employee.setEmployeeNo("EMP-POS-001");
        employee.setName("Demo Cashier");
        employee.setEmail("cashier@example.local");
        if (employee.getHireDate() == null) {
            employee.setHireDate(LocalDate.now());
        }
        employee.setStatus("ACTIVE");
        return employeeRepository.save(employee);
    }

    private Store repairDemoStore(Store store, UUID companyId) {
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
    }

    private Terminal repairDemoTerminal(Terminal terminal, UUID storeId) {
        terminal.setStoreId(storeId);
        terminal.setTerminalCode("DEMO-T-001");
        terminal.setName("Demo Terminal 01");
        terminal.setDeviceType("POS");
        terminal.setDeviceModel("Browser Demo Terminal");
        terminal.setAppVersion("local-demo");
        terminal.setStatus("ONLINE");
        terminal.setLastHeartbeatAt(LocalDateTime.now());
        return terminalRepository.save(terminal);
    }

    private String legacyCode(String code, UUID id) {
        return code + "-OLD-" + id.toString().substring(0, 8);
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

        seedDemoStock(seedProduct("DEMO-LATTE-12OZ", "拿鐵 12oz", coffee.getId(), "120.00", "4710000000011"));
        seedDemoStock(seedProduct("DEMO-AMERICANO-12OZ", "美式咖啡 12oz", coffee.getId(), "90.00", "4710000000012"));
        seedDemoStock(seedProduct("DEMO-OAT-LATTE-12OZ", "燕麥拿鐵 12oz", coffee.getId(), "145.00", "4710000000013"));
        seedDemoStock(seedProduct("DEMO-CROISSANT", "奶油可頌", bakery.getId(), "75.00", "4710000000021"));
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

    private ProductItem seedProduct(String sku, String name, UUID categoryId, String price, String barcode) {
        ProductItem item = productItemRepository.findBySku(sku).orElseGet(ProductItem::new);
        item.setSku(sku);
        item.setName(name);
        item.setCategoryId(categoryId);
        item.setBasePrice(new BigDecimal(price));
        item.setUnit(ProductItem.UnitType.PCS);
        item.setBarcodePrimary(barcode);
        item.setTrackInventory(true);
        item.setSellable(true);
        item.setWeightBased(false);
        item.setActive(true);
        return productItemRepository.save(item);
    }

    private void seedDemoStock(ProductItem item) {
        storeStockRepository.findByStoreIdAndItemId(DEMO_STORE_ID, item.getId()).orElseGet(() -> {
            StoreStock stock = new StoreStock();
            stock.setStoreId(DEMO_STORE_ID);
            stock.setItemId(item.getId());
            stock.setQuantity(new BigDecimal("100.000"));
            stock.setReservedQuantity(BigDecimal.ZERO);
            stock.setReorderPoint(new BigDecimal("10.000"));
            stock.setReorderQuantity(new BigDecimal("50.000"));
            return storeStockRepository.save(stock);
        });
    }
}
