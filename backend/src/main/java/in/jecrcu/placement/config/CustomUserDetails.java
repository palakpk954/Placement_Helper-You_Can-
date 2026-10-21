package in.jecrcu.placement.config;

import in.jecrcu.placement.entity.User;
import in.jecrcu.placement.entity.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String fullName;
    private final String branch;
    private final Integer graduationYear;
    private final BigDecimal cgpa;
    private final Integer activeBacklogs;
    private final Role role;
    private final boolean emailVerified;
    private final boolean hasApprovedExperience;
    private final Collection<? extends GrantedAuthority> authorities;

    private final boolean mentor;

    public CustomUserDetails(User user, boolean hasApprovedExperience) {
        this(user, hasApprovedExperience, false);
    }

    public CustomUserDetails(User user, boolean hasApprovedExperience, boolean isMentor) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.branch = user.getBranch();
        this.graduationYear = user.getGraduationYear();
        this.cgpa = user.getCgpa();
        this.activeBacklogs = user.getActiveBacklogs();
        this.role = user.getRole();
        this.emailVerified = user.getEmailVerified() != null && user.getEmailVerified();
        this.hasApprovedExperience = hasApprovedExperience;
        this.mentor = user.getRole() == Role.ROLE_ALUMNI;

        List<GrantedAuthority> authList = new java.util.ArrayList<>();
        authList.add(new SimpleGrantedAuthority(user.getRole().name()));
        this.authorities = java.util.Collections.unmodifiableList(authList);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
