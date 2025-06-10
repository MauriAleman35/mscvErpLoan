package com.miempresa.erp.repository;

import java.util.Map;

public interface UserRepositoryCustom {
    Map<String, Object> getBorrowerStatistics(Long borrowerId);
}
