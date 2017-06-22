package org.vaporwarecorp.doorbell.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.vaporwarecorp.doorbell.model.DoorbellConfiguration;

public interface DoorbellConfigurationRepository extends JpaRepository<DoorbellConfiguration, Integer> {
}
