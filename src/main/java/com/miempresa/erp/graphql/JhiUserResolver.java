package com.miempresa.erp.graphql;

import com.miempresa.erp.domain.User;
import com.miempresa.erp.graphql.JhiUserInput;
import com.miempresa.erp.graphql.UserFilter;
import com.miempresa.erp.repository.UserRepository;
import java.util.List;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class JhiUserResolver {

    private final UserRepository jhiUserRepository;

    public JhiUserResolver(UserRepository UserRepository) {
        this.jhiUserRepository = UserRepository;
    }

    // Queries
    @QueryMapping
    public User user(@Argument Long id) {
        return jhiUserRepository.findById(id).orElse(null);
    }

    @QueryMapping
    public User userByEmail(@Argument String email) {
        return jhiUserRepository.findOneByEmail(email).orElse(null);
    }

    @QueryMapping
    public List<User> users(@Argument(name = "filter") UserFilter filter, @Argument Integer page, @Argument Integer size) {
        if (filter == null) {
            return jhiUserRepository.findAll();
        }

        // Implementar filtros básicos
        if (filter.getEmail() != null) {
            return jhiUserRepository.findByEmailContaining(filter.getEmail());
        }

        if (filter.getUserType() != null) {
            return jhiUserRepository.findByUserType(filter.getUserType());
        }

        if (filter.getStatus() != null) {
            return jhiUserRepository.findByStatus(filter.getStatus());
        }

        return jhiUserRepository.findAll();
    }

    // Mutations
    @MutationMapping
    public User createUser(@Argument JhiUserInput input) {
        User user = new User();
        mapUserInputToEntity(input, user);
        return jhiUserRepository.save(user);
    }

    @MutationMapping
    public User updateUser(@Argument Long id, @Argument JhiUserInput input) {
        User user = jhiUserRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        mapUserInputToEntity(input, user);
        return jhiUserRepository.save(user);
    }

    @MutationMapping
    public Boolean deleteUser(@Argument Long id) {
        try {
            jhiUserRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @MutationMapping
    public User verifyUserIdentity(@Argument Long id, @Argument Boolean verified) {
        User user = jhiUserRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setIdentityVerified(verified);
        return jhiUserRepository.save(user);
    }

    @MutationMapping
    public User verifyUserAddress(@Argument Long id, @Argument Boolean verified) {
        User user = jhiUserRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        user.setAddressVerified(verified);
        return jhiUserRepository.save(user);
    }

    // Helper method
    private void mapUserInputToEntity(JhiUserInput input, User user) {
        if (input.getName() != null) user.setName(input.getName());
        if (input.getLastName() != null) user.setLastName(input.getLastName());
        if (input.getEmail() != null) user.setEmail(input.getEmail());
        if (input.getPhone() != null) user.setPhone(input.getPhone());
        if (input.getCi() != null) user.setCi(input.getCi());
        if (input.getPassword() != null) user.setPassword(input.getPassword());
        if (input.getScore() != null) user.setScore(input.getScore());
        if (input.getStatus() != null) user.setStatus(input.getStatus());
        if (input.getUserType() != null) user.setUserType(input.getUserType());
        if (input.getAdressVerified() != null) user.setAddressVerified(input.getAdressVerified());
        if (input.getIdentityVerified() != null) user.setIdentityVerified(input.getIdentityVerified());
    }
}
