package edu.bbte.daygig.backend.service.user;

import java.util.List;
import java.util.UUID;
import edu.bbte.daygig.backend.model.User;

public interface UserService {

    User getById(UUID id);

    List<User> getAll();

    void delete(UUID id);

    User update(UUID id,
                User user);


}
