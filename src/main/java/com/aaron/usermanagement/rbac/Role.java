package com.aaron.usermanagement.rbac;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    @Id
    String name; //"ADMIN", "USER", "MANAGER"
    String description;

    @ManyToMany
    Set<Permission> permissions; // Tập hợp các quyền thuộc vai trò này

}
