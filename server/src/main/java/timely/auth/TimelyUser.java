package timely.auth;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import timely.auth.util.DnUtils;

/**
 * A user of a TIMELY service. Typically, one or more of these users (a chain
 * where a user called an intermediate service which in turn called us) is
 * represented with a TimelyPrincipal.
 */
public class TimelyUser implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum UserType {
        USER, SERVER
    }

    public static final TimelyUser ANONYMOUS_USER = new TimelyUser(SubjectIssuerDNPair.of("ANONYMOUS"), UserType.USER,
            null, null, null, -1L);
    private final String name;
    private final String commonName;
    private final SubjectIssuerDNPair dn;
    private final UserType userType;
    private final Collection<String> auths;
    private final Collection<String> unmodifiableAuths;
    private final Collection<String> roles;
    private final Collection<String> unmodifiableRoles;
    private final Multimap<String, String> roleToAuthMapping;
    private final long creationTime;
    private final long expirationTime;

    /*
     * For Spring configuration
     */
    public TimelyUser(String subjectDn, Collection<String> auths, Collection<String> roles) {
        this.name = subjectDn;
        this.commonName = DnUtils.getCommonName(subjectDn);
        this.dn = SubjectIssuerDNPair.of(subjectDn);
        this.userType = DnUtils.isServerDN(subjectDn) ? UserType.SERVER : UserType.USER;
        this.auths = auths == null ? Collections.emptyList() : new LinkedHashSet<>(auths);
        this.unmodifiableAuths = Collections.unmodifiableCollection(this.auths);
        this.roles = roles == null ? Collections.emptyList() : new LinkedHashSet<>(roles);
        this.unmodifiableRoles = Collections.unmodifiableCollection(this.roles);
        this.roleToAuthMapping = null;
        this.creationTime = System.currentTimeMillis();
        this.expirationTime = -1L;
    }

    /*
     * For testing
     */
    public TimelyUser(String subjectDn, Collection<String> auths) {
        this(SubjectIssuerDNPair.of(subjectDn), UserType.USER, auths, null, null, System.currentTimeMillis(), -1L);
    }

    public TimelyUser(SubjectIssuerDNPair dn, UserType userType, Collection<String> auths, Collection<String> roles,
            Multimap<String, String> roleToAuthMapping, long creationTime) {
        this(dn, userType, auths, roles, roleToAuthMapping, creationTime, -1L);
    }

    @JsonCreator
    public TimelyUser(@JsonProperty(value = "dn", required = true) SubjectIssuerDNPair dn,
            @JsonProperty(value = "userType", required = true) UserType userType,
            @JsonProperty("auths") Collection<String> auths, @JsonProperty("roles") Collection<String> roles,
            @JsonProperty("roleToAuthMapping") Multimap<String, String> roleToAuthMapping,
            @JsonProperty(value = "creationTime", defaultValue = "-1L") long creationTime,
            @JsonProperty(value = "expirationTime", defaultValue = "-1L") long expirationTime) {
        this.name = dn.toString();
        this.commonName = DnUtils.getCommonName(dn.subjectDN());
        this.dn = dn;
        this.userType = userType;
        this.auths = auths == null ? Collections.emptyList() : new LinkedHashSet<>(auths);
        this.unmodifiableAuths = Collections.unmodifiableCollection(this.auths);
        this.roles = roles == null ? Collections.emptyList() : new LinkedHashSet<>(roles);
        this.unmodifiableRoles = Collections.unmodifiableCollection(this.roles);
        this.roleToAuthMapping = roleToAuthMapping == null ? LinkedHashMultimap.create()
                : Multimaps.unmodifiableMultimap(LinkedHashMultimap.create(roleToAuthMapping));
        this.creationTime = creationTime;
        this.expirationTime = expirationTime;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public String getCommonName() {
        return commonName;
    }

    public SubjectIssuerDNPair getDn() {
        return dn;
    }

    public UserType getUserType() {
        return userType;
    }

    public Collection<String> getAuths() {
        return unmodifiableAuths;
    }

    public Collection<String> getRoles() {
        return unmodifiableRoles;
    }

    public Multimap<String, String> getRoleToAuthMapping() {
        return roleToAuthMapping;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        TimelyUser that = (TimelyUser) o;

        return creationTime == that.creationTime && dn.equals(that.dn) && userType == that.userType
                && auths.equals(that.auths) && roles.equals(that.roles);
    }

    @Override
    public int hashCode() {
        int result = dn.hashCode();
        result = 31 * result + userType.hashCode();
        result = 31 * result + auths.hashCode();
        result = 31 * result + roles.hashCode();
        result = 31 * result + (int) (creationTime ^ (creationTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "TimelyUser{" + "name='" + getName() + "'" + ", userType=" + getUserType() + ", auths=" + getAuths()
                + ", roles=" + getRoles() + ", creationTime=" + getCreationTime() + "}";
    }
}
