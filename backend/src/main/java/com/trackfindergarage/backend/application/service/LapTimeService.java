package com.trackfindergarage.backend.application.service;

import com.trackfindergarage.backend.application.port.in.LapTimeUseCase;
import com.trackfindergarage.backend.application.port.out.LapTimePersistencePort;
import com.trackfindergarage.backend.application.port.out.TrackPersistencePort;
import com.trackfindergarage.backend.application.port.out.UserPersistencePort;
import com.trackfindergarage.backend.common.exception.ResourceNotFoundException;
import com.trackfindergarage.backend.domain.model.LapTime;
import com.trackfindergarage.backend.domain.model.Track;
import com.trackfindergarage.backend.domain.model.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Implementa la lógica de registro y consulta de tiempos de vuelta.
 *
 * <p>Valida la autoría de los registros, los datos mínimos de la vuelta y la existencia del usuario
 * y del circuito antes de persistir o devolver resultados.</p>
 */
@Service
@Transactional
public class LapTimeService implements LapTimeUseCase {

    private static final String USER_NOT_FOUND_WITH_ID = "Usuario no encontrado con id: ";
    private static final String TRACK_NOT_FOUND_WITH_ID = "Circuito no encontrado con id: ";
    private static final String LAP_TIME_NOT_FOUND_WITH_ID = "Tiempo de vuelta no encontrado con id: ";
    private static final String USER_ID_REQUIRED = "El id de usuario es obligatorio";
    private static final String AUTHENTICATED_EMAIL_REQUIRED = "El correo electrónico del usuario autenticado es obligatorio";
    private static final String USER_NOT_FOUND_WITH_EMAIL = "Usuario no encontrado con correo electrónico: ";
    private static final String TRACK_ID_REQUIRED = "El id del circuito es obligatorio";
    private static final String LAP_DATE_REQUIRED = "La fecha de la vuelta es obligatoria";
    private static final String ONLY_LAP_TIME_OWNER_CAN_DELETE = "Solo el propietario del tiempo de vuelta puede eliminarlo";

    private final LapTimePersistencePort lapTimePersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final TrackPersistencePort trackPersistencePort;

    public LapTimeService(LapTimePersistencePort lapTimePersistencePort,
                          UserPersistencePort userPersistencePort,
                          TrackPersistencePort trackPersistencePort) {
        this.lapTimePersistencePort = lapTimePersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.trackPersistencePort = trackPersistencePort;
    }

    /**
     * Registra un nuevo tiempo de vuelta para el usuario.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param trackId identificador del circuito
     * @param lapDate fecha de la vuelta
     * @param lapTimeMs tiempo en milisegundos
     * @param vehicle vehículo utilizado
     * @return tiempo de vuelta creado
     */
    @Override
    public LapTime createLapTimeForAuthenticatedUser(String authenticatedEmail,
                                                     Long trackId,
                                                     LocalDate lapDate,
                                                     Long lapTimeMs,
                                                     String vehicle) {
        User user = findUserByAuthenticatedEmail(authenticatedEmail);

        LapTime lapTime = new LapTime();
        lapTime.setUser(user);
        lapTime.setTrack(trackReference(trackId));
        lapTime.setLapDate(lapDate);
        lapTime.setLapTimeMs(lapTimeMs);
        lapTime.setVehicle(vehicle);

        return createLapTime(lapTime);
    }

    /**
     * Elimina un tiempo de vuelta del usuario.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @param id identificador del tiempo de vuelta
     */
    @Override
    public void deleteOwnLapTime(String authenticatedEmail, Long id) {
        User user = findUserByAuthenticatedEmail(authenticatedEmail);
        LapTime lapTime = findLapTimeOrThrow(id);

        if (lapTime.getUser() == null || !user.getId().equals(lapTime.getUser().getId())) {
            throw new AccessDeniedException(ONLY_LAP_TIME_OWNER_CAN_DELETE);
        }

        lapTimePersistencePort.delete(lapTime);
    }

    /**
     * Recupera los tiempos de vuelta del usuario autenticado.
     *
     * @param authenticatedEmail correo del usuario autenticado
     * @return listado de tiempos del usuario
     */
    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getLapTimesByAuthenticatedEmail(String authenticatedEmail) {
        return lapTimePersistencePort.findByUserId(findUserByAuthenticatedEmail(authenticatedEmail).getId());
    }

    /**
     * Recupera los tiempos de vuelta de un usuario concreto.
     *
     * @param userId identificador del usuario
     * @return listado de tiempos del usuario
     */
    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getLapTimesByUserId(Long userId) {
        requireUserExists(userId);
        return lapTimePersistencePort.findByUserId(userId);
    }

    /**
     * Recupera el ranking ordenado de un circuito.
     *
     * @param trackId identificador del circuito
     * @return ranking de tiempos
     */
    @Override
    @Transactional(readOnly = true)
    public List<LapTime> getRankingByTrackId(Long trackId) {
        requireTrackExists(trackId);

        return lapTimePersistencePort.findByTrackId(trackId)
                .stream()
                .sorted(lapTimeComparator())
                .toList();
    }

    // Valida y completa las referencias antes de persistir el tiempo de vuelta.
    private LapTime createLapTime(LapTime lapTime) {
        validateLapTime(lapTime);
        attachLapTimeReferences(lapTime, extractUserId(lapTime), extractTrackId(lapTime));
        return lapTimePersistencePort.save(lapTime);
    }

    // Valida los datos mínimos del registro y evita valores temporales incoherentes.
    private void validateLapTime(LapTime lapTime) {
        // Exige un usuario autor identificable.
        if (lapTime.getUser() == null || lapTime.getUser().getId() == null) {
            throw new IllegalArgumentException(USER_ID_REQUIRED);
        }
        // Exige un circuito de referencia identificable.
        if (lapTime.getTrack() == null || lapTime.getTrack().getId() == null) {
            throw new IllegalArgumentException(TRACK_ID_REQUIRED);
        }
        // Obliga a informar cuándo se realizó la vuelta.
        if (lapTime.getLapDate() == null) {
            throw new IllegalArgumentException(LAP_DATE_REQUIRED);
        }
        // Impide registrar vueltas con fecha futura.
        if (lapTime.getLapDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de la vuelta no puede ser posterior a la fecha actual");
        }
        // Rechaza tiempos vacíos o no positivos.
        if (lapTime.getLapTimeMs() == null || lapTime.getLapTimeMs() <= 0) {
            throw new IllegalArgumentException("El tiempo de vuelta no puede ser menor que 0");
        }
    }

    // Sustituye las referencias ligeras por entidades reales cargadas desde persistencia.
    private void attachLapTimeReferences(LapTime lapTime, Long userId, Long trackId) {
        lapTime.setUser(loadUserById(userId));
        lapTime.setTrack(loadTrackById(trackId));
    }

    // Crea una referencia mínima al circuito para construir el borrador inicial.
    private Track trackReference(Long trackId) {
        Track track = new Track();
        track.setId(trackId);
        return track;
    }

    // Extrae el id del usuario ya validado del tiempo de vuelta.
    private Long extractUserId(LapTime lapTime) {
        return lapTime.getUser().getId();
    }

    // Extrae el id del circuito ya validado del tiempo de vuelta.
    private Long extractTrackId(LapTime lapTime) {
        return lapTime.getTrack().getId();
    }

    // Ordena el ranking por mejor tiempo y resuelve empates de forma estable.
    private Comparator<LapTime> lapTimeComparator() {
        return Comparator.comparing(LapTime::getLapTimeMs)
                .thenComparing(LapTime::getLapDate)
                .thenComparing(lapTime -> lapTime.getId() == null ? Long.MAX_VALUE : lapTime.getId());
    }

    // Carga el usuario o falla si ya no existe.
    private User loadUserById(Long userId) {
        return userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_ID + userId));
    }

    // Carga el circuito o falla si ya no existe.
    private Track loadTrackById(Long trackId) {
        return trackPersistencePort.findById(trackId)
                .orElseThrow(() -> new ResourceNotFoundException(TRACK_NOT_FOUND_WITH_ID + trackId));
    }

    // Verifica que el usuario exista antes de consultar sus tiempos.
    private void requireUserExists(Long userId) {
        loadUserById(userId);
    }

    // Verifica que el circuito exista antes de consultar su ranking.
    private void requireTrackExists(Long trackId) {
        loadTrackById(trackId);
    }

    // Recupera el tiempo de vuelta o lanza una excepción de no encontrado.
    private LapTime findLapTimeOrThrow(Long id) {
        return lapTimePersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(LAP_TIME_NOT_FOUND_WITH_ID + id));
    }

    // Normaliza el correo autenticado y carga al usuario propietario.
    private User findUserByAuthenticatedEmail(String authenticatedEmail) {
        if (authenticatedEmail == null || authenticatedEmail.isBlank()) {
            throw new IllegalArgumentException(AUTHENTICATED_EMAIL_REQUIRED);
        }

        String normalizedEmail = authenticatedEmail.trim().toLowerCase(Locale.ROOT);
        return userPersistencePort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_WITH_EMAIL + normalizedEmail));
    }
}
