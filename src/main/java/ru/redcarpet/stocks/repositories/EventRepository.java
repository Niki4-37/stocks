package ru.redcarpet.stocks.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.redcarpet.stocks.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
}
