package com.ticketmaster.ticketmaster.customer;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerProfileService {

    private final CustomerProfileRepository repo;

    public CustomerProfileService(CustomerProfileRepository repo) {
        this.repo = repo;
    }

    /**
     * Resolves the current user's id from Spring Security.
     * For tonight's demo we assume the principal's name is the user id
     * (or fall back to user id 1 if anonymous, so you can still demo).
     * We'll tighten this once the team's auth/User module lands.
     */
    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            // DEMO FALLBACK: allows testing in Swagger without auth wired.
            return 1L;
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            // Principal isn't a numeric id (e.g. email). Use demo fallback
            // and note this for the team integration step.
            return 1L;
        }
    }

    @Transactional(readOnly = true)
    public CustomerProfileResponse getMyProfile() {
        Long userId = currentUserId();
        CustomerProfile p = repo.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Profile not found for user " + userId
                                + ". Create it via POST /api/customers/me or the registration hook."));
        return CustomerProfileResponse.from(p);
    }

    @Transactional
    public CustomerProfileResponse updateMyProfile(CustomerProfileRequest req) {
        Long userId = currentUserId();
        CustomerProfile p = repo.findByUserId(userId)
                .orElseGet(() -> new CustomerProfile(userId, req.fullName()));

        p.setFullName(req.fullName());
        p.setPhone(req.phone());
        p.setCity(req.city());
        p.setLatitude(req.latitude());
        p.setLongitude(req.longitude());

        return CustomerProfileResponse.from(repo.save(p));
    }

    /** Call this from the registration flow once the User module exists. */
    @Transactional
    public CustomerProfile createForUser(Long userId, String fullName) {
        return repo.findByUserId(userId).orElseGet(() ->
                repo.save(new CustomerProfile(userId, fullName)));
    }
}
