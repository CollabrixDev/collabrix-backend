package com.syncspace.service;

import com.syncspace.dto.CreateWorkspaceRequest;
import com.syncspace.dto.UpdateWorkspaceRequest;
import com.syncspace.dto.WorkspaceDTO;
import com.syncspace.entity.User;
import com.syncspace.entity.Workspace;
import com.syncspace.exception.InvalidInputException;
import com.syncspace.exception.ResourceNotFoundException;
import com.syncspace.factory.IWorkspaceFactory;
import com.syncspace.repository.IWorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WorkspaceService.
 * 
 * Tests:
 * - Workspace creation
 * - Duplicate name validation
 * - Member management
 * - Authorization checks
 * - DTO conversion
 * 
 * @author Prajeeth
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceService Tests")
class WorkspaceServiceTest {
    
    @Mock
    private IWorkspaceRepository workspaceRepository;
    
    @Mock
    private IWorkspaceFactory workspaceFactory;
    
    @InjectMocks
    private WorkspaceService workspaceService;
    
    private User owner;
    private User member;
    private Workspace workspace;
    private CreateWorkspaceRequest createRequest;
    
    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .name("Owner User")
                .build();
        
        member = User.builder()
                .id(2L)
                .email("member@test.com")
                .name("Member User")
                .build();
        
        workspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .description("Test Description")
                .owner(owner)
                .members(new HashSet<>())
                .isActive(true)
                .build();
        workspace.addMember(owner);
        
        createRequest = CreateWorkspaceRequest.builder()
                .name("Test Workspace")
                .description("Test Description")
                .build();
    }
    
    // ============ CREATE WORKSPACE TESTS ============
    
    @Test
    @DisplayName("Should create workspace successfully")
    void testCreateWorkspace() {
        when(workspaceRepository.findByNameAndOwnerId(createRequest.getName(), owner.getId()))
                .thenReturn(Optional.empty());
        when(workspaceFactory.createWorkspaceFromRequest(createRequest, owner))
                .thenReturn(workspace);
        when(workspaceRepository.save(workspace))
                .thenReturn(workspace);
        
        WorkspaceDTO result = workspaceService.createWorkspace(createRequest, owner);
        
        assertNotNull(result);
        assertEquals("Test Workspace", result.getName());
        assertEquals(owner.getId(), result.getOwner().getId());
        
        verify(workspaceRepository).save(workspace);
    }
    
    @Test
    @DisplayName("Should throw exception for duplicate workspace name")
    void testCreateWorkspaceDuplicateName() {
        when(workspaceRepository.findByNameAndOwnerId(createRequest.getName(), owner.getId()))
                .thenReturn(Optional.of(workspace));
        
        assertThrows(InvalidInputException.class, 
                () -> workspaceService.createWorkspace(createRequest, owner));
        
        verify(workspaceRepository, never()).save(any());
    }
    
    // ============ GET WORKSPACE TESTS ============
    
    @Test
    @DisplayName("Should get workspace by ID")
    void testGetWorkspaceById() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        
        WorkspaceDTO result = workspaceService.getWorkspaceById(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Workspace", result.getName());
    }
    
    @Test
    @DisplayName("Should throw exception when workspace not found")
    void testGetWorkspaceNotFound() {
        when(workspaceRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, 
                () -> workspaceService.getWorkspaceById(999L));
    }
    
    // ============ UPDATE WORKSPACE TESTS ============
    
    @Test
    @DisplayName("Should update workspace by owner")
    void testUpdateWorkspace() {
        UpdateWorkspaceRequest updateRequest = UpdateWorkspaceRequest.builder()
                .name("Updated Workspace")
                .description("Updated Description")
                .build();
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(workspace)).thenReturn(workspace);
        
        workspace.setName("Updated Workspace");
        workspace.setDescription("Updated Description");
        
        WorkspaceDTO result = workspaceService.updateWorkspace(1L, updateRequest, owner);
        
        assertNotNull(result);
        assertEquals("Updated Workspace", result.getName());
        
        verify(workspaceRepository).save(workspace);
    }
    
    @Test
    @DisplayName("Should throw exception when non-owner updates workspace")
    void testUpdateWorkspaceUnauthorized() {
        UpdateWorkspaceRequest updateRequest = UpdateWorkspaceRequest.builder()
                .name("Updated")
                .build();
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        
        assertThrows(InvalidInputException.class, 
                () -> workspaceService.updateWorkspace(1L, updateRequest, member));
        
        verify(workspaceRepository, never()).save(any());
    }
    
    // ============ DELETE WORKSPACE TESTS ============
    
    @Test
    @DisplayName("Should delete workspace by owner")
    void testDeleteWorkspace() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(workspace)).thenReturn(workspace);
        
        workspaceService.deleteWorkspace(1L, owner);
        
        assertFalse(workspace.getIsActive());
        verify(workspaceRepository).save(workspace);
    }
    
    @Test
    @DisplayName("Should throw exception when non-owner deletes workspace")
    void testDeleteWorkspaceUnauthorized() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        
        assertThrows(InvalidInputException.class, 
                () -> workspaceService.deleteWorkspace(1L, member));
        
        verify(workspaceRepository, never()).save(any());
    }
    
    // ============ MEMBER MANAGEMENT TESTS ============
    
    @Test
    @DisplayName("Should add member to workspace")
    void testAddMemberToWorkspace() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(workspace)).thenReturn(workspace);
        
        workspace.addMember(member);
        
        WorkspaceDTO result = workspaceService.addMemberToWorkspace(1L, member, owner);
        
        assertNotNull(result);
        assertEquals(2, result.getMembers().size());
        
        verify(workspaceRepository).save(workspace);
    }
    
    @Test
    @DisplayName("Should throw exception when adding duplicate member")
    void testAddMemberDuplicate() {
        workspace.addMember(member);
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        
        assertThrows(InvalidInputException.class, 
                () -> workspaceService.addMemberToWorkspace(1L, member, owner));
        
        verify(workspaceRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should remove member from workspace")
    void testRemoveMemberFromWorkspace() {
        workspace.addMember(member);
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.save(workspace)).thenReturn(workspace);
        
        workspace.removeMember(member);
        
        WorkspaceDTO result = workspaceService.removeMemberFromWorkspace(1L, member, owner);
        
        assertNotNull(result);
        assertEquals(1, result.getMembers().size());
        
        verify(workspaceRepository).save(workspace);
    }
    
    @Test
    @DisplayName("Should throw exception when removing owner")
    void testRemoveOwner() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        
        assertThrows(InvalidInputException.class, 
                () -> workspaceService.removeMemberFromWorkspace(1L, owner, owner));
        
        verify(workspaceRepository, never()).save(any());
    }
    
    @Test
    @DisplayName("Should throw exception when removing non-existent member")
    void testRemoveNonExistentMember() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        
        assertThrows(ResourceNotFoundException.class, 
                () -> workspaceService.removeMemberFromWorkspace(1L, member, owner));
        
        verify(workspaceRepository, never()).save(any());
    }
    
    // ============ AUTHORIZATION TESTS ============
    
    @Test
    @DisplayName("Should verify member of workspace")
    void testIsMemberOfWorkspace() {
        workspace.addMember(member);
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        
        assertTrue(workspaceService.isMemberOfWorkspace(1L, owner.getId()));
        assertTrue(workspaceService.isMemberOfWorkspace(1L, member.getId()));
    }
    
    @Test
    @DisplayName("Should return false for non-member")
    void testIsNotMemberOfWorkspace() {
        User otherUser = User.builder().id(3L).email("other@test.com").build();
        
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        
        assertFalse(workspaceService.isMemberOfWorkspace(1L, otherUser.getId()));
    }
}
