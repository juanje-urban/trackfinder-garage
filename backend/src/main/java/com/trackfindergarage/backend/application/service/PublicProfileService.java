package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.PublicProfileUseCase;
import com.trackfindergarage.backend.application.port.in.PublicUserProfileView;
import com.trackfindergarage.backend.application.port.out.EventBookingPersistencePort;
import com.trackfindergarage.backend.application.port.out.LapTimePersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.EventBooking;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementa la construcción del perfil público resumido de un usuario.
 *
 * <p>Calcula métricas visibles como eventos completados, circuitos visitados, vueltas destacadas y
 * poles a partir del histórico de reservas y tiempos de vuelta.</p>
 */
@Service
@Transactional(readOnly = true)
public class PublicProfileService implements PublicProfileUseCase {

    private static final String USER_NOT_FOUND_WITH_DISPLAY_NAME = "Usuario no encontrado con nombre: ";

    private final UserPersistencePort userPersistencePort;
    private final EventBookingPersistencePort eventBookingPersistencePort;
    private final LapTimePersistencePort lapTimePersistencePort;

    public PublicProfileService(UserPersistencePort userPersistencePort,
                                EventBookingPersistencePort eventBookingPersistencePort,
                                LapTimePersistencePort lapTimePersistencePort) {
        this.userPersistencePort = userPersistencePort;
        this.eventBookingPersistencePort = eventBookingPersistencePort;
        this.lapTimePersistencePort = lapTimePersistencePort;
    }

    /**
     * Recupera el perfil público resumido de un usuario a partir de su alias.
     *
     * @param displayName alias público del usuario
     * @return vista pública del perfil
     */
    @Override
    public PublicUserProfileView getPublicUserProfile(String displayName) {
        User user = loadUserByDisplayName(displayName);
        List<EventBooking> pastBookings = loadPastBookings(user.getId());
        List<LapTime> userLapTimes = lapTimePersistencePort.findByUserId(user.getId());
        Map<Long, List<LapTime>> rankingsByTrackId = buildRankingsByTrackId(userLapTimes);

        return new PublicUserProfileView(
                user.getId(),
                user.getDisplayName(),
                pastBookings.size(),
                countVisitedCircuits(pastBookings),
                countTopFiveLapTimes(userLapTimes, rankingsByTrackId),
                countPoles(user.getId(), rankingsByTrackId)
        );
    }

    // Normaliza el alias público y carga al usuario visible en el perfil.
    private User loadUserByDisplayName(String displayName) {
        String normalizedDisplayName = normalizeDisplayName(displayName);

        return userPersistencePort.findByDisplayName(normalizedDisplayName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        USER_NOT_FOUND_WITH_DISPLAY_NAME + normalizedDisplayName
                ));
    }

    // Conserva sólo las reservas asociadas a eventos ya celebrados.
    private List<EventBooking> loadPastBookings(Long userId) {
        LocalDate today = LocalDate.now();

        return eventBookingPersistencePort.findByUserId(userId).stream()
                .filter(booking -> booking.getEvent() != null
                        && booking.getEvent().getEventDate() != null
                        && booking.getEvent().getEventDate().isBefore(today))
                .toList();
    }

    // Construye el ranking completo por circuito para reutilizarlo en las métricas.
    private Map<Long, List<LapTime>> buildRankingsByTrackId(List<LapTime> lapTimes) {
        return lapTimes.stream()
                .map(lapTime -> lapTime.getTrack() != null ? lapTime.getTrack().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        trackId -> lapTimePersistencePort.findByTrackId(trackId)
                                .stream()
                                .sorted(lapTimeComparator())
                                .toList()
                ));
    }

    // Cuenta cuántas vueltas del usuario aparecen entre las cinco mejores de su circuito.
    private long countTopFiveLapTimes(List<LapTime> userLapTimes, Map<Long, List<LapTime>> rankingsByTrackId) {
        return userLapTimes.stream()
                .filter(lapTime -> isTopFiveLapTime(lapTime, rankingsByTrackId))
                .count();
    }

    // Cuenta en cuántos circuitos el usuario mantiene la mejor vuelta del ranking.
    private long countPoles(Long userId, Map<Long, List<LapTime>> rankingsByTrackId) {
        return rankingsByTrackId.values().stream()
                .filter(ranking -> !ranking.isEmpty())
                .filter(ranking -> ranking.get(0).getUser() != null
                        && Objects.equals(ranking.get(0).getUser().getId(), userId))
                .count();
    }

    // Cuenta los circuitos distintos en los que el usuario ya ha participado.
    private long countVisitedCircuits(List<EventBooking> pastBookings) {
        return pastBookings.stream()
                .map(booking -> booking.getEvent() != null && booking.getEvent().getTrack() != null
                        ? booking.getEvent().getTrack().getId()
                        : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    // Comprueba si una vuelta concreta ocupa alguna de las cinco primeras posiciones de su ranking.
    private boolean isTopFiveLapTime(LapTime lapTime, Map<Long, List<LapTime>> rankingsByTrackId) {
        // Descarta vueltas sin identidad suficiente para ubicarlas en el ranking.
        if (lapTime.getTrack() == null || lapTime.getTrack().getId() == null || lapTime.getId() == null) {
            return false;
        }

        List<LapTime> ranking = rankingsByTrackId.get(lapTime.getTrack().getId());
        // Si no hay ranking calculado para el circuito, no puede contar como top 5.
        if (ranking == null || ranking.isEmpty()) {
            return false;
        }

        // Recorre el ranking ordenado hasta localizar la vuelta del usuario.
        for (int index = 0; index < ranking.size(); index++) {
            if (Objects.equals(ranking.get(index).getId(), lapTime.getId())) {
                return index < 5;
            }
        }

        return false;
    }

    // Mantiene un criterio de orden estable para todos los rankings públicos.
    private Comparator<LapTime> lapTimeComparator() {
        return Comparator.comparing(LapTime::getLapTimeMs)
                .thenComparing(LapTime::getLapDate)
                .thenComparing(lapTime -> lapTime.getId() == null ? Long.MAX_VALUE : lapTime.getId());
    }

    // Normaliza el alias recibido y exige que venga informado.
    private String normalizeDisplayName(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }

        return displayName.trim();
    }
}
