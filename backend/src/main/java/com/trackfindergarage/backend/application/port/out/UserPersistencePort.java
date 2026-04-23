package com.trackfindergarage.backend.application.port.out;

import com.trackfindergarage.backend.domain.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia para operaciones básicas sobre usuarios.
 */
public interface UserPersistencePort {

    /**
     * Persiste un usuario nuevo o existente.
     *
     * @param user usuario a guardar
     * @return usuario persistido
     */
    User save(User user);

    /**
     * Busca un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return usuario encontrado, si existe
     */
    Optional<User> findById(Long id);

    /**
     * Busca un usuario por su alias público.
     *
     * @param displayName alias público del usuario
     * @return usuario encontrado, si existe
     */
    Optional<User> findByDisplayName(String displayName);

    /**
     * Busca un usuario por su email.
     *
     * @param email email del usuario
     * @return usuario encontrado, si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su telefono.
     *
     * @param phone teléfono del usuario
     * @return usuario encontrado, si existe
     */
    Optional<User> findByPhone(String phone);

    /**
     * Recupera todos los usuarios persistidos.
     *
     * @return listado completo de usuarios
     */
    List<User> findAll();

    /**
     * Elimina un usuario persistido.
     *
     * @param user usuario a eliminar
     */
    void delete(User user);
}
