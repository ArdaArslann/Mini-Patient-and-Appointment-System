import zipfile, os

OUT = os.path.join(os.path.dirname(__file__), "proje_raporu.docx")

def esc(t):
    return t.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")

# ── XML helpers ────────────────────────────────────────────────────────────

def para(text, bold=False, sz=20, color="000000", after=40, before=0):
    b = "<w:b/><w:bCs/>" if bold else ""
    return (
        f'<w:p><w:pPr><w:spacing w:before="{before}" w:after="{after}"/></w:pPr>'
        f'<w:r><w:rPr>{b}<w:sz w:val="{sz}"/><w:szCs w:val="{sz}"/>'
        f'<w:color w:val="{color}"/></w:rPr>'
        f'<w:t xml:space="preserve">{esc(text)}</w:t></w:r></w:p>'
    )

def heading1(text):
    return para(text, bold=True, sz=26, color="1F3864", after=60, before=120)

def heading2(text):
    return para(text, bold=True, sz=22, color="2E5496", after=40, before=80)

def normal(text):
    return para(text, sz=20, after=40)

def small(text):
    return para(text, sz=18, after=20)

def code_line(text):
    """Single monospace line (for tree diagrams)."""
    return (
        f'<w:p><w:pPr>'
        f'<w:shd w:val="clear" w:color="auto" w:fill="F5F5F5"/>'
        f'<w:spacing w:after="0" w:line="220" w:lineRule="exact"/>'
        f'</w:pPr>'
        f'<w:r><w:rPr>'
        f'<w:rFonts w:ascii="Courier New" w:hAnsi="Courier New"/>'
        f'<w:sz w:val="16"/><w:szCs w:val="16"/>'
        f'</w:rPr>'
        f'<w:t xml:space="preserve">{esc(text)}</w:t></w:r></w:p>'
    )

def tree_block(lines):
    return "".join(code_line(ln) for ln in lines)

# ── Class diagram as a bordered table ──────────────────────────────────────

def class_box(class_name, stereotype, fields, note=""):
    """Render a UML-style class box as a single-column bordered table."""
    border = ('<w:tcBorders>'
              '<w:top w:val="single" w:sz="8" w:color="2E5496"/>'
              '<w:bottom w:val="single" w:sz="8" w:color="2E5496"/>'
              '<w:left w:val="single" w:sz="8" w:color="2E5496"/>'
              '<w:right w:val="single" w:sz="8" w:color="2E5496"/>'
              '</w:tcBorders>')

    # header row – class name
    header_cell = (
        f'<w:tc><w:tcPr>{border}'
        f'<w:shd w:val="clear" w:color="auto" w:fill="D6E4F0"/></w:tcPr>'
        f'<w:p><w:pPr><w:jc w:val="center"/><w:spacing w:after="0"/></w:pPr>'
        f'<w:r><w:rPr><w:sz w:val="16"/><w:szCs w:val="16"/>'
        f'<w:color w:val="555555"/><w:i/></w:rPr>'
        f'<w:t xml:space="preserve">{esc(stereotype)}</w:t></w:r></w:p>'
        f'<w:p><w:pPr><w:jc w:val="center"/><w:spacing w:after="0"/></w:pPr>'
        f'<w:r><w:rPr><w:b/><w:bCs/><w:sz w:val="20"/><w:szCs w:val="20"/>'
        f'<w:color w:val="1F3864"/></w:rPr>'
        f'<w:t xml:space="preserve">{esc(class_name)}</w:t></w:r></w:p>'
        f'</w:tc>'
    )
    header_row = f'<w:tr>{header_cell}</w:tr>'

    # fields row
    field_paras = ""
    for f in fields:
        field_paras += (
            f'<w:p><w:pPr><w:spacing w:after="0"/></w:pPr>'
            f'<w:r><w:rPr><w:rFonts w:ascii="Courier New" w:hAnsi="Courier New"/>'
            f'<w:sz w:val="16"/><w:szCs w:val="16"/></w:rPr>'
            f'<w:t xml:space="preserve">{esc(f)}</w:t></w:r></w:p>'
        )
    fields_cell = f'<w:tc><w:tcPr>{border}</w:tcPr>{field_paras}</w:tc>'
    fields_row = f'<w:tr>{fields_cell}</w:tr>'

    # optional note row
    note_row = ""
    if note:
        note_cell = (
            f'<w:tc><w:tcPr>{border}'
            f'<w:shd w:val="clear" w:color="auto" w:fill="FFF8E1"/></w:tcPr>'
            f'<w:p><w:pPr><w:spacing w:after="0"/></w:pPr>'
            f'<w:r><w:rPr><w:i/><w:sz w:val="16"/><w:szCs w:val="16"/>'
            f'<w:color w:val="666666"/></w:rPr>'
            f'<w:t xml:space="preserve">{esc(note)}</w:t></w:r></w:p></w:tc>'
        )
        note_row = f'<w:tr>{note_cell}</w:tr>'

    return (
        f'<w:tbl><w:tblPr>'
        f'<w:tblW w:w="4200" w:type="dxa"/>'
        f'<w:jc w:val="left"/>'
        f'</w:tblPr>'
        f'{header_row}{fields_row}{note_row}'
        f'</w:tbl>'
    )


# ── Layout helper: 2 boxes side by side ────────────────────────────────────

def two_boxes_side_by_side(box1, box2, arrow_label="1 ──── N"):
    border_none = ('<w:tcBorders>'
                   '<w:top w:val="none" w:sz="0"/><w:bottom w:val="none" w:sz="0"/>'
                   '<w:left w:val="none" w:sz="0"/><w:right w:val="none" w:sz="0"/>'
                   '</w:tcBorders>')
    cell1 = f'<w:tc><w:tcPr><w:tcW w:w="4200" w:type="dxa"/>{border_none}</w:tcPr><w:p><w:pPr><w:spacing w:after="0"/></w:pPr></w:p>{box1}<w:p/></w:tc>'
    arrow_cell = (
        f'<w:tc><w:tcPr><w:tcW w:w="1200" w:type="dxa"/>{border_none}<w:vAlign w:val="center"/></w:tcPr>'
        f'<w:p><w:pPr><w:jc w:val="center"/><w:spacing w:before="400"/></w:pPr>'
        f'<w:r><w:rPr><w:b/><w:sz w:val="16"/><w:szCs w:val="16"/>'
        f'<w:color w:val="2E5496"/></w:rPr>'
        f'<w:t xml:space="preserve">{esc(arrow_label)}</w:t></w:r></w:p></w:tc>'
    )
    cell2 = f'<w:tc><w:tcPr><w:tcW w:w="4200" w:type="dxa"/>{border_none}</w:tcPr><w:p><w:pPr><w:spacing w:after="0"/></w:pPr></w:p>{box2}<w:p/></w:tc>'
    return (
        f'<w:tbl><w:tblPr>'
        f'<w:tblW w:w="9600" w:type="dxa"/>'
        f'<w:tblBorders>'
        f'<w:top w:val="none" w:sz="0"/><w:bottom w:val="none" w:sz="0"/>'
        f'<w:left w:val="none" w:sz="0"/><w:right w:val="none" w:sz="0"/>'
        f'<w:insideH w:val="none" w:sz="0"/><w:insideV w:val="none" w:sz="0"/>'
        f'</w:tblBorders>'
        f'</w:tblPr>'
        f'<w:tr>{cell1}{arrow_cell}{cell2}</w:tr>'
        f'</w:tbl>'
    )


# ── DOCUMENT BODY ─────────────────────────────────────────────────────────

body = ""

# Title
body += para("Mini Hasta ve Randevu Yönetim Sistemi — Teknik Rapor",
             bold=True, sz=28, color="1F3864", after=20, before=0)
body += para("Öğrenci: Arslan   |   Tarih: 6 Mayıs 2026   |   Proje: Mini Patient & Appointment System",
             sz=18, color="666666", after=20)
body += para("Teknolojiler: Spring Boot 3, React (Vite), MySQL 8, Hibernate/JPA, Lombok, Docker   |   IDE: IntelliJ IDEA   |   LLM: Claude (Anthropic)",
             sz=16, color="888888", after=60)

# ═══════════════════════ BÖLÜM 1 ═══════════════════════════════════════════
body += heading1("1. Veritabanı ve ORM Yapısı")

body += heading2("1.1 Veritabanı — MySQL 8 (Docker)")
body += normal(
    "Proje MySQL 8 veritabanı kullanmaktadır. Veritabanı sunucusu Docker container "
    "içinde (openemr-mysql, port 3333) çalışmakta olup karakter seti utf8mb4 ve collation "
    "utf8mb4_turkish_ci olarak yapılandırılmıştır. Sistemde dört tablo bulunmaktadır. "
    "Poliklinik-Doktor arasında 1:N, Hasta-Randevu arasında 1:N (CASCADE DELETE) ve "
    "Doktor-Randevu arasında 1:N (SET NULL) ilişkileri mevcuttur."
)

# Row 1: Polyclinic ──1:N── Doctor
polyclinic_box = class_box(
    "Polyclinic", "«entity» app_polyclinics",
    [
        "- id : BIGINT  «PK, AI»",
        "- name : VARCHAR(100)",
        "- description : VARCHAR(255)",
    ]
)
doctor_box = class_box(
    "Doctor", "«entity» app_doctors",
    [
        "- id : BIGINT  «PK, AI»",
        "- first_name : VARCHAR(100)",
        "- last_name : VARCHAR(100)",
        "- title : VARCHAR(50)",
        "- polyclinic_id : BIGINT  «FK»",
    ],
    note="FK → app_polyclinics(id)"
)
body += two_boxes_side_by_side(polyclinic_box, doctor_box, "1 ──── N")

# Row 2: Patient ──1:N── Appointment
patient_box = class_box(
    "Patient", "«entity» app_patients",
    [
        "- id : BIGINT  «PK, AI»",
        "- first_name : VARCHAR(100)",
        "- last_name : VARCHAR(100)",
        "- date_of_birth : DATE",
        "- phone_number : VARCHAR(50)",
        "- address : VARCHAR(255)",
    ]
)
appointment_box = class_box(
    "Appointment", "«entity» app_appointments",
    [
        "- id : BIGINT  «PK, AI»",
        "- patient_id : BIGINT  «FK»",
        "- doctor_id : BIGINT  «FK»",
        "- appointment_date : DATETIME",
        "- reason : VARCHAR(255)",
    ],
    note="FK → patients(CASCADE), doctors(SET NULL)"
)
body += two_boxes_side_by_side(patient_box, appointment_box, "1 ──── N")

body += heading2("1.2 ORM — Spring Data JPA / Hibernate")
body += normal(
    "Backend Spring Boot 3 + Spring Data JPA (Hibernate) kullanmaktadır. Her tabloya karşılık "
    "gelen bir @Entity sınıfı tanımlanmış, @Table ile tabloya, @Column ile sütunlara eşlenmiştir. "
    "Boilerplate kod Lombok @Data ile otomatik üretilir. Repository'ler JpaRepository<T,ID> arayüzünü "
    "extend ederek CRUD metodlarını SQL yazmadan sağlar. Her repository'de @Query ile arama (search) "
    "metodu tanımlanmıştır. DoctorRepository ayrıca findByPolyclinicId ile poliklinik bazlı filtreleme "
    "yapar. Uygulama ilk başlatıldığında DataSeeder (CommandLineRunner) 8 poliklinik ve 12 doktoru "
    "otomatik olarak veritabanına yükler."
)

# ═══════════════════════ BÖLÜM 2 ═══════════════════════════════════════════
body += heading1("2. Yazılım Mimarisi ve Dosya-Klasör Yapısı")

body += heading2("2.1 Mimari Yapı")
body += normal(
    "Proje üç katmanlı (3-tier) bir mimariye sahiptir. Sunum katmanında React (Vite, port 5173) "
    "tek sayfalık web uygulaması, uygulama katmanında Spring Boot REST API (port 8081) ve veri "
    "katmanında MySQL (Docker, port 3333) yer almaktadır. Ek olarak 8080 portundaki OpenEMR sistemine "
    "REST entegrasyonu bulunmaktadır."
)
body += normal(
    "Backend katmanlı mimari (Layered Architecture) ile düzenlenmiştir: Controller katmanı HTTP "
    "endpoint'lerini yönetir (her entity için CRUD + /search); Service katmanı iş mantığı ve OpenEMR "
    "entegrasyonunu içerir; Repository katmanı JPA ile veritabanı erişimini soyutlar; Model katmanı "
    "@Entity sınıflarını, DTO katmanı veri transfer nesnelerini, Config katmanı ise CORS, OpenEMR "
    "bağlantı ayarları ve DataSeeder (hazır veri yükleme) bileşenlerini barındırır."
)

body += heading2("2.2 Dosya-Klasör Yapısı")
body += tree_block([
    "openemr_project/",
    "├── docker-compose.yml",
    "├── pom.xml",
    "├── db_schema.sql",
    "├── frontend/",
    "│   ├── index.html, vite.config.js",
    "│   └── src/",
    "│       ├── main.jsx, App.jsx",
    "│       ├── App.css, index.css",
    "└── src/main/java/.../openemr_project/",
    "    ├── OpenemrProjectApplication.java",
    "    ├── config/",
    "    │   ├── CorsConfig.java",
    "    │   ├── OpenEmrProperties.java",
    "    │   └── DataSeeder.java",
    "    ├── model/",
    "    │   ├── Patient.java, Appointment.java",
    "    │   ├── Doctor.java, Polyclinic.java",
    "    ├── repository/",
    "    │   ├── PatientRepository.java",
    "    │   ├── AppointmentRepository.java",
    "    │   ├── DoctorRepository.java",
    "    │   └── PolyclinicRepository.java",
    "    ├── controller/",
    "    │   ├── PatientController.java",
    "    │   ├── AppointmentController.java",
    "    │   ├── DoctorController.java",
    "    │   ├── PolyclinicController.java",
    "    │   └── OpenEmrIntegrationController.java",
    "    ├── service/",
    "    │   └── OpenEmrService.java",
    "    └── dto/",
    "        ├── OpenEmrPatientRequest.java",
    "        ├── OpenEmrAppointmentRequest.java",
    "        ├── OpenEmrTokenResponse.java",
    "        ├── PatientWithAppointmentRequest.java",
    "        └── PatientWithAppointmentResponse.java",
])

body += heading2("2.3 Önemli Tasarım Kararları")
body += normal(
    "OpenEMR Entegrasyonu: Randevu oluşturulduğunda OpenEmrService, OAuth2 password-grant ile JWT "
    "token alır (önbelleklenir), OpenEMR'a ve yerel DB'ye eşzamanlı kayıt atar. Arama: Tüm controller'larda "
    "/search endpoint'i; frontend'de client-side filtreleme. Poliklinik seçilince doktorlar otomatik "
    "filtrelenir. DataSeeder ile 8 poliklinik ve 12 doktor hazır gelir. CORS ve docker-compose ile "
    "tüm servisler tek komutla başlatılır."
)

# ── XML ASSEMBLY ───────────────────────────────────────────────────────────

document_xml = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
    '<w:body>' + body +
    '<w:sectPr>'
    '<w:pgSz w:w="11906" w:h="16838"/>'
    '<w:pgMar w:top="900" w:right="900" w:bottom="900" w:left="900"/>'
    '</w:sectPr>'
    '</w:body></w:document>'
)

rels = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/'
    'relationships/styles" Target="styles.xml"/></Relationships>'
)

styles_xml = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
    '<w:style w:type="paragraph" w:styleId="Normal"><w:name w:val="Normal"/></w:style>'
    '</w:styles>'
)

content_types = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
    '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
    '<Default Extension="xml" ContentType="application/xml"/>'
    '<Override PartName="/word/document.xml"'
    ' ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>'
    '<Override PartName="/word/styles.xml"'
    ' ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>'
    '</Types>'
)

pkg_rels = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    '<Relationship Id="rId1"'
    ' Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"'
    ' Target="word/document.xml"/></Relationships>'
)

with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("[Content_Types].xml", content_types)
    z.writestr("_rels/.rels", pkg_rels)
    z.writestr("word/document.xml", document_xml)
    z.writestr("word/styles.xml", styles_xml)
    z.writestr("word/_rels/document.xml.rels", rels)

print(f"Created: {OUT}")
