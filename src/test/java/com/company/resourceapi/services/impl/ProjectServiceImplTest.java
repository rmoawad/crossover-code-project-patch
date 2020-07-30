package com.company.resourceapi.services.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.validation.Validation;
import javax.validation.Validator;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.company.resourceapi.entities.Project;
import com.company.resourceapi.entities.SdlcSystem;
import com.company.resourceapi.exceptions.ConflictException;
import com.company.resourceapi.exceptions.NotFoundException;
import com.company.resourceapi.repositories.ProjectRepository;
import com.company.resourceapi.repositories.SdlcSystemRepository;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ProjectServiceImplTest {

  private static final long ID_1 = 1L;
  private static final long ID_10 = 10L;
  private static final String validExId = "validExId";
  private static final Project existProject = getProject();

  @InjectMocks
  private ProjectServiceImpl projectServiceImpl;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private SdlcSystemRepository sdlcSystemRepository;

  @Mock
  private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);

  @Before
  public void setUp() {
    existProject.setId(ID_1);
    when(projectRepository.findById(eq(ID_1))).thenReturn(Optional.ofNullable(existProject));
    when(projectRepository.save(any(Project.class))).thenReturn(existProject);
    when(sdlcSystemRepository.findById(eq(ID_10))).thenReturn(Optional.ofNullable(getSdlcSystem()));
  }

  @Test
  public void shouldUpdateProject_whenDataHasNoIssue() {
    // Arrange
    Project project = getProject();

    // Act
    Project createdProject = projectServiceImpl.updateProject(ID_1, project);

    // Assert
    verify(projectRepository).save(projectCaptor.capture());
    SoftAssertions assertions = new SoftAssertions();
    assertions.assertThat(createdProject.getId()).isEqualTo(ID_1);
    assertions.assertThat(projectCaptor.getValue()).isEqualTo(existProject);
    assertions.assertAll();
  }
  
  @Test
  public void shouldUpdateProject_whenUpdatedProjectHasNoValues() {
    // Arrange
    Project project = new Project();

    // Act
    Project createdProject = projectServiceImpl.updateProject(ID_1, project);

    // Assert
    verify(projectRepository).save(projectCaptor.capture());
    SoftAssertions assertions = new SoftAssertions();
    assertions.assertThat(createdProject.getId()).isEqualTo(ID_1);
    assertions.assertThat(projectCaptor.getValue()).isEqualTo(existProject);
    assertions.assertAll();
  }

  @Test
  public void shouldUpdateProject_whenUpdatedProjectUpdateOnlyExternalId() {
    // Arrange
    Project project = new Project();
    project.setExternalId("New Ex Value");
    when(projectRepository.findBySdlcSystemIdAndExternalId(ID_10, validExId))
    .thenReturn(Optional.ofNullable(null));

    // Act
    Project createdProject = projectServiceImpl.updateProject(ID_1, project);

    // Assert
    verify(projectRepository).save(projectCaptor.capture());
    SoftAssertions assertions = new SoftAssertions();
    assertions.assertThat(createdProject.getId()).isEqualTo(ID_1);
    assertions.assertThat(projectCaptor.getValue().getExternalId()).isEqualTo("New Ex Value");
    assertions.assertAll();
  }
  
  @Test
  public void shouldThrowNotFoundException_whenSdlcSystemNotExist() {
    // Arrange
    Project project = getProject();
    project.getSdlcSystem().setId(123);

    // Act && Assert
    assertThrows(NotFoundException.class, () -> projectServiceImpl.updateProject(ID_1, project));
  }

  @Test
  public void shouldThrowBadException_whenUniqueKeyGetAffectted() {
    // Arrange
    Project project = getProject();
    when(projectRepository.findBySdlcSystemIdAndExternalId(ID_10, validExId))
        .thenReturn(Optional.ofNullable(new Project()));

    // Act && Assert
    assertThrows(ConflictException.class, () -> projectServiceImpl.updateProject(ID_1, project));
  }

  private static Project getProject() {
    return Project.builder()
        .id(ID_1)
        .externalId(validExId)
        .sdlcSystem(getSdlcSystem()).build();
  }
  
  private static SdlcSystem getSdlcSystem() {
    return SdlcSystem.builder()
        .id(ID_10).build();
  }
}
