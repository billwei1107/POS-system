package com.enterprise.organization.service;

import com.enterprise.organization.entity.Position;
import java.util.List;
import java.util.UUID;

public interface PositionService {
    Position create(Position position);
    Position update(UUID id, Position position);
    Position getById(UUID id);
    List<Position> listAll();
    void delete(UUID id);
}
