package ru.redcarpet.stocks.model.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.redcarpet.stocks.model.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
