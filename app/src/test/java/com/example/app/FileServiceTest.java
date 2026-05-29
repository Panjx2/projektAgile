package com.example.app;

import com.example.app.data.FileEntity;
import com.example.app.data.Project;
import com.example.app.data.Task;
import com.example.app.repository.FileEntityRepository;
import com.example.app.repository.ProjectRepository;
import com.example.app.repository.TaskRepository;
import com.example.app.service.FileService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileEntityRepository fileRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private FileService fileService;

    private final String TEST_DIR = "test-uploads";

    @BeforeEach
    void setup() throws IOException {
        Path path = Path.of(TEST_DIR);

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        org.springframework.test.util.ReflectionTestUtils
                .setField(fileService, "uploadDir", TEST_DIR);
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.walk(Path.of(TEST_DIR))
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(java.io.File::delete);
    }

    @Test
    void shouldUploadFileToProject() throws IOException {

        Project project = new Project();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(fileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());

        FileEntity result = fileService.uploadToProject(1L, file);

        assertEquals("test.txt", result.getName());
        assertNotNull(result.getPath());
        assertEquals(project, result.getProject());
    }

    @Test
    void shouldUploadFileToTask() throws IOException {

        Task task = new Task();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(fileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());

        FileEntity result = fileService.uploadToTask(1L, file);

        assertEquals("test.txt", result.getName());
        assertEquals(task, result.getTask());
        assertNull(result.getProject());
    }

    @Test
    void shouldGetFilesByProject() {

        when(projectRepository.existsById(1L)).thenReturn(true);
        when(fileRepository.findByProjectId(1L))
                .thenReturn(List.of(new FileEntity()));

        List<FileEntity> result = fileService.getFilesByProject(1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowWhenProjectNotFound() {

        when(projectRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> fileService.getFilesByProject(1L));
    }

    @Test
    void shouldGetFilesByTask() {

        when(taskRepository.existsById(1L)).thenReturn(true);
        when(fileRepository.findByTaskId(1L))
                .thenReturn(List.of(new FileEntity()));

        List<FileEntity> result = fileService.getFilesByTask(1L);

        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowWhenTaskNotFound() {

        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> fileService.getFilesByTask(1L));
    }

    @Test
    void shouldDownloadFile() throws Exception {

        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, "data".getBytes());

        FileEntity entity = new FileEntity();
        entity.setPath(tempFile.toString());

        when(fileRepository.findById(1L)).thenReturn(Optional.of(entity));

        Resource result = fileService.downloadFile(1L);

        assertTrue(result.exists());
        assertArrayEquals("data".getBytes(), result.getInputStream().readAllBytes());
    }

    @Test
    void shouldThrowWhenFileTooLargeForProject() {
        byte[] largeData = new byte[(int) (10 * 1024 * 1024) + 1]; // 10MB + 1 byte
        MockMultipartFile file =
                new MockMultipartFile("file", "big.bin", "application/octet-stream", largeData);

        assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadToProject(1L, file));
    }

    @Test
    void shouldThrowWhenFileTooLargeForTask() {
        byte[] largeData = new byte[(int) (10 * 1024 * 1024) + 1]; // 10MB + 1 byte
        MockMultipartFile file =
                new MockMultipartFile("file", "big.bin", "application/octet-stream", largeData);

        assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadToTask(1L, file));
    }

    @Test
    void shouldAcceptFileAtExactSizeLimit() throws IOException {
        // dokładnie 10MB powinno przejść
        byte[] maxData = new byte[10 * 1024 * 1024];
        Project project = new Project();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(fileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile("file", "limit.bin", "application/octet-stream", maxData);

        assertDoesNotThrow(() -> fileService.uploadToProject(1L, file));
    }

    @Test
    void shouldRejectOversizedFileAndNotSaveToDatabase() {
        // plik za duży — nie może trafić do bazy
        byte[] oversized = new byte[(int) (10 * 1024 * 1024) + 1];
        MockMultipartFile file =
                new MockMultipartFile("file", "too-big.bin", "application/octet-stream", oversized);

        assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadToProject(1L, file));

        verifyNoInteractions(fileRepository);
    }

    @Test
    void shouldRejectOversizedFileWithCorrectMessage() {
        byte[] oversized = new byte[(int) (10 * 1024 * 1024) + 1];
        MockMultipartFile file =
                new MockMultipartFile("file", "too-big.bin", "application/octet-stream", oversized);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadToProject(1L, file));

        assertTrue(ex.getMessage().toLowerCase().contains("too large") ||
                   ex.getMessage().toLowerCase().contains("za duż"));
    }

    @Test
    void shouldThrowWhenEmptyFileUploadedToProject() {
        MockMultipartFile file =
                new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadToProject(1L, file));
    }

    @Test
    void shouldThrowWhenEmptyFileUploadedToTask() {
        MockMultipartFile file =
                new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> fileService.uploadToTask(1L, file));
    }

    @Test
    void shouldThrowWhenDownloadedFileNotOnDisk() {
        FileEntity entity = new FileEntity();
        entity.setPath("/nonexistent/path/file.txt");

        when(fileRepository.findById(99L)).thenReturn(Optional.of(entity));

        assertThrows(EntityNotFoundException.class,
                () -> fileService.downloadFile(99L));
    }

    @Test
    void shouldPreserveOriginalFilenameOnProjectUpload() throws IOException {
        Project project = new Project();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(fileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile("file", "raport_końcowy.pdf", "application/pdf", "content".getBytes());

        FileEntity result = fileService.uploadToProject(1L, file);

        assertEquals("raport_końcowy.pdf", result.getName());
    }

    @Test
    void shouldPreserveOriginalFilenameOnTaskUpload() throws IOException {
        Task task = new Task();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(fileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile("file", "screenshot.png", "image/png", "img".getBytes());

        FileEntity result = fileService.uploadToTask(1L, file);

        assertEquals("screenshot.png", result.getName());
    }

    @Test
    void shouldUseUnnamedFallbackWhenFilenameIsNull() throws IOException {
        Project project = new Project();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(fileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile("file", null, "application/octet-stream", "data".getBytes());

        FileEntity result = fileService.uploadToProject(1L, file);

        assertEquals("unnamed_file", result.getName());
    }

    @Test
    void shouldStripPathTraversalFromFilename() throws IOException {
        Project project = new Project();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(fileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile("file", "../../etc/passwd", "text/plain", "data".getBytes());

        FileEntity result = fileService.uploadToProject(1L, file);

        assertEquals("passwd", result.getName());
    }


    @Test
    void shouldDeleteFile() throws IOException {

        Path tempFile = Files.createTempFile("test", ".txt");

        FileEntity entity = new FileEntity();
        entity.setPath(tempFile.toString());

        when(fileRepository.findById(1L)).thenReturn(Optional.of(entity));

        fileService.deleteFile(1L);

        assertFalse(Files.exists(tempFile));
        verify(fileRepository).delete(entity);
    }

    @Test
    void shouldThrowWhenProjectNotFoundOnUpload() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        MockMultipartFile file =
                new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> fileService.uploadToProject(99L, file));
    }

    @Test
    void shouldThrowWhenTaskNotFoundOnUpload() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        MockMultipartFile file =
                new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> fileService.uploadToTask(99L, file));
    }

    @Test
    void shouldThrowWhenFileEntityNotFoundOnDownload() {
        when(fileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> fileService.downloadFile(99L));
    }

    @Test
    void shouldThrowWhenFileEntityNotFoundOnDelete() {
        when(fileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(java.util.NoSuchElementException.class,
                () -> fileService.deleteFile(99L));
    }

    @Test
    void shouldGetFileById() {
        FileEntity entity = new FileEntity();
        entity.setId(1L);
        entity.setName("raport.pdf");

        when(fileRepository.findById(1L)).thenReturn(Optional.of(entity));

        FileEntity result = fileService.getFileById(1L);

        assertEquals("raport.pdf", result.getName());
    }

    @Test
    void shouldThrowWhenFileNotFoundOnGetById() {
        when(fileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> fileService.getFileById(99L));
    }

    @Test
    void shouldGetFilesByProjectReturnsEmptyListWhenNoneExist() {
        when(projectRepository.existsById(1L)).thenReturn(true);
        when(fileRepository.findByProjectId(1L)).thenReturn(List.of());

        List<FileEntity> result = fileService.getFilesByProject(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldUploadToProjectAndStoreFileOnDisk() throws IOException {
        Project project = new Project();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(fileRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file =
                new MockMultipartFile("file", "dokument.txt", "text/plain", "tresc".getBytes());

        FileEntity result = fileService.uploadToProject(1L, file);

        Path storedPath = Path.of(result.getPath());
        assertTrue(Files.exists(storedPath), "Plik powinien istniec na dysku");
        assertArrayEquals("tresc".getBytes(), Files.readAllBytes(storedPath));
    }
}
