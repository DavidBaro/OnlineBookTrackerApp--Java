package io.project.onlinebooktracker.usersignupandlogin;

import java.util.UUID;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;


@Table(value ="user_by_id")
public class User {

    // @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    
    // private @NonNull String id;

    @PrimaryKeyColumn(name = "user_email", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    @CassandraType(type = Name.TEXT)
    private String name;

    @PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    @CassandraType(type = Name.TIMEUUID)
    private UUID id;

    // @PrimaryKey
    // private @NonNull String id;

    @Column("user_first_name")
    @CassandraType(type = Name.TEXT)
    private String userFirstName;

    @Column("user_last_name")
    @CassandraType(type = Name.TEXT)
    private String userLastName;  

    @Column("user_password")
    @CassandraType(type = Name.TEXT)
    private String userPassword;

    // Getter and Setter

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    // To String method
    @Override
    public String toString() {
        return "User [name=" + name + ", userFirstName=" + userFirstName + ", userLastName="
                + userLastName + ", userPassword=" + userPassword + "]";
    }
    
}
