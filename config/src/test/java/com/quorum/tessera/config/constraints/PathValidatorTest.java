package com.quorum.tessera.config.constraints;

import com.quorum.tessera.io.FilesDelegate;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PathValidatorTest {

    @Test
    public void validateFileExists() {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkExists()).thenReturn(true);

        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path path = Paths.get("bogus");

        assertThat(pathValidator.isValid(path, context)).isFalse();
    }

    @Test
    public void validateFileExistsDontCheck() {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkExists()).thenReturn(false);

        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path path = Paths.get("bogus");

        assertThat(pathValidator.isValid(path, context)).isTrue();
    }

    @Test
    public void validateFileExistsWhenFileDoesExist() throws IOException {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkExists()).thenReturn(true);

        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path actualFile = Files.createTempFile(UUID.randomUUID().toString(), ".txt");

        assertThat(pathValidator.isValid(actualFile, context)).isTrue();
    }

    @Test
    public void nullPathReturnsTrue() {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkExists()).thenReturn(true);

        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        assertThat(pathValidator.isValid(null, context)).isTrue();

        verifyZeroInteractions(context);
    }

    @Test
    public void checkCanCreateFile() throws IOException {
        final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkCanCreate()).thenReturn(true);

        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path path = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
        Files.deleteIfExists(path);

        assertThat(pathValidator.isValid(path, context)).isTrue();

        verifyZeroInteractions(context);
    }

    @Test
    public void checkCannotCreateFile() {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkCanCreate()).thenReturn(true);
        when(validPath.checkExists()).thenReturn(Boolean.FALSE);
        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path path = mock(Path.class);

        FilesDelegate filesDelegate = mock(FilesDelegate.class);

        when(filesDelegate.notExists(path)).thenReturn(Boolean.TRUE);
        when(filesDelegate.createFile(path)).thenThrow(UncheckedIOException.class);
        when(filesDelegate.deleteIfExists(path)).thenThrow(UncheckedIOException.class); // final block coverage

        pathValidator.setFilesDelegate(filesDelegate);

        assertThat(pathValidator.isValid(path, context)).isFalse();

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());

        verifyNoMoreInteractions(context);
    }

    @Test
    public void checkCannotCreateFileExistingFile() {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkCanCreate()).thenReturn(true);
        when(validPath.checkExists()).thenReturn(Boolean.FALSE);
        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path path = mock(Path.class);

        FilesDelegate filesDelegate = mock(FilesDelegate.class);

        when(filesDelegate.notExists(path)).thenReturn(Boolean.FALSE);

        pathValidator.setFilesDelegate(filesDelegate);

        assertThat(pathValidator.isValid(path, context)).isTrue();

        verifyNoMoreInteractions(context);
    }

    @Test
    public void checkCannotCreateFileDontCheck() {

        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(mock(ConstraintValidatorContext.ConstraintViolationBuilder.class));

        ValidPath validPath = mock(ValidPath.class);
        when(validPath.checkCanCreate()).thenReturn(false);
        when(validPath.checkExists()).thenReturn(false);
        PathValidator pathValidator = new PathValidator();
        pathValidator.initialize(validPath);

        Path path = mock(Path.class);

        FilesDelegate filesDelegate = mock(FilesDelegate.class);

        when(filesDelegate.notExists(path)).thenReturn(Boolean.TRUE);

        pathValidator.setFilesDelegate(filesDelegate);

        assertThat(pathValidator.isValid(path, context)).isTrue();

        verifyNoMoreInteractions(context);
    }
}
