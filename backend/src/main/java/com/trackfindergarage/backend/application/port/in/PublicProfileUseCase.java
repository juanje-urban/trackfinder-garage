package com.trackfindergarage.backend.application.port.in;

public interface PublicProfileUseCase {

    PublicUserProfileView getPublicUserProfile(String displayName);
}
