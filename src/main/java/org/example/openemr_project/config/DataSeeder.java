package org.example.openemr_project.config;

import org.example.openemr_project.model.Doctor;
import org.example.openemr_project.model.Polyclinic;
import org.example.openemr_project.repository.DoctorRepository;
import org.example.openemr_project.repository.PolyclinicRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final PolyclinicRepository polyclinicRepository;
    private final DoctorRepository doctorRepository;

    public DataSeeder(PolyclinicRepository polyclinicRepository, DoctorRepository doctorRepository) {
        this.polyclinicRepository = polyclinicRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void run(String... args) {
        // Eğer zaten veri varsa tekrar ekleme
        if (polyclinicRepository.count() > 0) {
            return;
        }

        // Poliklinikler
        Polyclinic dahiliye = createPolyclinic("Dahiliye", "İç hastalıkları polikliniği");
        Polyclinic kardiyoloji = createPolyclinic("Kardiyoloji", "Kalp ve damar hastalıkları polikliniği");
        Polyclinic ortopedi = createPolyclinic("Ortopedi", "Kemik ve eklem hastalıkları polikliniği");
        Polyclinic gogus = createPolyclinic("Göğüs Hastalıkları", "Akciğer ve solunum yolu hastalıkları polikliniği");
        Polyclinic noroloji = createPolyclinic("Nöroloji", "Sinir sistemi hastalıkları polikliniği");
        Polyclinic dermatoloji = createPolyclinic("Dermatoloji", "Cilt hastalıkları polikliniği");
        Polyclinic kbb = createPolyclinic("KBB", "Kulak burun boğaz hastalıkları polikliniği");
        Polyclinic goz = createPolyclinic("Göz Hastalıkları", "Göz hastalıkları polikliniği");

        // Doktorlar
        createDoctor("Ahmet", "Yılmaz", "Prof. Dr.", dahiliye.getId());
        createDoctor("Fatma", "Demir", "Doç. Dr.", dahiliye.getId());
        createDoctor("Mehmet", "Kaya", "Prof. Dr.", kardiyoloji.getId());
        createDoctor("Ayşe", "Çelik", "Uzm. Dr.", kardiyoloji.getId());
        createDoctor("Hasan", "Öztürk", "Prof. Dr.", ortopedi.getId());
        createDoctor("Elif", "Arslan", "Doç. Dr.", ortopedi.getId());
        createDoctor("Ali", "Şahin", "Uzm. Dr.", gogus.getId());
        createDoctor("Canan", "Erdoğan", "Doç. Dr.", gogus.getId());
        createDoctor("Zeynep", "Koç", "Prof. Dr.", noroloji.getId());
        createDoctor("Tarık", "Polat", "Uzm. Dr.", noroloji.getId());
        createDoctor("Mustafa", "Aydın", "Uzm. Dr.", dermatoloji.getId());
        createDoctor("Gülşen", "Kılıç", "Op. Dr.", dermatoloji.getId());
        createDoctor("Seda", "Yıldırım", "Doç. Dr.", kbb.getId());
        createDoctor("Burak", "Çetin", "Prof. Dr.", kbb.getId());
        createDoctor("Emre", "Kurt", "Uzm. Dr.", goz.getId());
        createDoctor("Büşra", "Özdemir", "Op. Dr.", goz.getId());
    }

    private Polyclinic createPolyclinic(String name, String description) {
        Polyclinic p = new Polyclinic();
        p.setName(name);
        p.setDescription(description);
        return polyclinicRepository.save(p);
    }

    private void createDoctor(String firstName, String lastName, String title, Long polyclinicId) {
        Doctor d = new Doctor();
        d.setFirstName(firstName);
        d.setLastName(lastName);
        d.setTitle(title);
        d.setPolyclinicId(polyclinicId);
        doctorRepository.save(d);
    }
}
