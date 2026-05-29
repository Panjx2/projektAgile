package com.example.app;

import com.example.app.data.*;
import com.example.app.dto.MessageDTO;
import com.example.app.repository.*;
import com.example.app.service.MessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private FileEntityRepository fileRepository;

    @InjectMocks
    private MessageService messageService;

    private User makeUser(long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        return u;
    }

    private Message savedMessage(long id, Message m) {
        m.setId(id);
        return m;
    }

    @Test
    void shouldSavePrivateTextMessage() {
        User sender = makeUser(1L, "jan");
        User receiver = makeUser(2L, "ania");

        MessageDTO dto = new MessageDTO(null, 1L, "jan", 2L, null, "Czesc", MessageType.TEXT, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.save(any())).thenAnswer(i -> savedMessage(10L, i.getArgument(0)));

        MessageDTO result = messageService.savePrivateMessage(dto);

        assertEquals(1L, result.senderId());
        assertEquals(2L, result.receiverId());
        assertEquals("Czesc", result.content());
        assertEquals(MessageType.TEXT, result.type());
        assertNull(result.projectId());
    }

    @Test
    void shouldSavePrivateMessageWithFileAttachment() {
        User sender = makeUser(1L, "jan");
        User receiver = makeUser(2L, "ania");
        FileEntity file = new FileEntity();
        file.setId(5L);
        file.setName("doc.pdf");

        MessageDTO dto = new MessageDTO(null, 1L, "jan", 2L, null, "Plik", MessageType.FILE, 5L, "doc.pdf", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(fileRepository.findById(5L)).thenReturn(Optional.of(file));
        when(messageRepository.save(any())).thenAnswer(i -> savedMessage(11L, i.getArgument(0)));

        MessageDTO result = messageService.savePrivateMessage(dto);

        assertEquals(5L, result.fileId());
        assertEquals("doc.pdf", result.fileName());
    }

    @Test
    void shouldThrowWhenSenderNotFoundForPrivateMessage() {
        MessageDTO dto = new MessageDTO(null, 99L, null, 2L, null, "Hej", MessageType.TEXT, null, null, null);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> messageService.savePrivateMessage(dto));
    }

    @Test
    void shouldThrowWhenReceiverNotFoundForPrivateMessage() {
        User sender = makeUser(1L, "jan");
        MessageDTO dto = new MessageDTO(null, 1L, null, 99L, null, "Hej", MessageType.TEXT, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> messageService.savePrivateMessage(dto));
    }

    @Test
    void shouldSaveProjectTextMessage() {
        User sender = makeUser(1L, "jan");
        Project project = new Project();
        project.setId(3L);

        MessageDTO dto = new MessageDTO(null, 1L, "jan", null, 3L, "Hej projekcie!", MessageType.TEXT, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(projectRepository.findById(3L)).thenReturn(Optional.of(project));
        when(messageRepository.save(any())).thenAnswer(i -> savedMessage(20L, i.getArgument(0)));

        MessageDTO result = messageService.saveProjectMessage(dto);

        assertEquals(1L, result.senderId());
        assertEquals(3L, result.projectId());
        assertNull(result.receiverId());
    }

    @Test
    void shouldThrowWhenSenderNotFoundForProjectMessage() {
        MessageDTO dto = new MessageDTO(null, 99L, null, null, 3L, "Hej", MessageType.TEXT, null, null, null);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> messageService.saveProjectMessage(dto));
    }

    @Test
    void shouldThrowWhenProjectNotFoundForProjectMessage() {
        User sender = makeUser(1L, "jan");
        MessageDTO dto = new MessageDTO(null, 1L, null, null, 99L, "Hej", MessageType.TEXT, null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> messageService.saveProjectMessage(dto));
    }

    @Test
    void toDtoShouldMapPrivateMessageCorrectly() {
        User sender = makeUser(1L, "jan");
        User receiver = makeUser(2L, "ania");

        Message message = new Message();
        message.setId(10L);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent("Hej");
        message.setType(MessageType.TEXT);
        message.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));

        MessageDTO dto = MessageService.toDto(message);

        assertEquals(10L, dto.id());
        assertEquals(1L, dto.senderId());
        assertEquals("jan", dto.senderUsername());
        assertEquals(2L, dto.receiverId());
        assertNull(dto.projectId());
        assertEquals("Hej", dto.content());
    }

    @Test
    void toDtoShouldMapProjectMessageCorrectly() {
        User sender = makeUser(1L, "jan");
        Project project = new Project();
        project.setId(5L);

        Message message = new Message();
        message.setId(11L);
        message.setSender(sender);
        message.setProject(project);
        message.setContent("Hej projekt");
        message.setType(MessageType.TEXT);
        message.setCreatedAt(LocalDateTime.now());

        MessageDTO dto = MessageService.toDto(message);

        assertEquals(5L, dto.projectId());
        assertNull(dto.receiverId());
    }

    @Test
    void toDtoShouldReturnNullReceiverIdWhenReceiverIsNull() {
        User sender = makeUser(1L, "x");

        Message message = new Message();
        message.setId(1L);
        message.setSender(sender);
        message.setReceiver(null);
        message.setContent("test");
        message.setType(MessageType.TEXT);

        assertNull(MessageService.toDto(message).receiverId());
    }

    @Test
    void toDtoShouldReturnNullProjectIdWhenProjectIsNull() {
        User sender = makeUser(1L, "x");

        Message message = new Message();
        message.setId(1L);
        message.setSender(sender);
        message.setProject(null);
        message.setContent("test");
        message.setType(MessageType.TEXT);

        assertNull(MessageService.toDto(message).projectId());
    }

    @Test
    void toDtoShouldReturnNullFileFieldsWhenFileIsNull() {
        User sender = makeUser(1L, "x");

        Message message = new Message();
        message.setId(1L);
        message.setSender(sender);
        message.setFile(null);
        message.setContent("test");
        message.setType(MessageType.TEXT);

        MessageDTO dto = MessageService.toDto(message);

        assertNull(dto.fileId());
        assertNull(dto.fileName());
    }
}
