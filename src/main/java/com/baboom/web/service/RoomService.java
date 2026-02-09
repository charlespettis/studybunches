package com.baboom.web.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import com.baboom.web.dto.RoomDto;
import com.baboom.web.exception.QuestionGenerationException;
import com.baboom.web.exception.RoomNotFoundException;
import com.baboom.web.integration.OpenAI;
import com.baboom.web.model.QuestionList;
import com.baboom.web.model.Room;
import com.baboom.web.store.RoomStore;

@Service
public class RoomService {
    private final RoomStore store;
    private final OpenAI openAI;

    public RoomService(RoomStore store, OpenAI openAI){
        this.store = store;
        this.openAI = openAI;
    }

    private String generateRandomRoomCode(){
        SecureRandom rnd = new SecureRandom();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    private RoomDto toDto(Room room){
        return new RoomDto(room.getId(), room.getCode(), room.getQuestions(), room.getScores());
    }

    public RoomDto createRoom(){
        Long id = store.generateId();

        String roomCode = generateRandomRoomCode();

        Room room = new Room(id, roomCode);
        
        store.save(room);

        return toDto(room);
    }

    public RoomDto getRoom(String code){

        Room room = store.findByCode(code)
        .orElseThrow(() -> new RoomNotFoundException(code));
        
        return toDto(room);
    }

    public boolean generateQuestions(String prompt, String code){
        
        QuestionList questions = openAI.generate(prompt)
        .orElseThrow(() -> new QuestionGenerationException("Error generating questions"));

        Room room = store.findByCode(code)
        .orElseThrow(() -> new RoomNotFoundException(code));

        room.setQuestions(questions);

        return true;
    }

    public void incrementScore(String name, String code){
        Room room = store.findByCode(code)
        .orElseThrow(() -> new RoomNotFoundException(code));
        
        room.updateScore(name, 1);
    }

    public void resetScore(String name, String code){
        Room room = store.findByCode(code)
        .orElseThrow(() -> new RoomNotFoundException(code));

        room.resetScore(name);
    }
}

