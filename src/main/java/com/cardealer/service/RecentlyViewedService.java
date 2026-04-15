package com.cardealer.service;

import com.cardealer.model.Car;
import com.cardealer.model.User;
import com.cardealer.model.ViewHistory;
import com.cardealer.repository.CarRepository;
import com.cardealer.repository.ViewHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecentlyViewedService {

    public static final String RECENTLY_VIEWED_SESSION_KEY = "recently-viewed-cars";
    private static final int MAX_RECENTLY_VIEWED = 10;

    private final CarRepository carRepository;
    private final ViewHistoryRepository viewHistoryRepository;

    @Transactional
    public void addToRecentlyViewed(Long carId, HttpSession session) {
        addToRecentlyViewed(carId, session, null, null);
    }

    @Transactional
    public void addToRecentlyViewed(Long carId, HttpSession session, User user, HttpServletRequest request) {
        String sessionId = session.getId();
        List<Long> currentIds = new ArrayList<>(getRecentlyViewed(session, MAX_RECENTLY_VIEWED));
        currentIds.remove(carId);
        currentIds.add(0, carId);
        if (currentIds.size() > MAX_RECENTLY_VIEWED) {
            currentIds = currentIds.subList(0, MAX_RECENTLY_VIEWED);
        }
        session.setAttribute(RECENTLY_VIEWED_SESSION_KEY, currentIds);

        carRepository.findById(carId).ifPresent(car -> {
            ViewHistory history = new ViewHistory();
            history.setSessionId(sessionId);
            history.setCar(car);
            history.setUser(user);
            history.setIpAddress(extractIpAddress(request));
            viewHistoryRepository.save(history);
        });
    }

    public List<Long> getRecentlyViewed(HttpSession session, int limit) {
        Object sessionValue = session.getAttribute(RECENTLY_VIEWED_SESSION_KEY);
        if (!(sessionValue instanceof List<?> rawIds)) {
            return List.of();
        }

        return rawIds.stream()
            .filter(Long.class::isInstance)
            .map(Long.class::cast)
            .limit(limit)
            .toList();
    }

    public List<Car> getRecentlyViewedCars(HttpSession session, int limit) {
        List<Long> ids = getRecentlyViewed(session, limit);
        if (ids.isEmpty()) {
            return List.of();
        }

        List<Car> cars = carRepository.findAllById(new LinkedHashSet<>(ids));
        return ids.stream()
            .map(id -> cars.stream().filter(car -> car.getId().equals(id)).findFirst())
            .flatMap(Optional::stream)
            .collect(Collectors.toList());
    }

    @Transactional
    public void clearRecentlyViewed(HttpSession session) {
        session.removeAttribute(RECENTLY_VIEWED_SESSION_KEY);
        viewHistoryRepository.deleteBySessionId(session.getId());
    }

    private String extractIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
