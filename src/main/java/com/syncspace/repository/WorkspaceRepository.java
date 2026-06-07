package com.syncspace.repository;

import com.syncspace.entity.Workspace;
import org.springframework.stereotype.Repository;

/**
 * Workspace repository implementation.
 * 
 * Extends the interface-based repository with Spring Data JPA proxy.
 * All query methods are inherited from IWorkspaceRepository.
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@Repository
public interface WorkspaceRepository extends IWorkspaceRepository {
}
