package com.enterprise.organization.service.impl;

import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.organization.entity.Position;
import com.enterprise.organization.repository.PositionRepository;
import com.enterprise.organization.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;

    @Override
    public Position create(Position position) {
        return positionRepository.save(position);
    }

    @Override
    public Position update(UUID id, Position position) {
        Position existing = getById(id);
        existing.setName(position.getName());
        existing.setCode(position.getCode());
        existing.setLevel(position.getLevel());
        existing.setDescription(position.getDescription());
        return positionRepository.save(existing);
    }

    @Override
    public Position getById(UUID id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found with id: " + id));
    }

    @Override
    public List<Position> listAll() {
        return positionRepository.findAll();
    }

    @Override
    public void delete(UUID id) {
        positionRepository.deleteById(id);
    }
}
