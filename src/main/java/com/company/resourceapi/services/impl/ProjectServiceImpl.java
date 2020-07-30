package com.company.resourceapi.services.impl;

import com.company.resourceapi.entities.Project;
import com.company.resourceapi.entities.SdlcSystem;
import com.company.resourceapi.exceptions.ConflictException;
import com.company.resourceapi.exceptions.GeneralException;
import com.company.resourceapi.exceptions.NotFoundException;
import com.company.resourceapi.repositories.ProjectRepository;
import com.company.resourceapi.repositories.SdlcSystemRepository;
import com.company.resourceapi.services.ProjectService;

import lombok.RequiredArgsConstructor;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

  private final ProjectRepository projectRepository;
  private final SdlcSystemRepository sdlcSystemRepository;
  private final Validator validator;

  public Project getProject(long id) {
    return projectRepository.findById(id).orElseThrow(() -> new NotFoundException(Project.class, id));
  }

  @Override
  public Project updateProject(long projectId, Project project) {
    Project existProject = getProject(projectId);
    existProject.copyNotNullValues(project);
    validateOnUpdate(existProject);
    injectSystem(existProject);
    return projectRepository.save(existProject);
  }

  private void validateOnUpdate(Project project) {
    validateAnnotations(project);
    validateUniqueConstrain(project);
  }

  private void validateAnnotations(Project project) {
    Set<ConstraintViolation<Project>> violations = validator.validate(project);
    if (!violations.isEmpty()) {
      ConstraintViolation<Project> violation = violations.iterator().next();
      // Needs enhancement
      throw new GeneralException(violation.getPropertyPath() + " " + violation.getMessage());
    }
  }

  private void validateUniqueConstrain(Project project) {
    Long linkedProjectId = projectRepository
        .findBySdlcSystemIdAndExternalId(project.getSdlcSystem().getId(), project.getExternalId())
        .map(Project::getId).orElse(null);
    if (linkedProjectId != null && linkedProjectId != project.getId()) {
      throw new ConflictException("There another project with same external id already exist and linked to same SdlcSystem");
    }
  }

  private void injectSystem(Project project) {
    SdlcSystem sdlcSystem = sdlcSystemRepository.findById(project.getSdlcSystem().getId())
        .orElseThrow(() -> new NotFoundException(SdlcSystem.class, project.getSdlcSystem().getId()));
    project.setSdlcSystem(sdlcSystem);

  }
}
