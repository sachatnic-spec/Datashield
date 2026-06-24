package io.datasheild.tenantservice;

import io.datasheild.tenantservice.entity.Tenant;
import io.datasheild.tenantservice.repository.TenantRepository;
import io.datasheild.tenantservice.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
@Import(TenantService.class)
public class TenantServiceApplicationTests {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private TenantRepository tenantRepository;

    private Tenant testTenant;

    @BeforeEach
    public void setup() {
        testTenant = Tenant.builder()
            .id(UUID.randomUUID())
            .name("Test Tenant")
            .schemaName("t_test_tenant")
            .tier(Tenant.TenantTier.STARTER)
            .subscriptionStatus(Tenant.SubscriptionStatus.ACTIVE)
            .provisioningStatus(Tenant.ProvisioningStatus.PENDING)
            .maxDataPrincipals(10000L)
            .maxConsents(100000L)
            .maxDPRRequests(50000L)
            .maxStorageGB(1000L)
            .apiRateLimitRPM(10000)
            .build();
    }

    @Test
    public void testTenantCreation() {
        em.persistAndFlush(testTenant);
        Optional<Tenant> found = tenantRepository.findByName("Test Tenant");
        assertThat(found).isPresent();
        assertThat(found.get().getTier()).isEqualTo(Tenant.TenantTier.STARTER);
    }

    @Test
    public void testTenantBySchemaName() {
        em.persistAndFlush(testTenant);
        Optional<Tenant> found = tenantRepository.findBySchemaName("t_test_tenant");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(testTenant.getId());
    }

    @Test
    public void testFindByTier() {
        em.persistAndFlush(testTenant);
        Long count = tenantRepository.countByTier(Tenant.TenantTier.STARTER);
        assertThat(count).isGreaterThan(0);
    }

    @Test
    public void testProvisioningStatusUpdate() {
        testTenant.setProvisioningStatus(Tenant.ProvisioningStatus.CREATING);
        em.persistAndFlush(testTenant);

        Optional<Tenant> found = tenantRepository.findById(testTenant.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getProvisioningStatus())
            .isEqualTo(Tenant.ProvisioningStatus.CREATING);
    }

    @Test
    public void testSubscriptionStatusUpdate() {
        em.persistAndFlush(testTenant);
        testTenant.setSubscriptionStatus(Tenant.SubscriptionStatus.SUSPENDED);
        em.flush();

        Optional<Tenant> found = tenantRepository.findById(testTenant.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSubscriptionStatus())
            .isEqualTo(Tenant.SubscriptionStatus.SUSPENDED);
    }

    @Test
    public void testTenantArchival() {
        em.persistAndFlush(testTenant);
        testTenant.setArchivedAt(LocalDateTime.now());
        testTenant.setSubscriptionStatus(Tenant.SubscriptionStatus.ARCHIVED);
        em.flush();

        Optional<Tenant> found = tenantRepository.findById(testTenant.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getArchivedAt()).isNotNull();
        assertThat(found.get().getSubscriptionStatus())
            .isEqualTo(Tenant.SubscriptionStatus.ARCHIVED);
    }

    @Test
    public void testTenantContractValidation() {
        LocalDateTime now = LocalDateTime.now();
        testTenant.setContractStartDate(now);
        testTenant.setContractEndDate(now.plusDays(30));
        em.persistAndFlush(testTenant);

        Optional<Tenant> found = tenantRepository.findById(testTenant.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getContractEndDate()).isAfter(now);
    }

    @Test
    public void testTierBasedLimits() {
        em.persistAndFlush(testTenant);
        
        Tenant enterprise = Tenant.builder()
            .id(UUID.randomUUID())
            .name("Enterprise Tenant")
            .schemaName("t_enterprise")
            .tier(Tenant.TenantTier.ENTERPRISE)
            .subscriptionStatus(Tenant.SubscriptionStatus.ACTIVE)
            .maxDataPrincipals(1000000L)
            .maxConsents(10000000L)
            .maxDPRRequests(500000L)
            .maxStorageGB(100000L)
            .apiRateLimitRPM(1000000)
            .build();

        em.persistAndFlush(enterprise);

        Tenant foundEnterprise = tenantRepository.findByName("Enterprise Tenant").get();
        assertThat(foundEnterprise.getMaxDataPrincipals())
            .isGreaterThan(testTenant.getMaxDataPrincipals());
    }
}
