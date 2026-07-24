package edu.bbte.daygig.backend.config;

import java.math.BigDecimal;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import edu.bbte.daygig.backend.model.enums.Currency;
import edu.bbte.daygig.backend.model.enums.JobCategory;
import edu.bbte.daygig.backend.model.enums.JobStatus;
import edu.bbte.daygig.backend.model.enums.Role;
import edu.bbte.daygig.backend.model.Job;
import edu.bbte.daygig.backend.model.User;
import edu.bbte.daygig.backend.repository.JobRepository;
import edu.bbte.daygig.backend.repository.UserRepository;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements ApplicationRunner {

    private static final String DEV_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        User customer = seedUser("peter@gmail.com", "Dev", "Customer", "0711111111");
        User worker = seedUser("gergo@gmail.com", "Dev", "Worker", "0722222222");

        seedJob("Apartment cleaning help", "Need help cleaning a two-room apartment before the weekend.",
                JobCategory.CLEANING, new BigDecimal("45.00"), "Cluj-Napoca, Marasti", new BigDecimal("46.783364"),
                new BigDecimal("23.624457"), customer);

        seedJob("Move sofa aadsfvaergvdzscnd boxes",
                "Looking for help moving a sofa and several boxes to a nearby apartment.", JobCategory.MOVING,
                new BigDecimal("60.00"), "Cluj-Napoca, Zorilor", new BigDecimal("46.753701"),
                new BigDecimal("23.582734"), customer);
        seedJob("Move sofaasdawrqddsa and boxes",
                "Looking for help moving a sofa and several boxes to a nearby apartment.", JobCategory.MOVING,
                new BigDecimal("60.00"), "Cluj-Napoca, Zorilor", new BigDecimal("46.753701"),
                new BigDecimal("23.582734"), customer);
        seedJob("Move sofa and asdasdboxes", "Looking for help moving a sofa and several boxes to a nearby apartment.",
                JobCategory.MOVING, new BigDecimal("60.00"), "Cluj-Napoca, Zorilor", new BigDecimal("46.753701"),
                new BigDecimal("23.582734"), customer);
        seedJob("Move sofaasdasd and boxes", "Looking for help moving a sofa and several boxes to a nearby apartment.",
                JobCategory.MOVING, new BigDecimal("60.00"), "Cluj-Napoca, Zorilor", new BigDecimal("46.753701"),
                new BigDecimal("23.582734"), customer);
        seedJob("Move sofa ZDasand boxes", "Looking for help moving a sofa and several boxes to a nearby apartment.",
                JobCategory.MOVING, new BigDecimal("60.00"), "Cluj-Napoca, Zorilor", new BigDecimal("46.753701"),
                new BigDecimal("23.582734"), customer);
        seedJob("Move sofa and boxes", "Looking for help moving a sofa and several boxes to a nearby apartment.",
                JobCategory.MOVING, new BigDecimal("60.00"), "Cluj-Napoca, Zorilor", new BigDecimal("46.753701"),
                new BigDecimal("23.582734"), customer);


        seedJob("Garden cleanup", "Small garden cleanup with leaves, branches, and basic trimming.",
                JobCategory.GARDENING, new BigDecimal("50.00"), "Floresti", new BigDecimal("46.745642"),
                new BigDecimal("23.493561"), worker);
    }

    private User seedUser(String email,
                          String firstName,
                          String lastName,
                          String phoneNumber) {
        User existingUser = userRepository.findByEmail(email);

        if (existingUser != null) {
            return existingUser;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(DEV_PASSWORD));
        user.setRole(Role.ROLE_USER);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setDeleted(false);

        return userRepository.save(user);
    }

    private void seedJob(String title,
                         String description,
                         JobCategory category,
                         BigDecimal hourlyRate,
                         String locationName,
                         BigDecimal latitude,
                         BigDecimal longitude,
                         User owner) {
        if (jobRepository.existsByTitle(title)) {
            return;
        }

        Job job = new Job();
        job.setTitle(title);
        job.setDescription(description);
        job.setCategory(category);
        job.setStatus(JobStatus.OPEN);
        job.setHourlyRate(hourlyRate);
        job.setCurrency(Currency.RON);
        job.setLocationName(locationName);
        job.setLatitude(latitude);
        job.setLongitude(longitude);
        job.setOwner(owner);

        jobRepository.save(job);
    }
}
